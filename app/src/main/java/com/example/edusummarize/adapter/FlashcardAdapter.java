package com.example.edusummarize.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edusummarize.R;
import com.example.edusummarize.model.Flashcard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.ViewHolder> {
    private List<Flashcard> cards;

    // track which side is showing for each position
    private Map<Integer, Boolean> isFrontMap = new HashMap<>();

    public FlashcardAdapter(List<Flashcard> cards) {
        this.cards = cards;
        for (int i = 0; i < cards.size(); i++) isFrontMap.put(i, true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flashcard card = cards.get(position);
        boolean isFront = isFrontMap.getOrDefault(position, true);
        holder.tvCardText.setText(isFront ? card.getFront() : card.getBack());

        holder.itemView.setOnClickListener(v -> {
            flipCard(holder, position, card);
        });
    }

    private void flipCard(ViewHolder holder, int position, Flashcard card) {
        // simple 3D-like flip using rotationY
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(holder.itemView, "rotationY", 0f, 90f);
        flipOut.setDuration(180);
        ObjectAnimator flipIn = ObjectAnimator.ofFloat(holder.itemView, "rotationY", -90f, 0f);
        flipIn.setDuration(180);

        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // toggle content
                boolean currentlyFront = isFrontMap.getOrDefault(position, true);
                boolean nowFront = !currentlyFront;
                isFrontMap.put(position, nowFront);
                holder.tvCardText.setText(nowFront ? card.getFront() : card.getBack());
                flipIn.start();
            }
        });
        flipOut.start();
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public Flashcard getCardAt(int position) {
        if (position < 0 || position >= cards.size()) return null;
        return cards.get(position);
    }

    public void removeCardAt(int position) {
        if (position < 0 || position >= cards.size()) return;
        cards.remove(position);
        // rebuild isFrontMap
        isFrontMap.clear();
        for (int i = 0; i < cards.size(); i++) isFrontMap.put(i, true);
        notifyDataSetChanged();
    }

    public int getCardsCount() {
        return cards.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardText, tvTap;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardText = itemView.findViewById(R.id.tv_card_text);
            tvTap = itemView.findViewById(R.id.tv_tap);
            // increase camera distance for nicer 3D effect
            float scale = itemView.getResources().getDisplayMetrics().density;
            itemView.setCameraDistance(8000 * scale);
        }
    }
}
