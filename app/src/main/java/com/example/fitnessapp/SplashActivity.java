package com.example.fitnessapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import android.widget.Toast;

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
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String TAG = "SplashActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Dodatkowe sprawdzenie SharedPreferences dla onboardingu (wymóg lokalnego zapisu)
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean onboardingCompletedLocal = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
        
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Jeśli onboarding lokalny jest ukończony, idziemy do MainActivity (chyba że użytkownik nie jest zalogowany)
        if (onboardingCompletedLocal && mAuth.getCurrentUser() != null) {
            startActivity(new Intent(SplashActivity.this, ChoiceActivity.class));
            finish();
            return;
        }

        // Dodajemy logowanie stanu na wypadek gdyby Firebase "wisiał"
        Log.d(TAG, "onCreate: Initializing Firebase Auth...");
        checkUserAuth();
    }

    private void checkUserAuth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "checkUserAuth: User is null, signing in anonymously...");
            signInAnonymously();
        } else {
            Log.d(TAG, "checkUserAuth: User exists, fetching data: " + currentUser.getUid());
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
                    String errorMessage = "Błąd autoryzacji: " + (task.getException() != null ? task.getException().getMessage() : "Nieznany błąd");
                    Toast.makeText(SplashActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    // Dodajmy informację dla użytkownika, że musi skonfigurować Firebase
                    if (task.getException() != null && task.getException().getMessage() != null 
                        && (task.getException().getMessage().contains("configuration") || task.getException().getMessage().contains("google-services.json"))) {
                        Toast.makeText(SplashActivity.this, "UWAGA: Brakuje pliku google-services.json w folderze app/ lub projekt Firebase nie jest skonfigurowany.", Toast.LENGTH_LONG).show();
                    }
                    if (task.getException() != null && task.getException().getMessage() != null 
                        && task.getException().getMessage().contains("anonymous auth")) {
                        Toast.makeText(SplashActivity.this, "Włącz 'Logowanie anonimowe' w Firebase Console -> Authentication -> Sign-in method.", Toast.LENGTH_LONG).show();
                    }
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
                Log.d(TAG, "initializeNewUser: Success");
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "initializeNewUser: failure", e);
                String errorMsg = "Błąd inicjalizacji Firestore: " + e.getMessage();
                Toast.makeText(SplashActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                
                if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                    Toast.makeText(SplashActivity.this, "Sprawdź reguły (Rules) w Cloud Firestore: zezwól na odczyt/zapis.", Toast.LENGTH_LONG).show();
                }
            });
    }

    private void fetchUserData(String uid) {
        Log.d(TAG, "fetchUserData: Fetching data for UID: " + uid);
        mFirestore.collection("users").document(uid).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "fetchUserData: Document exists");
                        
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        boolean onboardingCompletedLocal = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
                        Boolean onboardingCompletedRemote = document.getBoolean("onboardingCompleted");
                        
                        if (onboardingCompletedLocal || (onboardingCompletedRemote != null && onboardingCompletedRemote)) {
                            Log.d(TAG, "fetchUserData: Navigating to MainActivity");
                            startActivity(new Intent(SplashActivity.this, ChoiceActivity.class));
                        } else {
                            Log.d(TAG, "fetchUserData: Navigating to OnboardingActivity");
                            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                        }
                    } else {
                        Log.d(TAG, "fetchUserData: Document does not exist, initializing...");
                        // Jeśli dokument nie istnieje, zainicjalizuj go
                        initializeNewUser(uid);
                        return;
                    }
                    finish();
                } else {
                    Log.w(TAG, "fetchUserData: failure", task.getException());
                    String errorMsg = "Błąd pobierania danych z Firestore: " + (task.getException() != null ? task.getException().getMessage() : "Nieznany");
                    Toast.makeText(SplashActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    
                    if (task.getException() != null && task.getException().getMessage() != null 
                        && task.getException().getMessage().contains("PERMISSION_DENIED")) {
                        Toast.makeText(SplashActivity.this, "BŁĄD UPRAWNIEŃ: Przejdź do Firebase Console -> Firestore -> Rules i ustaw: allow read, write: if true; (lub skonfiguruj dostęp dla zalogowanych).", Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
}
