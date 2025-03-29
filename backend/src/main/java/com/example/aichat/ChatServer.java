package com.example.aichat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ChatServer {

    private static final int PORT = 8080; // 服务器监听端口
    private static final int THREAD_POOL_SIZE = 10; // 线程池大小

    public static void main(String[] args) throws IOException {

        System.out.println("正在加载配置...");
        WebConfig webConfig = ConfigLoader.loadOrCreateConfig(
                ConfigLoader.WEB_CONFIG_FILE,
                WebConfig.class,
                ConfigLoader.getDefaultWebConfig()
        );
        BasicConfig basicConfig = ConfigLoader.loadOrCreateConfig(
                ConfigLoader.BASIC_CONFIG_FILE,
                BasicConfig.class,
                ConfigLoader.getDefaultBasicConfig()
        );
        System.out.println("配置加载完成。");

        // 验证基础配置的关键项
        if (basicConfig.getApiKeys() == null || basicConfig.getApiKeys().isEmpty() ||
                (basicConfig.getApiKeys().size() == 1 && "YOUR_DEFAULT_API_KEY_PLACEHOLDER".equals(basicConfig.getApiKeys().get(0)))) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("! 警告：在 " + ConfigLoader.BASIC_CONFIG_FILE + " 文件中未找到有效的 API 密钥！!");
            System.err.println("! 请编辑该文件并添加您的 Gemini API 密钥。             !");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }


        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        // 将加载的配置传递给处理器
        server.createContext("/api/chat", new ChatHandler(webConfig, basicConfig));
        // 为获取和保存配置创建上下文
        server.createContext("/api/config/get", new ConfigGetHandler());
        server.createContext("/api/config/save", new ConfigSaveHandler());
        // 设置服务器执行器（线程池）
        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));
        // 启动服务器
        server.start();
        // 打印服务器启动信息
        System.out.println("AI 聊天服务器已在端口 " + PORT + " 启动");
        System.out.println("API 端点: http://localhost:" + PORT + "/api/chat");
        System.out.println("正在使用模型: " + basicConfig.getModelName());
        System.out.println("已配置代理: " + (webConfig.getProxyUrl() != null && !webConfig.getProxyUrl().isEmpty() ? webConfig.getProxyUrl() : "无"));
        System.out.println("配置获取端点: http://localhost:" + PORT + "/api/config/get");
        System.out.println("配置保存端点: http://localhost:" + PORT + "/api/config/save");

    }
}
