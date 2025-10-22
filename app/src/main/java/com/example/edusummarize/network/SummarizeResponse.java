package com.example.edusummarize.network;

import com.google.gson.annotations.SerializedName;

public class SummarizeResponse {

    @SerializedName("summary")
    private String summary;

    @SerializedName("status")
    private String status;

    public SummarizeResponse() {
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}