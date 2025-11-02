package com.example.edusummarize.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class QuizResult implements Serializable {
    private String id;
    private String quizId;
    private String userId;
    private int correctAnswers;
    private int totalQuestions;
    private int[] userAnswers;
    private Timestamp completedAt;

    public QuizResult() {}

    public QuizResult(String id, String quizId, String userId, int correctAnswers, int totalQuestions, int[] userAnswers, Timestamp completedAt) {
        this.id = id;
        this.quizId = quizId;
        this.userId = userId;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.userAnswers = userAnswers;
        this.completedAt = completedAt;
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

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
}

