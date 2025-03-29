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
    private final Yaml yamlWriter;

    public ConfigSaveHandler() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

        Representer representer = new Representer(options);
        representer.addClassTag(com.example.aichat.WebConfig.class, Tag.MAP);
        representer.addClassTag(com.example.aichat.BasicConfig.class, Tag.MAP);
        this.yamlWriter = new Yaml(representer, options);
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setupCORSHeaders(exchange); 
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); return; 
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "方法不允许", createErrorJson("仅支持 POST 方法。")); return;
        }

        try {
            String requestBody = readRequestBody(exchange.getRequestBody()); 
            System.out.println("收到配置保存请求体: " + requestBody);
            ConfigData newConfigData = gson.fromJson(requestBody, ConfigData.class);
            if (newConfigData == null || newConfigData.getWebConfig() == null || newConfigData.getBasicConfig() == null) {
                throw new IllegalArgumentException("收到的配置数据结构无效。");
            }
            if (newConfigData.getBasicConfig().getApiKeys() == null) {
                newConfigData.getBasicConfig().setApiKeys(new ArrayList<>());
                System.err.println("警告：收到的 apiKeys 列表为 null，已设置为空列表。");
            }
            Path webConfigPath = Paths.get(ConfigLoader.WEB_CONFIG_FILE);
            try (Writer writer = Files.newBufferedWriter(webConfigPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                yamlWriter.dump(newConfigData.getWebConfig(), writer);
                System.out.println("Web 配置已保存至: " + webConfigPath.toAbsolutePath());
            } catch (IOException e) {
                throw new IOException("保存 Web 配置失败。", e);
            }

            Path basicConfigPath = Paths.get(ConfigLoader.BASIC_CONFIG_FILE);
            try (Writer writer = Files.newBufferedWriter(basicConfigPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                yamlWriter.dump(newConfigData.getBasicConfig(), writer);
                System.out.println("Basic 配置已保存至: " + basicConfigPath.toAbsolutePath());
            } catch (IOException e) {
            
                throw new IOException("保存 Basic 配置失败。", e);
            }

          
            sendResponse(exchange, 200, "OK", "{\"message\":\"配置保存成功。\"}");

        } catch (JsonSyntaxException | IllegalArgumentException e) {
          
            System.err.println("处理保存请求时出错 (错误请求): " + e.getMessage());
            sendResponse(exchange, 400, "错误请求", createErrorJson("无效的配置数据格式: " + e.getMessage()));
        } catch (IOException e) {
   
            System.err.println("保存配置文件时出错 (内部服务器错误): " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "内部服务器错误", createErrorJson("保存配置文件失败: " + e.getMessage()));
        } catch (Exception e) {

            System.err.println("配置保存期间发生意外错误 (内部服务器错误): " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "内部服务器错误", createErrorJson("保存配置时发生意外错误。"));
        }
    }



    private void setupCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); 
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type"); 
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
        errorObject.addProperty("error", message);

        return gson.toJson(errorObject);
    }

}
