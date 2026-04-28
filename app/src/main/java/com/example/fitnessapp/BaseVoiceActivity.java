package com.example.fitnessapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Base class for activities that use voice navigation.
 * Extracts common VoiceNavigator pattern to reduce code duplication.
 */
public abstract class BaseVoiceActivity extends AppCompatActivity {

    protected VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupVoiceNavigation() {
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
        voiceNavigator.startListening();
    }

    private void updateListeningVisual(boolean isListening) {
        // No FAB mic visual feedback since mic button was removed from layouts
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
