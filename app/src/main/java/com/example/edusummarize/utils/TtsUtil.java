package com.example.edusummarize.utils;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.io.File;
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
                // Try Vietnamese first
                int result = tts.setLanguage(new Locale("vi", "VN"));
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Vietnamese not supported, using English");
                    tts.setLanguage(Locale.US);
                }

                // Set speech rate and pitch
                tts.setSpeechRate(0.9f);
                tts.setPitch(1.0f);

                isInitialized = true;
                Log.d(TAG, "TTS initialized successfully");
            } else {
                Log.e(TAG, "TTS initialization failed with status: " + status);
            }
        });
    }

    public void convertTextToSpeech(String text, TtsCallback callback) {
        if (tts == null) {
            Log.e(TAG, "TTS is null");
            callback.onError("TTS chưa được khởi tạo");
            return;
        }

        if (!isInitialized) {
            Log.e(TAG, "TTS not initialized yet, waiting...");
            // Wait a bit and retry
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    if (isInitialized) {
                        convertTextToSpeech(text, callback);
                    } else {
                        callback.onError("TTS không thể khởi tạo. Vui lòng thử lại.");
                    }
                } catch (InterruptedException e) {
                    callback.onError("Lỗi khi chờ TTS khởi tạo");
                }
            }).start();
            return;
        }

        new Thread(() -> {
            try {
                // Create audio directory
                File audioDir = new File(context.getExternalFilesDir(null), "Audio");
                if (!audioDir.exists()) {
                    boolean created = audioDir.mkdirs();
                    Log.d(TAG, "Audio directory created: " + created);
                }

                // Create audio file with .mp3 extension (TTS will create WAV but we name it mp3 for compatibility)
                String timestamp = String.valueOf(System.currentTimeMillis());
                File audioFile = new File(audioDir, "audio_" + timestamp + ".mp3");

                Log.d(TAG, "Creating audio file: " + audioFile.getAbsolutePath());
                Log.d(TAG, "Text length: " + text.length() + " characters");

                String utteranceId = "utterance_" + timestamp;

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "TTS synthesis started for: " + utteranceId);
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(TAG, "TTS synthesis completed");
                        Log.d(TAG, "Audio file created: " + audioFile.exists());
                        Log.d(TAG, "Audio file size: " + audioFile.length() + " bytes");

                        if (audioFile.exists() && audioFile.length() > 0) {
                            callback.onSuccess(audioFile);
                        } else {
                            callback.onError("File audio được tạo nhưng rỗng");
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.e(TAG, "TTS synthesis error for: " + utteranceId);
                        callback.onError("Lỗi khi tạo audio");
                    }

                    @Override
                    public void onError(String utteranceId, int errorCode) {
                        Log.e(TAG, "TTS synthesis error: " + errorCode);
                        callback.onError("Lỗi TTS code: " + errorCode);
                    }
                });

                // Use new API for Android API 21+
                Bundle params = new Bundle();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);

                int result = tts.synthesizeToFile(text, params, audioFile, utteranceId);

                if (result == TextToSpeech.ERROR) {
                    Log.e(TAG, "synthesizeToFile returned ERROR");
                    callback.onError("Không thể tạo file audio");
                } else {
                    Log.d(TAG, "synthesizeToFile started successfully");
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
            Log.d(TAG, "TTS shutdown");
        }
    }
}