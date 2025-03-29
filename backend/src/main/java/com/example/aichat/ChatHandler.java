package com.example.aichat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatHandler implements HttpHandler {


    private static final String GEMINI_API_ENDPOINT_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final Path HISTORY_FILE_PATH = Paths.get("conversation_history.json");
    private static final Type CHAT_MESSAGE_LIST_TYPE = new TypeToken<List<ChatMessage>>(){}.getType();


    private final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final WebConfig webConfig;
    private final BasicConfig basicConfig;
    private final AtomicInteger currentApiKeyIndex = new AtomicInteger(0);


    public ChatHandler(WebConfig webConfig, BasicConfig basicConfig) {
        this.webConfig = webConfig;
        this.basicConfig = basicConfig;


        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30));

        String proxyUrl = webConfig.getProxyUrl();
        if (proxyUrl != null && !proxyUrl.trim().isEmpty()) {
            try {
                URI proxyUri = new URI(proxyUrl);
                String host = proxyUri.getHost();
                int port = proxyUri.getPort();
                if (host != null && port != -1) {
                    System.out.println("配置 HttpClient 使用 HTTP 代理: " + host + ":" + port);
                    clientBuilder.proxy(ProxySelector.of(new InetSocketAddress(host, port)));
                } else {
                    System.err.println("配置文件中代理 URL 格式无效: " + proxyUrl + "。忽略代理。");
                }
            } catch (URISyntaxException | IllegalArgumentException e) {
                System.err.println("解析代理 URL '" + proxyUrl + "' 时出错。忽略代理。错误: " + e.getMessage());
            }
        } else {
            System.out.println("未配置代理。");
        }
        this.httpClient = clientBuilder.build();


        if (basicConfig.getApiKeys() == null || basicConfig.getApiKeys().isEmpty()) {
            System.err.println("严重: BasicConfig 中的 API 密钥列表为 null 或为空！");
        }
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {

        setupCORSHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed", createErrorJson("仅支持 POST 方法。"));
            return;
        }


        List<String> apiKeys = basicConfig.getApiKeys();
        if (apiKeys == null || apiKeys.isEmpty() || apiKeys.get(0).equals("YOUR_DEFAULT_API_KEY_PLACEHOLDER")) {
            System.err.println("未配置有效的 API 密钥。无法调用 Gemini API。");
            sendResponse(exchange, 500, "Internal Server Error", createErrorJson("后端配置错误：未配置有效的 API 密钥。"));
            return;
        }


        String requestBody = readRequestBody(exchange.getRequestBody());
        String userMessageText;
        try {
            JsonObject jsonRequest = gson.fromJson(requestBody, JsonObject.class);
            if (jsonRequest == null || !jsonRequest.has("message")) {
                sendResponse(exchange, 400, "Bad Request", createErrorJson("JSON 正文中缺少 'message' 字段。"));
                return;
            }
            userMessageText = jsonRequest.get("message").getAsString();
            if (userMessageText.trim().isEmpty()) {
                sendResponse(exchange, 400, "Bad Request", createErrorJson("收到空消息。"));
                return;
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            sendResponse(exchange, 400, "Bad Request", createErrorJson("无效的 JSON 格式: " + e.getMessage()));
            return;
        }
        System.out.println("收到来自前端的消息: " + userMessageText);


        List<ChatMessage> conversationHistory = loadHistoryFromFile();
        System.out.println("从历史记录文件加载了 " + conversationHistory.size() + " 条消息。");


        ChatMessage currentUserMessage = new ChatMessage("user", userMessageText);
        conversationHistory.add(currentUserMessage);


        String aiReplyText = null;
        boolean success = false;
        int initialIndex = currentApiKeyIndex.get();
        int retries = apiKeys.size();

        for (int i = 0; i < retries; i++) {
            int keyIndex = (initialIndex + i) % apiKeys.size();
            String currentApiKey = apiKeys.get(keyIndex);
            // 跳过占位符密钥
            if ("YOUR_DEFAULT_API_KEY_PLACEHOLDER".equals(currentApiKey)) { continue; }

            System.out.println("尝试使用密钥索引进行 Gemini API 调用: " + keyIndex);
            try {
                // 调用 Gemini API
                aiReplyText = callGeminiApi(conversationHistory, currentApiKey);
                success = true;
                currentApiKeyIndex.set(keyIndex); // 更新当前使用的密钥索引
                System.out.println("使用密钥索引 " + keyIndex + " 的 API 调用成功。");
                break; // 成功则跳出循环
            } catch (ApiException e) {
                System.err.println("API 密钥索引 " + keyIndex + " 出错: " + e.getStatusCode() + " - " + e.getMessage());
                // 如果是认证、权限、速率限制或无效请求错误，尝试下一个密钥
                if (e.getStatusCode() == 401 || e.getStatusCode() == 403 || e.getStatusCode() == 429 || e.getStatusCode() == 400) {
                    System.out.println("尝试使用下一个密钥...");
                } else {
                    // 对于其他 API 错误，直接返回错误给客户端
                    sendResponse(exchange, e.getStatusCode(), "API Error", createErrorJson("来自 AI 服务端的错误: " + e.getMessage()));
                    return;
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("使用密钥索引 " + keyIndex + " 进行 API 调用期间发生网络/IO错误: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "Internal Server Error", createErrorJson("与 AI 服务通信失败: " + e.getMessage()));
                return; // 网络或中断错误，停止处理
            }
        }


        if (!success) {
            System.err.println("所有 API 密钥均失败或请求被阻止。");
            // 如果所有密钥都尝试失败
            sendResponse(exchange, 500, "Internal Server Error", createErrorJson("尝试所有可用密钥后，未能从 AI 服务获取响应。"));
            return;
        }


        ChatMessage aiResponseMessage = new ChatMessage("ai", aiReplyText);
        conversationHistory.add(aiResponseMessage);
        saveHistoryToFile(conversationHistory);


        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("reply", aiReplyText);
        String responseBody = gson.toJson(jsonResponse);
        System.out.println("发送回复: " + aiReplyText);
        sendResponse(exchange, 200, "OK", responseBody);
    }


    // 调用 Gemini API
    private String callGeminiApi(List<ChatMessage> history, String apiKeyToUse) throws IOException, InterruptedException, ApiException {
        String modelName = basicConfig.getModelName();
        String initialPersona = basicConfig.getInitialPersona();
        String apiUrl = String.format(GEMINI_API_ENDPOINT_TEMPLATE, modelName, apiKeyToUse);

        JsonArray contentsArray = buildGeminiContents(history, initialPersona);
        JsonObject geminiRequestBody = new JsonObject();
        geminiRequestBody.add("contents", contentsArray);



        String requestJson = gson.toJson(geminiRequestBody);
        System.out.println("发送到 Gemini ("+ contentsArray.size() +" 轮): " + requestJson);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(120)) // 增加超时时间
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));


        int statusCode = response.statusCode();
        String responseBody = response.body();
        System.out.println("Gemini 响应状态: " + statusCode);

        if (statusCode == 200) {
            return parseGeminiResponse(responseBody);
        } else {
            handleGeminiApiError(statusCode, responseBody);
            // 理论上 handleGeminiApiError 会抛出异常，这里是为了编译器满意
            throw new ApiException(statusCode, "API 调用在错误处理后意外失败。");
        }
    }


    // 构建发送给 Gemini API 的 contents 数组
    private JsonArray buildGeminiContents(List<ChatMessage> history, String initialPersona) {
        JsonArray contentsArray = new JsonArray();
        boolean personaIncluded = false; // 标记是否已添加角色设定

        // 如果有初始角色设定，将其作为对话的开始
        if (initialPersona != null && !initialPersona.trim().isEmpty()) {
            contentsArray.add(createContentEntry("user", initialPersona));
            // 添加一个模型的简单回应，表示理解了角色设定
            contentsArray.add(createContentEntry("model", "好的。"));
            personaIncluded = true;
        }

        // 遍历历史记录
        for (int i = 0; i < history.size(); i++) {
            ChatMessage message = history.get(i);
            String role = mapSenderToRole(message.getSender());

            // 跳过无效的消息
            if (role == null || message.getText() == null || message.getText().trim().isEmpty()) {
                System.err.println("在历史记录处理中跳过无效消息: " + message);
                continue;
            }

            // 如果角色设定已添加，并且当前消息是历史记录中的第一条用户消息且内容与角色设定相同，则跳过
            if (personaIncluded && i == 0 && role.equals("user") && message.getText().equals(initialPersona)) {
                System.out.println("检测到角色设定是历史记录中的第一条消息，跳过重复项。");
                continue;
            }

            if (contentsArray.size() > 0) {
                JsonObject lastEntry = contentsArray.get(contentsArray.size() - 1).getAsJsonObject();
                if (lastEntry.get("role").getAsString().equals(role)) {
                    System.err.println("警告: 发现角色 '" + role + "' 的连续消息。仍然发送。");
                }
            }

            contentsArray.add(createContentEntry(role, message.getText()));
        }
        return contentsArray;
    }


    // 解析 Gemini API 的成功响应
    private String parseGeminiResponse(String responseBody) throws ApiException {
        try {
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            // 检查 candidates 字段
            if (jsonResponse.has("candidates") && jsonResponse.get("candidates").isJsonArray()) {
                JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();

                    // 检查是否有 finishReason 表明内容被阻止
                    if (firstCandidate.has("finishReason") &&
                            ("SAFETY".equals(firstCandidate.get("finishReason").getAsString()) ||
                                    "RECITATION".equals(firstCandidate.get("finishReason").getAsString()))) {
                        System.err.println("Gemini 内容因以下原因被阻止: " + firstCandidate.get("finishReason").getAsString());
                        throw new ApiException(400, "响应被 AI 安全过滤器阻止 (" + firstCandidate.get("finishReason").getAsString() + ")。");
                    }

                    // 提取内容
                    if (firstCandidate.has("content") && firstCandidate.getAsJsonObject("content").has("parts")) {
                        JsonArray parts = firstCandidate.getAsJsonObject("content").getAsJsonArray("parts");
                        if (parts.size() > 0 && parts.get(0).getAsJsonObject().has("text")) {
                            return parts.get(0).getAsJsonObject().get("text").getAsString();
                        }
                    }
                }
            }
            // 如果结构不符合预期或没有文本内容
            System.err.println("无法从 Gemini 响应结构中提取文本。完整响应: " + responseBody);
            throw new ApiException(500, "已收到 AI 的响应，但无法解析预期的内容。");
        } catch (JsonSyntaxException | IllegalStateException | NullPointerException e) {
            System.err.println("解析 Gemini JSON 响应时出错: " + e.getMessage() + "。完整响应: " + responseBody);
            throw new ApiException(500, "解析 AI 服务响应失败: " + e.getMessage());
        }
    }


    // 处理 Gemini API 的错误响应
    private void handleGeminiApiError(int statusCode, String responseBody) throws ApiException {
        String errorMessage = "API 调用失败，状态码 " + statusCode;
        try {
            // 尝试解析错误响应体中的具体错误信息
            JsonObject errorJson = gson.fromJson(responseBody, JsonObject.class);
            if (errorJson.has("error") && errorJson.getAsJsonObject("error").has("message")) {
                errorMessage += ": " + errorJson.getAsJsonObject("error").get("message").getAsString();
            } else {
                errorMessage += ". 响应体: " + responseBody; // 如果没有标准错误结构，附加原始响应体
            }
        } catch (Exception parseEx) {
            // 如果解析错误响应体也失败了，附加原始响应体
            errorMessage += ". 响应体: " + responseBody;
        }
        // 抛出自定义异常，包含状态码和错误消息
        throw new ApiException(statusCode, errorMessage);
    }


    // 将内部的 sender ('user', 'ai') 映射到 Gemini API 的 'role' ('user', 'model')
    private String mapSenderToRole(String sender) {
        if ("user".equalsIgnoreCase(sender)) {
            return "user";
        } else if ("ai".equalsIgnoreCase(sender) || "model".equalsIgnoreCase(sender)) {
            // 接受 'ai' 或 'model' 作为模型的角色
            return "model";
        }
        System.err.println("历史记录中未知的发送者类型: " + sender + "。视为 'user' 处理。");
        return "user"; // 默认或备用情况
    }


    // 创建 Gemini API content 条目的辅助方法
    private JsonObject createContentEntry(String role, String text) {
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        JsonArray partsArray = new JsonArray();
        partsArray.add(part);
        JsonObject content = new JsonObject();
        content.add("parts", partsArray);
        content.addProperty("role", role);
        return content;
    }


    // 从文件加载对话历史
    private List<ChatMessage> loadHistoryFromFile() {
        if (Files.exists(HISTORY_FILE_PATH)) {
            try (Reader reader = Files.newBufferedReader(HISTORY_FILE_PATH, StandardCharsets.UTF_8)) {
                List<ChatMessage> history = gson.fromJson(reader, CHAT_MESSAGE_LIST_TYPE);
                // 确保即使文件为空或格式无效也返回一个空列表而不是 null
                return history != null ? history : new ArrayList<>();
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("读取或解析历史文件 '" + HISTORY_FILE_PATH + "' 时出错。将开始新的历史记录。错误: " + e.getMessage());
                // 出错时返回空列表，开始新的对话
                return new ArrayList<>();
            }
        } else {
            System.out.println("未找到历史文件。开始新对话。");
            return new ArrayList<>(); // 文件不存在，返回空列表
        }
    }


    // 将对话历史保存到文件
    private void saveHistoryToFile(List<ChatMessage> history) {
        try (Writer writer = Files.newBufferedWriter(HISTORY_FILE_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,       // 如果文件不存在则创建
                StandardOpenOption.WRITE,        // 以写入模式打开
                StandardOpenOption.TRUNCATE_EXISTING)) { // 覆盖现有内容
            gson.toJson(history, writer);
            System.out.println("对话历史已保存至 " + HISTORY_FILE_PATH.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("写入历史文件 '" + HISTORY_FILE_PATH + "' 时出错: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪以获取更多细节
        }
    }


    // 设置 CORS 响应头
    private void setupCORSHeaders(HttpExchange exchange) {
        // 允许所有来源，对于生产环境应更严格
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        // 允许的方法
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        // 允许的请求头
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    // 读取请求体
    private String readRequestBody(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    // 发送 HTTP 响应
    private void sendResponse(HttpExchange exchange, int statusCode, String statusMessage, String responseBody) throws IOException {
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // 创建 JSON 格式的错误消息体
    private String createErrorJson(String message) {
        JsonObject errorObject = new JsonObject();
        errorObject.addProperty("error", message);
        return gson.toJson(errorObject);
    }


    // 自定义异常类，用于封装 API 调用错误
    private static class ApiException extends Exception {
        private final int statusCode;
        public ApiException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
        public int getStatusCode() {
            return statusCode;
        }
    }


}