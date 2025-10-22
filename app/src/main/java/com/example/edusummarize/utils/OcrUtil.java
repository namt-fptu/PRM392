package com.example.edusummarize.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ML Kit Text Recognition Utility
 */
public class OcrUtil {

    private static final String TAG = "OcrUtil";

    public static String extractTextFromImage(Context context, Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(context, imageUri);
            return recognizeText(image);
        } catch (IOException e) {
            Log.e(TAG, "Error loading image", e);
            return "";
        }
    }

    public static String extractTextFromBitmap(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        return recognizeText(image);
    }

    private static String recognizeText(InputImage image) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        AtomicReference<String> resultText = new AtomicReference<>("");
        CountDownLatch latch = new CountDownLatch(1);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    StringBuilder text = new StringBuilder();
                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        text.append(block.getText()).append("\n");
                    }
                    resultText.set(text.toString().trim());
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed", e);
                    latch.countDown();
                });

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "OCR interrupted", e);
        }

        return resultText.get();
    }
}