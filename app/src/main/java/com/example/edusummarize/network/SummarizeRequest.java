package com.example.edusummarize.network;

import com.google.gson.annotations.SerializedName;

public class SummarizeRequest {

    @SerializedName("text")
    private String text;

    @SerializedName("max_length")
    private int maxLength;

    public SummarizeRequest(String text, int maxLength) {
        this.text = text;
        this.maxLength = maxLength;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}