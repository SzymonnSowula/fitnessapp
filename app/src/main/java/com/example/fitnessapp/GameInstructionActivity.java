package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class GameInstructionActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_TYPE = "game_type";

    public static final int GAME_MEMORY = 1;
    public static final int GAME_COLORS = 2;
    public static final int GAME_LIQUID = 3;
    public static final int GAME_2048 = 4;

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
        voiceNavigator.setup();
        voiceNavigator.startListening();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Setup help button
        ImageView btnHelp = findViewById(R.id.btn_help);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> VoiceHelpDialog.show(this));
        }

        int gameType = getIntent().getIntExtra(EXTRA_GAME_TYPE, GAME_MEMORY);
        setupGameInstruction(gameType);
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "start":
            case "confirm":
            case "next":
                voiceNavigator.speak(getString(R.string.difficulty_selection_voice));
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    View btnStart = findViewById(R.id.btn_start);
                    if (btnStart != null) btnStart.performClick();
                }, 1000);
                break;
            case "read":
            case "repeat":
                voiceNavigator.speak(tvInstructionText.getText().toString());
                break;
            case "help":
                voiceNavigator.speak(getString(R.string.instruction_read_voice));
                break;
        }
    }

    private void setupGameInstruction(int gameType) {
        // All games: btn_start goes to GameDifficultyActivity
        View btnStart = findViewById(R.id.btn_start);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                Intent intent = new Intent(this, GameDifficultyActivity.class);
                intent.putExtra(GameDifficultyActivity.EXTRA_GAME_TYPE, gameType);
                startActivity(intent);
                finish();
            });
        }

        switch (gameType) {
            case GAME_MEMORY:
                ivGameIcon.setImageResource(R.drawable.ic_brain);
                ivGameIcon.setColorFilter(0xFF7C3AED);
                tvGameTitle.setText(R.string.game_memory);
                tvInstructionText.setText(getString(R.string.memory_help_voice).toUpperCase());
                voiceNavigator.speakDelayed(getString(R.string.launching_memory) + " " + getString(R.string.instruction_read_voice), 800);
                break;

            case GAME_COLORS:
                ivGameIcon.setImageResource(R.drawable.ic_mood_happy);
                ivGameIcon.setColorFilter(0xFF059669);
                tvGameTitle.setText(R.string.game_colors);
                tvInstructionText.setText(getString(R.string.color_tap_help_voice).toUpperCase());
                voiceNavigator.speakDelayed(getString(R.string.launching_colors) + " " + getString(R.string.instruction_read_voice), 800);
                break;

            case GAME_LIQUID:
                ivGameIcon.setImageResource(R.drawable.ic_plan);
                ivGameIcon.setColorFilter(0xFF2563EB);
                tvGameTitle.setText(R.string.game_liquid);
                tvInstructionText.setText(getString(R.string.liquid_help_voice).toUpperCase());
                voiceNavigator.speakDelayed(getString(R.string.launching_liquid) + " " + getString(R.string.instruction_read_voice), 800);
                break;

            case GAME_2048:
                ivGameIcon.setImageResource(R.drawable.ic_onboarding_3);
                ivGameIcon.setColorFilter(0xFFEA580C);
                tvGameTitle.setText(R.string.game_2048);
                tvInstructionText.setText(getString(R.string.game_2048_help_voice).toUpperCase());
                voiceNavigator.speakDelayed(getString(R.string.launching_2048) + " " + getString(R.string.instruction_read_voice), 800);
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
