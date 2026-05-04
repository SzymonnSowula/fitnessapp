package com.example.fitnessapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.ref.WeakReference;

public class VoiceNavigator implements VoiceManager.VoiceCallback {
    private static final String TAG = "VoiceNavigator";
    private final WeakReference<Activity> activityRef;
    private VoiceCallback callback;
    private final Handler pendingHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSpeech;
    private boolean isDialogShowing = false;

    public interface VoiceCallback {
        void onVoiceCommand(String command);
        default void onListeningStateChanged(boolean isListening) {}
    }

    public VoiceNavigator(Activity activity, VoiceCallback callback) {
        this.activityRef = new WeakReference<>(activity);
        this.callback = callback;
    }

    public void setup() {
        VoiceManager.getInstance().addCallback(this);
    }

    public void cleanup() {
        VoiceManager.getInstance().removeCallback(this);
        stopListening();
        cancelPendingSpeech();
        callback = null;
    }

    public void cancelPendingSpeech() {
        if (pendingSpeech != null) {
            pendingHandler.removeCallbacks(pendingSpeech);
            pendingSpeech = null;
        }
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
        cancelPendingSpeech();
        pendingSpeech = () -> {
            speak(text);
            pendingSpeech = null;
        };
        pendingHandler.postDelayed(pendingSpeech, delayMs);
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
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;

        switch (command) {
            case "home":
            case "back_main":
                speak("Przechodzę do strony głównej");
                navigateTo(activity, ChoiceActivity.class);
                break;
            case "exercises":
            case "body":
                speak("Przechodzę do ćwiczeń");
                navigateTo(activity, MainActivity.class);
                break;
            case "games":
            case "mind":
                speak("Przechodzę do gier");
                navigateTo(activity, MindGamesActivity.class);
                break;
            case "settings":
                speak("Przechodzę do ustawień");
                navigateTo(activity, SettingsActivity.class);
                break;
            case "back":
                activity.onBackPressed();
                break;
            case "exit":
                if (isDialogShowing) return;
                isDialogShowing = true;
                speak("Czy na pewno chcesz wyjść z aplikacji?");
                ConfirmationHelper.showExitConfirmation(activity, new ConfirmationHelper.ConfirmationCallback() {
                    @Override
                    public void onConfirm() {
                        isDialogShowing = false;
                        activity.finishAffinity();
                    }
                    @Override
                    public void onCancel() {
                        isDialogShowing = false;
                        speak("Zostajesz w aplikacji.");
                    }
                });
                break;
            case "profile":
                speak("Funkcja profilu nie jest jeszcze dostępna");
                break;
        }
    }

    private void navigateTo(Activity activity, Class<?> to) {
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

    @Override
    public void onListeningStateChanged(boolean isListening) {
        Log.d(TAG, "Listening state changed: " + isListening);
        if (callback != null) {
            callback.onListeningStateChanged(isListening);
        }
    }
}
