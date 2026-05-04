package com.example.fitnessapp;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import com.example.fitnessapp.utils.AppExecutors;
import com.example.fitnessapp.utils.ScreenUtils;
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
    private VoiceNavigator voiceNavigator;
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    private final int[] cardImages = {
            R.drawable.ic_heart,
            R.drawable.ic_home,
            R.drawable.ic_dumbbell,
            R.drawable.ic_brain,
            R.drawable.ic_sitting,
            R.drawable.ic_standing,
            R.drawable.ic_chair,
            R.drawable.ic_bed
    };

    private int currentColumns, currentRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });

        voiceNavigator.setup();
        voiceNavigator.startListening();

        rvCards = findViewById(R.id.rv_cards);
        tvScore = findViewById(R.id.tv_score);
        tvMoves = findViewById(R.id.tv_moves);
        tvTitle = findViewById(R.id.tv_title);

        currentColumns = getIntent().getIntExtra("EXTRA_COLUMNS", 4);
        currentRows = getIntent().getIntExtra("EXTRA_ROWS", 4);

        setupGame(currentColumns, currentRows);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_restart).setOnClickListener(v -> setupGame(currentColumns, currentRows));

        voiceNavigator.speakDelayed("Gra Memory. Znajdź pary jednakowych kart.", 500);
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "new_game":
            case "restart":
            case "reset":
                setupGame(currentColumns, currentRows);
                voiceNavigator.speak("Nowa gra.");
                break;
            case "back":
                onBackPressed();
                break;
            case "exit":
                finish();
                break;
            case "stop":
                voiceNavigator.stopSpeaking();
                break;
            case "help":
                voiceNavigator.speak(VoiceCommands.getGameHelpText());
                break;
            case "read":
            case "repeat":
                voiceNavigator.speak("Gra Memory. Znajdź wszystkie pary kart. Wynik: " + score + ". Ruchy: " + moves);
                break;
        }
    }

    private void setupGame(int columns, int rows) {
        currentColumns = columns;
        currentRows = rows;

        int totalCards = columns * rows;
        if (totalCards % 2 != 0) totalCards--;
        int pairs = totalCards / 2;

        score = 0;
        moves = 0;
        if (tvScore != null) tvScore.setText("0");
        if (tvMoves != null) tvMoves.setText("0");
        if (tvTitle != null) tvTitle.setText("Memory " + columns + "x" + rows);
        isLocked = false;

        List<Integer> selectedImages = new ArrayList<>();
        for (int i = 0; i < pairs; i++) {
            int img = cardImages[i % cardImages.length];
            selectedImages.add(img);
            selectedImages.add(img);
        }

        Collections.shuffle(selectedImages);

        adapter = new CardAdapter(selectedImages, rows, new CardAdapter.OnCardClickListener() {
            @Override
            public void onCardClick(int position) {
                if (!isLocked && !adapter.isCardFlipped(position) && !adapter.isCardRemoved(position)) {
                    adapter.flipCard(position);
                    checkMatch();
                }
            }
        });

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
                if (tvScore != null) tvScore.setText(String.valueOf(score));
                adapter.removeCards(pos1, pos2);

                if (adapter.isAllRemoved()) {
                    showWinDialog();
                }
                isLocked = false;
            } else {
                mainHandler.postDelayed(() -> {
                    adapter.unflipCards(pos1, pos2);
                    isLocked = false;
                }, 800);
            }
        }
    }

    private void showWinDialog() {
        // Save game session to history
        AppExecutors.getInstance().diskIO().execute(() -> {
            GameSession session = new GameSession();
            session.gameType = "memory";
            session.score = score;
            session.level = currentRows * currentColumns;
            session.completedAt = System.currentTimeMillis();
            AppDatabase.getDatabase(MemoryGameActivity.this).gameSessionDao().insert(session);
        });

        voiceNavigator.speak("Brawo! Ukończyłeś grę w " + moves + " ruchach.");

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_win, null);
        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvMsg = dialogView.findViewById(R.id.tv_win_message);
        com.google.android.material.button.MaterialButton btnAction = dialogView.findViewById(R.id.btn_win_action);
        com.google.android.material.button.MaterialButton btnRestart = dialogView.findViewById(R.id.btn_win_restart);
        com.google.android.material.button.MaterialButton btnExit = dialogView.findViewById(R.id.btn_win_exit);

        boolean canIncrease = false;
        int nextCols = currentColumns;
        int nextRows = currentRows;

        // Memory progression: 2x3 → 3x4 → 4x4 → 4x5
        if (currentColumns == 2 && currentRows == 3) {
            nextCols = 3; nextRows = 4; canIncrease = true;
        } else if (currentColumns == 3 && currentRows == 4) {
            nextCols = 4; nextRows = 4; canIncrease = true;
        } else if (currentColumns == 4 && currentRows == 4) {
            nextCols = 4; nextRows = 5; canIncrease = true;
        }

        if (canIncrease) {
            tvMsg.setText("BRAWO!!!!\nCzy chcesz zwiększyć poziom trudności?");
            btnAction.setText("Zwiększ poziom");
            btnRestart.setVisibility(View.VISIBLE);
            int finalNextCols = nextCols;
            int finalNextRows = nextRows;
            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
                setupGame(finalNextCols, finalNextRows);
            });
        } else {
            tvMsg.setText("BRAWO!!!!\nUkończyłeś najtrudniejszy poziom!");
            btnAction.setText("Zagraj jeszcze raz");
            btnRestart.setVisibility(View.GONE);
            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
                setupGame(currentColumns, currentRows);
            });
        }

        btnRestart.setOnClickListener(v -> {
            dialog.dismiss();
            setupGame(currentColumns, currentRows);
        });

        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceNavigator != null) {
            voiceNavigator.cleanup();
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

        public CardAdapter(List<Integer> cards, int rows, OnCardClickListener listener) {
            this.cards = cards;
            this.rows = rows;
            this.listener = listener;
        }

        public int getItemHeight() {
            return itemHeight;
        }

        void updateItemHeight(int containerHeight) {
            this.itemHeight = containerHeight / rows;
            notifyItemRangeChanged(0, getItemCount());
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

        boolean isAllRemoved() {
            return removedPositions.size() == cards.size();
        }

        @Override
        public CardViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            MaterialCardView cardView = new MaterialCardView(parent.getContext());
            int marginPx = ScreenUtils.dpToPx(parent.getContext(), 6);
            int displayHeight = itemHeight > 0 ? (itemHeight - (marginPx * 2)) : ViewGroup.LayoutParams.WRAP_CONTENT;
            GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(
                    GridLayoutManager.LayoutParams.MATCH_PARENT,
                    displayHeight
            );
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
            cardView.setLayoutParams(params);
            cardView.setRadius(ScreenUtils.dpToPx(parent.getContext(), 32));
            cardView.setCardElevation(ScreenUtils.dpToPx(parent.getContext(), 2));
            cardView.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
            return new CardViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            if (itemHeight > 0) {
                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                int marginPx = ScreenUtils.dpToPx(holder.itemView.getContext(), 4);
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
                int height = cardView.getLayoutParams().height;
                int p = height / 5;
                if (p < 12) p = 12;
                if (p > 48) p = 48;
                ivCard.setPadding(p, p, p, p);

                if (isRemoved) {
                    ivCard.setVisibility(View.INVISIBLE);
                    cardView.setVisibility(View.INVISIBLE);
                } else {
                    cardView.setVisibility(View.VISIBLE);
                    ivCard.setVisibility(View.VISIBLE);
                    if (isFlipped) {
                        ivCard.setImageResource(imageRes);
                        ivCard.setColorFilter(0xFFFFFFFF);
                        cardView.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFF004A99));
                        cardView.setStrokeWidth(0);
                    } else {
                        ivCard.setImageResource(R.drawable.ic_help);
                        ivCard.setColorFilter(0xFF004A99);
                        cardView.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
                        cardView.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFF004A99));
                        cardView.setStrokeWidth(6);
                    }
                }
                cardView.setOnClickListener(v -> onClick.run());
            }
        }
    }
}
