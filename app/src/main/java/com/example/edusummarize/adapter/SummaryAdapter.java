package com.example.edusummarize.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edusummarize.R;
import com.example.edusummarize.model.Flashcard;
import com.example.edusummarize.model.Summary;
import com.example.edusummarize.repository.FirebaseFlashcardRepository;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.ViewHolder> {

    private Context context;
    private List<Summary> summaryList;
    private ExoPlayer exoPlayer;
    private int currentPlayingPosition = -1;
    private FirebaseFlashcardRepository flashcardRepository;
    // MEMORY LEAK FIX: Add lifecycle owner to properly manage ExoPlayer
    private androidx.lifecycle.LifecycleOwner lifecycleOwner;

    public SummaryAdapter(Context context, List<Summary> summaryList) {
        this.context = context;
        this.summaryList = summaryList;
        flashcardRepository = new FirebaseFlashcardRepository();

        // Try to get lifecycle owner for proper cleanup
        if (context instanceof androidx.lifecycle.LifecycleOwner) {
            this.lifecycleOwner = (androidx.lifecycle.LifecycleOwner) context;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Summary summary = summaryList.get(position);

        holder.tvTitle.setText(summary.getTitle());

        if (summary.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String date = sdf.format(summary.getCreatedAt().toDate());
            holder.tvDate.setText(date);
        }

        // Truncate summary text for preview
        String summaryPreview = summary.getSummaryText();
        if (summaryPreview.length() > 100) {
            summaryPreview = summaryPreview.substring(0, 100) + "...";
        }
        holder.tvSummary.setText(summaryPreview);

        // Update play button based on playing state
        if (position == currentPlayingPosition && exoPlayer != null && exoPlayer.isPlaying()) {
            holder.btnPlay.setImageResource(R.drawable.ic_pause);
        } else {
            holder.btnPlay.setImageResource(R.drawable.ic_play);
        }

        holder.btnPlay.setOnClickListener(v -> {
            if (position == currentPlayingPosition && exoPlayer != null && exoPlayer.isPlaying()) {
                pauseAudio();
                holder.btnPlay.setImageResource(R.drawable.ic_play);
            } else {
                playAudio(summary.getAudioUrl(), position);
                notifyDataSetChanged();
            }
        });

        holder.itemView.setOnClickListener(v -> showSummaryDetail(summary));

        holder.btnFlashcards.setOnClickListener(v -> {
            // Prevent double-click
            holder.btnFlashcards.setEnabled(false);

            // Check if flashcards already exist for this summary
            Toast.makeText(context, "Đang tải flashcards...", Toast.LENGTH_SHORT).show();

            flashcardRepository.getFlashcardsBySummaryId(summary.getId(), new FirebaseFlashcardRepository.RepositoryCallback<List<Flashcard>>() {
                @Override
                public void onSuccess(List<Flashcard> existingCards) {
                    holder.btnFlashcards.setEnabled(true);

                    if (existingCards != null && !existingCards.isEmpty()) {
                        // Flashcards already exist - load them
                        Toast.makeText(context, "Tìm thấy " + existingCards.size() + " flashcards", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, com.example.edusummarize.ui.FlashcardActivity.class);
                        intent.putParcelableArrayListExtra("cards", new ArrayList<>(existingCards));
                        context.startActivity(intent);
                    } else {
                        // No flashcards exist - generate new ones
                        Toast.makeText(context, "Đang tạo flashcards mới...", Toast.LENGTH_SHORT).show();

                        String textToUse = summary.getSummaryText();
                        if (textToUse == null || textToUse.trim().isEmpty()) {
                            textToUse = summary.getOriginalText();
                        }

                        if (textToUse == null || textToUse.trim().isEmpty()) {
                            Toast.makeText(context, "Lỗi: Không có nội dung để tạo flashcard", Toast.LENGTH_LONG).show();
                            holder.btnFlashcards.setEnabled(true);
                            return;
                        }

                        final String finalText = textToUse;
                        flashcardRepository.generateFlashcards(summary.getId(), finalText, new FirebaseFlashcardRepository.RepositoryCallback<List<Flashcard>>() {
                            @Override
                            public void onSuccess(List<Flashcard> result) {
                                holder.btnFlashcards.setEnabled(true);
                                Toast.makeText(context, "Đã tạo " + result.size() + " flashcards!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, com.example.edusummarize.ui.FlashcardActivity.class);
                                intent.putParcelableArrayListExtra("cards", new ArrayList<>(result));
                                context.startActivity(intent);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                holder.btnFlashcards.setEnabled(true);
                                Toast.makeText(context, "Lỗi tạo flashcards: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    holder.btnFlashcards.setEnabled(true);
                    // If error checking, try to generate new ones anyway
                    Toast.makeText(context, "Đang tạo flashcards mới...", Toast.LENGTH_SHORT).show();

                    String textToUse = summary.getSummaryText();
                    if (textToUse == null || textToUse.trim().isEmpty()) {
                        textToUse = summary.getOriginalText();
                    }

                    if (textToUse == null || textToUse.trim().isEmpty()) {
                        Toast.makeText(context, "Lỗi: Không có nội dung để tạo flashcard", Toast.LENGTH_LONG).show();
                        return;
                    }

                    final String finalText = textToUse;
                    flashcardRepository.generateFlashcards(summary.getId(), finalText, new FirebaseFlashcardRepository.RepositoryCallback<List<Flashcard>>() {
                        @Override
                        public void onSuccess(List<Flashcard> result) {
                            Toast.makeText(context, "Đã tạo " + result.size() + " flashcards!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, com.example.edusummarize.ui.FlashcardActivity.class);
                            intent.putParcelableArrayListExtra("cards", new ArrayList<>(result));
                            context.startActivity(intent);
                        }

                        @Override
                        public void onFailure(Exception e2) {
                            Toast.makeText(context, "Lỗi: " + e2.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        });

        holder.btnQuiz.setOnClickListener(v -> {
            // Open QuizActivity
            Intent intent = new Intent(context, com.example.edusummarize.ui.QuizActivity.class);
            intent.putExtra("summaryId", summary.getId());
            intent.putExtra("summaryText", summary.getSummaryText());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return summaryList.size();
    }

    private void playAudio(String audioUrl, int position) {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
        }

        exoPlayer = new ExoPlayer.Builder(context).build();
        MediaItem mediaItem = MediaItem.fromUri(audioUrl);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();

        currentPlayingPosition = position;

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    currentPlayingPosition = -1;
                    notifyDataSetChanged();
                }
            }
        });
    }

    private void pauseAudio() {
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    public void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private void showSummaryDetail(Summary summary) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_summary_detail, null);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvDate = dialogView.findViewById(R.id.tv_dialog_date);
        TextView tvOriginal = dialogView.findViewById(R.id.tv_dialog_original);
        TextView tvSummary = dialogView.findViewById(R.id.tv_dialog_summary);

        tvTitle.setText(summary.getTitle());

        if (summary.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvDate.setText(sdf.format(summary.getCreatedAt().toDate()));
        }

        tvOriginal.setText(summary.getOriginalText());
        tvSummary.setText(summary.getSummaryText());

        new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .setPositiveButton("Đóng", null)
                .show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvSummary;
        ImageButton btnPlay, btnFlashcards, btnQuiz;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            btnPlay = itemView.findViewById(R.id.btn_play);
            btnFlashcards = itemView.findViewById(R.id.btn_flashcards);
            btnQuiz = itemView.findViewById(R.id.btn_quiz);
        }
    }
}