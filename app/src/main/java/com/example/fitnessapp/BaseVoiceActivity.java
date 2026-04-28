package com.example.fitnessapp;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Base class for activities that use voice navigation.
 * Extracts common VoiceNavigator pattern to reduce code duplication.
 */
public abstract class BaseVoiceActivity extends AppCompatActivity {

    protected VoiceNavigator voiceNavigator;
    private FloatingActionButton fabMic;
    private Animation pulseAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupVoiceNavigation() {
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_pulse);

        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }

            @Override
            public void onListeningStateChanged(boolean isListening) {
                runOnUiThread(() -> updateListeningVisual(isListening));
            }
        });
        voiceNavigator.setup();

        fabMic = findViewById(R.id.fab_mic);
        if (fabMic != null) {
            fabMic.setOnClickListener(v -> {
                if (VoiceManager.getInstance().isListening()) {
                    voiceNavigator.stopListening();
                } else {
                    voiceNavigator.startListening();
                    voiceNavigator.speak("Słucham.");
                }
            });
        }
    }

    private void updateListeningVisual(boolean isListening) {
        if (fabMic != null) {
            if (isListening) {
                fabMic.startAnimation(pulseAnimation);
            } else {
                fabMic.clearAnimation();
            }
        }
    }

    protected void speak(String text) {
        if (voiceNavigator != null) {
            voiceNavigator.speak(text);
        }
    }

    protected void speakDelayed(String text, long delayMs) {
        if (voiceNavigator != null) {
            voiceNavigator.speakDelayed(text, delayMs);
        }
    }

    protected void stopSpeaking() {
        if (voiceNavigator != null) {
            voiceNavigator.stopSpeaking();
        }
    }

    protected String getContextualHelp() {
        return VoiceCommands.getHelpText();
    }

    protected void showHelpDialog() {
        VoiceHelpDialog.show(this);
    }

    protected abstract void handleVoiceCommand(String command);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceNavigator != null) {
            voiceNavigator.cleanup();
            voiceNavigator = null;
        }
    }
}
