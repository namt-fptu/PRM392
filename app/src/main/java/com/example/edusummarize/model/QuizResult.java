package com.example.edusummarize.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuizResult implements Serializable {
    private String id;
    private String quizId;
    private String userId;
    private int correctAnswers;
    private int totalQuestions;
    private int[] userAnswers;
    private long completedAtSeconds; // Use long instead of Timestamp for Firestore

    public QuizResult() {}

    public QuizResult(String id, String quizId, String userId, int correctAnswers, int totalQuestions, int[] userAnswers, Timestamp completedAt) {
        this.id = id;
        this.quizId = quizId;
        this.userId = userId;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.userAnswers = userAnswers;
        this.completedAtSeconds = completedAt != null ? completedAt.getSeconds() : 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int[] getUserAnswers() { return userAnswers; }
    public void setUserAnswers(int[] userAnswers) { this.userAnswers = userAnswers; }

    // Firestore compatibility - convert int[] to List
    public List<Integer> getUserAnswersList() {
        if (userAnswers == null) return new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        for (int answer : userAnswers) {
            list.add(answer);
        }
        return list;
    }

    public void setUserAnswersList(List<Integer> answersList) {
        if (answersList != null) {
            this.userAnswers = new int[answersList.size()];
            for (int i = 0; i < answersList.size(); i++) {
                this.userAnswers[i] = answersList.get(i);
            }
        }
    }

    public Timestamp getCompletedAt() {
        return new Timestamp(completedAtSeconds, 0);
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAtSeconds = completedAt != null ? completedAt.getSeconds() : 0;
    }

    // Firestore-compatible getters/setters for long fields
    public long getCompletedAtSeconds() {
        return completedAtSeconds;
    }

    public void setCompletedAtSeconds(long completedAtSeconds) {
        this.completedAtSeconds = completedAtSeconds;
    }
}
