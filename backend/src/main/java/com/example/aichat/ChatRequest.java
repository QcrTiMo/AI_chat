package com.example.aichat;

import java.util.List;
import java.util.ArrayList;


public class ChatRequest {

    private List<ChatMessage> history;


    public List<ChatMessage> getHistory() { return history; }
    public void setHistory(List<ChatMessage> history) { this.history = history; }


    public ChatRequest() {
        this.history = new ArrayList<>();
    }
}