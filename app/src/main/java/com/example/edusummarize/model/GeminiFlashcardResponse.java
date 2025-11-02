package com.example.edusummarize.model;

import java.util.List;

public class GeminiFlashcardResponse {
    private List<FlashcardPair> cards;

    public GeminiFlashcardResponse() {}

    public GeminiFlashcardResponse(List<FlashcardPair> cards) {
        this.cards = cards;
    }

    public List<FlashcardPair> getCards() { return cards; }
    public void setCards(List<FlashcardPair> cards) { this.cards = cards; }

    public static class FlashcardPair {
        private String front;
        private String back;

        public FlashcardPair() {}
        public FlashcardPair(String front, String back) { this.front = front; this.back = back; }
        public String getFront() { return front; }
        public void setFront(String front) { this.front = front; }
        public String getBack() { return back; }
        public void setBack(String back) { this.back = back; }
    }
}

