package com.example.aichat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ConfigSaveHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final Yaml yamlWriter; // 用于写入 YAML 的实例

    public ConfigSaveHandler() {
        DumperOptions options = new DumperOptions();
        // 使用块样式，看起来更整洁
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        // 美化输出
        options.setPrettyFlow(true);
        // 内容缩进设置为 2 个空格
        options.setIndent(2);
        // 指示符缩进通常不需要显式设置，默认为 0
        // options.setIndicatorIndent(2); // 移除或注释掉这一行
        // 使用普通标量样式（无引号，除非必要）
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

        Representer representer = new Representer(options);
        // 告诉 SnakeYAML 如何处理我们的配置类，避免输出类名标签
        representer.addClassTag(com.example.aichat.WebConfig.class, Tag.MAP);
        representer.addClassTag(com.example.aichat.BasicConfig.class, Tag.MAP);

        this.yamlWriter = new Yaml(representer, options);
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setupCORSHeaders(exchange); // 设置 CORS 头部
        // 处理 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); return; // 无内容响应
        }
        // 只接受 POST 请求
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "方法不允许", createErrorJson("仅支持 POST 方法。")); return;
        }

        try {
            // 读取请求体内容
            String requestBody = readRequestBody(exchange.getRequestBody()); // 调用辅助方法
            System.out.println("收到配置保存请求体: " + requestBody);

            // 使用 Gson 将 JSON 请求体解析为 ConfigData 对象
            ConfigData newConfigData = gson.fromJson(requestBody, ConfigData.class);

            // 验证接收到的数据结构是否有效
            if (newConfigData == null || newConfigData.getWebConfig() == null || newConfigData.getBasicConfig() == null) {
                throw new IllegalArgumentException("收到的配置数据结构无效。");
            }
            // 处理 apiKeys 可能为 null 的情况，确保它是一个列表
            if (newConfigData.getBasicConfig().getApiKeys() == null) {
                newConfigData.getBasicConfig().setApiKeys(new ArrayList<>());
                System.err.println("警告：收到的 apiKeys 列表为 null，已设置为空列表。");
            }

            // 保存 Web 配置
            Path webConfigPath = Paths.get(ConfigLoader.WEB_CONFIG_FILE);
            try (Writer writer = Files.newBufferedWriter(webConfigPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                // 使用配置好的 yamlWriter 将 WebConfig 对象写入文件
                yamlWriter.dump(newConfigData.getWebConfig(), writer);
                System.out.println("Web 配置已保存至: " + webConfigPath.toAbsolutePath());
            } catch (IOException e) {
                // 如果写入失败，抛出包含原始异常信息的新异常
                throw new IOException("保存 Web 配置失败。", e);
            }

            // 保存 Basic 配置
            Path basicConfigPath = Paths.get(ConfigLoader.BASIC_CONFIG_FILE);
            try (Writer writer = Files.newBufferedWriter(basicConfigPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                // 使用配置好的 yamlWriter 将 BasicConfig 对象写入文件
                yamlWriter.dump(newConfigData.getBasicConfig(), writer);
                System.out.println("Basic 配置已保存至: " + basicConfigPath.toAbsolutePath());
            } catch (IOException e) {
                // 如果写入失败，抛出包含原始异常信息的新异常
                throw new IOException("保存 Basic 配置失败。", e);
            }

            // 发送成功响应
            sendResponse(exchange, 200, "OK", "{\"message\":\"配置保存成功。\"}"); // 调用辅助方法

        } catch (JsonSyntaxException | IllegalArgumentException e) {
            // 处理 JSON 解析错误或无效参数错误 (400 Bad Request)
            System.err.println("处理保存请求时出错 (错误请求): " + e.getMessage());
            sendResponse(exchange, 400, "错误请求", createErrorJson("无效的配置数据格式: " + e.getMessage()));
        } catch (IOException e) {
            // 处理文件保存时的 IO 错误 (500 Internal Server Error)
            System.err.println("保存配置文件时出错 (内部服务器错误): " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪以供调试
            sendResponse(exchange, 500, "内部服务器错误", createErrorJson("保存配置文件失败: " + e.getMessage()));
        } catch (Exception e) {

            System.err.println("配置保存期间发生意外错误 (内部服务器错误): " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "内部服务器错误", createErrorJson("保存配置时发生意外错误。"));
        }
    }



    private void setupCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // 允许所有来源
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS"); // 允许的方法
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type"); // 允许的请求头
    }


    private String readRequestBody(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }


    private void sendResponse(HttpExchange exchange, int statusCode, String statusMessage, String responseBody) throws IOException {
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }


    private String createErrorJson(String message) {
        JsonObject errorObject = new JsonObject();
        // 添加错误消息字段
        errorObject.addProperty("error", message);

        return gson.toJson(errorObject);
    }

}