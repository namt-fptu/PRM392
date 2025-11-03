package com.example.edusummarize.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Quiz implements Parcelable {
    private String id;
    private String summaryId;
    private String userId;
    private List<Question> questions;
    private long createdAtSeconds; // Store as long instead of Timestamp

    public Quiz() {
        // Required for Firebase
    }

    public Quiz(String id, String summaryId, String userId, List<Question> questions, Timestamp createdAt) {
        this.id = id;
        this.summaryId = summaryId;
        this.userId = userId;
        this.questions = questions;
        this.createdAtSeconds = createdAt != null ? createdAt.getSeconds() : 0;
    }

    protected Quiz(Parcel in) {
        id = in.readString();
        summaryId = in.readString();
        userId = in.readString();
        questions = new ArrayList<>();
        in.readList(questions, Question.class.getClassLoader());
        createdAtSeconds = in.readLong();
    }

    public static final Creator<Quiz> CREATOR = new Creator<Quiz>() {
        @Override
        public Quiz createFromParcel(Parcel in) {
            return new Quiz(in);
        }

        @Override
        public Quiz[] newArray(int size) {
            return new Quiz[size];
        }
    };

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSummaryId() { return summaryId; }
    public void setSummaryId(String summaryId) { this.summaryId = summaryId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public Timestamp getCreatedAt() {
        return new Timestamp(createdAtSeconds, 0);
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAtSeconds = createdAt != null ? createdAt.getSeconds() : 0;
    }

    // Firebase-compatible getters/setters for long fields
    public long getCreatedAtSeconds() {
        return createdAtSeconds;
    }

    public void setCreatedAtSeconds(long createdAtSeconds) {
        this.createdAtSeconds = createdAtSeconds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(summaryId);
        dest.writeString(userId);
        dest.writeList(questions);
        dest.writeLong(createdAtSeconds);
    }

    public static class Question implements Parcelable {
        private String question;
        private String[] options;
        private int correctAnswer;
        private String explanation;

        public Question() {}

        public Question(String question, String[] options, int correctAnswer, String explanation) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
        }

        protected Question(Parcel in) {
            question = in.readString();
            options = in.createStringArray();
            correctAnswer = in.readInt();
            explanation = in.readString();
        }

        public static final Creator<Question> CREATOR = new Creator<Question>() {
            @Override
            public Question createFromParcel(Parcel in) {
                return new Question(in);
            }

            @Override
            public Question[] newArray(int size) {
                return new Question[size];
            }
        };

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String[] getOptions() { return options; }
        public void setOptions(String[] options) { this.options = options; }

        // Firestore compatibility - convert List to array
        public java.util.List<String> getOptionsList() {
            if (options == null) return new java.util.ArrayList<>();
            return java.util.Arrays.asList(options);
        }

        public void setOptionsList(java.util.List<String> optionsList) {
            if (optionsList != null) {
                this.options = optionsList.toArray(new String[0]);
            }
        }

        public int getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(question);
            dest.writeStringArray(options);
            dest.writeInt(correctAnswer);
            dest.writeString(explanation);
        }
    }
}
