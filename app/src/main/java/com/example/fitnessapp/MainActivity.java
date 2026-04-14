package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // 1. Deklaracja zmiennej autoryzacji
    private FirebaseAuth mAuth;
    private static final String PREFS_NAME = "FitnessAppPrefs";
    private static final String KEY_USER_NAME = "user_name";

    private ModelRunner modelRunner;
    private List<Exercise> exerciseDatabase;

    private TextView tvRecommendationTitle;
    private CardView cardRecommendation;
    private TextView tvRecommendedCategory;
    private TextView tvRecommendedExercises;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Inicjalizacja Firebase (musi być przed jakimkolwiek użyciem mAuth!)
        mAuth = FirebaseAuth.getInstance();

        // Spersonalizowane powitanie
        TextView tvWelcome = findViewById(R.id.tv_welcome_message);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(KEY_USER_NAME, "");
        if (!name.isEmpty()) {
            tvWelcome.setText(getString(R.string.welcome_personalized, name) + "\nJak się dzisiaj czujesz?");
        }

        // Obsługa czatu
        EditText etChatInput = findViewById(R.id.et_chat_input);
        // W przyszłości tutaj dodamy obsługę wysyłania wiadomości do AI

        // Inicjalizacja ModelRunner
        modelRunner = new ModelRunner();
        try {
            modelRunner.init(this);
            loadExerciseDatabase();
        } catch (Exception e) {
            Log.e(TAG, "Błąd inicjalizacji modelu", e);
            Toast.makeText(this, "Błąd ładowania modelu rekomendacji", Toast.LENGTH_SHORT).show();
        }

        // UI dla rekomendacji
        tvRecommendationTitle = findViewById(R.id.tv_recommendation_title);
        cardRecommendation = findViewById(R.id.card_recommendation);
        tvRecommendedCategory = findViewById(R.id.tv_recommended_category);
        tvRecommendedExercises = findViewById(R.id.tv_recommended_exercises);

        // Obsługa nastrojów
        findViewById(R.id.card_mood_happy).setOnClickListener(v -> generateRecommendation(1.0f, 0.8f, 0.8f));
        findViewById(R.id.card_mood_sad).setOnClickListener(v -> generateRecommendation(0.5f, 0.4f, 0.3f));
        findViewById(R.id.card_mood_very_sad).setOnClickListener(v -> generateRecommendation(0.2f, 0.2f, 0.1f));

        // Nawigacja dolna
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_start);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_start) {
                return true;
            } else if (itemId == R.id.nav_assistant) {
                Toast.makeText(this, "Asystent (Wkrótce)", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                // Nie używamy tu finish(), aby użytkownik mógł wrócić do MainActivity
                return true;
            }
            return false;
        });

        // Wylogowanie - teraz przekierowuje do SplashActivity (która zainicjuje nowe anonimowe konto)
        Button btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                // Czyścimy lokalny onboarding przy wylogowaniu
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                finishAffinity();
            });
        }
    }

    private void loadExerciseDatabase() {
        exerciseDatabase = new ArrayList<>();
        // Przykładowe dane (w realnej apce wczytywane z CSV/Bazy)
        // Cechy: sila, elastycznosc, kardio, postawa, intensywnosc, trudnosc, krzeslo, lozko, siedzac, stania, podlogi, zrodlo
        
        Exercise e1 = new Exercise();
        e1.name = "Spacer w miejscu";
        e1.wplywNaKardioNum = 0.8f; e1.intensywnoscNum = 0.4f; e1.poziomTrudnosciNum = 0.2f; e1.wymagaStaniaBin = 1f; e1.category = "kardio";
        exerciseDatabase.add(e1);

        Exercise e2 = new Exercise();
        e2.name = "Przysiady przy krześle";
        e2.wplywNaSileNum = 0.9f; e2.intensywnoscNum = 0.6f; e2.poziomTrudnosciNum = 0.4f; e2.wspomaganeKrzeslemBin = 1f; e2.wymagaStaniaBin = 1f; e2.category = "sila";
        exerciseDatabase.add(e2);

        Exercise e3 = new Exercise();
        e3.name = "Rozciąganie w łóżku";
        e3.wplywNaElastycznoscNum = 0.7f; e3.intensywnoscNum = 0.2f; e3.poziomTrudnosciNum = 0.1f; e3.moznaWLozkuBin = 1f; e3.category = "mobilnosc";
        exerciseDatabase.add(e3);

        Exercise e4 = new Exercise();
        e4.name = "Pajacyki (wolne)";
        e4.wplywNaKardioNum = 0.9f; e4.intensywnoscNum = 0.7f; e4.poziomTrudnosciNum = 0.5f; e4.wymagaStaniaBin = 1f; e4.category = "kardio";
        exerciseDatabase.add(e4);

        Exercise e5 = new Exercise();
        e5.name = "Unoszenie nóg siedząc";
        e5.wplywNaSileNum = 0.6f; e5.intensywnoscNum = 0.3f; e5.poziomTrudnosciNum = 0.2f; e5.moznaSiedzacBin = 1f; e5.category = "sila";
        exerciseDatabase.add(e5);
        
        Exercise e6 = new Exercise();
        e6.name = "Głębokie oddychanie";
        e6.wplywNaPostaweNum = 0.8f; e6.intensywnoscNum = 0.1f; e6.poziomTrudnosciNum = 0.1f; e6.moznaSiedzacBin = 1f; e6.category = "postura";
        exerciseDatabase.add(e6);
    }

    private void generateRecommendation(float energyLevel, float intensityPref, float difficultyPref) {
        if (modelRunner == null) {
            Log.e(TAG, "modelRunner is null");
            return;
        }

        try {
            // Symulujemy cechy użytkownika/kontekstu (12 cech zgodnie z metadata.json)
            float[] moodFeatures = new float[12];
            // 4. "intensywnosc_num"
            // 5. "poziom_trudnosci_num"
            // Indeksy od 0 do 11.
            moodFeatures[4] = energyLevel; // intensywnosc
            moodFeatures[5] = difficultyPref; // trudnosc
            
            Log.d(TAG, "Wykonuję predict dla cech: energy=" + energyLevel + ", intensity=" + intensityPref);
            float[] probs = modelRunner.predict(moodFeatures);
            
            if (probs == null || probs.length == 0) {
                Log.e(TAG, "Prawdopodobieństwa są puste");
                return;
            }

            List<String> classes = modelRunner.getClasses();
            
            int maxIdx = 0;
            for (int i = 1; i < probs.length; i++) {
                if (probs[i] > probs[maxIdx]) maxIdx = i;
            }
            
            if (classes != null && maxIdx < classes.size()) {
                String recommendedCategory = classes.get(maxIdx);
                Log.d(TAG, "Rekomendowana kategoria: " + recommendedCategory);
                displayRecommendation(recommendedCategory);
            } else {
                Log.e(TAG, "Błąd indeksowania klas: maxIdx=" + maxIdx + ", classes size=" + (classes != null ? classes.size() : "null"));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Błąd podczas predict", e);
            Toast.makeText(this, "Błąd generowania rekomendacji: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void displayRecommendation(String category) {
        tvRecommendationTitle.setVisibility(View.VISIBLE);
        cardRecommendation.setVisibility(View.VISIBLE);
        
        String categoryDisplay = category.substring(0, 1).toUpperCase() + category.substring(1);
        tvRecommendedCategory.setText("Kategoria: " + categoryDisplay);
        
        StringBuilder exercisesText = new StringBuilder();
        int count = 0;
        for (Exercise e : exerciseDatabase) {
            if (e.category != null && e.category.equalsIgnoreCase(category)) {
                exercisesText.append("• ").append(e.name).append("\n");
                count++;
            }
            if (count >= 3) break;
        }
        
        if (exercisesText.length() == 0) {
            tvRecommendedExercises.setText("Dzisiaj postaw na relaks i lekkie rozciąganie.");
        } else {
            tvRecommendedExercises.setText(exercisesText.toString().trim());
        }
        
        cardRecommendation.getParent().requestChildFocus(cardRecommendation, cardRecommendation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (modelRunner != null) {
            try {
                modelRunner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Jeśli nikt nie jest zalogowany - odsyłamy do SplashActivity
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }
    }
}