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
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Upload audio file to Firebase Storage
     */
    public void uploadAudio(File audioFile, UploadCallback callback) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fileName = audioFile.getName();

        StorageReference audioRef = storage.getReference()
                .child(STORAGE_AUDIO_PATH)
                .child(userId)
                .child(fileName);

        Uri fileUri = Uri.fromFile(audioFile);

        audioRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "Audio uploaded successfully: " + uri.toString());
                        callback.onSuccess(uri.toString());
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting download URL", e);
                        callback.onError("Không thể lấy URL: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading audio", e);
                    callback.onError("Upload thất bại: " + e.getMessage());
                });
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
     * Load summaries for a specific user
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