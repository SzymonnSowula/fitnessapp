package com.example.fitnessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class VoiceManager implements TextToSpeech.OnInitListener {
    private static final String TAG = "VoiceManager";
    private static final String PREFS_NAME = "VoiceSettings";
    private static final String KEY_TTS_ENABLED = "tts_enabled";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_SPEECH_ENABLED = "speech_enabled";

    private static VoiceManager instance;
    private static volatile boolean isInitialized = false;

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Context appContext;
    private boolean ttsReady = false;
    private boolean isListeningDesired = false;
    private boolean isSpeakingNow = false;

    private final CopyOnWriteArraySet<VoiceCallback> callbacks = new CopyOnWriteArraySet<>();
    private String currentLanguage = "pl-PL";

    public interface VoiceCallback {
        void onSpeechResult(String text, boolean isFinal);
        void onSpeechError(int errorCode, String errorMessage);
        void onTTSReady();
        void onTTSStarted();
        void onTTSDone();
    }

    public static VoiceManager getInstance() {
        if (instance == null) {
            synchronized (VoiceManager.class) {
                if (instance == null) {
                    instance = new VoiceManager();
                }
            }
        }
        return instance;
    }

    private VoiceManager() {}

    public void init(Context context) {
        if (isInitialized) return;
        this.appContext = context.getApplicationContext();
        this.tts = new TextToSpeech(appContext, this);
        isInitialized = true;
        Log.d(TAG, "VoiceManager initializing...");
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale polish = new Locale("pl", "PL");
            int result = tts.setLanguage(polish);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.getDefault());
                currentLanguage = "default";
            } else {
                currentLanguage = "pl-PL";
            }

            tts.setSpeechRate(0.9f);
            tts.setPitch(1.0f);

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    isSpeakingNow = true;
                    // Zatrzymujemy mikrofon gdy aplikacja mówi
                    pauseListeningTemporarily();
                    for (VoiceCallback cb : callbacks) cb.onTTSStarted();
                }

                @Override
                public void onDone(String utteranceId) {
                    isSpeakingNow = false;
                    // Wznawiamy słuchanie po zakończeniu mowy
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isListeningDesired) restartListening();
                    }, 500);
                    for (VoiceCallback cb : callbacks) cb.onTTSDone();
                }

                @Override
                public void onError(String utteranceId) {
                    isSpeakingNow = false;
                    if (isListeningDesired) restartListening();
                }
            });

            ttsReady = true;
            for (VoiceCallback cb : callbacks) cb.onTTSReady();
        }
    }

    private RecognitionListener createRecognitionListener() {
        return new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { Log.d(TAG, "Mic Ready"); }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                String msg = getSpeechErrorMessage(error);
                Log.e(TAG, "Speech Error: " + error + " (" + msg + ")");
                
                for (VoiceCallback cb : callbacks) cb.onSpeechError(error, msg);

                // Autorestart przy błędach (poza krytycznymi)
                if (isListeningDesired && !isSpeakingNow) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isListeningDesired) restartListening();
                    }, 1000);
                }
            }

            @Override
            public void onResults(Bundle results) {
                if (results != null) {
                    java.util.ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String text = matches.get(0);
                        Log.d(TAG, "Recognized: " + text);
                        for (VoiceCallback cb : callbacks) cb.onSpeechResult(text, true);
                    }
                }
                // KLUCZ: Restart po wyniku
                if (isListeningDesired && !isSpeakingNow) {
                    restartListening();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                java.util.ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    for (VoiceCallback cb : callbacks) cb.onSpeechResult(matches.get(0), false);
                }
            }

            @Override public void onEvent(int eventType, Bundle params) {}
        };
    }

    public void startListening() {
        isListeningDesired = true;
        restartListening();
    }

    private void restartListening() {
        if (!isListeningDesired || isSpeakingNow) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (speechRecognizer != null) {
                    speechRecognizer.destroy();
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext);
                } else {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext);
                }

                speechRecognizer.setRecognitionListener(createRecognitionListener());

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pl-PL");
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
                
                speechRecognizer.startListening(intent);
                Log.d(TAG, "Recognizer restarted");
            } catch (Exception e) {
                Log.e(TAG, "Restart failed: " + e.getMessage());
            }
        });
    }

    private void pauseListeningTemporarily() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (speechRecognizer != null) {
                speechRecognizer.cancel();
            }
        });
    }

    public void stopListening() {
        isListeningDesired = false;
        pauseListeningTemporarily();
    }

    public void speak(String text) {
        if (!ttsReady || !isTTSEnabled()) return;
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());
    }

    public void stopSpeech() {
        if (tts != null) tts.stop();
        isSpeakingNow = false;
    }

    private String getSpeechErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO: return "Błąd audio";
            case SpeechRecognizer.ERROR_CLIENT: return "Błąd klienta";
            case SpeechRecognizer.ERROR_NO_MATCH: return "Nie rozpoznano";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Zajęty";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "Cisza";
            default: return "Błąd " + error;
        }
    }

    public void setSpeechRate(float rate) {
        if (ttsReady && tts != null) {
            tts.setSpeechRate(rate);
        }
    }

    public void setTTSEnabled(boolean enabled) {
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_TTS_ENABLED, enabled)
                .apply();
    }

    public void setSpeechEnabled(boolean enabled) {
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_SPEECH_ENABLED, enabled)
                .apply();
        if (!enabled) {
            stopListening();
        } else {
            startListening();
        }
    }

    public boolean isSpeechEnabled() {
        return appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_SPEECH_ENABLED, true);
    }

    public void addCallback(VoiceCallback callback) { callbacks.add(callback); }
    public void removeCallback(VoiceCallback callback) { callbacks.remove(callback); }
    public boolean isTTSEnabled() { return appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TTS_ENABLED, true); }
}
