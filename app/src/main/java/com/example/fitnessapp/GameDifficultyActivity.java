package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
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
    private ImageView ivGameIcon;
    private TextView tvGameTitle, tvEasyDesc, tvMediumDesc, tvHardDesc;
    private VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_difficulty);

        gameType = getIntent().getIntExtra(EXTRA_GAME_TYPE, GameInstructionActivity.GAME_MEMORY);

        ivGameIcon = findViewById(R.id.iv_game_icon);
        tvGameTitle = findViewById(R.id.tv_game_title);
        tvEasyDesc = findViewById(R.id.tv_easy_desc);
        tvMediumDesc = findViewById(R.id.tv_medium_desc);
        tvHardDesc = findViewById(R.id.tv_hard_desc);

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
                voiceNavigator.speak("Rozpoczynam grę.");
                new android.os.Handler().postDelayed(this::launchGame, 1000);
                break;
            case "help":
                voiceNavigator.speak("Wybierz poziom trudności: łatwy, średni lub trudny. Powiedz start aby rozpocząć grę.");
                break;
            case "easy":
                selectedDifficulty = DIFFICULTY_EASY;
                updateDifficultyUI();
                voiceNavigator.speak("Wybrano poziom łatwy. Powiedz start aby zacząć.");
                break;
            case "medium":
                selectedDifficulty = DIFFICULTY_MEDIUM;
                updateDifficultyUI();
                voiceNavigator.speak("Wybrano poziom średni. Powiedz start aby zacząć.");
                break;
            case "hard":
                selectedDifficulty = DIFFICULTY_HARD;
                updateDifficultyUI();
                voiceNavigator.speak("Wybrano poziom trudny. Powiedz start aby zacząć.");
                break;
        }
    }

    private void setupGameDifficulty() {
        switch (gameType) {
            case GameInstructionActivity.GAME_MEMORY:
                ivGameIcon.setImageResource(R.drawable.ic_brain);
                ivGameIcon.setColorFilter(0xFF7C3AED);
                tvGameTitle.setText("MEMORY");
                tvEasyDesc.setText("3 pary kart");
                tvMediumDesc.setText("6 par kart");
                tvHardDesc.setText("8 par kart");
                voiceNavigator.speakDelayed("Wybierz poziom trudności gry Memory. Łatwy, średni lub trudny.", 800);
                break;

            case GameInstructionActivity.GAME_COLORS:
                ivGameIcon.setImageResource(R.drawable.ic_mood_happy);
                ivGameIcon.setColorFilter(0xFF059669);
                tvGameTitle.setText("KOLORY");
                tvEasyDesc.setText("Krótkie sekwencje");
                tvMediumDesc.setText("Średnie sekwencje");
                tvHardDesc.setText("Długie sekwencje");
                voiceNavigator.speakDelayed("Wybierz poziom trudności gry Kolory. Łatwy, średni lub trudny.", 800);
                break;

            case GameInstructionActivity.GAME_LIQUID:
                ivGameIcon.setImageResource(R.drawable.ic_plan);
                ivGameIcon.setColorFilter(0xFF2563EB);
                tvGameTitle.setText("PŁYNY");
                tvEasyDesc.setText("3 kolory");
                tvMediumDesc.setText("4 kolory");
                tvHardDesc.setText("5 kolorów");
                voiceNavigator.speakDelayed("Wybierz poziom trudności gry Płyny. Łatwy, średni lub trudny.", 800);
                break;

            case GameInstructionActivity.GAME_2048:
                ivGameIcon.setImageResource(R.drawable.ic_onboarding_3);
                ivGameIcon.setColorFilter(0xFFEA580C);
                tvGameTitle.setText("2048");
                tvEasyDesc.setText("Cel: 512");
                tvMediumDesc.setText("Cel: 1024");
                tvHardDesc.setText("Cel: 2048");
                voiceNavigator.speakDelayed("Wybierz poziom trudności gry dwa tysiące czterdzieści osiem. Łatwy, średni lub trudny.", 800);
                break;
        }
    }

    private void updateDifficultyUI() {
        // Easy card - selected: green, unselected: light blue
        if (selectedDifficulty == DIFFICULTY_EASY) {
            cardEasy.setCardBackgroundColor(0xFF057A32);
            cardMedium.setCardBackgroundColor(0xFFDBEAFE);
            cardHard.setCardBackgroundColor(0xFFDBEAFE);
        } else if (selectedDifficulty == DIFFICULTY_MEDIUM) {
            cardEasy.setCardBackgroundColor(0xFFDBEAFE);
            cardMedium.setCardBackgroundColor(0xFFEA580C);
            cardHard.setCardBackgroundColor(0xFFDBEAFE);
        } else {
            cardEasy.setCardBackgroundColor(0xFFDBEAFE);
            cardMedium.setCardBackgroundColor(0xFFDBEAFE);
            cardHard.setCardBackgroundColor(0xFFDC2626);
        }

        // Update text and icon colors based on selection for accessibility
        updateCardSelectionState(cardEasy, R.id.iv_easy_icon, 0xFF34D399, selectedDifficulty == DIFFICULTY_EASY);
        updateCardSelectionState(cardMedium, R.id.iv_medium_icon, 0xFF93C5FD, selectedDifficulty == DIFFICULTY_MEDIUM);
        updateCardSelectionState(cardHard, R.id.iv_hard_icon, 0xFFFCA5A5, selectedDifficulty == DIFFICULTY_HARD);
    }

    private void updateCardSelectionState(CardView card, int iconId, int indicatorColor, boolean isSelected) {
        if (card.getChildCount() > 0 && card.getChildAt(0) instanceof android.view.ViewGroup) {
            android.view.ViewGroup container = (android.view.ViewGroup) card.getChildAt(0);

            // 1. Update Icon and its indicator background
            if (container.getChildCount() > 0 && container.getChildAt(0) instanceof android.view.ViewGroup) {
                android.view.ViewGroup iconFrame = (android.view.ViewGroup) container.getChildAt(0);
                if (iconFrame.getChildCount() >= 2) {
                    android.view.View indicator = iconFrame.getChildAt(0);
                    ImageView icon = (ImageView) iconFrame.getChildAt(1);

                    if (isSelected) {
                        indicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0x40FFFFFF)); // Semi-transparent white
                        icon.setColorFilter(0xFFFFFFFF);
                    } else {
                        indicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(indicatorColor));
                        // Pick a dark color for the icon on light background
                        icon.setColorFilter(0xFF1E40AF);
                    }
                }
            }

            // 2. Update Text Colors
            if (container.getChildCount() > 1 && container.getChildAt(1) instanceof android.view.ViewGroup) {
                android.view.ViewGroup textContainer = (android.view.ViewGroup) container.getChildAt(1);
                for (int i = 0; i < textContainer.getChildCount(); i++) {
                    if (textContainer.getChildAt(i) instanceof TextView) {
                        TextView tv = (TextView) textContainer.getChildAt(i);
                        if (isSelected) {
                            tv.setTextColor(0xFFFFFFFF);
                        } else {
                            tv.setTextColor(i == 0 ? 0xFF004A99 : 0xFF1E40AF);
                        }
                    }
                }
            }
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
