package com.example.aichat;

import java.util.List;
import java.util.ArrayList;


public class BasicConfig {
    private String modelName;
    private List<String> apiKeys;
    private String initialPersona;
    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<String> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(List<String> apiKeys) {
        this.apiKeys = apiKeys;
    }

    public String getInitialPersona() {
        return initialPersona;
    }

    public void setInitialPersona(String initialPersona) {
        this.initialPersona = initialPersona;
    }

    public BasicConfig() {
        this.apiKeys = new ArrayList<>();
    }
}