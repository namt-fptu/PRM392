package com.example.edusummarize.model;

import java.util.List;

public class GeminiQuizResponse {
    private List<QuizItem> items;

    public GeminiQuizResponse() {}

    public GeminiQuizResponse(List<QuizItem> items) {
        this.items = items;
    }

    public List<QuizItem> getItems() { return items; }
    public void setItems(List<QuizItem> items) { this.items = items; }

    public static class QuizItem {
        private String question;
        private String[] options;
        private int correctAnswer;
        private String explanation;

        public QuizItem() {}

        public QuizItem(String question, String[] options, int correctAnswer, String explanation) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
        }

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String[] getOptions() { return options; }
        public void setOptions(String[] options) { this.options = options; }

        public int getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }
}

