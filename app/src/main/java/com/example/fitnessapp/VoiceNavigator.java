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

    public boolean isSpeaking() {
        return VoiceManager.getInstance().isSpeaking();
    }

    @Override
    public void onSpeechResult(String text, boolean isFinal) {
        if (!isFinal) return;

        Log.d(TAG, "Recognized text: " + text);

        String command = VoiceCommands.matchCommand(text);

        if (command != null) {
            Log.d(TAG, "Matched command: " + command);

            if (VoiceCommands.isNavigationCommand(command)) {
                handleNavigationCommand(command);
            } else if (callback != null) {
                callback.onVoiceCommand(command);
            }
        }
    }

    public void handleNavigationCommand(String command) {
        if (activity == null || activity.isFinishing()) return;

        switch (command) {
            case "home":
            case "back_main":
                speak("Przechodzę do strony głównej");
                navigateTo(ChoiceActivity.class);
                break;
            case "exercises":
            case "body":
                speak("Przechodzę do ćwiczeń");
                navigateTo(MainActivity.class);
                break;
            case "games":
            case "mind":
                speak("Przechodzę do gier");
                navigateTo(MindGamesActivity.class);
                break;
            case "settings":
                speak("Przechodzę do ustawień");
                navigateTo(SettingsActivity.class);
                break;
            case "back":
                activity.onBackPressed();
                break;
            case "exit":
                speak("Zamykam aplikację");
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> activity.finishAffinity(), 1000);
                break;
            case "profile":
                speak("Funkcja profilu nie jest jeszcze dostępna");
                break;
        }
    }

    private void navigateTo(Class<?> to) {
        if (activity.getClass().equals(to)) return;
        Intent intent = new Intent(activity, to);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    public void onSpeechError(int errorCode, String errorMessage) {
        Log.e(TAG, "Speech error: " + errorMessage);
    }

    @Override
    public void onTTSReady() {
        Log.d(TAG, "TTS Ready");
    }

    @Override
    public void onTTSStarted() {
        Log.d(TAG, "TTS Started speaking");
    }

    @Override
    public void onTTSDone() {
        Log.d(TAG, "TTS Done speaking");
    }

    @Override
    public void onListeningStarted() {
        Log.d(TAG, "Listening started");
    }

    @Override
    public void onListeningStopped() {
        Log.d(TAG, "Listening stopped");
    }
}