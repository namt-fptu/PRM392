package com.example.edusummarize.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edusummarize.R;
import com.google.android.material.card.MaterialCardView;

public class QuestionIndicatorAdapter extends RecyclerView.Adapter<QuestionIndicatorAdapter.ViewHolder> {

    private int totalQuestions;
    private int currentQuestion;
    private boolean[] answeredStatus;
    private OnQuestionClickListener listener;

    public interface OnQuestionClickListener {
        void onQuestionClick(int position);
    }

    public QuestionIndicatorAdapter(int totalQuestions, OnQuestionClickListener listener) {
        this.totalQuestions = totalQuestions;
        this.currentQuestion = 0;
        this.answeredStatus = new boolean[totalQuestions];
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_indicator, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return totalQuestions;
    }

    public void setCurrentQuestion(int position) {
        int oldCurrent = this.currentQuestion;
        this.currentQuestion = position;
        notifyItemChanged(oldCurrent);
        notifyItemChanged(position);
    }

    public void setAnsweredStatus(int position, boolean answered) {
        this.answeredStatus[position] = answered;
        notifyItemChanged(position);
    }

    public void updateAllAnswers(boolean[] answers) {
        this.answeredStatus = answers.clone();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvQuestionNum;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_question_indicator);
            tvQuestionNum = itemView.findViewById(R.id.tv_question_num);
        }

        void bind(int position) {
            tvQuestionNum.setText(String.valueOf(position + 1));

            int backgroundColor;
            int strokeColor;
            int strokeWidth;
            int textColor;

            if (position == currentQuestion) {
                // Current question - highlighted with border
                backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.question_current);
                strokeColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_dark);
                strokeWidth = 8; // 4dp
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.white);
            } else if (answeredStatus[position]) {
                // Answered question - purple/blue color
                backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.question_answered);
                strokeColor = android.graphics.Color.TRANSPARENT;
                strokeWidth = 0;
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.white);
            } else {
                // Unanswered question - light gray
                backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.question_unanswered);
                strokeColor = android.graphics.Color.TRANSPARENT;
                strokeWidth = 0;
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.text_secondary);
            }

            cardView.setCardBackgroundColor(backgroundColor);
            cardView.setStrokeColor(strokeColor);
            cardView.setStrokeWidth(strokeWidth);
            tvQuestionNum.setTextColor(textColor);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuestionClick(position);
                }
            });
        }
    }
}

