package com.example.edusummarize.utils;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Utility class for Firebase authentication debugging
 */
public class FirebaseAuthDebug {

    private static final String TAG = "FirebaseAuthDebug";

    public static void logAuthStatus() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        Log.d(TAG, "Firebase Auth Status:");
        Log.d(TAG, "- Auth instance: " + (auth != null ? "OK" : "NULL"));
        Log.d(TAG, "- Current user: " + (user != null ? "Logged in" : "Not logged in"));

        if (user != null) {
            Log.d(TAG, "- User UID: " + user.getUid());
            Log.d(TAG, "- User email: " + user.getEmail());
            Log.d(TAG, "- Email verified: " + user.isEmailVerified());
        }
    }

    public static void logError(String operation, Exception e) {
        Log.e(TAG, "Operation: " + operation);
        if (e != null) {
            Log.e(TAG, "Error message: " + e.getMessage());
            Log.e(TAG, "Error type: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }
}
