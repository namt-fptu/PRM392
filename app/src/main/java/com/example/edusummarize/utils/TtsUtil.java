package com.example.edusummarize.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Text-to-Speech Utility
 * Converts text to audio file using Android TTS
 */
public class TtsUtil {

    private static final String TAG = "TtsUtil";
    private Context context;
    private TextToSpeech tts;
    private boolean isInitialized = false;

    public interface TtsCallback {
        void onSuccess(File audioFile);
        void onError(String error);
    }

    public TtsUtil(Context context) {
        this.context = context;
        initTts();
    }

    private void initTts() {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("vi", "VN"));
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to English
                    tts.setLanguage(Locale.US);
                }
                isInitialized = true;
            } else {
                Log.e(TAG, "TTS initialization failed");
            }
        });
    }

    public void convertTextToSpeech(String text, TtsCallback callback) {
        if (!isInitialized) {
            callback.onError("TTS chưa được khởi tạo");
            return;
        }

        new Thread(() -> {
            try {
                // Create audio file
                String timestamp = String.valueOf(System.currentTimeMillis());
                File audioFile = new File(context.getExternalFilesDir("Audio"),
                        "audio_" + timestamp + ".wav");

                if (!audioFile.getParentFile().exists()) {
                    audioFile.getParentFile().mkdirs();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, timestamp);

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "TTS started");
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(TAG, "TTS completed");
                        callback.onSuccess(audioFile);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.e(TAG, "TTS error");
                        callback.onError("Lỗi khi tạo audio");
                    }
                });

                int result = tts.synthesizeToFile(text, params, audioFile.getAbsolutePath());

                if (result == TextToSpeech.ERROR) {
                    callback.onError("Không thể tạo file audio");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in TTS conversion", e);
                callback.onError("Lỗi: " + e.getMessage());
            }
        }).start();
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}