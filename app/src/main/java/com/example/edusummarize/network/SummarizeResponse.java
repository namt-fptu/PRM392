package com.example.edusummarize.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SummarizeResponse {

    @SerializedName("candidates")
    private List<Candidate> candidates;

    public static class Candidate {
        @SerializedName("content")
        private Content content;

        public Content getContent() {
            return content;
        }
    }

    public static class Content {
        @SerializedName("parts")
        private List<Part> parts;

        public List<Part> getParts() {
            return parts;
        }
    }

    public static class Part {
        @SerializedName("text")
        private String text;

        public String getText() {
            return text;
        }
    }

    public String getSummary() {
        if (candidates != null && !candidates.isEmpty() &&
            candidates.get(0).getContent() != null &&
            candidates.get(0).getContent().getParts() != null &&
            !candidates.get(0).getContent().getParts().isEmpty()) {
            return candidates.get(0).getContent().getParts().get(0).getText();
        }
        return null;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
}