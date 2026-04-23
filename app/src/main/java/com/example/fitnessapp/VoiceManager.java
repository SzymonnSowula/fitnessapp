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

    // Retry mechanism for speech recognition
    private static final int MAX_RETRIES = 3;
    private int currentRetryCount = 0;
    private static final long RETRY_DELAY_MS = 1500;

    public interface VoiceCallback {
        void onSpeechResult(String text, boolean isFinal);
        void onSpeechError(int errorCode, String errorMessage);
        void onTTSReady();
        void onTTSStarted();
        void onTTSDone();
        void onListeningStarted();
        void onListeningStopped();
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
                Log.w(TAG, "Polish TTS not available, using default language");
            } else {
                currentLanguage = "pl-PL";
            }

            // Slightly slower speech rate for seniors
            tts.setSpeechRate(0.85f);
            tts.setPitch(1.0f);

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    isSpeakingNow = true;
                    pauseListeningTemporarily();
                    for (VoiceCallback cb : callbacks) cb.onTTSStarted();
                }

                @Override
                public void onDone(String utteranceId) {
                    isSpeakingNow = false;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isListeningDesired) restartListening();
                    }, 600);
                    for (VoiceCallback cb : callbacks) cb.onTTSDone();
                }

                @Override
                public void onError(String utteranceId) {
                    isSpeakingNow = false;
                    if (isListeningDesired) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            restartListening();
                        }, RETRY_DELAY_MS);
                    }
                }

                @Override
                public void onError(String utteranceId, int errorCode) {
                    // Handle TTS error - required for older API
                    isSpeakingNow = false;
                    Log.e(TAG, "TTS Error: " + errorCode);
                    if (isListeningDesired) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            restartListening();
                        }, RETRY_DELAY_MS);
                    }
                }
            });

            ttsReady = true;
            for (VoiceCallback cb : callbacks) cb.onTTSReady();
            Log.d(TAG, "TTS initialized successfully with language: " + currentLanguage);
        } else {
            Log.e(TAG, "TTS initialization failed with status: " + status);
        }
    }

    private RecognitionListener createRecognitionListener() {
        return new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Mic Ready for speech");
                currentRetryCount = 0;
                for (VoiceCallback cb : callbacks) cb.onListeningStarted();
            }

            @Override public void onBeginningOfSpeech() {}

            @Override public void onRmsChanged(float rmsdB) {}

            @Override public void onBufferReceived(byte[] buffer) {}

            @Override public void onEndOfSpeech() {
                for (VoiceCallback cb : callbacks) cb.onListeningStopped();
            }

            @Override
            public void onError(int error) {
                String msg = getSpeechErrorMessage(error);
                Log.e(TAG, "Speech Error: " + error + " (" + msg + ")");

                for (VoiceCallback cb : callbacks) cb.onSpeechError(error, msg);

                // Don't restart on critical errors
                if (!isListeningDesired) return;

                // Handle specific errors differently
                if (error == SpeechRecognizer.ERROR_NO_MATCH ||
                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    // These are normal - just restart listening
                    if (!isSpeakingNow) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListeningDesired) restartListening();
                        }, 800);
                    }
                } else if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    // Wait longer before retry
                    if (!isSpeakingNow && currentRetryCount < MAX_RETRIES) {
                        currentRetryCount++;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListeningDesired) restartListening();
                        }, RETRY_DELAY_MS);
                    }
                } else {
                    // Other errors - retry with delay
                    if (!isSpeakingNow) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListeningDesired) restartListening();
                        }, RETRY_DELAY_MS);
                    }
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
                currentRetryCount = 0;
                if (isListeningDesired && !isSpeakingNow) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isListeningDesired) restartListening();
                    }, 300);
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

    public void restartListening() {
        if (!isListeningDesired || isSpeakingNow) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (speechRecognizer != null) {
                    try {
                        speechRecognizer.destroy();
                    } catch (Exception e) {
                        Log.w(TAG, "Error destroying recognizer: " + e.getMessage());
                    }
                    speechRecognizer = null;
                }

                if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                    Log.e(TAG, "Speech recognition not available on this device");
                    return;
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext);
                } else {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext);
                }

                if (speechRecognizer == null) {
                    Log.e(TAG, "Failed to create speech recognizer");
                    return;
                }

                speechRecognizer.setRecognitionListener(createRecognitionListener());

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pl-PL");
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

                speechRecognizer.startListening(intent);
                Log.d(TAG, "Recognizer started, waiting for speech...");
            } catch (Exception e) {
                Log.e(TAG, "Failed to start recognizer: " + e.getMessage(), e);
                if (isListeningDesired && !isSpeakingNow) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isListeningDesired) restartListening();
                    }, RETRY_DELAY_MS);
                }
            }
        });
    }

    private void pauseListeningTemporarily() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (speechRecognizer != null) {
                try {
                    speechRecognizer.cancel();
                } catch (Exception e) {
                    Log.w(TAG, "Error canceling recognizer: " + e.getMessage());
                }
            }
        });
    }

    public void stopListening() {
        isListeningDesired = false;
        pauseListeningTemporarily();
        for (VoiceCallback cb : callbacks) cb.onListeningStopped();
    }

    public void speak(String text) {
        if (!ttsReady || !isTTSEnabled()) {
            Log.w(TAG, "TTS not ready or disabled, skipping: " + text);
            return;
        }
        try {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());
            Log.d(TAG, "Speaking: " + text);
        } catch (Exception e) {
            Log.e(TAG, "Error speaking: " + e.getMessage());
        }
    }

    public void speakLongText(String text) {
        if (!ttsReady || !isTTSEnabled()) return;
        // For longer text, add a short delay before speaking
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());
            } catch (Exception e) {
                Log.e(TAG, "Error speaking long text: " + e.getMessage());
            }
        }, 300);
    }

    public void stopSpeech() {
        if (tts != null) {
            try {
                tts.stop();
            } catch (Exception e) {
                Log.w(TAG, "Error stopping speech: " + e.getMessage());
            }
        }
        isSpeakingNow = false;
    }

    public boolean isSpeaking() {
        return isSpeakingNow;
    }

    public boolean isListening() {
        return isListeningDesired && speechRecognizer != null;
    }

    private String getSpeechErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO: return "Błąd nagrywania audio";
            case SpeechRecognizer.ERROR_CLIENT: return "Błąd aplikacji";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Brak uprawnień do mikrofonu";
            case SpeechRecognizer.ERROR_NETWORK: return "Błąd połączenia internetowego";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Przekroczono czas połączenia";
            case SpeechRecognizer.ERROR_NO_MATCH: return "Nie rozpoznano mowy. Spróbuj ponownie.";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "System jest zajęty";
            case SpeechRecognizer.ERROR_SERVER: return "Błąd serwera";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "Cisza. Mów głośniej.";
            default: return "Błąd rozpoznawania mowy";
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

    public void cleanup() {
        stopListening();
        stopSpeech();
        if (tts != null) {
            try {
                tts.shutdown();
            } catch (Exception e) {
                Log.w(TAG, "Error shutting down TTS: " + e.getMessage());
            }
        }
        if (speechRecognizer != null) {
            try {
                speechRecognizer.destroy();
            } catch (Exception e) {
                Log.w(TAG, "Error destroying recognizer: " + e.getMessage());
            }
        }
    }
}