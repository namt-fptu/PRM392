package com.example.edusummarize.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.example.edusummarize.R;

/**
 * Circular Progress Indicator with smooth animation
 * Better visual feedback than default ProgressBar
 */
public class CircularProgressView extends View {
    private Paint progressPaint;
    private Paint backgroundPaint;
    private RectF oval;
    private float progress = 0f;
    private float animatedProgress = 0f;
    private ValueAnimator animator;

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(12f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(getContext().getColor(R.color.primary));

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(12f);
        backgroundPaint.setColor(getContext().getColor(R.color.stroke));

        oval = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = progressPaint.getStrokeWidth() / 2;
        oval.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background circle
        canvas.drawArc(oval, 0, 360, false, backgroundPaint);

        // Draw progress arc
        canvas.drawArc(oval, -90, animatedProgress * 3.6f, false, progressPaint);
    }

    public void setProgress(int progressValue) {
        this.progress = progressValue;
        animateProgress();
    }

    private void animateProgress() {
        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(animatedProgress, progress);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animatedProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }
}

