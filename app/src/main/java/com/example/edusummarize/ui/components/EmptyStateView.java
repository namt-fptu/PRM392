package com.example.edusummarize.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.edusummarize.R;

/**
 * Reusable Empty State Component
 * Shows when lists are empty with illustration and message
 */
public class EmptyStateView extends ConstraintLayout {
    private ImageView ivIllustration;
    private TextView tvTitle;
    private TextView tvMessage;

    public EmptyStateView(Context context) {
        super(context);
        init();
    }

    public EmptyStateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_empty_state, this, true);
        ivIllustration = findViewById(R.id.iv_empty_illustration);
        tvTitle = findViewById(R.id.tv_empty_title);
        tvMessage = findViewById(R.id.tv_empty_message);
    }

    public void setEmptyState(@DrawableRes int iconRes, String title, String message) {
        ivIllustration.setImageResource(iconRes);
        tvTitle.setText(title);
        tvMessage.setText(message);
    }
}

