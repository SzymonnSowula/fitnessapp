package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameInstructionActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_TYPE = "game_type";

    public static final int GAME_MEMORY = 1;
    public static final int GAME_COLORS = 2;
    public static final int GAME_LIQUID = 3;

    private ImageView ivGameIcon;
    private TextView tvGameTitle;
    private TextView tvInstructionText;
    private VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_instruction);

ivGameIcon = findViewById(R.id.iv_game_icon);
 tvGameTitle = findViewById(R.id.tv_game_title);
 tvInstructionText = findViewById(R.id.tv_instruction_text);

        // Setup voice navigation
        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });

        // Start listening when activity is shown
        new android.os.Handler().postDelayed(() -> {
            voiceNavigator.setup();
            voiceNavigator.startListening();
        }, 500);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        int gameType = getIntent().getIntExtra(EXTRA_GAME_TYPE, GAME_MEMORY);
        setupGameInstruction(gameType);
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "back":
            case "exit":
                finish();
                break;
            case "start":
            case "confirm":
            case "next":
                // Start the game
                View btnStart = findViewById(R.id.btn_start);
                if (btnStart != null) btnStart.performClick();
                break;
            case "read":
            case "repeat":
                voiceNavigator.speak(tvInstructionText.getText().toString());
                break;
            case "help":
                voiceNavigator.speak("Komendy: start lub następny - rozpocznij grę. Wstecz - wróć. Czytaj - przeczytaj instrukcję.");
                break;
        }
    }

    private void setupGameInstruction(int gameType) {
        switch (gameType) {
            case GAME_MEMORY:
                ivGameIcon.setImageResource(R.drawable.ic_brain);
                ivGameIcon.setColorFilter(0xFF004A99);
                tvGameTitle.setText("GRA PAMIĘĆ");
                tvInstructionText.setText(
                    "Znajdź wszystkie pary jednakowych kart.\n\n" +
                    "Kliknij dwie karty - jeśli są takie same,\n" +
                    "zostają odkryte.\n\n" +
                    "Zapamiętaj położenie kart\n" +
                    "i znajdź ich pary."
                );
                findViewById(R.id.btn_start).setOnClickListener(v -> {
                    startActivity(new Intent(this, EasyGamesActivity.class));
                    finish();
                });
                voiceNavigator.speakDelayed("Gra Pamięć. Znajdź wszystkie pary jednakowych kart. Powiedz start aby rozpocząć.", 800);
                break;

            case GAME_COLORS:
                ivGameIcon.setImageResource(R.drawable.ic_onboarding_2);
                ivGameIcon.setColorFilter(0xFF057A32);
                tvGameTitle.setText("GRA KOLORY");
                tvInstructionText.setText(
                    "Obserwuj sekwencję kolorów,\n" +
                    "która będzie migać.\n\n" +
                    "Gdy pojawi się napis DOBRZE,\n" +
                    "powtórz sekwencję.\n\n" +
                    "Klikaj kolory w kolejności,\n" +
                    "którą zapamiętałeś."
                );
                findViewById(R.id.btn_start).setOnClickListener(v -> {
                    Intent intent = new Intent(this, ColorTapActivity.class);
                    intent.putExtra(ColorTapActivity.EXTRA_DIFFICULTY, ColorTapActivity.DIFFICULTY_EASY);
                    startActivity(intent);
                    finish();
                });
                voiceNavigator.speakDelayed("Gra Kolory. Obserwuj sekwencję kolorów, którą musisz powtórzyć. Powiedz start aby rozpocząć.", 800);
                break;

            case GAME_LIQUID:
                ivGameIcon.setImageResource(R.drawable.ic_onboarding_1);
                ivGameIcon.setColorFilter(0xFF994A00);
                tvGameTitle.setText("GRA PŁYNY");
                tvInstructionText.setText(
                    "Sortuj kolory przelewając płyny\n" +
                    "między probówkami.\n\n" +
                    "Kliknij na probówkę, aby ją wybrać.\n" +
                    "Następnie kliknij drugą probówkę,\n" +
                    "aby przelać płyn.\n\n" +
                    "Sortuj kolory tak, aby wszystkie\n" +
                    "takie same znalazły się razem."
                );
                findViewById(R.id.btn_start).setOnClickListener(v -> {
                    startActivity(new Intent(this, LiquidSortActivity.class));
                    finish();
                });
                voiceNavigator.speakDelayed("Gra Płyny. Sortuj kolory przelewając płyny między probówkami. Powiedz start aby rozpocząć.", 800);
                break;
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