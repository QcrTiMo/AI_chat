package com.example.aichat;

public class ChatMessage {
    private String sender;
    private String text;
    private String mimeType;
    private String imageData;


    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getImageData() { return imageData; }
    public void setImageData(String imageData) { this.imageData = imageData; }


    public ChatMessage() {}

    public ChatMessage(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public ChatMessage(String sender, String text, String mimeType, String imageData) {
        this.sender = sender;
        this.text = text;
        this.mimeType = mimeType;
        this.imageData = imageData;
    }


    @Override
    public String toString() {
        return "ChatMessage{" +
                "sender='" + sender + '\'' +
                ", text='" + (text != null ? text.substring(0, Math.min(text.length(), 50)) + "..." : "null") + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", imageData=" + (imageData != null ? "present (" + imageData.length() + " chars)" : "null") +
                '}';
    }
}