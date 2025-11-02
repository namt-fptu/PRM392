package com.example.edusummarize.model;

public class GeminiQuizRequest {
    private String text;

    public GeminiQuizRequest() {}

    public GeminiQuizRequest(String text) {
        this.text = text;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}

