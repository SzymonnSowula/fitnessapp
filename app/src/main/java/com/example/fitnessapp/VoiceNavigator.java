package com.example.fitnessapp;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class VoiceNavigator implements VoiceManager.VoiceCallback {
    private static final String TAG = "VoiceNavigator";
    private Activity activity;
    private VoiceCallback callback;

    public interface VoiceCallback {
        void onVoiceCommand(String command);
    }

    public VoiceNavigator(Activity activity, VoiceCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public void setup() {
        VoiceManager.getInstance().addCallback(this);
        // Automatyczny start słuchania przy wejściu na ekran
        startListening();
    }

    public void cleanup() {
        VoiceManager.getInstance().removeCallback(this);
        stopListening();
    }

    public void startListening() {
        VoiceManager.getInstance().startListening();
    }

    public void stopListening() {
        VoiceManager.getInstance().stopListening();
    }

    public void speak(String text) {
        VoiceManager.getInstance().speak(text);
    }

    public void speakDelayed(String text, long delayMs) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
            () -> speak(text), delayMs
        );
    }

    public void stopSpeaking() {
        VoiceManager.getInstance().stopSpeech();
    }

    @Override
    public void onSpeechResult(String text, boolean isFinal) {
        if (!isFinal) return;

        Log.d(TAG, "Recognized text: " + text);
        
        // 1. Sprawdzamy dopasowanie w VoiceCommands (teraz rygorystyczne)
        String command = VoiceCommands.matchCommand(text);
        
        if (command != null) {
            Log.d(TAG, "Matched command: " + command);
            
            // Reaguj tylko jeśli:
            // a) To komenda nawigacyjna (zawsze ważne)
            // b) Tekst zawierał słowo "Fitness" (zaimplementowane w matchCommand)
            // c) Lub po prostu komenda jest znaleziona (matchCommand odsieje śmieci)
            
            if (isNavigationCommand(command)) {
                handleNavigationCommand(command);
            } else if (callback != null) {
                callback.onVoiceCommand(command);
            }
        }
    }

    private boolean isNavigationCommand(String command) {
        return command.equals("home") || command.equals("exercises") || 
               command.equals("games") || command.equals("settings") || 
               command.equals("back") || command.equals("exit");
    }

    public void handleNavigationCommand(String command) {
        if (activity == null || activity.isFinishing()) return;

        switch (command) {
            case "home": navigateTo(ChoiceActivity.class); break;
            case "exercises": navigateTo(MainActivity.class); break;
            case "games": navigateTo(MindGamesActivity.class); break;
            case "settings": navigateTo(SettingsActivity.class); break;
            case "back": activity.onBackPressed(); break;
            case "exit": activity.finish(); break;
        }
    }

    private void navigateTo(Class<?> to) {
        if (activity.getClass().equals(to)) return; // Już tu jesteśmy
        Intent intent = new Intent(activity, to);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override public void onSpeechError(int errorCode, String errorMessage) {}
    @Override public void onTTSReady() {}
    @Override public void onTTSStarted() {}
    @Override public void onTTSDone() {}
}
