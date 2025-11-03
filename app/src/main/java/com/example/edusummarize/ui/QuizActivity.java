package com.example.edusummarize.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edusummarize.R;
import com.example.edusummarize.adapter.QuestionIndicatorAdapter;
import com.example.edusummarize.model.Quiz;
import com.example.edusummarize.model.QuizResult;
import com.example.edusummarize.repository.FirebaseQuizRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.UUID;

public class QuizActivity extends AppCompatActivity {
    private TextView tvQuestionNumber, tvQuestion, tvProgressText;
    private RadioGroup radioGroupOptions;
    private RadioButton radioA, radioB, radioC, radioD;
    private MaterialButton btnGenerateQuiz, btnBack, btnNext;
    private ProgressBar progressLoading;
    private MaterialCardView cardQuiz, cardNavigationBar;
    private View layoutButtons;
    private RecyclerView rvQuestionIndicators;
    private ScrollView scrollViewQuiz;

    private FirebaseQuizRepository repository;
    private Quiz currentQuiz;
    private int currentQuestionIndex = 0;
    private int[] userAnswers;
    private QuestionIndicatorAdapter indicatorAdapter;

    private String summaryId;
    private String summaryText;
    private String userId;

    // Flag to prevent listener from triggering during programmatic changes
    private boolean isUpdatingQuestion = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Get data from intent
        summaryId = getIntent().getStringExtra("summaryId");
        summaryText = getIntent().getStringExtra("summaryText");

        // Get current user
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        initViews();
        repository = new FirebaseQuizRepository();

