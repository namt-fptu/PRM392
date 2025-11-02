package com.example.edusummarize.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.edusummarize.R;
import com.example.edusummarize.adapter.FlashcardAdapter;
import com.example.edusummarize.model.Flashcard;
import com.example.edusummarize.repository.FirebaseFlashcardRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FlashcardActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private FlashcardAdapter adapter;
    private TextView tvProgress;
    private MaterialButton btnAgain, btnHard, btnGood, btnEasy;
    private FirebaseFlashcardRepository repository;
    private List<Flashcard> cards = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        viewPager = findViewById(R.id.view_pager);
        tvProgress = findViewById(R.id.tv_progress);
        btnAgain = findViewById(R.id.btn_again);
        btnHard = findViewById(R.id.btn_hard);
        btnGood = findViewById(R.id.btn_good);
        btnEasy = findViewById(R.id.btn_easy);

        repository = new FirebaseFlashcardRepository();

        // Get cards from intent - USE getParcelableArrayListExtra for Parcelable
        ArrayList<Flashcard> cardsFromIntent = getIntent().getParcelableArrayListExtra("cards");
        if (cardsFromIntent != null) {
            cards = cardsFromIntent;
        }

        if (cards == null || cards.isEmpty()) {
            // fallback: load due today
            String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ?
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            if (userId == null) {
                Toast.makeText(this, "No cards available", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            repository.getFlashcardsDueToday(userId, new FirebaseFlashcardRepository.RepositoryCallback<List<Flashcard>>() {
                @Override
                public void onSuccess(List<Flashcard> result) {
                    cards = result;
                    setupPager();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(FlashcardActivity.this, "Failed to load cards: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } else {
            setupPager();
        }

        btnAgain.setOnClickListener(v -> onRateCurrent("Again"));
        btnHard.setOnClickListener(v -> onRateCurrent("Hard"));
        btnGood.setOnClickListener(v -> onRateCurrent("Good"));
        btnEasy.setOnClickListener(v -> onRateCurrent("Easy"));
    }

    private void setupPager() {
        runOnUiThread(() -> {
            adapter = new FlashcardAdapter(cards);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(3);
            updateProgress();
        });
    }

    private void updateProgress() {
        int total = adapter != null ? adapter.getItemCount() : cards.size();
        int index = viewPager.getCurrentItem() + 1;
        if (total == 0) index = 0;
        tvProgress.setText(String.format("%d/%d cards", index, total));
    }

    private void onRateCurrent(String rating) {
        int pos = viewPager.getCurrentItem();
        if (adapter == null) return;
        Flashcard card = adapter.getCardAt(pos);
        if (card == null) return;

        // Update review in Firestore
        repository.updateReview(card.getId(), rating, new FirebaseFlashcardRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(FlashcardActivity.this, "Saved: " + rating, Toast.LENGTH_SHORT).show();
                    // remove card and advance
                    adapter.removeCardAt(pos);
                    if (adapter.getItemCount() == 0) {
                        Toast.makeText(FlashcardActivity.this, "You're done for now!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        int next = Math.min(pos, adapter.getItemCount() - 1);
                        viewPager.setCurrentItem(next, true);
                        updateProgress();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(FlashcardActivity.this, "Failed to save review: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewPager != null) {
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateProgress();
                }
            });
        }
    }
}
