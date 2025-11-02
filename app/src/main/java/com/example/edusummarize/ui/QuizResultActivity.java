package com.example.edusummarize.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edusummarize.R;
import com.example.edusummarize.model.Quiz;
import com.example.edusummarize.model.QuizResult;
import com.example.edusummarize.repository.FirebaseQuizRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;

public class QuizResultActivity extends AppCompatActivity {
    private ImageView ivTrophy;
    private TextView tvScore, tvMessage;
    private ProgressBar progressCircular;
    private MaterialCardView cardWrongAnswers;
    private RecyclerView rvWrongAnswers;
    private MaterialButton btnViewAnswers, btnHome;

    private Quiz quiz;
    private int[] userAnswers;
    private int correctCount;
    private FirebaseQuizRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        initViews();
        repository = new FirebaseQuizRepository();

        // Get data from intent - USE getParcelableExtra for Parcelable
        quiz = getIntent().getParcelableExtra("quiz");
        userAnswers = getIntent().getIntArrayExtra("userAnswers");
        correctCount = getIntent().getIntExtra("correctCount", 0);

        // Validate data
        if (quiz == null || userAnswers == null) {
            Toast.makeText(this, "Lỗi: Không nhận được dữ liệu quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayResults();
        saveQuizResult();

        btnViewAnswers.setOnClickListener(v -> showWrongAnswers());
        btnHome.setOnClickListener(v -> goHome());
    }

    private void initViews() {
        ivTrophy = findViewById(R.id.iv_trophy);
        tvScore = findViewById(R.id.tv_score);
        tvMessage = findViewById(R.id.tv_message);
        progressCircular = findViewById(R.id.progress_circular);
        cardWrongAnswers = findViewById(R.id.card_wrong_answers);
        rvWrongAnswers = findViewById(R.id.rv_wrong_answers);
        btnViewAnswers = findViewById(R.id.btn_view_answers);
        btnHome = findViewById(R.id.btn_home);
    }

    private void displayResults() {
        int total = quiz.getQuestions().size();

        // Display score with animation
        tvScore.setText(String.format("%d/%d", correctCount, total));
        animateScore();

        // Calculate percentage
        int percentage = (int) ((correctCount * 100.0) / total);
        progressCircular.setProgress(percentage);

        // Display message based on score
        String message;
        if (percentage >= 80) {
            message = "Xuất sắc! Bạn đã làm rất tốt!";
        } else if (percentage >= 60) {
            message = "Tốt lắm! Bạn đang tiến bộ!";
        } else if (percentage >= 40) {
            message = "Cố gắng lên! Bạn có thể làm tốt hơn!";
        } else {
            message = "Đừng nản chí! Hãy thử lại nhé!";
        }
        tvMessage.setText(message);
    }

    private void animateScore() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
            0.0f, 1.0f, 0.0f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(500);
        scaleAnimation.setFillAfter(true);
        tvScore.startAnimation(scaleAnimation);

        // Also animate trophy
        ivTrophy.startAnimation(scaleAnimation);
    }

    private void saveQuizResult() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        QuizResult result = new QuizResult(
            UUID.randomUUID().toString(),
            quiz.getId(),
            userId,
            correctCount,
            quiz.getQuestions().size(),
            userAnswers,
            Timestamp.now()
        );

        repository.saveQuizResult(result, new FirebaseQuizRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // Saved successfully
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(QuizResultActivity.this,
                    "Không thể lưu kết quả: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showWrongAnswers() {
        if (cardWrongAnswers.getVisibility() == View.VISIBLE) {
            cardWrongAnswers.setVisibility(View.GONE);
            btnViewAnswers.setText("Xem đáp án");
        } else {
            cardWrongAnswers.setVisibility(View.VISIBLE);
            btnViewAnswers.setText("Ẩn đáp án");
            setupWrongAnswersRecyclerView();
        }
    }

    private void setupWrongAnswersRecyclerView() {
        // Simple text view for wrong answers
        rvWrongAnswers.setLayoutManager(new LinearLayoutManager(this));

        // Create a simple adapter showing wrong answers
        // For now, we'll just hide the RecyclerView as creating a full adapter
        // would require additional adapter class
        StringBuilder wrongAnswersText = new StringBuilder();
        for (int i = 0; i < userAnswers.length; i++) {
            if (userAnswers[i] != quiz.getQuestions().get(i).getCorrectAnswer()) {
                Quiz.Question q = quiz.getQuestions().get(i);
                wrongAnswersText.append("Câu ").append(i + 1).append(": ")
                    .append(q.getQuestion()).append("\n")
                    .append("Đáp án đúng: ")
                    .append(q.getOptions()[q.getCorrectAnswer()]).append("\n")
                    .append("Giải thích: ").append(q.getExplanation()).append("\n\n");
            }
        }

        if (wrongAnswersText.length() == 0) {
            wrongAnswersText.append("Bạn đã trả lời đúng tất cả!");
        }

        // For simplicity, show as Toast for now
        Toast.makeText(this, wrongAnswersText.toString(), Toast.LENGTH_LONG).show();
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
