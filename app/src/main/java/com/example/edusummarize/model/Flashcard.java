package com.example.edusummarize.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class Flashcard implements Parcelable {
    private String id;
    private String summaryId;
    private String userId;
    private String front;
    private String back;
    private int difficulty; // 0=new,1=learning,2=mastered
    private long nextReviewSeconds;
    private int reviewCount;
    private long createdAtSeconds;

    public Flashcard() {
        // Required for Firebase
    }

    public Flashcard(String id, String summaryId, String userId, String front, String back, int difficulty, Timestamp nextReview, int reviewCount, Timestamp createdAt) {
        this.id = id;
        this.summaryId = summaryId;
        this.userId = userId;
        this.front = front;
        this.back = back;
        this.difficulty = difficulty;
        this.nextReviewSeconds = nextReview != null ? nextReview.getSeconds() : 0;
        this.reviewCount = reviewCount;
        this.createdAtSeconds = createdAt != null ? createdAt.getSeconds() : 0;
    }

    protected Flashcard(Parcel in) {
        id = in.readString();
        summaryId = in.readString();
        userId = in.readString();
        front = in.readString();
        back = in.readString();
        difficulty = in.readInt();
        nextReviewSeconds = in.readLong();
        reviewCount = in.readInt();
        createdAtSeconds = in.readLong();
    }

    public static final Creator<Flashcard> CREATOR = new Creator<Flashcard>() {
        @Override
        public Flashcard createFromParcel(Parcel in) {
            return new Flashcard(in);
        }

        @Override
        public Flashcard[] newArray(int size) {
            return new Flashcard[size];
        }
    };

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSummaryId() { return summaryId; }
    public void setSummaryId(String summaryId) { this.summaryId = summaryId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }

    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public Timestamp getNextReview() {
        return new Timestamp(nextReviewSeconds, 0);
    }
    public void setNextReview(Timestamp nextReview) {
        this.nextReviewSeconds = nextReview != null ? nextReview.getSeconds() : 0;
    }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public Timestamp getCreatedAt() {
        return new Timestamp(createdAtSeconds, 0);
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAtSeconds = createdAt != null ? createdAt.getSeconds() : 0;
    }

    // Firebase-compatible getters/setters for long fields
    public long getNextReviewSeconds() {
        return nextReviewSeconds;
    }

    public void setNextReviewSeconds(long nextReviewSeconds) {
        this.nextReviewSeconds = nextReviewSeconds;
    }

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
        dest.writeString(front);
        dest.writeString(back);
        dest.writeInt(difficulty);
        dest.writeLong(nextReviewSeconds);
        dest.writeInt(reviewCount);
        dest.writeLong(createdAtSeconds);
    }
}
