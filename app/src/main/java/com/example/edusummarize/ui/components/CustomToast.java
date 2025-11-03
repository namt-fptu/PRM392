package com.example.edusummarize.ui.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.example.edusummarize.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Custom Snackbar/Toast with better design
 * Supports success, error, info, warning states
 */
public class CustomToast {

    public enum Type {
        SUCCESS, ERROR, INFO, WARNING
    }

    public static void show(Context context, String message, Type type) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        MaterialCardView card = layout.findViewById(R.id.toast_card);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_message);

        text.setText(message);

        // Set icon and color based on type
        int iconRes;
        int bgColor;
        switch (type) {
            case SUCCESS:
                iconRes = R.drawable.ic_success;
                bgColor = ContextCompat.getColor(context, R.color.success);
                break;
            case ERROR:
                iconRes = R.drawable.ic_error;
                bgColor = ContextCompat.getColor(context, R.color.error);
                break;
            case WARNING:
                iconRes = R.drawable.ic_info;
                bgColor = ContextCompat.getColor(context, R.color.warning);
                break;
            case INFO:
            default:
                iconRes = R.drawable.ic_info;
                bgColor = ContextCompat.getColor(context, R.color.info);
                break;
        }

        icon.setImageResource(iconRes);
        card.setCardBackgroundColor(bgColor);

        // Animate entrance
        layout.setAlpha(0f);
        layout.setTranslationY(-100f);
        layout.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setListener(null);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();

        // Animate exit after delay
        layout.postDelayed(() -> {
            layout.animate()
                .alpha(0f)
                .translationY(-100f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        toast.cancel();
                    }
                });
        }, 2000);
    }

    public static void success(Context context, String message) {
        show(context, message, Type.SUCCESS);
    }

    public static void error(Context context, String message) {
        show(context, message, Type.ERROR);
    }

    public static void info(Context context, String message) {
        show(context, message, Type.INFO);
    }

    public static void warning(Context context, String message) {
        show(context, message, Type.WARNING);
    }
}

