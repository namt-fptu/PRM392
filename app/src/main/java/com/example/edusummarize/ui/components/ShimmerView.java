package com.example.edusummarize.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Shimmer Loading Effect for better UX
 * Shows animated placeholder while loading data
 */
public class ShimmerView extends View {
    private Paint paint;
    private LinearGradient gradient;
    private float translateX = 0;
    private int shimmerWidth;

    public ShimmerView(Context context) {
        super(context);
        init();
    }

    public ShimmerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        shimmerWidth = w;

        gradient = new LinearGradient(
            0, 0, shimmerWidth, 0,
            new int[]{0x00FFFFFF, 0x40FFFFFF, 0x00FFFFFF},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        paint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(translateX, 0);
        canvas.drawRect(0, 0, shimmerWidth, getHeight(), paint);
        canvas.restore();
    }

    public void startShimmer() {
        post(shimmerRunnable);
    }

    public void stopShimmer() {
        removeCallbacks(shimmerRunnable);
    }

    private final Runnable shimmerRunnable = new Runnable() {
        @Override
        public void run() {
            translateX += 20;
            if (translateX > shimmerWidth) {
                translateX = -shimmerWidth;
            }
            invalidate();
            postDelayed(this, 16); // 60 FPS
        }
    };
}

