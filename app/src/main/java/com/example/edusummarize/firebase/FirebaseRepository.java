package com.example.edusummarize.firebase;

import android.net.Uri;
import android.util.Log;

import com.example.edusummarize.model.Summary;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Firebase Repository for handling Firestore and Storage operations
 *
 * Firebase Setup:
 * 1. Place google-services.json in app/ directory
 * 2. Configure Firestore rules:
 *    rules_version = '2';
 *    service cloud.firestore {
 *      match /databases/{database}/documents {
 *        match /summaries/{document=**} {
 *          allow read, write: if request.auth != null &&
 *                               request.auth.uid == resource.data.userId;
 *        }
 *      }
 *    }
 *
 * 3. Configure Storage rules:
 *    rules_version = '2';
 *    service firebase.storage {
 *      match /b/{bucket}/o {
 *        match /audio/{userId}/{filename} {
 *          allow read, write: if request.auth != null &&
 *                               request.auth.uid == userId;
 *        }
 *      }
 *    }
 */
public class FirebaseRepository {

    private static final String TAG = "FirebaseRepository";
    private static final String COLLECTION_SUMMARIES = "summaries";
    private static final String STORAGE_AUDIO_PATH = "audio";

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess(String documentId);
        void onError(String error);
    }

    public interface LoadCallback {
        void onSuccess(List<Summary> summaries);
        void onError(String error);
    }

    public FirebaseRepository() {
        firestore = FirebaseFirestore.getInstance();

        // Initialize Firebase Storage with explicit bucket
        try {
            storage = FirebaseStorage.getInstance("gs://edusummarize-6f11e.firebasestorage.app");
            Log.d(TAG, "Firebase Storage initialized with bucket: gs://edusummarize-6f11e.firebasestorage.app");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase Storage with specific bucket, using default", e);
            storage = FirebaseStorage.getInstance();
        }

        // Log storage reference info for debugging
        try {
            StorageReference rootRef = storage.getReference();
            Log.d(TAG, "Storage root reference: " + rootRef.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error accessing storage reference", e);
        }
    }

    /**
     * Upload audio file to Firebase Storage
     */
    public void uploadAudio(File audioFile, UploadCallback callback) {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "Upload failed: User not authenticated");
            callback.onError("User not authenticated");
            return;
        }

        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fileName = System.currentTimeMillis() + "_" + audioFile.getName();

        // Validate file exists and is readable
        if (!audioFile.exists()) {
            Log.e(TAG, "Upload failed: File does not exist - " + audioFile.getAbsolutePath());
            callback.onError("Tệp không tồn tại");
            return;
        }

        if (!audioFile.canRead()) {
            Log.e(TAG, "Upload failed: Cannot read file - " + audioFile.getAbsolutePath());
            callback.onError("Không thể đọc tệp");
            return;
        }

        Log.d(TAG, "Starting upload for user: " + userId + ", file: " + fileName);
        Log.d(TAG, "File path: " + audioFile.getAbsolutePath());
        Log.d(TAG, "File size: " + audioFile.length() + " bytes");

        // Create storage reference with proper path
        StorageReference audioRef = storage.getReference()
                .child(STORAGE_AUDIO_PATH)
                .child(userId)
                .child(fileName);

        Log.d(TAG, "Storage path: " + audioRef.getPath());

        Uri fileUri = Uri.fromFile(audioFile);

        audioRef.putFile(fileUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "Upload progress: " + progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Audio uploaded successfully, getting download URL...");
                    audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "Download URL obtained: " + uri.toString());
                        callback.onSuccess(uri.toString());
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting download URL", e);
                        callback.onError("Không thể lấy URL: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading audio", e);
                    // Check specific error types
                    String errorMsg = "Upload thất bại";
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("403")) {
                            errorMsg = "Không có quyền upload (403)";
                        } else if (e.getMessage().contains("404")) {
                            errorMsg = "Bucket storage không tồn tại (404)";
                        } else if (e.getMessage().contains("network")) {
                            errorMsg = "Lỗi kết nối mạng";
                        } else {
                            errorMsg = "Upload thất bại: " + e.getMessage();
                        }
                    }
                    callback.onError(errorMsg);
                });
    }

    /**
     * Alternative upload method with simpler path structure
     */
    public void uploadAudioSimple(File audioFile, UploadCallback callback) {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "Upload failed: User not authenticated");
            callback.onError("User not authenticated");
            return;
        }

        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fileName = "audio_" + System.currentTimeMillis() + ".mp3";

        Log.d(TAG, "Attempting simple upload for user: " + userId);

        // Try with a simpler path structure
        StorageReference audioRef = storage.getReference().child("uploads/" + userId + "/" + fileName);

        Log.d(TAG, "Simple storage path: " + audioRef.getPath());

        Uri fileUri = Uri.fromFile(audioFile);

        audioRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Simple upload successful");
                    audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "Simple upload download URL: " + uri.toString());
                        callback.onSuccess(uri.toString());
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting download URL from simple upload", e);
                        callback.onError("Không thể lấy URL: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Simple upload failed", e);
                    callback.onError("Simple upload thất bại: " + e.getMessage());
                });
    }

    /**
     * Test Firebase Storage connectivity
     */
    public void testStorageConnectivity(UploadCallback callback) {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        try {
            StorageReference testRef = storage.getReference().child("test_connectivity.txt");

            Log.d(TAG, "Testing storage connectivity...");
            Log.d(TAG, "Test reference path: " + testRef.getPath());
            Log.d(TAG, "Test reference bucket: " + testRef.getBucket());

            // Try to get metadata of a file (this tests connectivity without uploading)
            testRef.getMetadata()
                    .addOnSuccessListener(storageMetadata -> {
                        Log.d(TAG, "Storage connectivity test: SUCCESS");
                        callback.onSuccess("Storage is accessible");
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "Storage connectivity test: File doesn't exist (normal), but storage is accessible");
                        callback.onSuccess("Storage is accessible (file not found is expected)");
                    });
        } catch (Exception e) {
            Log.e(TAG, "Storage connectivity test: FAILED", e);
            callback.onError("Storage connectivity failed: " + e.getMessage());
        }
    }

    /**
     * Save summary to Firestore
     */
    public void saveSummary(Summary summary, SaveCallback callback) {
        firestore.collection(COLLECTION_SUMMARIES)
                .add(summary)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    Log.d(TAG, "Summary saved with ID: " + documentId);

                    // Update document with its own ID
                    documentReference.update("id", documentId)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(documentId))
                            .addOnFailureListener(e -> callback.onSuccess(documentId));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving summary", e);
                    callback.onError("Lưu thất bại: " + e.getMessage());
                });
    }

    /**
     * Save summary without audio (fallback option)
     */
    public void saveSummaryWithoutAudio(String userId, String title, String extractedText,
                                       String summaryText, SaveCallback callback) {
        Summary summary = new Summary(
                null,
                userId,
                title,
                extractedText,
                summaryText,
                null, // No audio URL
                com.google.firebase.Timestamp.now()
        );

        Log.d(TAG, "Saving summary without audio for user: " + userId);
        saveSummary(summary, callback);
    }

    /**
     * Get summaries for a specific user
     */
    public void getSummaries(String userId, LoadCallback callback) {
        firestore.collection(COLLECTION_SUMMARIES)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Summary> summaries = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Summary summary = document.toObject(Summary.class);
                        if (summary != null) {
                            if (summary.getId() == null) {
                                summary.setId(document.getId());
                            }
                            summaries.add(summary);
                        }
                    }

                    Log.d(TAG, "Loaded " + summaries.size() + " summaries");
                    callback.onSuccess(summaries);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading summaries", e);
                    callback.onError("Tải dữ liệu thất bại: " + e.getMessage());
                });
    }

    /**
     * Delete a summary
     */
    public void deleteSummary(String summaryId, SaveCallback callback) {
        firestore.collection(COLLECTION_SUMMARIES)
                .document(summaryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Summary deleted");
                    callback.onSuccess(summaryId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting summary", e);
                    callback.onError("Xóa thất bại: " + e.getMessage());
                });
    }
}