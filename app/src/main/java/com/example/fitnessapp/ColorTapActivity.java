package com.example.fitnessapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ColorTapActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    private View containerGrid;
    private TextView tvScore, tvLevel, tvResult, tvInstruction, tvTitle;
    private LinearLayout indicatorContainer;
    private int score = 0;
    private int level = 1;
    private int difficulty = DIFFICULTY_EASY;

    private List<Integer> colorSequence = new ArrayList<>();
    private List<Integer> userSequence = new ArrayList<>();
    private List<CardView> colorCards = new ArrayList<>();
    private boolean isShowingSequence = false;
    private VoiceNavigator voiceNavigator;

    private final int[] colors = {
            0xFF004A99, // blue
            0xFF057A32, // green
            0xFF994A00, // orange
            0xFF6355B2  // purple
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_tap);

        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });
        voiceNavigator.setup();

        containerGrid = findViewById(R.id.container_grid);
        tvScore = findViewById(R.id.tv_score);
        tvLevel = findViewById(R.id.tv_level);
        tvInstruction = findViewById(R.id.tv_instruction);
        tvTitle = findViewById(R.id.tv_title);
        indicatorContainer = findViewById(R.id.indicator_container);

        difficulty = getIntent().getIntExtra(EXTRA_DIFFICULTY, DIFFICULTY_EASY);

        // Set appropriate starting level based on difficulty
        switch (difficulty) {
            case DIFFICULTY_EASY:
                level = 1;
                break;
            case DIFFICULTY_MEDIUM:
                level = 3;
                break;
            case DIFFICULTY_HARD:
                level = 5;
                break;
        }

        tvScore.setText("Wynik: 0");
        tvLevel.setText("Poziom " + level);

        findViewById(R.id.btn_back_game).setOnClickListener(v -> finish());
        findViewById(R.id.btn_restart).setOnClickListener(v -> restartGame());

        setupTiles();
        startLevel();

        voiceNavigator.speakDelayed("Gra Kolory. Powtarzaj sekwencję kolorów.", 500);
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "new_game":
            case "restart":
            case "reset":
                restartGame();
                voiceNavigator.speak("Nowa gra.");
                break;
            case "stop":
                voiceNavigator.stopSpeaking();
                break;
            case "help":
                voiceNavigator.speak("Obserwuj sekwencję kolorów i ją powtórz.");
                break;
            case "read":
            case "repeat":
                voiceNavigator.speak("Gra Kolory. Poziom " + level + ". Wynik: " + score);
                break;
        }
    }

    private void setupTiles() {
        colorCards.clear();
        colorCards.add(findViewById(R.id.tile_1));
        colorCards.add(findViewById(R.id.tile_2));
        colorCards.add(findViewById(R.id.tile_3));
        colorCards.add(findViewById(R.id.tile_4));

        for (int i = 0; i < colorCards.size(); i++) {
            final int index = i;
            colorCards.get(i).setOnClickListener(v -> {
                if (!isShowingSequence) {
                    onColorTapped(index);
                }
            });
        }
    }

    private void startLevel() {
        userSequence.clear();
        isShowingSequence = true;
        tvTitle.setText("Patrz!");
        tvInstruction.setText("Zapamiętaj sekwencję...");
        updateIndicators();

        int minLength = 2, maxLength = 4, delay = 1000;
        switch (difficulty) {
            case DIFFICULTY_EASY: minLength = 2; maxLength = 4; delay = 1200; break;
            case DIFFICULTY_MEDIUM: minLength = 3; maxLength = 6; delay = 900; break;
            case DIFFICULTY_HARD: minLength = 4; maxLength = 8; delay = 700; break;
        }

        int sequenceLength = Math.min(minLength + level - 1, maxLength);
        colorSequence.clear();
        for (int i = 0; i < sequenceLength; i++) {
            colorSequence.add((int) (Math.random() * colors.length));
        }

        showSequence(delay);
    }

    private void showSequence(int delayMs) {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int currentIndex = 0;
            @Override
            public void run() {
                if (currentIndex < colorSequence.size()) {
                    flashColor(colorSequence.get(currentIndex));
                    currentIndex++;
                    handler.postDelayed(this, delayMs);
                } else {
                    isShowingSequence = false;
                    tvTitle.setText("Dobrze!");
                    tvInstruction.setText("Powtórz sekwencję!");
                    voiceNavigator.speak("Twoja kolej. Powtórz sekwencję.");
                }
            }
        };
        handler.postDelayed(runnable, 800);
    }

private void flashColor(int index) {
        CardView card = colorCards.get(index);
        // First: flash with neon green overlay
        card.setCardBackgroundColor(0xFF39FF14); // Neon green flash
        card.setAlpha(1.0f);
        new Handler().postDelayed(() -> {
            // Then return to original color
            card.setCardBackgroundColor(colors[index]);
            card.setAlpha(1.0f);
        }, 400);
    }

    private void onColorTapped(int index) {
        userSequence.add(index);
        flashColor(index);
        updateIndicators();

        int checkIndex = userSequence.size() - 1;
        if (userSequence.get(checkIndex) != colorSequence.get(checkIndex)) {
            isShowingSequence = true;
            tvTitle.setText("Ups!");
            tvInstruction.setText("Spróbuj jeszcze raz.");
            voiceNavigator.speak("Niestety błąd. Spróbuj jeszcze raz.");
            new Handler().postDelayed(this::startLevel, 1500);
            return;
        }

        if (userSequence.size() == colorSequence.size()) {
            isShowingSequence = true;
            score += colorSequence.size() * 50;
            level++;
            tvScore.setText("Wynik: " + score);
            tvLevel.setText("Poziom " + level);
            tvTitle.setText("Świetnie!");
            voiceNavigator.speak("Świetnie! Przechodzisz do poziomu " + level);
            new Handler().postDelayed(this::startLevel, 1500);
        }
    }

    private void updateIndicators() {
        indicatorContainer.removeAllViews();
        int total = colorSequence.size();
        int current = userSequence.size();
        for (int i = 0; i < total; i++) {
            View view = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int)(32 * getResources().getDisplayMetrics().density),
                    (int)(8 * getResources().getDisplayMetrics().density)
            );
            params.setMargins(8, 0, 8, 0);
            view.setLayoutParams(params);
            if (i < current) {
                view.setBackgroundResource(R.drawable.bg_indicator_active);
                view.getBackground().setTint(0xFF057A32);
            } else {
                view.setBackgroundResource(R.drawable.bg_indicator_inactive);
            }
            indicatorContainer.addView(view);
        }
    }

    private void restartGame() {
        score = 0;
        level = 1;
        tvScore.setText("Wynik: 0");
        tvLevel.setText("Poziom 1");
        startLevel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceNavigator != null) {
            voiceNavigator.cleanup();
        }
    }
}
