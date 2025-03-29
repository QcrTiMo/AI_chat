package com.example.aichat;

public class ConfigData { // 确保此类为 public
    private WebConfig webConfig;
    private BasicConfig basicConfig;

    // Getter 和 Setter 方法
    public WebConfig getWebConfig() { return webConfig; }
    public void setWebConfig(WebConfig webConfig) { this.webConfig = webConfig; }
    public BasicConfig getBasicConfig() { return basicConfig; }
    public void setBasicConfig(BasicConfig basicConfig) { this.basicConfig = basicConfig; }

    // 默认构造函数
    public ConfigData() {}

    // 便捷构造函数
    public ConfigData(WebConfig webConfig, BasicConfig basicConfig) {
        this.webConfig = webConfig;
        this.basicConfig = basicConfig;
    }
}
// --- 文件结束，不应在此包含 ConfigGetHandler 或其他 public 类的定义 ---