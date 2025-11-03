package com.example.edusummarize.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SummarizeRequest {

    @SerializedName("contents")
    private List<Content> contents;

    public SummarizeRequest(String text) {
        Part part = new Part(
                "Bạn là trợ lý học tập thông minh. Hãy tóm tắt đoạn văn sau **một cách ngắn gọn nhưng vẫn đầy đủ kiến thức chính**, bằng tiếng Việt.\n" +
                        "- Giữ lại các khái niệm, định nghĩa, nguyên nhân – kết quả, luận điểm và kết luận quan trọng.\n" +
                        "- Lược bỏ ví dụ, số liệu phụ, hoặc chi tiết lặp lại.\n" +
                        "- Đảm bảo người đọc có thể hiểu trọn vẹn nội dung gốc sau khi đọc bản tóm tắt.\n" +
                        "- Viết theo văn phong học tập, mạch lạc và rõ ràng.\n" +
                        "- Độ dài tóm tắt nên khoảng 40–60% so với văn bản gốc.\n\n" +
                        "Nội dung cần tóm tắt:\n\n" + text
        );
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