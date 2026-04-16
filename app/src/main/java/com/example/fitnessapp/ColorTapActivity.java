package com.example.fitnessapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.List;

public class ColorTapActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    private LinearLayout containerCircles;
    private TextView tvScore, tvLevel, tvResult, tvInstruction;
    private int score = 0;
    private int level = 1;
    private int difficulty = DIFFICULTY_EASY;

    // Store sequence as INDICES (0,1,2,3) not color values
    private List<Integer> colorSequence = new ArrayList<>();
    private List<Integer> userSequence = new ArrayList<>();
    private List<CardView> colorCards = new ArrayList<>();
    private boolean isShowingSequence = false;

    // Colors for the game (index 0=blue, 1=green, 2=orange, 3=purple)
    private final int[] colors = {
            0xFF2563EB, // blue
            0xFF059669, // green
            0xFFEA580C, // orange
            0xFF7C3AED  // purple
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_tap);

        containerCircles = findViewById(R.id.container_circles);
        tvScore = findViewById(R.id.tv_score);
        tvLevel = findViewById(R.id.tv_level);
        tvResult = findViewById(R.id.tv_result);
        tvInstruction = findViewById(R.id.tv_instruction);

        // Get difficulty from intent
        difficulty = getIntent().getIntExtra(EXTRA_DIFFICULTY, DIFFICULTY_EASY);

        // Set title based on difficulty
        String[] titles = {"Łatwe - Kolory", "Średnie - Kolory", "Trudne - Kolory"};
        ((TextView) findViewById(R.id.tv_title)).setText(titles[difficulty]);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_restart).setOnClickListener(v -> restartGame());

        startLevel();
    }

    private void startLevel() {
        userSequence.clear();
        isShowingSequence = true; // Block input during sequence showing
        tvResult.setVisibility(View.INVISIBLE);
        tvInstruction.setText("Patrz uważnie...");

        // Generate sequence based on difficulty
        int minLength, maxLength, delay;
        switch (difficulty) {
            case DIFFICULTY_EASY:
                minLength = 2;
                maxLength = 3;
                delay = 1200;
                break;
            case DIFFICULTY_MEDIUM:
                minLength = 3;
                maxLength = 4;
                delay = 900;
                break;
            case DIFFICULTY_HARD:
            default:
                minLength = 4;
                maxLength = 6;
                delay = 600;
                break;
        }

        // Level progression: increase sequence length
        int sequenceLength = Math.min(minLength + level - 1, maxLength);

        // Build the sequence immediately so it's ready before input is allowed
        colorSequence.clear();
        for (int i = 0; i < sequenceLength; i++) {
            int randomIndex = (int) (Math.random() * colors.length);
            colorSequence.add(randomIndex);
        }

        setupColorCircles();
        showSequence(delay);
    }

    private void setupColorCircles() {
        containerCircles.removeAllViews();
        colorCards.clear();

        int circleSize = 140;
        int margin = 20;

        // Create 2x2 grid of colored circles
        for (int row = 0; row < 2; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            rowLayout.setGravity(android.view.Gravity.CENTER);

            for (int col = 0; col < 2; col++) {
                final int colorIndex = row * 2 + col;

                CardView cardView = new CardView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(circleSize, circleSize);
                params.setMargins(margin, margin, margin, margin);
                cardView.setLayoutParams(params);
                cardView.setRadius(70f);
                cardView.setCardElevation(6f);
                cardView.setCardBackgroundColor(colors[colorIndex]);
                cardView.setClickable(true);
                cardView.setFocusable(true);

                // Click listener passes the COLOR INDEX
                final int index = colorIndex;
                cardView.setOnClickListener(v -> {
                    if (!isShowingSequence) {
                        onColorTapped(index);
                    }
                });

                colorCards.add(cardView);
                rowLayout.addView(cardView);
            }
            containerCircles.addView(rowLayout);
        }
    }

    private void showSequence(int delayMs) {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int currentIndex = 0;

            @Override
            public void run() {
                if (currentIndex < colorSequence.size()) {
                    int colorIndexToShow = colorSequence.get(currentIndex);
                    flashColor(colorIndexToShow);
                    currentIndex++;
                    handler.postDelayed(this, delayMs);
                } else {
                    // Sequence done showing - allow input
                    isShowingSequence = false;
                    tvInstruction.setText("Powtórz sekwencję!");
                }
            }
        };
        handler.postDelayed(runnable, 500);
    }

    private void flashColor(int colorIndex) {
        if (colorIndex < 0 || colorIndex >= colorCards.size()) return;

        CardView card = colorCards.get(colorIndex);
        int originalColor = colors[colorIndex];
        card.setCardBackgroundColor(0xFFFFFFFF);

        new Handler().postDelayed(() -> card.setCardBackgroundColor(originalColor), 400);
    }

    private void onColorTapped(int colorIndex) {
        userSequence.add(colorIndex);

        // Flash the tapped color
        CardView card = colorCards.get(colorIndex);
        int originalColor = colors[colorIndex];
        card.setCardBackgroundColor(0xFFFFFFFF);
        new Handler().postDelayed(() -> card.setCardBackgroundColor(originalColor), 200);

        int checkIndex = userSequence.size() - 1;

        // Check if correct
        if (userSequence.get(checkIndex) != colorSequence.get(checkIndex)) {
            // Wrong!
            isShowingSequence = true; // Block further input
            tvResult.setText("Błąd! Spróbuj ponownie.");
            tvResult.setTextColor(getColor(R.color.red_icon));
            tvResult.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> startLevel(), 1500);
            return;
        }

        // Check if sequence complete
        if (userSequence.size() == colorSequence.size()) {
            isShowingSequence = true; // Block further input
            score += colorSequence.size() * 10;
            level++;
            tvScore.setText("Wynik: " + score);
            tvLevel.setText("Poziom: " + level);
            tvResult.setText("Dobrze!");
            tvResult.setTextColor(getColor(R.color.green_icon));
            tvResult.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> startLevel(), 1500);
        }
    }

    private void restartGame() {
        score = 0;
        level = 1;
        tvScore.setText("Wynik: 0");
        tvLevel.setText("Poziom: 1");
        startLevel();
    }
}
