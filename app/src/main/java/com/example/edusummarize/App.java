package com.example.edusummarize;

import android.app.Application;
import com.google.firebase.FirebaseApp;

/**
 * Application class to initialize Firebase
 * Make sure to place google-services.json in app/ directory
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}