package com.example.fitnessapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FitnessAppPrefs";
    private static final String KEY_DEVICE_GUID = "deviceGuid";
    private static final String TAG = "SplashActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Opcjonalnie: setContentView(R.layout.activity_splash); 
        // ale na razie możemy wyświetlać domyślny biały ekran lub dodać prosty layout

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        checkUserAuth();
    }

    private void checkUserAuth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            signInAnonymously();
        } else {
            fetchUserData(currentUser.getUid());
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInAnonymously:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        initializeNewUser(user.getUid());
                    }
                } else {
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                    // Tutaj można dodać obsługę błędu, np. Toast lub ponowna próba
                }
            });
    }

    private String getOrCreateDeviceGuid() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String guid = prefs.getString(KEY_DEVICE_GUID, null);
        if (guid == null) {
            guid = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_DEVICE_GUID, guid).apply();
        }
        return guid;
    }

    private void initializeNewUser(String uid) {
        String deviceGuid = getOrCreateDeviceGuid();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("deviceGuid", deviceGuid);
        userMap.put("createdAt", System.currentTimeMillis());
        userMap.put("onboardingCompleted", false);
        // optional: name, ageRange, mobilityLevel, caregiverEnabled
        userMap.put("name", "");
        userMap.put("ageRange", "");
        userMap.put("mobilityLevel", "");
        userMap.put("caregiverEnabled", false);

        mFirestore.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener(aVoid -> {
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error initializing user", e);
            });
    }

    private void fetchUserData(String uid) {
        mFirestore.collection("users").document(uid).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Boolean onboardingCompleted = document.getBoolean("onboardingCompleted");
                        if (onboardingCompleted != null && onboardingCompleted) {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        } else {
                            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                        }
                    } else {
                        // Jeśli dokument nie istnieje, zainicjalizuj go
                        initializeNewUser(uid);
                        return;
                    }
                    finish();
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
    }
}
