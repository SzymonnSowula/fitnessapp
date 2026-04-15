package com.example.fitnessapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends AppCompatActivity {

    private RecyclerView rvCards;
    private TextView tvScore, tvMoves;
    private int score = 0;
    private int moves = 0;
    private CardAdapter adapter;
    private boolean isLocked = false;

    private final int[] cardImages = {
            R.drawable.ic_mood_happy,
            R.drawable.ic_mood_sad,
            R.drawable.ic_heart,
            R.drawable.ic_mood_very_sad,
            R.drawable.ic_profile,
            R.drawable.ic_settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        rvCards = findViewById(R.id.rv_cards);
        tvScore = findViewById(R.id.tv_score);
        tvMoves = findViewById(R.id.tv_moves);
        tvScore.setText("0");
        tvMoves.setText("Ruchy: 0");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_restart).setOnClickListener(v -> restartGame());

        setupGame();
    }

    private void setupGame() {
        score = 0;
        moves = 0;
        tvScore.setText("0");
        tvMoves.setText("Ruchy: 0");
        isLocked = false;

        List<Integer> cards = new ArrayList<>();
        for (int img : cardImages) {
            cards.add(img);
            cards.add(img);
        }
        Collections.shuffle(cards);

        adapter = new CardAdapter(cards, new CardAdapter.OnCardClickListener() {
            @Override
            public void onCardClick(int position) {
                if (!isLocked && !adapter.isCardFlipped(position) && !adapter.isCardRemoved(position)) {
                    adapter.flipCard(position);
                    checkMatch();
                }
            }
        });

        rvCards.setLayoutManager(new GridLayoutManager(this, 3));
        rvCards.setAdapter(adapter);
    }

    private void checkMatch() {
        List<Integer> flipped = adapter.getFlippedPositions();
        if (flipped.size() == 2) {
            isLocked = true;
            moves++;
            tvMoves.setText("Ruchy: " + moves);

            int pos1 = flipped.get(0);
            int pos2 = flipped.get(1);

            if (adapter.getCardImage(pos1) == adapter.getCardImage(pos2)) {
                score += 10;
                tvScore.setText(String.valueOf(score));
                adapter.removeCards(pos1, pos2);

                if (adapter.getItemCount() == 0) {
                    Toast.makeText(this, "Gratulacje! Wynik: " + score, Toast.LENGTH_LONG).show();
                }
                isLocked = false;
            } else {
                new Handler().postDelayed(() -> {
                    adapter.unflipCards(pos1, pos2);
                    isLocked = false;
                }, 800);
            }
        }
    }

    private void restartGame() {
        setupGame();
    }

    private static class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
        private final List<Integer> cards;
        private final List<Integer> flippedPositions = new ArrayList<>();
        private final List<Integer> removedPositions = new ArrayList<>();
        private final OnCardClickListener listener;

        interface OnCardClickListener {
            void onCardClick(int position);
        }

        CardAdapter(List<Integer> cards, OnCardClickListener listener) {
            this.cards = cards;
            this.listener = listener;
        }

        boolean isCardFlipped(int position) {
            return flippedPositions.contains(position);
        }

        boolean isCardRemoved(int position) {
            return removedPositions.contains(position);
        }

        void flipCard(int position) {
            if (!flippedPositions.contains(position) && !removedPositions.contains(position)) {
                flippedPositions.add(position);
                notifyItemChanged(position);
            }
        }

        void unflipCards(int pos1, int pos2) {
            flippedPositions.remove(Integer.valueOf(pos1));
            flippedPositions.remove(Integer.valueOf(pos2));
            notifyItemChanged(pos1);
            notifyItemChanged(pos2);
        }

        void removeCards(int pos1, int pos2) {
            removedPositions.add(pos1);
            removedPositions.add(pos2);
            flippedPositions.remove(Integer.valueOf(pos1));
            flippedPositions.remove(Integer.valueOf(pos2));
            notifyItemChanged(pos1);
            notifyItemChanged(pos2);
        }

        List<Integer> getFlippedPositions() {
            return new ArrayList<>(flippedPositions);
        }

        int getCardImage(int position) {
            return cards.get(position);
        }

        @Override
        public CardViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            CardView cardView = new CardView(parent.getContext());
            int margin = 10;
            GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(
                    GridLayoutManager.LayoutParams.MATCH_PARENT,
                    320
            );
            params.setMargins(margin, margin, margin, margin);
            cardView.setLayoutParams(params);
            cardView.setRadius(16f);
            cardView.setCardElevation(6f);
            cardView.setCardBackgroundColor(0xFFE0E0E0);
            return new CardViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            boolean isFlipped = isCardFlipped(position) || isCardRemoved(position);
            holder.bind(cards.get(position), isFlipped, () -> listener.onCardClick(position));
        }

        @Override
        public int getItemCount() {
            return cards.size();
        }

        static class CardViewHolder extends RecyclerView.ViewHolder {
            private final CardView cardView;
            private final ImageView ivCard;

            CardViewHolder(CardView itemView) {
                super(itemView);
                this.cardView = itemView;
                ivCard = new ImageView(itemView.getContext());
                ivCard.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT));
                ivCard.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ivCard.setPadding(32, 32, 32, 32);
                itemView.addView(ivCard);
            }

            void bind(int imageRes, boolean isFlipped, Runnable onClick) {
                if (isFlipped) {
                    ivCard.setImageResource(imageRes);
                    cardView.setCardBackgroundColor(0xFFF0F4FF);
                } else {
                    ivCard.setImageResource(android.R.drawable.ic_menu_help);
                    cardView.setCardBackgroundColor(0xFFE0E0E0);
                }
                cardView.setOnClickListener(v -> onClick.run());
            }
        }
    }
}