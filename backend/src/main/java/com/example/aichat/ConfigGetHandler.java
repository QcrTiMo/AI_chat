package com.example.aichat;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ConfigGetHandler implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setupCORSHeaders(exchange);


        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }


        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "方法不允许", createErrorJson("仅支持 GET 方法。"));
            return;
        }

        try {


            WebConfig webConfig = ConfigLoader.loadOrCreateConfig(
                    ConfigLoader.WEB_CONFIG_FILE, WebConfig.class, ConfigLoader.getDefaultWebConfig()
            );

            BasicConfig basicConfig = ConfigLoader.loadOrCreateConfig(
                    ConfigLoader.BASIC_CONFIG_FILE, BasicConfig.class, ConfigLoader.getDefaultBasicConfig()
            );



            ConfigData currentConfig = new ConfigData(webConfig, basicConfig);


            String responseBody = gson.toJson(currentConfig);
            System.out.println("发送当前配置到前端: " + responseBody);


            sendResponse(exchange, 200, "OK", responseBody);

        } catch (Exception e) {

            System.err.println("在 ConfigGetHandler 中读取或处理配置文件时出错: " + e.getMessage());
            e.printStackTrace(); // 打印详细错误堆栈到后端控制台

            sendResponse(exchange, 500, "内部服务器错误", createErrorJson("读取服务器配置失败。"));
        }
    }



    private void setupCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    //辅助方法：发送 HTTP 响应
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