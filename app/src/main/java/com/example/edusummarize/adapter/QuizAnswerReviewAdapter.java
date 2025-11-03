package com.example.edusummarize.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edusummarize.R;
import com.example.edusummarize.model.Quiz;

import java.util.List;
import java.util.Locale;

public class QuizAnswerReviewAdapter extends RecyclerView.Adapter<QuizAnswerReviewAdapter.ViewHolder> {

    private final List<Quiz.Question> questions;
    private final int[] userAnswers;

    public QuizAnswerReviewAdapter(List<Quiz.Question> questions, int[] userAnswers) {
        this.questions = questions;
        this.userAnswers = userAnswers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_answer_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Quiz.Question question = questions.get(position);
        int userAnswer = userAnswers[position];
        int correctAnswer = question.getCorrectAnswer();

        // Set question number
        holder.tvQuestionNumber.setText(
                String.format(Locale.getDefault(), "Câu %d", position + 1)
        );

        // Set question text
        holder.tvQuestionText.setText(question.getQuestion());

        // Check if answer is correct
        boolean isCorrect = userAnswer == correctAnswer;

        // Set status icon
        if (userAnswer == -1) {
            // Not answered
            holder.tvStatusIcon.setText("⊘");
            holder.tvStatusIcon.setTextColor(holder.itemView.getContext().getColor(android.R.color.darker_gray));
        } else if (isCorrect) {
            // Correct answer
            holder.tvStatusIcon.setText("✓");
            holder.tvStatusIcon.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            // Wrong answer
            holder.tvStatusIcon.setText("✗");
            holder.tvStatusIcon.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        }

        // Get options
        String[] options = question.getOptions();

        // Set user answer
        if (userAnswer == -1) {
            holder.tvUserAnswer.setText(R.string.not_answered);
            holder.layoutUserAnswer.setBackgroundColor(
                    holder.itemView.getContext().getColor(android.R.color.darker_gray)
            );
        } else if (userAnswer >= 0 && userAnswer < options.length) {
            String userAnswerText = String.format(Locale.getDefault(), "%c. %s",
                    (char)('A' + userAnswer), options[userAnswer]);
            holder.tvUserAnswer.setText(userAnswerText);

            if (isCorrect) {
                holder.layoutUserAnswer.setBackgroundColor(
                        holder.itemView.getContext().getColor(android.R.color.holo_green_light)
                );
            } else {
                holder.layoutUserAnswer.setBackgroundColor(
                        holder.itemView.getContext().getColor(android.R.color.holo_red_light)
                );
            }
        }

        // Set correct answer
        if (correctAnswer >= 0 && correctAnswer < options.length) {
            String correctAnswerText = String.format(Locale.getDefault(), "%c. %s",
                    (char)('A' + correctAnswer), options[correctAnswer]);
            holder.tvCorrectAnswer.setText(correctAnswerText);
        }

        // Set explanation if available
        String explanation = question.getExplanation();
        if (explanation != null && !explanation.trim().isEmpty()) {
            holder.layoutExplanation.setVisibility(View.VISIBLE);
            holder.tvExplanation.setText(explanation);
        } else {
            holder.layoutExplanation.setVisibility(View.GONE);
        }

        // Update background colors for better visibility
        if (isCorrect) {
            holder.itemView.setBackgroundColor(
                    holder.itemView.getContext().getColor(android.R.color.white)
            );
        } else {
            // Slightly different background for wrong answers
            holder.itemView.setBackgroundColor(
                    holder.itemView.getContext().getColor(android.R.color.white)
            );
        }
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionNumber;
        TextView tvStatusIcon;
        TextView tvQuestionText;
        LinearLayout layoutUserAnswer;
        TextView tvUserAnswer;
        LinearLayout layoutCorrectAnswer;
        TextView tvCorrectAnswer;
        LinearLayout layoutExplanation;
        TextView tvExplanation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionNumber = itemView.findViewById(R.id.tv_question_number);
            tvStatusIcon = itemView.findViewById(R.id.tv_status_icon);
            tvQuestionText = itemView.findViewById(R.id.tv_question_text);
            layoutUserAnswer = itemView.findViewById(R.id.layout_user_answer);
            tvUserAnswer = itemView.findViewById(R.id.tv_user_answer);
            layoutCorrectAnswer = itemView.findViewById(R.id.layout_correct_answer);
            tvCorrectAnswer = itemView.findViewById(R.id.tv_correct_answer);
            layoutExplanation = itemView.findViewById(R.id.layout_explanation);
            tvExplanation = itemView.findViewById(R.id.tv_explanation);
        }
    }
}

