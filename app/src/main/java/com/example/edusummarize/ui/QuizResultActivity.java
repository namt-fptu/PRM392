package com.example.edusummarize.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edusummarize.R;
import com.example.edusummarize.adapter.QuizAnswerReviewAdapter;
import com.example.edusummarize.model.Quiz;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;

public class QuizResultActivity extends AppCompatActivity {

    private TextView tvScore;
    private TextView tvPercentage;
    private TextView tvMessage;
    private TextView tvCorrectCount;
    private TextView tvWrongCount;
    private MaterialButton btnViewAnswers;
    private MaterialButton btnHome;
    private CardView cardAnswersDetail;
    private RecyclerView rvAnswersDetail;
    private TextView tvToggleDetail;

    private ArrayList<Quiz.Question> questions;
    private int[] userAnswers;
    private int score;
    private int totalQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        initViews();
        getDataFromIntent();
        displayResults();
        setupListeners();
    }

    private void initViews() {
        tvScore = findViewById(R.id.tv_score);
        tvPercentage = findViewById(R.id.tv_percentage);
        tvMessage = findViewById(R.id.tv_message);
        tvCorrectCount = findViewById(R.id.tv_correct_count);
        tvWrongCount = findViewById(R.id.tv_wrong_count);
        btnViewAnswers = findViewById(R.id.btn_view_answers);
        btnHome = findViewById(R.id.btn_home);
        cardAnswersDetail = findViewById(R.id.card_answers_detail);
        rvAnswersDetail = findViewById(R.id.rv_answers_detail);
        tvToggleDetail = findViewById(R.id.tv_toggle_detail);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        questions = intent.getParcelableArrayListExtra("questions");
        userAnswers = intent.getIntArrayExtra("userAnswers");
        score = intent.getIntExtra("score", 0);
        totalQuestions = intent.getIntExtra("totalQuestions", 0);

        // Validate data
        if (questions == null) {
            questions = new ArrayList<>();
        }
        if (userAnswers == null) {
            userAnswers = new int[questions.size()];
            for (int i = 0; i < userAnswers.length; i++) {
                userAnswers[i] = -1;
            }
        }
        if (totalQuestions == 0) {
            totalQuestions = questions.size();
        }
    }

    private void displayResults() {
        // Display score
        tvScore.setText(String.format(Locale.getDefault(), "%d/%d", score, totalQuestions));

        // Calculate percentage
        int percentage = totalQuestions > 0 ? (score * 100) / totalQuestions : 0;
        tvPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));

        // Display counts
        int wrongCount = totalQuestions - score;
        tvCorrectCount.setText(String.valueOf(score));
        tvWrongCount.setText(String.valueOf(wrongCount));

        // Display message based on percentage
        String message;
        if (percentage >= 90) {
            message = "Xuất sắc! 🎉";
        } else if (percentage >= 70) {
            message = "Tốt lắm! 👍";
        } else if (percentage >= 50) {
            message = "Cố gắng hơn nhé! 💪";
        } else {
            message = "Hãy ôn tập thêm! 📚";
        }
        tvMessage.setText(message);

        // Setup RecyclerView for answer details
        setupAnswersList();
    }

    private void setupAnswersList() {
        if (questions != null && !questions.isEmpty()) {
            QuizAnswerReviewAdapter adapter = new QuizAnswerReviewAdapter(questions, userAnswers);
            rvAnswersDetail.setLayoutManager(new LinearLayoutManager(this));
            rvAnswersDetail.setAdapter(adapter);
            rvAnswersDetail.setNestedScrollingEnabled(false);
        }
    }

    private void setupListeners() {
        // View answers button
        btnViewAnswers.setOnClickListener(v -> toggleAnswersDetail());

        // Toggle detail text (collapse button)
        tvToggleDetail.setOnClickListener(v -> toggleAnswersDetail());

        // Home button
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void toggleAnswersDetail() {
        if (cardAnswersDetail.getVisibility() == View.GONE) {
            // Show answers
            cardAnswersDetail.setVisibility(View.VISIBLE);
            btnViewAnswers.setText(R.string.hide_answers_detail);
            btnViewAnswers.setIconResource(android.R.drawable.ic_menu_close_clear_cancel);
            tvToggleDetail.setText(R.string.collapse);
        } else {
            // Hide answers
            cardAnswersDetail.setVisibility(View.GONE);
            btnViewAnswers.setText(R.string.view_answers_detail);
            btnViewAnswers.setIconResource(android.R.drawable.ic_menu_view);
            tvToggleDetail.setText(R.string.view_detail);
        }
    }

    @Override
    public void onBackPressed() {
        // Go back to home when back button is pressed
        Intent intent = new Intent(QuizResultActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
