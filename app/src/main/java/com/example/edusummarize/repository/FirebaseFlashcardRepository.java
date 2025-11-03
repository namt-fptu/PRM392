package com.example.edusummarize.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.edusummarize.model.Flashcard;
import com.example.edusummarize.model.GeminiFlashcardRequest;
import com.example.edusummarize.model.GeminiFlashcardResponse;
import com.example.edusummarize.network.ApiClient;
import com.example.edusummarize.network.FlashcardService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class FirebaseFlashcardRepository {
    private static final String TAG = "FlashcardRepo";
    private final FirebaseFirestore db;

    // Embedded key removed for security; prefer BuildConfig at runtime
    private static final String FALLBACK_GEMINI_API_KEY = "";

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public FirebaseFlashcardRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // Generate flashcards from text. This implementation attempts to call Gemini API, falls back to heuristic.
    public void generateFlashcards(String summaryId, String summaryText, RepositoryCallback<List<Flashcard>> callback) {
        // Use BuildConfig.GEMINI_API_KEY if set, otherwise fallback
        String apiKey = "";
        try {
            apiKey = com.example.edusummarize.BuildConfig.GEMINI_API_KEY;
        } catch (Exception ignored) {}
        if (apiKey == null || apiKey.isEmpty()) apiKey = FALLBACK_GEMINI_API_KEY;

        // Build prompt for Gemini - CẢI THIỆN: Tạo flashcard thay vì trắc nghiệm
        String prompt =
                "Tạo 10 flashcards học tập từ nội dung sau.\n" +
                        "Yêu cầu:\n" +
                        "- Mỗi flashcard gồm 2 mặt: câu hỏi (front) và đáp án (back)\n" +
                        "- Câu hỏi ngắn gọn, dễ hiểu, tập trung vào kiến thức chính\n" +
                        "- Đáp án rõ ràng, chính xác, không dài dòng\n" +
                        "- Phù hợp để ôn tập và ghi nhớ\n" +
                        "- Kết quả xuất ra theo định dạng JSON, không thêm chú thích hay văn bản ngoài JSON\n" +
                        "Định dạng JSON mẫu:\n" +
                        "[\n" +
                        "  {\"front\": \"Câu hỏi ở đây?\", \"back\": \"Đáp án ở đây\"},\n" +
                        "  {\"front\": \"Câu hỏi khác?\", \"back\": \"Đáp án khác\"},\n" +
                        "  ... (10 phần tử tương tự)\n" +
                        "]\n\n" +
                        "Văn bản: " + summaryText;
        // Build request body similar to SummarizeRequest used elsewhere (contents -> parts -> text)
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        List<Object> parts = new ArrayList<>();
        parts.add(part);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", parts);
        List<Object> contents = new ArrayList<>();
        contents.add(content);
        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);

        FlashcardService service = ApiClient.getClient().create(FlashcardService.class);
        Call<ResponseBody> call = service.generate(apiKey, body);

        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "Gemini API not successful, falling back to heuristic");
                    heuristicGenerate(summaryId, summaryText, callback);
                    return;
                }

                try {
                    String respStr = response.body().string();
                    Log.d(TAG, "Gemini raw response: " + respStr);

                    // Try to parse: look for first JSON array in response string
                    Gson gson = new Gson();
                    JsonArray arr = null;

                    // First try parse as object and navigate common fields
                    try {
                        JsonObject root = gson.fromJson(respStr, JsonObject.class);
                        // common possible paths: candidates[0].content.parts[0].text
                        if (root.has("candidates")) {
                            JsonArray candidates = root.getAsJsonArray("candidates");
                            if (candidates.size() > 0) {
                                JsonObject first = candidates.get(0).getAsJsonObject();
                                if (first.has("content")) {
                                    JsonObject content = first.getAsJsonObject("content");
                                    if (content.has("parts")) {
                                        JsonArray parts = content.getAsJsonArray("parts");
                                        StringBuilder sb = new StringBuilder();
                                        for (JsonElement el : parts) {
                                            JsonObject o = el.getAsJsonObject();
                                            if (o.has("text")) sb.append(o.get("text").getAsString());
                                        }
                                        String combined = sb.toString().trim();
                                        arr = extractFirstJsonArray(combined, gson);
                                    }
                                } else if (first.has("output")) {
                                    String out = first.get("output").getAsString();
                                    arr = extractFirstJsonArray(out, gson);
                                }
                            }
                        }

                        // fallback: try common field 'output' at root
                        if (arr == null && root.has("output")) {
                            arr = extractFirstJsonArray(root.get("output").getAsString(), gson);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to parse as structured response", e);
                    }

                    // If still null, try direct parse for array in the whole response string
                    if (arr == null) {
                        arr = extractFirstJsonArray(respStr, gson);
                    }

                    List<Flashcard> cards = new ArrayList<>();
                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                    Timestamp createdAt = Timestamp.now();

                    if (arr != null && arr.size() > 0) {
                        for (JsonElement el : arr) {
                            try {
                                JsonObject obj = el.getAsJsonObject();
                                String front = obj.has("front") ? obj.get("front").getAsString() : null;
                                String back = obj.has("back") ? obj.get("back").getAsString() : null;

                                // Validate both front and back exist
                                if (front == null || back == null || front.trim().isEmpty() || back.trim().isEmpty()) {
                                    continue;
                                }

                                Flashcard card = new Flashcard();
                                card.setId(UUID.randomUUID().toString());
                                card.setSummaryId(summaryId);
                                card.setUserId(userId);
                                card.setFront(front.trim());
                                card.setBack(back.trim());
                                card.setDifficulty(0);
                                card.setNextReview(Timestamp.now());
                                card.setReviewCount(0);
                                card.setCreatedAt(createdAt);
                                cards.add(card);
                            } catch (Exception ex) {
                                Log.w(TAG, "Failed to parse flashcard entry", ex);
                            }
                        }
                    }

                    if (cards.isEmpty()) {
                        Log.w(TAG, "No cards parsed from Gemini response, falling back to heuristic");
                        heuristicGenerate(summaryId, summaryText, callback);
                        return;
                    }

                    // Save cards
                    saveCardsRecursive(cards, 0, new ArrayList<>(), callback);

                } catch (IOException e) {
                    Log.e(TAG, "Failed to read Gemini response", e);
                    heuristicGenerate(summaryId, summaryText, callback);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Gemini API call failed", t);
                heuristicGenerate(summaryId, summaryText, callback);
            }
        });
    }

    // Helper: extract first JSON array from input string
    private JsonArray extractFirstJsonArray(String s, Gson gson) {
        if (s == null) return null;
        int start = s.indexOf('[');
        while (start >= 0) {
            int depth = 0;
            for (int i = start; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') depth--;
                if (depth == 0) {
                    String candidate = s.substring(start, i + 1);
                    try {
                        JsonElement el = gson.fromJson(candidate, JsonElement.class);
                        if (el != null && el.isJsonArray()) return el.getAsJsonArray();
                    } catch (Exception e) {
                        // try next '['
                    }
                    break;
                }
            }
            start = s.indexOf('[', start + 1);
        }
        return null;
    }

    private void heuristicGenerate(String summaryId, String summaryText, RepositoryCallback<List<Flashcard>> callback) {
        // Cải thiện: Tạo flashcard từ câu văn một cách thông minh hơn
        String[] sentences = summaryText.split("(?<=[\\.!?])\\s+");
        List<Flashcard> cards = new ArrayList<>();
        String userId = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        Timestamp createdAt = Timestamp.now();
        int count = 0;

        // Tạo flashcard từ các câu có độ dài hợp lý
        for (String sentence : sentences) {
            String trimmed = sentence.trim();

            // Skip câu quá ngắn hoặc quá dài
            if (trimmed.length() < 10 || trimmed.length() > 200) continue;

            // Tạo câu hỏi và đáp án từ câu văn
            String front, back;

            // Nếu câu có từ khóa quan trọng, tạo câu hỏi điền từ
            if (trimmed.contains(" là ") || trimmed.contains(" được ") || trimmed.contains(" có ")) {
                String[] parts = trimmed.split(" là | được | có ", 2);
                if (parts.length == 2) {
                    front = parts[0].trim() + " là gì?";
                    back = parts[1].trim();
                } else {
                    front = "Giải thích: " + trimmed.substring(0, Math.min(50, trimmed.length())) + "...";
                    back = trimmed;
                }
            } else {
                // Tạo câu hỏi chung
                front = "Điền vào chỗ trống: " + maskImportantWords(trimmed);
                back = trimmed;
            }

            Flashcard card = new Flashcard();
            card.setId(UUID.randomUUID().toString());
            card.setSummaryId(summaryId);
            card.setUserId(userId);
            card.setFront(front);
            card.setBack(back);
            card.setDifficulty(0);
            card.setNextReview(Timestamp.now());
            card.setReviewCount(0);
            card.setCreatedAt(createdAt);
            cards.add(card);

            count++;
            if (count >= 10) break; // Tối đa 10 flashcards
        }

        // Nếu không có đủ flashcard, tạo thêm từ văn bản
        if (cards.isEmpty()) {
            // Fallback: chia nhỏ văn bản
            String shortText = summaryText.length() > 100 ? summaryText.substring(0, 100) : summaryText;
            Flashcard card = new Flashcard();
            card.setId(UUID.randomUUID().toString());
            card.setSummaryId(summaryId);
            card.setUserId(userId);
            card.setFront("Nội dung chính của văn bản là gì?");
            card.setBack(shortText);
            card.setDifficulty(0);
            card.setNextReview(Timestamp.now());
            card.setReviewCount(0);
            card.setCreatedAt(createdAt);
            cards.add(card);
        }

        List<Flashcard> saved = new ArrayList<>();
        saveCardsRecursive(cards, 0, saved, callback);
    }

    // Helper method: Mask important words for fill-in-the-blank questions
    private String maskImportantWords(String text) {
        // Tìm từ quan trọng (danh từ riêng, số, từ dài) và thay bằng _____
        String[] words = text.split("\\s+");
        if (words.length < 3) return text;

        // Ẩn từ ở giữa câu (thường là từ quan trọng)
        int middleIndex = words.length / 2;
        StringBuilder masked = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i == middleIndex || (words[i].length() > 8 && Character.isUpperCase(words[i].charAt(0)))) {
                masked.append("_____ ");
            } else {
                masked.append(words[i]).append(" ");
            }
        }

        return masked.toString().trim();
    }

    private void saveCardsRecursive(List<Flashcard> cards, int idx, List<Flashcard> saved, RepositoryCallback<List<Flashcard>> callback) {
        if (idx >= cards.size()) {
            callback.onSuccess(saved);
            return;
        }
        Flashcard card = cards.get(idx);
        Map<String, Object> data = new HashMap<>();
        data.put("id", card.getId());
        data.put("summaryId", card.getSummaryId());
        data.put("userId", card.getUserId());
        data.put("front", card.getFront());
        data.put("back", card.getBack());
        data.put("difficulty", card.getDifficulty());
        data.put("nextReviewSeconds", card.getNextReviewSeconds());
        data.put("reviewCount", card.getReviewCount());
        data.put("createdAtSeconds", card.getCreatedAtSeconds());

        db.collection("flashcards").document(card.getId()).set(data)
                .addOnSuccessListener(aVoid -> {
                    saved.add(card);
                    saveCardsRecursive(cards, idx + 1, saved, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed save flashcard", e);
                    callback.onFailure(e);
                });
    }

    public void saveFlashcard(Flashcard card, RepositoryCallback<Void> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", card.getId());
        data.put("summaryId", card.getSummaryId());
        data.put("userId", card.getUserId());
        data.put("front", card.getFront());
        data.put("back", card.getBack());
        data.put("difficulty", card.getDifficulty());
        data.put("nextReviewSeconds", card.getNextReviewSeconds());
        data.put("reviewCount", card.getReviewCount());
        data.put("createdAtSeconds", card.getCreatedAtSeconds());

        db.collection("flashcards").document(card.getId()).set(data)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void getFlashcardsDueToday(String userId, RepositoryCallback<List<Flashcard>> callback) {
        Date now = new Date();
        Timestamp nowTs = Timestamp.now();
        db.collection("flashcards")
                .whereEqualTo("userId", userId)
                .whereLessThanOrEqualTo("nextReview", nowTs)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Flashcard> result = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                Flashcard c = doc.toObject(Flashcard.class);
                                result.add(c);
                            } catch (Exception e) {
                                // fallback manual map
                                Flashcard c = new Flashcard();
                                c.setId(doc.getString("id"));
                                c.setSummaryId(doc.getString("summaryId"));
                                c.setUserId(doc.getString("userId"));
                                c.setFront(doc.getString("front"));
                                c.setBack(doc.getString("back"));
                                Object nr = doc.get("nextReview");
                                if (nr instanceof com.google.firebase.Timestamp) c.setNextReview((com.google.firebase.Timestamp) nr);
                                result.add(c);
                            }
                        }
                        callback.onSuccess(result);
                    }
                }).addOnFailureListener(callback::onFailure);
    }

    public void updateReview(String cardId, String rating, RepositoryCallback<Void> callback) {
        // rating: "Again","Hard","Good","Easy"
        Timestamp nextReview;
        int difficulty = 1;
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        switch (rating) {
            case "Again":
                cal.add(Calendar.MINUTE, 1);
                difficulty = 0;
                break;
            case "Hard":
                cal.add(Calendar.MINUTE, 10);
                difficulty = 1;
                break;
            case "Good":
                cal.add(Calendar.DAY_OF_YEAR, 1);
                difficulty = 1;
                break;
            case "Easy":
                cal.add(Calendar.DAY_OF_YEAR, 4);
                difficulty = 2;
                break;
            default:
                cal.add(Calendar.MINUTE, 1);
        }
        nextReview = new Timestamp(cal.getTime());

        Map<String, Object> updates = new HashMap<>();
        updates.put("nextReview", nextReview);
        updates.put("difficulty", difficulty);
        updates.put("lastReviewedAt", Timestamp.now());
        // increment reviewCount using FieldValue.increment(1)

        db.collection("flashcards").document(cardId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // NEW: Get flashcards by summaryId
    public void getFlashcardsBySummaryId(String summaryId, RepositoryCallback<List<Flashcard>> callback) {
        db.collection("flashcards")
                .whereEqualTo("summaryId", summaryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Flashcard> result = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Flashcard c = doc.toObject(Flashcard.class);
                            result.add(c);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse flashcard", e);
                        }
                    }
                    callback.onSuccess(result);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
