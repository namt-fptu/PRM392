package com.example.edusummarize.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SummarizeRequest {

    @SerializedName("contents")
    private List<Content> contents;

    public SummarizeRequest(String text) {
        Part part = new Part("Hãy tóm tắt nội dung sau đây một cách ngắn gọn và súc tích bằng tiếng Việt:\n\n" + text);
        Content content = new Content(java.util.Arrays.asList(part));
        this.contents = java.util.Arrays.asList(content);
    }

    public static class Content {
        @SerializedName("parts")
        private final List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        @SerializedName("text")
        private final String text;

        public Part(String text) {
            this.text = text;
        }
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }
}