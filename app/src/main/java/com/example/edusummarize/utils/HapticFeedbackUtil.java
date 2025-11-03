package com.example.edusummarize.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

/**
 * Haptic Feedback Utility
 * Provides tactile feedback for better UX
 */
public class HapticFeedbackUtil {

    public static void lightVibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(10);
            }
        }
    }

    public static void mediumVibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(30);
            }
        }
    }

    public static void successVibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] pattern = {0, 50, 50, 50};
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                long[] pattern = {0, 50, 50, 50};
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    public static void errorVibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] pattern = {0, 100, 50, 100};
                int[] amplitudes = {0, 255, 0, 255};
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1));
            } else {
                long[] pattern = {0, 100, 50, 100};
                vibrator.vibrate(pattern, -1);
            }
        }
    }
}

