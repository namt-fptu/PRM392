package com.example.edusummarize.model;

import com.google.firebase.Timestamp;

public class Summary {
    private String id;
    private String userId;
    private String title;
    private String originalText;
    private String summaryText;
    private String audioUrl;
    private Timestamp createdAt;

    public Summary() {
        // Required empty constructor for Firestore
    }

    public Summary(String id, String userId, String title, String originalText,
                   String summaryText, String audioUrl, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.originalText = originalText;
        this.summaryText = summaryText;
        this.audioUrl = audioUrl;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}