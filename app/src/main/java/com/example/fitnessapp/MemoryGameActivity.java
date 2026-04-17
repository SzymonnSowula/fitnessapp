package com.example.fitnessapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends AppCompatActivity {

    private RecyclerView rvCards;
    private TextView tvScore, tvMoves, tvTitle;
    private int score = 0;
    private int moves = 0;
    private CardAdapter adapter;
    private boolean isLocked = false;

    private final int[] cardImages = {
            R.drawable.ic_heart,
            R.drawable.ic_brain,
            R.drawable.ic_home,
            R.drawable.ic_profile,
            R.drawable.ic_settings,
            R.drawable.ic_onboarding_1,
            R.drawable.ic_onboarding_2,
            R.drawable.ic_onboarding_3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        rvCards = findViewById(R.id.rv_cards);
        tvScore = findViewById(R.id.tv_score);
        tvMoves = findViewById(R.id.tv_moves);
        tvTitle = findViewById(R.id.tv_title);

        // Get grid size from intent
        int columns = getIntent().getIntExtra("EXTRA_COLUMNS", 4);
        int rows = getIntent().getIntExtra("EXTRA_ROWS", 4);
        int totalCards = columns * rows;
        int pairs = totalCards / 2;

        // Update title
        tvTitle.setText("Memory " + columns + "x" + rows);

        tvScore.setText("0");
        tvMoves.setText("0");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_restart).setOnClickListener(v -> setupGame(columns, rows));

        tvScore = findViewById(R.id.tv_score);
        tvMoves = findViewById(R.id.tv_moves);
        tvTitle = findViewById(R.id.tv_title);

        setupGame(columns, rows);
    }

    private void setupGame(int columns, int rows) {
        score = 0;
        moves = 0;
        if (tvScore != null) tvScore.setText("0");
        if (tvMoves != null) tvMoves.setText("0");
        isLocked = false;

        int totalCards = columns * rows;
        int pairs = totalCards / 2;

        // Select enough images for pairs
        List<Integer> selectedImages = new ArrayList<>();
        for (int i = 0; i < pairs && i < cardImages.length; i++) {
            selectedImages.add(cardImages[i]);
            selectedImages.add(cardImages[i]);
        }

        // If we need more pairs than available images, duplicate with different approach
        while (selectedImages.size() < totalCards) {
            // Add variations by adding same images again they'll be treated as different pairs
            int extraIndex = (selectedImages.size() / 2) % cardImages.length;
            selectedImages.add(cardImages[extraIndex]);
            selectedImages.add(cardImages[extraIndex]);
        }

        Collections.shuffle(selectedImages);

        // If we have too many, trim
        while (selectedImages.size() > totalCards) {
            selectedImages.remove(selectedImages.size() - 1);
        }

        final int finalColumns = columns;
        adapter = new CardAdapter(selectedImages, rows, new CardAdapter.OnCardClickListener() {
            @Override
            public void onCardClick(int position) {
                if (!isLocked && !adapter.isCardFlipped(position) && !adapter.isCardRemoved(position)) {
                    adapter.flipCard(position);
                    checkMatch();
                }
            }
        });

        // Calculate height to fit all rows on screen without scrolling
        rvCards.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int containerHeight = v.getHeight() - v.getPaddingTop() - v.getPaddingBottom();
                if (containerHeight > 0) {
                    int newItemHeight = containerHeight / rows;
                    if (adapter.getItemHeight() != newItemHeight) {
                        adapter.updateItemHeight(containerHeight);
                    }
                }
            }
        });

        rvCards.setLayoutManager(new GridLayoutManager(this, columns));
        rvCards.setAdapter(adapter);
    }

    private void checkMatch() {
        List<Integer> flipped = adapter.getFlippedPositions();
        if (flipped.size() == 2) {
            isLocked = true;
            moves++;
            if (tvMoves != null) tvMoves.setText(String.valueOf(moves));

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

    private static class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
        private final List<Integer> cards;
        private final int rows;
        private final List<Integer> flippedPositions = new ArrayList<>();
        private final List<Integer> removedPositions = new ArrayList<>();
        private final OnCardClickListener listener;
        private int itemHeight = 0;

        interface OnCardClickListener {
            void onCardClick(int position);
        }

        CardAdapter(List<Integer> cards, int rows, OnCardClickListener listener) {
            this.cards = cards;
            this.rows = rows;
            this.listener = listener;
        }

        public int getItemHeight() {
            return itemHeight;
        }

        void updateItemHeight(int containerHeight) {
            this.itemHeight = containerHeight / rows;
            notifyDataSetChanged();
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
            MaterialCardView cardView = new MaterialCardView(parent.getContext());
            
            float density = parent.getContext().getResources().getDisplayMetrics().density;
            int marginPx = (int) (6 * density); // Slightly smaller margin to save space
            
            int displayHeight = itemHeight > 0 ? (itemHeight - (marginPx * 2)) : ViewGroup.LayoutParams.WRAP_CONTENT;
            
            GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(
                    GridLayoutManager.LayoutParams.MATCH_PARENT,
                    displayHeight
            );
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
            cardView.setLayoutParams(params);
            cardView.setRadius(32f * density); // Adaptive radius
            cardView.setCardElevation(2f * density);
            cardView.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
            return new CardViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            if (itemHeight > 0) {
                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;
                int marginPx = (int) (4 * density); // Consistent margin
                int calculatedHeight = itemHeight - (marginPx * 2);
                if (params.height != calculatedHeight) {
                    params.height = calculatedHeight;
                    holder.itemView.setLayoutParams(params);
                }
            }
            
            boolean isFlipped = isCardFlipped(position);
            boolean isRemoved = isCardRemoved(position);
            holder.bind(cards.get(position), isFlipped, isRemoved, () -> listener.onCardClick(position));
        }

        @Override
        public int getItemCount() {
            return cards.size();
        }

        static class CardViewHolder extends RecyclerView.ViewHolder {
            private final MaterialCardView cardView;
            private final ImageView ivCard;

            CardViewHolder(MaterialCardView itemView) {
                super(itemView);
                this.cardView = itemView;
                ivCard = new ImageView(itemView.getContext());
                ivCard.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT));
                ivCard.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                itemView.addView(ivCard);
            }

            void bind(int imageRes, boolean isFlipped, boolean isRemoved, Runnable onClick) {
                // Adjust padding based on card size to ensure icons are visible but not too large
                int height = cardView.getLayoutParams().height;
                int p = height / 5; // Reduced padding to ensure icon visibility on flatter cards
                if (p < 12) p = 12;
                if (p > 48) p = 48;
                ivCard.setPadding(p, p, p, p);

                if (isRemoved) {
                    ivCard.setVisibility(View.INVISIBLE);
                    cardView.setVisibility(View.INVISIBLE);
                } else if (isFlipped) {
                    ivCard.setVisibility(View.VISIBLE);
                    ivCard.setImageResource(imageRes);
                    ivCard.setColorFilter(0xFFFFFFFF); // White icon on dark blue background
                    cardView.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFF004A99)); // Dark blue for active/flipped
                    cardView.setStrokeWidth(0);
                } else {
                    ivCard.setVisibility(View.VISIBLE);
                    // Use a question mark icon if available, otherwise fallback
                    ivCard.setImageResource(R.drawable.ic_onboarding_3);
                    ivCard.setColorFilter(0xFF004A99); // Dark blue icon on white background
                    cardView.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
                    // Add stroke/border
                    cardView.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFF004A99));
                    cardView.setStrokeWidth(6);
                }
                cardView.setOnClickListener(v -> onClick.run());
            }
        }
    }
}
