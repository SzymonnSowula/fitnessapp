package com.example.fitnessapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;


public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "VoiceSettings";
    private static final String KEY_TTS_ENABLED = "tts_enabled";
    private static final String KEY_SPEECH_ENABLED = "speech_enabled";
    private static final String KEY_SPEECH_RATE = "speech_rate";

    private SwitchCompat switchVoiceEnabled;
    private SeekBar seekbarSpeechRate;
    private VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });

        // Inicjalizacja Navbar
        NavbarHelper.initNavbar(this);

        voiceNavigator.setup();
        voiceNavigator.startListening();

        initVoiceSettings();
        initAccountSettings();
    }

    private void initVoiceSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        switchVoiceEnabled = findViewById(R.id.switch_voice_enabled);
        seekbarSpeechRate = findViewById(R.id.seekbar_speech_rate);

        // Load saved settings
        boolean voiceEnabled = prefs.getBoolean(KEY_SPEECH_ENABLED, true);
        int speechRate = prefs.getInt(KEY_SPEECH_RATE, 10);

        switchVoiceEnabled.setChecked(voiceEnabled);
        seekbarSpeechRate.setProgress(speechRate);

        // Apply saved speech rate
        VoiceManager.getInstance().setSpeechRate(getSpeechRateFloat(speechRate));

        // Master Voice switch listener
        switchVoiceEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            VoiceManager.getInstance().setSpeechEnabled(isChecked);
            VoiceManager.getInstance().setTTSEnabled(isChecked);
            prefs.edit().putBoolean(KEY_SPEECH_ENABLED, isChecked).apply();
            prefs.edit().putBoolean(KEY_TTS_ENABLED, isChecked).apply();
            
            if (isChecked) {
                VoiceManager.getInstance().speak("Obsługa głosowa włączona");
            } else {
                VoiceManager.getInstance().stopSpeech();
            }
        });

        // Speech rate change listener
        seekbarSpeechRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float rate = getSpeechRateFloat(progress);
                    VoiceManager.getInstance().setSpeechRate(rate);
                    prefs.edit().putInt(KEY_SPEECH_RATE, progress).apply();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_SPEECH_RATE, seekBar.getProgress());
                editor.apply();
            }
        });

        // Test speech rate button
        findViewById(R.id.layout_speech_rate).setOnClickListener(v -> {
            VoiceManager.getInstance().speak("Przykład mowy. Czy teraz jest lepiej?");
        });
    }

    private float getSpeechRateFloat(int progress) {
        // Progress 0-20 maps to 0.5-2.0 speech rate
        return 0.5f + (progress / 20.0f) * 1.5f;
    }

    private void initAccountSettings() {
        // Wylogowanie – lokalne czyszczenie danych
        Button btnLogout = findViewById(R.id.btn_logout_settings);
        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
            finishAffinity();
        });

        // Obsługa kliknięć w opcje
        findViewById(R.id.tv_change_name).setOnClickListener(v ->
            Toast.makeText(this, "Funkcja zmiany imienia będzie dostępna wkrótce", Toast.LENGTH_SHORT).show());

        findViewById(R.id.tv_edit_conditions).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, EditConditionsActivity.class));
        });

        findViewById(R.id.tv_history).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, HistoryActivity.class));
        });
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "stop":
                voiceNavigator.stopSpeaking();
                break;
            case "help":
                voiceNavigator.speak("Ustawienia głosowe pozwalają kontrolować syntezę mowy i rozpoznawanie głosu.");
                break;
            case "read":
            case "repeat":
                voiceNavigator.speak("Ustawienia głosowe: włączanie głosu oraz szybkość mowy.");
                break;
        }
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceNavigator != null) {
            voiceNavigator.cleanup();
        }
    }
}
