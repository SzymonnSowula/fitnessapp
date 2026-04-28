package com.example.fitnessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
    private static final String KEY_QUIET_MODE = "quiet_mode";

    private static VoiceManager instance;
    private static volatile boolean isInitialized = false;

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Context appContext;
    private boolean ttsReady = false;
    private boolean isListeningDesired = false;
    private boolean isSpeakingNow = false;
    private boolean isInQuietMode = false;
    private boolean isManualMode = true; // Manual mode by default - tap to listen

    private final CopyOnWriteArraySet<VoiceCallback> callbacks = new CopyOnWriteArraySet<>();
    private String currentLanguage = "pl-PL";

    // Retry mechanism for speech recognition
    private static final int MAX_RETRIES = 3;
    private int currentRetryCount = 0;
    private static final long RETRY_DELAY_MS = 2000;
    private static final long QUIET_RESTART_DELAY_MS = 3000;
    private static final long NORMAL_RESTART_DELAY_MS = 1000;

    // Restart delay adjusted based on results
    private long lastRestartDelay = NORMAL_RESTART_DELAY_MS;

    // Error debounce - prevents constant restart on repeated errors
    private static final int MAX_CONSECUTIVE_ERRORS = 3;
    private int consecutiveErrorCount = 0;
    private boolean isInErrorCooldown = false;
    private static final long ERROR_COOLDOWN_MS = 5000;

    // Tone generator for listening feedback
    private ToneGenerator toneGenerator;

    // Pending TTS message to speak after init
    private String pendingSpeak = null;

    public interface VoiceCallback {
        void onSpeechResult(String text, boolean isFinal);
        void onSpeechError(int errorCode, String errorMessage);
        void onTTSReady();
        void onTTSStarted();
        void onTTSDone();
        void onListeningStarted();
        void onListeningStopped();
        default void onListeningStateChanged(boolean isListening) {} // For visual feedback
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

        // Initialize tone generator for listening feedback
        try {
            this.toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50);
        } catch (Exception e) {
            Log.w(TAG, "Could not create ToneGenerator: " + e.getMessage());
        }

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

            // Slower speech rate for seniors with slightly higher pitch for clarity
            tts.setSpeechRate(0.8f);
            tts.setPitch(1.1f);

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
                    // Only restart listening if in auto mode and desired
                    if (isListeningDesired && !isManualMode) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListeningDesired) restartListening();
                        }, getRestartDelay());
                    }
                    for (VoiceCallback cb : callbacks) cb.onTTSDone();
                }

                @Override
                public void onError(String utteranceId) {
                    isSpeakingNow = false;
                    if (isListeningDesired && !isManualMode) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            restartListening();
                        }, RETRY_DELAY_MS);
                    }
                }

                @Override
                public void onError(String utteranceId, int errorCode) {
                    isSpeakingNow = false;
                    Log.e(TAG, "TTS Error: " + errorCode);
                    if (isListeningDesired && !isManualMode) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            restartListening();
                        }, RETRY_DELAY_MS);
                    }
                }
            });

            ttsReady = true;
            for (VoiceCallback cb : callbacks) cb.onTTSReady();

            // Speak pending message if any
            if (pendingSpeak != null) {
                speak(pendingSpeak);
                pendingSpeak = null;
            } else {
                speak("Asystent głosowy jest gotowy. Dotknij ekranu i mów, lub powiedz hej aby aktywować nasłuchiwanie.");
            }

            Log.d(TAG, "TTS initialized successfully with language: " + currentLanguage);
        } else {
            Log.e(TAG, "TTS initialization failed with status: " + status);
        }
    }

    private long getRestartDelay() {
        return isInQuietMode ? QUIET_RESTART_DELAY_MS : NORMAL_RESTART_DELAY_MS;
    }

    private void playListeningBeep() {
        if (isInQuietMode) return;
        try {
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error playing tone: " + e.getMessage());
        }
    }

    private void playSuccessBeep() {
        try {
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 80);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error playing success tone: " + e.getMessage());
        }
    }

    private RecognitionListener createRecognitionListener() {
        return new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Mic Ready for speech");
                currentRetryCount = 0;
                playListeningBeep();
                consecutiveErrorCount = 0; // Reset error count on successful ready
                for (VoiceCallback cb : callbacks) {
                    cb.onListeningStarted();
                    cb.onListeningStateChanged(true);
                }
            }

            @Override public void onBeginningOfSpeech() {}

            @Override public void onRmsChanged(float rmsdB) {}

            @Override public void onBufferReceived(byte[] buffer) {}

            @Override public void onEndOfSpeech() {
                for (VoiceCallback cb : callbacks) {
                    cb.onListeningStopped();
                    cb.onListeningStateChanged(false);
                }
            }

            @Override
            public void onError(int error) {
                String msg = getSpeechErrorMessage(error);
                Log.e(TAG, "Speech Error: " + error + " (" + msg + ")");

                for (VoiceCallback cb : callbacks) cb.onSpeechError(error, msg);

                // Don't restart on critical errors or if not desired
                if (!isListeningDesired) return;

                // Increment consecutive error counter
                consecutiveErrorCount++;

                // Check if we should enter error cooldown (debounce)
                if (consecutiveErrorCount >= MAX_CONSECUTIVE_ERRORS) {
                    isInErrorCooldown = true;
                    Log.d(TAG, "Entering error cooldown after " + consecutiveErrorCount + " errors");
                    // Notify callbacks about error cooldown
                    for (VoiceCallback cb : callbacks) cb.onListeningStateChanged(false);
                    // Announce cooldown to user with TTS
                    speak("Chwilowa przerwa. Spróbuj ponownie za chwilę.");
                    // Schedule exit from cooldown
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isInErrorCooldown = false;
                        consecutiveErrorCount = 0;
                        Log.d(TAG, "Error cooldown ended");
                    }, ERROR_COOLDOWN_MS);
                    return;
                }

                // Normal restart logic for first few errors
                if (error == SpeechRecognizer.ERROR_NO_MATCH ||
                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    // Normal silence - restart after delay
                    if (!isSpeakingNow && !isInErrorCooldown) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListeningDesired && !isInErrorCooldown) restartListening();
                        }, isInQuietMode ? QUIET_RESTART_DELAY_MS : 800);
                    }
                } else if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    if (!isSpeakingNow && currentRetryCount < MAX_RETRIES) {
                        currentRetryCount++;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListeningDesired && !isInErrorCooldown) restartListening();
                        }, RETRY_DELAY_MS);
                    }
                } else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    speak("Brak uprawnień do mikrofonu. Sprawdź ustawienia aplikacji.");
                } else {
                    if (!isSpeakingNow && !isInErrorCooldown) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListeningDesired && !isInErrorCooldown) restartListening();
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

                        // Check for wake word - must be at start of text for activation
                        String lower = text.toLowerCase().trim();
                        boolean isWakeWord = lower.startsWith("hej") || lower.startsWith("hey") || lower.startsWith("hei");

                        if (isWakeWord) {
                            playSuccessBeep();
                            speak("Słucham. Co mogę dla Ciebie zrobić?");
                            // Don't auto-restart here - TTS will trigger restart via onDone callback
                        } else {
                            playSuccessBeep();
                            for (VoiceCallback cb : callbacks) cb.onSpeechResult(text, true);
                            // After processing a command, restart listening for next command
                            if (isListeningDesired && !isSpeakingNow && !isInErrorCooldown) {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    if (isListeningDesired && !isInErrorCooldown) restartListening();
                                }, 1000);
                            }
                        }
                    } else {
                        // No matches - restart
                        if (isListeningDesired && !isSpeakingNow && !isInErrorCooldown) {
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                if (isListeningDesired && !isInErrorCooldown) restartListening();
                            }, 300);
                        }
                    }
                } else {
                    // Null results - restart
                    if (isListeningDesired && !isSpeakingNow && !isInErrorCooldown) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListeningDesired && !isInErrorCooldown) restartListening();
                        }, 300);
                    }
                }
                currentRetryCount = 0;
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                java.util.ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    // Just pass partial results to callbacks - don't speak for wake word here
                    // Wake word detection happens in onResults for final confirmation
                    for (VoiceCallback cb : callbacks) cb.onSpeechResult(text, false);
                }
            }

            @Override public void onEvent(int eventType, Bundle params) {}
        };
    }

    /**
     * Start listening for speech. In manual mode this is triggered by user tap.
     * In auto mode this runs continuously.
     */
    public void startListening() {
        isListeningDesired = true;
        restartListening();
    }

    /**
     * Restart the speech recognizer. Creates new instance if needed.
     */
    public void restartListening() {
        if (!isListeningDesired || isSpeakingNow || isInErrorCooldown) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                // Check if recognizer exists
                boolean needsNew = (speechRecognizer == null);

                if (needsNew) {
                    if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                        Log.e(TAG, "Speech recognition not available on this device");
                        speak("Rozpoznawanie mowy niedostępne na tym urządzeniu.");
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
                }

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pl-PL");
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

                speechRecognizer.startListening(intent);
                Log.d(TAG, "Recognizer started, waiting for speech...");
            } catch (Exception e) {
                Log.e(TAG, "Failed to start recognizer: " + e.getMessage(), e);
                destroyRecognizer();
                if (isListeningDesired && !isSpeakingNow) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isListeningDesired) restartListening();
                    }, RETRY_DELAY_MS);
                }
            }
        });
    }

    private void destroyRecognizer() {
        if (speechRecognizer != null) {
            try {
                speechRecognizer.destroy();
            } catch (Exception e) {
                Log.w(TAG, "Error destroying recognizer: " + e.getMessage());
            }
            speechRecognizer = null;
        }
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
        isInErrorCooldown = false;
        consecutiveErrorCount = 0;
        pauseListeningTemporarily();
        destroyRecognizer();
        for (VoiceCallback cb : callbacks) cb.onListeningStopped();
    }

    public void speak(String text) {
        if (!ttsReady) {
            Log.w(TAG, "TTS not ready, queuing: " + text);
            pendingSpeak = text;
            return;
        }
        if (!isTTSEnabled()) {
            Log.w(TAG, "TTS disabled, skipping: " + text);
            return;
        }
        isSpeakingNow = true;
        pauseListeningTemporarily();
        try {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());
            Log.d(TAG, "Speaking: " + text);
        } catch (Exception e) {
            Log.e(TAG, "Error speaking: " + e.getMessage());
            isSpeakingNow = false;
        }
    }

    public void speakLongText(String text) {
        if (!ttsReady || !isTTSEnabled()) return;
        isSpeakingNow = true;
        pauseListeningTemporarily();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());
            } catch (Exception e) {
                Log.e(TAG, "Error speaking long text: " + e.getMessage());
                isSpeakingNow = false;
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
        return isListeningDesired;
    }

    public boolean isInQuietMode() {
        return isInQuietMode;
    }

    public void setQuietMode(boolean enabled) {
        isInQuietMode = enabled;
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_QUIET_MODE, enabled)
            .apply();
    }

    public void setManualMode(boolean manual) {
        isManualMode = manual;
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
        destroyRecognizer();
        if (tts != null) {
            try {
                tts.shutdown();
            } catch (Exception e) {
                Log.w(TAG, "Error shutting down TTS: " + e.getMessage());
            }
        }
        if (toneGenerator != null) {
            try {
                toneGenerator.release();
            } catch (Exception e) {
                Log.w(TAG, "Error releasing tone generator: " + e.getMessage());
            }
        }
    }
}
