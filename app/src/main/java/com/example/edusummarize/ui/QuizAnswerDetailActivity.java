package com.example.edusummarize.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.edusummarize.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

public class QuizAnswerDetailActivity extends AppCompatActivity {

    private TextView questionText;
    private TextView yourAnswerText;
    private TextView correctAnswerText;
    private TextView explanationText;
    private Chip resultChip;
    private MaterialButton backToQuizButton;
    private MaterialToolbar toolbar;
    private MaterialCardView yourAnswerCard;
    private MaterialCardView correctAnswerCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_answer_detail);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        questionText = findViewById(R.id.questionText);
        yourAnswerText = findViewById(R.id.yourAnswerText);
        correctAnswerText = findViewById(R.id.correctAnswerText);
        explanationText = findViewById(R.id.explanationText);
        resultChip = findViewById(R.id.resultChip);
        backToQuizButton = findViewById(R.id.backToQuizButton);
        yourAnswerCard = findViewById(R.id.yourAnswerCard);
        correctAnswerCard = findViewById(R.id.correctAnswerCard);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Get data from intent
        String question = getIntent().getStringExtra("question");
        String yourAnswer = getIntent().getStringExtra("yourAnswer");
        String correctAnswer = getIntent().getStringExtra("correctAnswer");
        String explanation = getIntent().getStringExtra("explanation");
        boolean isCorrect = getIntent().getBooleanExtra("isCorrect", false);
        int questionNumber = getIntent().getIntExtra("questionNumber", 1);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 1);

        // Update toolbar title with question number
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết câu " + questionNumber + "/" + totalQuestions);
        }

        // Set data to views
        if (question != null) {
            questionText.setText(question);
        }

        if (yourAnswer != null) {
            yourAnswerText.setText(yourAnswer);
        }

        if (correctAnswer != null) {
            correctAnswerText.setText(correctAnswer);
        }

        if (explanation != null) {
            explanationText.setText(explanation);
        }

        // Set result chip and card styling
        if (isCorrect) {
            resultChip.setText("✓ Đúng");
            resultChip.setChipBackgroundColorResource(R.color.success);
            resultChip.setTextColor(getResources().getColor(android.R.color.white, null));

            // Style your answer card for correct answer
            yourAnswerCard.setStrokeColor(getResources().getColor(R.color.success, null));
            yourAnswerCard.setStrokeWidth(4);

            // Hide correct answer card when user is correct (same answer)
            correctAnswerCard.setVisibility(View.GONE);
        } else {
            resultChip.setText("✗ Sai");
            resultChip.setChipBackgroundColorResource(R.color.error);
            resultChip.setTextColor(getResources().getColor(android.R.color.white, null));

            // Style your answer card for wrong answer
            yourAnswerCard.setStrokeColor(getResources().getColor(R.color.error, null));
            yourAnswerCard.setStrokeWidth(4);

            // Show correct answer card
            correctAnswerCard.setVisibility(View.VISIBLE);
        }

        // Setup button listener
        backToQuizButton.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
