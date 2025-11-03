package com.example.edusummarize.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Animation Utilities for smooth transitions
 */
public class AnimationUtil {

    // Fade In animation
    public static void fadeIn(View view) {
        fadeIn(view, 300, null);
    }

    public static void fadeIn(View view, long duration, Runnable onComplete) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
    }

    // Fade Out animation
    public static void fadeOut(View view) {
        fadeOut(view, 300, () -> view.setVisibility(View.GONE));
    }

    public static void fadeOut(View view, long duration, Runnable onComplete) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
    }

    // Slide Up animation
    public static void slideUp(View view) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);
        view.animate()
            .translationY(0)
            .setDuration(400)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(null);
    }

    // Slide Down animation
    public static void slideDown(View view) {
        view.animate()
            .translationY(view.getHeight())
            .setDuration(400)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    view.setTranslationY(0);
                }
            });
    }

    // Scale animation
    public static void scale(View view, float fromScale, float toScale) {
        view.setScaleX(fromScale);
        view.setScaleY(fromScale);
        view.animate()
            .scaleX(toScale)
            .scaleY(toScale)
            .setDuration(300)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(null);
    }

    // Bounce animation for buttons
    public static void bounce(View view) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .setListener(null);
                }
            });
    }

    // Expand animation for expanding views
    public static void expand(View view) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 0;
        view.setVisibility(View.VISIBLE);

        view.animate()
            .setDuration(300)
            .setUpdateListener(animation -> {
                view.getLayoutParams().height = (int) (targetHeight * animation.getAnimatedFraction());
                view.requestLayout();
            })
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
            });
    }

    // Collapse animation
    public static void collapse(View view) {
        final int initialHeight = view.getMeasuredHeight();

        view.animate()
            .setDuration(300)
            .setUpdateListener(animation -> {
                view.getLayoutParams().height = initialHeight - (int) (initialHeight * animation.getAnimatedFraction());
                view.requestLayout();
            })
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
    }
}

