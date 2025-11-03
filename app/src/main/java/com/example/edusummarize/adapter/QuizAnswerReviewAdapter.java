package com.example.edusummarize.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edusummarize.R;
import com.example.edusummarize.model.Quiz;

import java.util.List;

public class QuizAnswerReviewAdapter extends RecyclerView.Adapter<QuizAnswerReviewAdapter.ViewHolder> {

    private List<Quiz.Question> questions;
    private int[] userAnswers;

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
        boolean isCorrect = userAnswer == correctAnswer;

        // Set question number and text
        holder.tvQuestionNumber.setText("Câu " + (position + 1));
        holder.tvQuestionText.setText(question.getQuestion());

        // Set status icon
        if (isCorrect) {
            holder.ivStatus.setImageResource(R.drawable.ic_check);
            holder.ivStatus.setColorFilter(0xFF4CAF50); // Green
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_close);
            holder.ivStatus.setColorFilter(0xFFF44336); // Red
        }

        // Show user's answer if wrong
        if (!isCorrect) {
            holder.layoutUserAnswer.setVisibility(View.VISIBLE);
            String userAnswerText = userAnswer >= 0 && userAnswer < question.getOptions().length
                    ? question.getOptions()[userAnswer]
                    : "Chưa trả lời";
            holder.tvUserAnswer.setText(userAnswerText);
        } else {
            holder.layoutUserAnswer.setVisibility(View.GONE);
        }

        // Show correct answer
        holder.tvCorrectAnswer.setText(question.getOptions()[correctAnswer]);

        // Show explanation if available
        if (!TextUtils.isEmpty(question.getExplanation())) {
            holder.layoutExplanation.setVisibility(View.VISIBLE);
            holder.tvExplanation.setText(question.getExplanation());
        } else {
            holder.layoutExplanation.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionNumber;
        TextView tvQuestionText;
        ImageView ivStatus;
        LinearLayout layoutUserAnswer;
        TextView tvUserAnswer;
        TextView tvCorrectAnswer;
        LinearLayout layoutExplanation;
        TextView tvExplanation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionNumber = itemView.findViewById(R.id.tv_question_number);
            tvQuestionText = itemView.findViewById(R.id.tv_question_text);
            ivStatus = itemView.findViewById(R.id.iv_status);
            layoutUserAnswer = itemView.findViewById(R.id.layout_user_answer);
            tvUserAnswer = itemView.findViewById(R.id.tv_user_answer);
            tvCorrectAnswer = itemView.findViewById(R.id.tv_correct_answer);
            layoutExplanation = itemView.findViewById(R.id.layout_explanation);
            tvExplanation = itemView.findViewById(R.id.tv_explanation);
        }
    }
}

