package com.example.edusummarize.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edusummarize.R;
import com.example.edusummarize.model.Flashcard;
import com.example.edusummarize.repository.FirebaseFlashcardRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FlashcardStatsActivity extends AppCompatActivity {
    private TextView tvLearnedToday, tvDue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_stats);

        tvLearnedToday = findViewById(R.id.tv_learned_today);
        tvDue = findViewById(R.id.tv_due);

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, R.string.not_signed_in, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFlashcardRepository repo = new FirebaseFlashcardRepository();
        repo.getFlashcardsDueToday(userId, new FirebaseFlashcardRepository.RepositoryCallback<List<Flashcard>>() {
            @Override
            public void onSuccess(List<Flashcard> result) {
                runOnUiThread(() -> tvDue.setText(getString(R.string.due_count, result.size())));
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(FlashcardStatsActivity.this, getString(R.string.failed_load_due, e.getMessage()), Toast.LENGTH_LONG).show());
            }
        });

        // Query flashcards reviewed today (lastReviewedAt >= start of day)
        FirebaseFirestore.getInstance().collection("flashcards")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener((QuerySnapshot snapshot) -> {
                    List<Flashcard> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Object lr = doc.get("lastReviewedAt");
                        if (lr instanceof Timestamp) {
                            Timestamp time = (Timestamp) lr;
                            Date d = time.toDate();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(new Date());
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            Date startOfDay = cal.getTime();
                            if (d.after(startOfDay) || d.equals(startOfDay)) {
                                list.add(doc.toObject(Flashcard.class));
                            }
                        }
                    }
                    int learned = list.size();
                    runOnUiThread(() -> tvLearnedToday.setText(getString(R.string.learned_today_count, learned)));
                })
                .addOnFailureListener(e -> runOnUiThread(() -> Toast.makeText(FlashcardStatsActivity.this, getString(R.string.failed_load_stats, e.getMessage()), Toast.LENGTH_LONG).show()));
    }
}
