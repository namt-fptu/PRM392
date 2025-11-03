package com.example.edusummarize.repository;

import android.util.Log;

import com.example.edusummarize.model.Quiz;
import com.example.edusummarize.model.QuizResult;
import com.example.edusummarize.network.ApiClient;
import com.example.edusummarize.network.FlashcardService;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class FirebaseQuizRepository {
    private static final String TAG = "QuizRepo";
    private final FirebaseFirestore db;
    private static final String GEMINI_API_KEY = "AIzaSyAiI2W81wzjNFbovEVA3oTPU-nH6hslO5A";

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public FirebaseQuizRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void generateQuiz(String summaryId, String summaryText, RepositoryCallback<Quiz> callback) {
        // Validate input
        if (summaryText == null || summaryText.trim().isEmpty()) {
            callback.onFailure(new Exception("Văn bản tóm tắt trống"));
            return;
        }

        String prompt = "Tạo 5 câu hỏi trắc nghiệm từ văn bản sau. Trả về JSON array với format: [{\"question\": \"câu hỏi\", \"options\": [\"A\", \"B\", \"C\", \"D\"], \"correctAnswer\": 0, \"explanation\": \"giải thích\"}]. Văn bản: " + summaryText;

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
        Call<ResponseBody> call = service.generate(GEMINI_API_KEY, body);

        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "Gemini API not successful, code: " + response.code());
                    callback.onFailure(new Exception("API không phản hồi. Mã lỗi: " + response.code()));
                    return;
                }

                try {
                    String respStr = response.body().string();
                    Log.d(TAG, "Gemini quiz response: " + respStr);

                    if (respStr == null || respStr.trim().isEmpty()) {
                        callback.onFailure(new Exception("API trả về dữ liệu rỗng"));
                        return;
                    }

                    Gson gson = new Gson();
                    JsonArray arr = extractJsonArray(respStr, gson);

                    if (arr == null || arr.size() == 0) {
                        Log.e(TAG, "No questions array found in response");
                        callback.onFailure(new Exception("Không thể tạo câu hỏi từ văn bản này"));
                        return;
                    }

                    List<Quiz.Question> questions = new ArrayList<>();
                    for (JsonElement el : arr) {
                        try {
                            JsonObject obj = el.getAsJsonObject();
                            String question = obj.has("question") ? obj.get("question").getAsString() : "";

                            if (question.trim().isEmpty()) continue;

                            String[] options = new String[4];
                            if (obj.has("options") && obj.get("options").isJsonArray()) {
                                JsonArray optArr = obj.getAsJsonArray("options");
                                for (int i = 0; i < Math.min(4, optArr.size()); i++) {
                                    options[i] = optArr.get(i).getAsString();
                                }
                            }

                            // Validate options
                            boolean hasValidOptions = false;
                            for (String opt : options) {
                                if (opt != null && !opt.trim().isEmpty()) {
                                    hasValidOptions = true;
                                    break;
                                }
                            }

                            if (!hasValidOptions) continue;

                            int correctAnswer = obj.has("correctAnswer") ? obj.get("correctAnswer").getAsInt() : 0;
                            String explanation = obj.has("explanation") ? obj.get("explanation").getAsString() : "";

                            questions.add(new Quiz.Question(question, options, correctAnswer, explanation));
                        } catch (Exception ex) {
                            Log.e(TAG, "Error parsing question", ex);
                        }
                    }

                    if (questions.isEmpty()) {
                        callback.onFailure(new Exception("Không thể tạo câu hỏi hợp lệ"));
                        return;
                    }

                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                        FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

                    Quiz quiz = new Quiz(UUID.randomUUID().toString(), summaryId, userId, questions, Timestamp.now());
                    callback.onSuccess(quiz);

                } catch (IOException e) {
                    Log.e(TAG, "Failed to read response", e);
                    callback.onFailure(new Exception("Lỗi đọc dữ liệu: " + e.getMessage()));
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error", e);
                    callback.onFailure(new Exception("Lỗi không mong muốn: " + e.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                callback.onFailure(new Exception("Không thể kết nối API: " + t.getMessage()));
            }
        });
    }

    private JsonArray extractJsonArray(String s, Gson gson) {
        if (s == null) return null;

        // Try to parse as object first
        try {
            JsonObject root = gson.fromJson(s, JsonObject.class);
            if (root.has("candidates")) {
                JsonArray candidates = root.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject first = candidates.get(0).getAsJsonObject();
                    if (first.has("content")) {
                        JsonObject contentObj = first.getAsJsonObject("content");
                        if (contentObj.has("parts")) {
                            JsonArray parts = contentObj.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                JsonObject partObj = parts.get(0).getAsJsonObject();
                                if (partObj.has("text")) {
                                    String text = partObj.get("text").getAsString();
                                    return extractArrayFromText(text, gson);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing root", e);
        }

        // Fallback: find array in string
        return extractArrayFromText(s, gson);
    }

    private JsonArray extractArrayFromText(String s, Gson gson) {
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
                        // try next
                    }
                    break;
                }
            }
            start = s.indexOf('[', start + 1);
        }
        return null;
    }

    public void saveQuiz(Quiz quiz, RepositoryCallback<Void> callback) {
        // Convert Quiz to Firestore-compatible Map
        Map<String, Object> data = new HashMap<>();
        data.put("id", quiz.getId());
        data.put("summaryId", quiz.getSummaryId());
        data.put("userId", quiz.getUserId());

        // Convert questions list to list of maps (Firestore-compatible)
        List<Map<String, Object>> questionsList = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            for (Quiz.Question q : quiz.getQuestions()) {
                Map<String, Object> questionMap = new HashMap<>();
                questionMap.put("question", q.getQuestion());
                // Use List instead of array for Firestore
                questionMap.put("optionsList", q.getOptionsList());
                questionMap.put("correctAnswer", q.getCorrectAnswer());
                questionMap.put("explanation", q.getExplanation());
                questionsList.add(questionMap);
            }
        }
        data.put("questions", questionsList);
        data.put("createdAtSeconds", quiz.getCreatedAtSeconds());

        db.collection("quizzes").document(quiz.getId()).set(data)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }

    public void getQuizBySummaryId(String summaryId, RepositoryCallback<Quiz> callback) {
        db.collection("quizzes")
            .whereEqualTo("summaryId", summaryId)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    callback.onFailure(new Exception("No quiz found"));
                    return;
                }
                try {
                    // Manual parsing to handle Firestore data properly
                    Map<String, Object> data = queryDocumentSnapshots.getDocuments().get(0).getData();
                    if (data == null) {
                        callback.onFailure(new Exception("Quiz data is null"));
                        return;
                    }

                    Quiz quiz = new Quiz();
                    quiz.setId((String) data.get("id"));
                    quiz.setSummaryId((String) data.get("summaryId"));
                    quiz.setUserId((String) data.get("userId"));

                    Object createdAtObj = data.get("createdAtSeconds");
                    if (createdAtObj instanceof Long) {
                        quiz.setCreatedAtSeconds((Long) createdAtObj);
                    } else if (createdAtObj instanceof Number) {
                        quiz.setCreatedAtSeconds(((Number) createdAtObj).longValue());
                    }

                    // Parse questions
                    List<Quiz.Question> questions = new ArrayList<>();
                    Object questionsObj = data.get("questions");
                    if (questionsObj instanceof List) {
                        List<?> questionsList = (List<?>) questionsObj;
                        for (Object qObj : questionsList) {
                            if (qObj instanceof Map) {
                                Map<?, ?> qMap = (Map<?, ?>) qObj;
                                Quiz.Question question = new Quiz.Question();
                                question.setQuestion((String) qMap.get("question"));

                                // Handle options - try optionsList first, then fall back to options
                                Object optionsObj = qMap.get("optionsList");
                                if (optionsObj == null) {
                                    optionsObj = qMap.get("options");
                                }

                                if (optionsObj instanceof List) {
                                    List<?> optList = (List<?>) optionsObj;
                                    String[] options = new String[optList.size()];
                                    for (int i = 0; i < optList.size(); i++) {
                                        options[i] = (String) optList.get(i);
                                    }
                                    question.setOptions(options);
                                }

                                Object correctAnswerObj = qMap.get("correctAnswer");
                                if (correctAnswerObj instanceof Long) {
                                    question.setCorrectAnswer(((Long) correctAnswerObj).intValue());
                                } else if (correctAnswerObj instanceof Number) {
                                    question.setCorrectAnswer(((Number) correctAnswerObj).intValue());
                                }

                                question.setExplanation((String) qMap.get("explanation"));
                                questions.add(question);
                            }
                        }
                    }
                    quiz.setQuestions(questions);

                    callback.onSuccess(quiz);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing quiz", e);
                    callback.onFailure(e);
                }
            })
            .addOnFailureListener(callback::onFailure);
    }

    public void saveQuizResult(QuizResult result, RepositoryCallback<Void> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", result.getId());
        data.put("quizId", result.getQuizId());
        data.put("userId", result.getUserId());
        data.put("correctAnswers", result.getCorrectAnswers());
        data.put("totalQuestions", result.getTotalQuestions());
        // Use List instead of array for Firestore
        data.put("userAnswersList", result.getUserAnswersList());
        data.put("completedAtSeconds", result.getCompletedAtSeconds());

        db.collection("quizResults").document(result.getId()).set(data)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }

    public void getQuizResults(String userId, RepositoryCallback<List<QuizResult>> callback) {
        db.collection("quizResults")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<QuizResult> results = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    QuizResult result = document.toObject(QuizResult.class);
                    results.add(result);
                }
                callback.onSuccess(results);
            })
            .addOnFailureListener(callback::onFailure);
    }
}
