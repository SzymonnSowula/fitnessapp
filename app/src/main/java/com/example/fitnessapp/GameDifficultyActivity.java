package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


public class GameDifficultyActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_TYPE = "game_type";

    // Difficulty constants
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    private int gameType = GameInstructionActivity.GAME_MEMORY;
    private int selectedDifficulty = DIFFICULTY_EASY;

    private CardView cardEasy, cardMedium, cardHard;
    private android.view.View containerEasy, containerMedium, containerHard;
    private ImageView ivEasyIcon, ivMediumIcon, ivHardIcon;
    private TextView tvEasyTitle, tvMediumTitle, tvHardTitle;
    private TextView tvEasyDesc, tvMediumDesc, tvHardDesc;
    private ImageView ivCheckEasy, ivCheckMedium, ivCheckHard;
    private VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_difficulty);

        gameType = getIntent().getIntExtra(EXTRA_GAME_TYPE, GameInstructionActivity.GAME_MEMORY);

        tvEasyDesc = findViewById(R.id.tv_easy_desc);
        tvMediumDesc = findViewById(R.id.tv_medium_desc);
        tvHardDesc = findViewById(R.id.tv_hard_desc);

        tvEasyTitle = findViewById(R.id.tv_easy_title);
        tvMediumTitle = findViewById(R.id.tv_medium_title);
        tvHardTitle = findViewById(R.id.tv_hard_title);

        ivEasyIcon = findViewById(R.id.iv_easy_icon);
        ivMediumIcon = findViewById(R.id.iv_medium_icon);
        ivHardIcon = findViewById(R.id.iv_hard_icon);

        containerEasy = findViewById(R.id.icon_container_easy);
        containerMedium = findViewById(R.id.icon_container_medium);
        containerHard = findViewById(R.id.icon_container_hard);

        ivCheckEasy = findViewById(R.id.iv_check_easy);
        ivCheckMedium = findViewById(R.id.iv_check_medium);
        ivCheckHard = findViewById(R.id.iv_check_hard);

        cardEasy = findViewById(R.id.card_easy);
        cardMedium = findViewById(R.id.card_medium);
        cardHard = findViewById(R.id.card_hard);

        // Setup voice navigation
        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });
        voiceNavigator.setup();
        voiceNavigator.startListening();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Help button
        ImageView btnHelp = findViewById(R.id.btn_help);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> VoiceHelpDialog.show(this));
        }

        // Setup difficulty selection
        cardEasy.setOnClickListener(v -> {
            selectedDifficulty = DIFFICULTY_EASY;
            updateDifficultyUI();
        });

        cardMedium.setOnClickListener(v -> {
            selectedDifficulty = DIFFICULTY_MEDIUM;
            updateDifficultyUI();
        });

        cardHard.setOnClickListener(v -> {
            selectedDifficulty = DIFFICULTY_HARD;
            updateDifficultyUI();
        });

        // Start button
        findViewById(R.id.btn_start).setOnClickListener(v -> launchGame());

        setupGameDifficulty();
        updateDifficultyUI();
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "start":
            case "confirm":
            case "next":
                voiceNavigator.speak(getString(R.string.launching_game));
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::launchGame, 1000);
                break;
            case "help":
                voiceNavigator.speak(getString(R.string.difficulty_help_voice));
                break;
            case "easy":
                selectedDifficulty = DIFFICULTY_EASY;
                updateDifficultyUI();
                voiceNavigator.speak(getString(R.string.selected_easy));
                break;
            case "medium":
                selectedDifficulty = DIFFICULTY_MEDIUM;
                updateDifficultyUI();
                voiceNavigator.speak(getString(R.string.selected_medium));
                break;
            case "hard":
                selectedDifficulty = DIFFICULTY_HARD;
                updateDifficultyUI();
                voiceNavigator.speak(getString(R.string.selected_hard));
                break;
        }
    }

    private void setupGameDifficulty() {
        switch (gameType) {
            case GameInstructionActivity.GAME_MEMORY:
                tvEasyDesc.setText("3 pary kart");
                tvMediumDesc.setText("6 par kart");
                tvHardDesc.setText("8 par kart");
                voiceNavigator.speakDelayed(getString(R.string.difficulty_selection_voice) + " " + getString(R.string.game_memory), 800);
                break;

            case GameInstructionActivity.GAME_COLORS:
                tvEasyDesc.setText("Krótkie sekwencje");
                tvMediumDesc.setText("Średnie sekwencje");
                tvHardDesc.setText("Długie sekwencje");
                voiceNavigator.speakDelayed(getString(R.string.difficulty_selection_voice) + " " + getString(R.string.game_colors), 800);
                break;

            case GameInstructionActivity.GAME_LIQUID:
                tvEasyDesc.setText("3 kolory");
                tvMediumDesc.setText("4 kolory");
                tvHardDesc.setText("5 kolorów");
                voiceNavigator.speakDelayed(getString(R.string.difficulty_selection_voice) + " " + getString(R.string.game_liquid), 800);
                break;

            case GameInstructionActivity.GAME_2048:
                tvEasyDesc.setText("Cel: 512");
                tvMediumDesc.setText("Cel: 1024");
                tvHardDesc.setText("Cel: 2048");
                voiceNavigator.speakDelayed(getString(R.string.difficulty_selection_voice) + " " + getString(R.string.game_2048), 800);
                break;
        }
    }

    private void updateDifficultyUI() {
        // Reset all to unselected state
        setCardSelected(cardEasy, containerEasy, ivEasyIcon, tvEasyTitle, tvEasyDesc, ivCheckEasy, false, 0xFFE6F9EE, 0xFF10B981);
        setCardSelected(cardMedium, containerMedium, ivMediumIcon, tvMediumTitle, tvMediumDesc, ivCheckMedium, false, 0xFFE0F2FE, 0xFF004A99); // Blue for medium
        setCardSelected(cardHard, containerHard, ivHardIcon, tvHardTitle, tvHardDesc, ivCheckHard, false, 0xFFFEF2F2, 0xFFEF4444);

        // Apply selected state
        if (selectedDifficulty == DIFFICULTY_EASY) {
            setCardSelected(cardEasy, containerEasy, ivEasyIcon, tvEasyTitle, tvEasyDesc, ivCheckEasy, true, 0xFF34D399, 0xFFFFFFFF);
        } else if (selectedDifficulty == DIFFICULTY_MEDIUM) {
            setCardSelected(cardMedium, containerMedium, ivMediumIcon, tvMediumTitle, tvMediumDesc, ivCheckMedium, true, 0xFF337ACC, 0xFFFFFFFF);
        } else if (selectedDifficulty == DIFFICULTY_HARD) {
            setCardSelected(cardHard, containerHard, ivHardIcon, tvHardTitle, tvHardDesc, ivCheckHard, true, 0xFFEF4444, 0xFFFFFFFF);
        }
    }

    private void setCardSelected(CardView card, android.view.View iconContainer, ImageView icon, 
                                TextView title, TextView desc, ImageView check, boolean selected, 
                                int iconBgColor, int iconColor) {
        if (selected) {
            card.setCardBackgroundColor(0xFF004A99);
            title.setTextColor(0xFFFFFFFF);
            desc.setTextColor(0xFFDBEAFE);
            iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(iconBgColor));
            icon.setColorFilter(iconColor);
            check.setVisibility(android.view.View.VISIBLE);
        } else {
            card.setCardBackgroundColor(0xFFFFFFFF);
            title.setTextColor(0xFF004A99);
            desc.setTextColor(0xFF64748B);
            iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(iconBgColor));
            icon.setColorFilter(iconColor);
            check.setVisibility(android.view.View.GONE);
        }
    }

    private void launchGame() {
        Intent intent = null;

        switch (gameType) {
            case GameInstructionActivity.GAME_MEMORY:
                intent = new Intent(this, MemoryGameActivity.class);
                switch (selectedDifficulty) {
                    case DIFFICULTY_EASY:
                        intent.putExtra("EXTRA_COLUMNS", 2);
                        intent.putExtra("EXTRA_ROWS", 3);
                        break;
                    case DIFFICULTY_MEDIUM:
                        intent.putExtra("EXTRA_COLUMNS", 3);
                        intent.putExtra("EXTRA_ROWS", 4);
                        break;
                    case DIFFICULTY_HARD:
                        intent.putExtra("EXTRA_COLUMNS", 4);
                        intent.putExtra("EXTRA_ROWS", 4);
                        break;
                }
                break;

            case GameInstructionActivity.GAME_COLORS:
                intent = new Intent(this, ColorTapActivity.class);
                intent.putExtra(ColorTapActivity.EXTRA_DIFFICULTY, selectedDifficulty);
                break;

            case GameInstructionActivity.GAME_LIQUID:
                intent = new Intent(this, LiquidSortActivity.class);
                intent.putExtra(LiquidSortActivity.EXTRA_DIFFICULTY, selectedDifficulty);
                break;

            case GameInstructionActivity.GAME_2048:
                intent = new Intent(this, Game2048Activity.class);
                intent.putExtra("EXTRA_DIFFICULTY", selectedDifficulty);
                break;
        }

        if (intent != null) {
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceNavigator != null) {
            voiceNavigator.cleanup();
        }
    }
}
