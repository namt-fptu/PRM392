package com.example.edusummarize.model;

public class GeminiFlashcardRequest {
    private String text;

    public GeminiFlashcardRequest() {}

    public GeminiFlashcardRequest(String text) {
        this.text = text;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}

