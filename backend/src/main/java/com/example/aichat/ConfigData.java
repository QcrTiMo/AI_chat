package com.example.aichat;

public class ConfigData { 
    private WebConfig webConfig;
    private BasicConfig basicConfig;
    public WebConfig getWebConfig() { return webConfig; }
    public void setWebConfig(WebConfig webConfig) { this.webConfig = webConfig; }
    public BasicConfig getBasicConfig() { return basicConfig; }
    public void setBasicConfig(BasicConfig basicConfig) { this.basicConfig = basicConfig; }


    public ConfigData() {}


    public ConfigData(WebConfig webConfig, BasicConfig basicConfig) {
        this.webConfig = webConfig;
        this.basicConfig = basicConfig;
    }
}