        // Set up radio group listener to update answered status
        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
            // Only process if not updating question programmatically
            if (!isUpdatingQuestion && currentQuiz != null && checkedId != -1) {
                saveCurrentAnswer();
                updateIndicatorForCurrentQuestion();
                updateProgressText();
            }
        });

        // Try to load existing quiz first
        loadExistingQuiz();

        btnGenerateQuiz.setOnClickListener(v -> generateQuiz());
        btnBack.setOnClickListener(v -> previousQuestion());
        btnNext.setOnClickListener(v -> nextQuestion());
    }

    private void initViews() {
        tvQuestionNumber = findViewById(R.id.tv_question_number);
        tvProgressText = findViewById(R.id.tv_progress_text);
        tvQuestion = findViewById(R.id.tv_question);
        radioGroupOptions = findViewById(R.id.radio_group_options);
        radioA = findViewById(R.id.radio_option_a);
        radioB = findViewById(R.id.radio_option_b);
        radioC = findViewById(R.id.radio_option_c);
        radioD = findViewById(R.id.radio_option_d);
        btnGenerateQuiz = findViewById(R.id.btn_generate_quiz);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);
        progressLoading = findViewById(R.id.progress_loading);
        cardQuiz = findViewById(R.id.card_quiz);
        layoutButtons = findViewById(R.id.layout_buttons);
        cardNavigationBar = findViewById(R.id.card_navigation_bar);
        rvQuestionIndicators = findViewById(R.id.rv_question_indicators);
        scrollViewQuiz = findViewById(R.id.scroll_view_quiz);
    }

    private void setupQuestionIndicators() {
        if (currentQuiz == null) return;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvQuestionIndicators.setLayoutManager(layoutManager);

        indicatorAdapter = new QuestionIndicatorAdapter(
                currentQuiz.getQuestions().size(),
                position -> navigateToQuestion(position)
        );
        rvQuestionIndicators.setAdapter(indicatorAdapter);

        // Update answered status based on current user answers
        updateAllIndicators();

        // Show the navigation bar
        cardNavigationBar.setVisibility(View.VISIBLE);
        tvProgressText.setVisibility(View.VISIBLE);
        updateProgressText();
    }

    private void navigateToQuestion(int position) {
        if (position >= 0 && position < currentQuiz.getQuestions().size()) {
            saveCurrentAnswer();
            displayQuestion(position);

            // Smooth scroll to top of quiz card
            scrollViewQuiz.smoothScrollTo(0, 0);

            // Scroll the indicator to make the selected item visible
            rvQuestionIndicators.smoothScrollToPosition(position);
        }
    }

    private void updateIndicatorForCurrentQuestion() {
        if (indicatorAdapter != null) {
            boolean isAnswered = userAnswers[currentQuestionIndex] != -1;
            indicatorAdapter.setAnsweredStatus(currentQuestionIndex, isAnswered);
        }
    }

    private void updateAllIndicators() {
        if (indicatorAdapter != null && userAnswers != null) {
            boolean[] answeredStatus = new boolean[userAnswers.length];
            for (int i = 0; i < userAnswers.length; i++) {
                answeredStatus[i] = userAnswers[i] != -1;
            }
            indicatorAdapter.updateAllAnswers(answeredStatus);
        }
    }

    private void updateProgressText() {
        if (userAnswers != null) {
            int answeredCount = 0;
            for (int answer : userAnswers) {
                if (answer != -1) answeredCount++;
            }
            tvProgressText.setText(String.format("%d/%d câu đã trả lời", answeredCount, userAnswers.length));
        }
    }

    private void loadExistingQuiz() {
        // Show loading
        progressLoading.setVisibility(View.VISIBLE);
        btnGenerateQuiz.setVisibility(View.GONE);

        repository.getQuizBySummaryId(summaryId, new FirebaseQuizRepository.RepositoryCallback<Quiz>() {
            @Override
            public void onSuccess(Quiz result) {
                runOnUiThread(() -> {
                    currentQuiz = result;
                    userAnswers = new int[currentQuiz.getQuestions().size()];
                    for (int i = 0; i < userAnswers.length; i++) {
                        userAnswers[i] = -1; // -1 means not answered
                    }

                    progressLoading.setVisibility(View.GONE);
                    cardQuiz.setVisibility(View.VISIBLE);
                    layoutButtons.setVisibility(View.VISIBLE);

                    setupQuestionIndicators();
                    displayQuestion(0);
                    Toast.makeText(QuizActivity.this, "Đã tải quiz từ database", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                // No existing quiz found, show generate button
                runOnUiThread(() -> {
                    progressLoading.setVisibility(View.GONE);
                    btnGenerateQuiz.setVisibility(View.VISIBLE);
                    Toast.makeText(QuizActivity.this, "Chưa có quiz. Nhấn để tạo mới", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void generateQuiz() {
        btnGenerateQuiz.setVisibility(View.GONE);
        progressLoading.setVisibility(View.VISIBLE);

        repository.generateQuiz(summaryId, summaryText, new FirebaseQuizRepository.RepositoryCallback<Quiz>() {
            @Override
            public void onSuccess(Quiz result) {
                runOnUiThread(() -> {
                    currentQuiz = result;
                    userAnswers = new int[currentQuiz.getQuestions().size()];
                    for (int i = 0; i < userAnswers.length; i++) {
                        userAnswers[i] = -1; // -1 means not answered
                    }

                    // Save quiz to Firestore
                    repository.saveQuiz(currentQuiz, new FirebaseQuizRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            runOnUiThread(() -> {
                                progressLoading.setVisibility(View.GONE);
                                cardQuiz.setVisibility(View.VISIBLE);
                                layoutButtons.setVisibility(View.VISIBLE);

                                setupQuestionIndicators();
                                displayQuestion(0);
                                Toast.makeText(QuizActivity.this, "Đã tạo và lưu quiz thành công", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                progressLoading.setVisibility(View.GONE);
                                cardQuiz.setVisibility(View.VISIBLE);
                                layoutButtons.setVisibility(View.VISIBLE);

                                setupQuestionIndicators();
                                displayQuestion(0);
                                Toast.makeText(QuizActivity.this, "Quiz được tạo nhưng không lưu được", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    progressLoading.setVisibility(View.GONE);
                    btnGenerateQuiz.setVisibility(View.VISIBLE);
                    Toast.makeText(QuizActivity.this, "Không thể tạo quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void displayQuestion(int index) {
        if (currentQuiz == null || currentQuiz.getQuestions().isEmpty()) return;

        // Set flag to prevent listener from triggering
        isUpdatingQuestion = true;

        currentQuestionIndex = index;
        Quiz.Question question = currentQuiz.getQuestions().get(index);

        tvQuestionNumber.setText(String.format("Câu %d/%d", index + 1, currentQuiz.getQuestions().size()));
        tvQuestion.setText(question.getQuestion());

        String[] options = question.getOptions();
        radioA.setText(options[0] != null ? options[0] : "");
        radioB.setText(options[1] != null ? options[1] : "");
        radioC.setText(options[2] != null ? options[2] : "");
        radioD.setText(options[3] != null ? options[3] : "");

        // Restore previous answer if any
        radioGroupOptions.clearCheck();
        if (userAnswers[index] != -1) {
            int selectedId = getRadioButtonId(userAnswers[index]);
            radioGroupOptions.check(selectedId);
        }

        // Update button text
        if (index == currentQuiz.getQuestions().size() - 1) {
            btnNext.setText("Nộp bài");
        } else {
            btnNext.setText("Tiếp theo");
        }

        btnBack.setEnabled(index > 0);

        // Update indicator adapter
        if (indicatorAdapter != null) {
            indicatorAdapter.setCurrentQuestion(index);
        }

        // Reset flag after all updates are done
        isUpdatingQuestion = false;
    }

    private int getRadioButtonId(int index) {
        switch (index) {
            case 0:
                return R.id.radio_option_a;
            case 1:
                return R.id.radio_option_b;
            case 2:
                return R.id.radio_option_c;
            case 3:
                return R.id.radio_option_d;
            default:
                return -1;
        }
    }

    private int getSelectedOptionIndex() {
        int selectedId = radioGroupOptions.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_option_a) return 0;
        if (selectedId == R.id.radio_option_b) return 1;
        if (selectedId == R.id.radio_option_c) return 2;
        if (selectedId == R.id.radio_option_d) return 3;
        return -1;
    }

    private void previousQuestion() {
        if (currentQuestionIndex > 0) {
            saveCurrentAnswer();
            displayQuestion(currentQuestionIndex - 1);
            scrollViewQuiz.smoothScrollTo(0, 0);
        }
    }

    private void nextQuestion() {
        saveCurrentAnswer();

        if (currentQuestionIndex < currentQuiz.getQuestions().size() - 1) {
            displayQuestion(currentQuestionIndex + 1);
            scrollViewQuiz.smoothScrollTo(0, 0);
        } else {
            // Submit quiz
            submitQuiz();
        }
    }

    private void saveCurrentAnswer() {
        int selected = getSelectedOptionIndex();
        if (selected != -1) {
            userAnswers[currentQuestionIndex] = selected;
        }
    }

    private void submitQuiz() {
        // Check if all questions are answered
        for (int i = 0; i < userAnswers.length; i++) {
            if (userAnswers[i] == -1) {
                Toast.makeText(this, "Vui lòng trả lời tất cả các câu hỏi", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Calculate score
        int correctCount = 0;
        for (int i = 0; i < userAnswers.length; i++) {
            if (userAnswers[i] == currentQuiz.getQuestions().get(i).getCorrectAnswer()) {
                correctCount++;
            }
        }

        final int finalCorrectCount = correctCount;
        final int totalQuestions = currentQuiz.getQuestions().size();

        // Create quiz result
        String resultId = UUID.randomUUID().toString();
        QuizResult result = new QuizResult(
                resultId,
                currentQuiz.getId(),
                userId,
                correctCount,
                totalQuestions,
                userAnswers,
                Timestamp.now()
        );

        // Save result to database
        if (userId != null) {
            repository.saveQuizResult(result, new FirebaseQuizRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    runOnUiThread(() -> {
                        showResultDialog(finalCorrectCount, totalQuestions, "Đã lưu kết quả thành công");
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        showResultDialog(finalCorrectCount, totalQuestions, "Lỗi lưu kết quả: " + e.getMessage());
                    });
                }
            });
        } else {
            showResultDialog(finalCorrectCount, totalQuestions, "Chưa đăng nhập - không lưu được kết quả");
        }
    }

    private void showResultDialog(int correctCount, int totalQuestions, String saveStatus) {
        double percentage = (correctCount * 100.0) / totalQuestions;
        String grade;
        String emoji;

        if (percentage >= 90) {
            grade = "Xuất sắc!";
            emoji = "🎉";
        } else if (percentage >= 80) {
            grade = "Tốt lắm!";
            emoji = "👍";
        } else if (percentage >= 70) {
            grade = "Khá tốt!";
            emoji = "😊";
        } else if (percentage >= 50) {
            grade = "Trung bình";
            emoji = "😐";
        } else {
            grade = "Cần cố gắng thêm";
            emoji = "💪";
        }

        // Create custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_result, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        // Setup views
        TextView tvResultIcon = dialogView.findViewById(R.id.tv_result_icon);
        TextView tvResultTitle = dialogView.findViewById(R.id.tv_result_title);
        TextView tvScorePercentage = dialogView.findViewById(R.id.tv_score_percentage);
        TextView tvScoreFraction = dialogView.findViewById(R.id.tv_score_fraction);
        TextView tvSaveStatus = dialogView.findViewById(R.id.tv_save_status);
        MaterialButton btnClose = dialogView.findViewById(R.id.btn_close);
        MaterialButton btnViewDetail = dialogView.findViewById(R.id.btn_view_detail);

        // Set data
        tvResultIcon.setText(emoji);
        tvResultTitle.setText(grade);
        tvScorePercentage.setText(String.format("%.0f%%", percentage));
        tvScoreFraction.setText(String.format("%d/%d câu đúng", correctCount, totalQuestions));

        // Format save status with icon
        if (saveStatus.contains("thành công")) {
            tvSaveStatus.setText("✓ " + saveStatus);
            tvSaveStatus.setTextColor(getResources().getColor(R.color.success));
        } else {
            tvSaveStatus.setText("⚠ " + saveStatus);
            tvSaveStatus.setTextColor(getResources().getColor(R.color.warning));
        }

        // Button actions
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        btnViewDetail.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(QuizActivity.this, QuizResultActivity.class);

            // Truyền đúng dữ liệu mà QuizResultActivity cần
            ArrayList<Quiz.Question> questions = new ArrayList<>(currentQuiz.getQuestions());
            intent.putParcelableArrayListExtra("questions", questions);
            intent.putExtra("userAnswers", userAnswers);
            intent.putExtra("score", correctCount);
            intent.putExtra("totalQuestions", currentQuiz.getQuestions().size());

            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}
