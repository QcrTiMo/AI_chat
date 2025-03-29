package com.example.aichat;

// 用于Web代理设置的简单配置
public class WebConfig {
    // 代理服务器的 URL (例如, "http://localhost:7890", "socks://localhost:1080")
    // 如果不需要代理，请保持为空或 null。
    private String proxyUrl;
    // SnakeYAML 需要 getter 和 setter 方法（或公共字段）才能正确工作。
    public String getProxyUrl() {
        return proxyUrl;
    }
    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }
    // SnakeYAML 需要默认构造函数
    public WebConfig() {}
}