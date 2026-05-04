package com.example.fitnessapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FitnessAppPrefs";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private Runnable splashRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Inicjalizacja systemu głosowego
        VoiceManager.getInstance().init(this);

        // Sprawdzenie lokalnych preferencji – rejestracja jest tylko lokalna
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);

        splashRunnable = () -> {
            if (onboardingCompleted) {
                startActivity(new Intent(SplashActivity.this, ChoiceActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
            }
            finish();
        };
        splashHandler.postDelayed(splashRunnable, 1200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashRunnable != null) {
            splashHandler.removeCallbacks(splashRunnable);
        }
        VoiceManager.getInstance().cleanup();
    }
}
