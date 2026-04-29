package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


public class ChoiceActivity extends AppCompatActivity {

    private VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        // Prośba o uprawnienia do mikrofonu
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
        }

        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });
        voiceNavigator.setup();
        voiceNavigator.startListening();

        NavbarHelper.initNavbar(this);

        // Initialize voice manager
        VoiceManager.getInstance().init(this);

        CardView cardBody = findViewById(R.id.card_body);
        CardView cardMind = findViewById(R.id.card_mind);

        cardBody.setOnClickListener(v -> {
            startActivity(new Intent(ChoiceActivity.this, MainActivity.class));
        });

        cardMind.setOnClickListener(v -> {
            startActivity(new Intent(ChoiceActivity.this, MindGamesActivity.class));
        });

voiceNavigator.speakDelayed("Co chcesz poćwiczyć? Wybierz Ciało lub Umysł.", 500);

        // Help button listener
        ImageButton btnHelp = findViewById(R.id.btn_help);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> VoiceHelpDialog.show(this));
        }
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "stop":
                voiceNavigator.stopSpeaking();
                break;
            case "help":
                voiceNavigator.speak("Powiedz 'ciało' aby wybrać ćwiczenia fizyczne, lub 'umysł' aby wybrać gry umysłowe.");
                break;
            case "read":
            case "repeat":
                voiceNavigator.speak("Co chcesz poćwiczyć? Wybierz obszar treningu: Ciało dla siły i równowagi, Umysł dla pamięci i koncentracji.");
                break;
            case "body":
            case "exercises":
                voiceNavigator.speak("Przechodzę do ćwiczeń.");
                new android.os.Handler().postDelayed(() -> {
                    startActivity(new Intent(ChoiceActivity.this, MainActivity.class));
                }, 1000);
                break;
            case "mind":
            case "games":
                voiceNavigator.speak("Przechodzę do gier umysłowych.");
                new android.os.Handler().postDelayed(() -> {
                    startActivity(new Intent(ChoiceActivity.this, MindGamesActivity.class));
                }, 1000);
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
