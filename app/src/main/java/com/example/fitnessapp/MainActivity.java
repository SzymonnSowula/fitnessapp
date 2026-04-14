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
    private AppDatabase db;

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

        // Inicjalizacja ModelRunner i Bazy
        modelRunner = new ModelRunner();
        db = AppDatabase.getDatabase(this);
        try {
            modelRunner.init(this);
            new Thread(this::loadExerciseDatabase).start();
        } catch (Exception e) {
            Log.e(TAG, "Błąd inicjalizacji", e);
            Toast.makeText(this, "Błąd ładowania systemu rekomendacji", Toast.LENGTH_SHORT).show();
        }

        // UI dla rekomendacji
        tvRecommendationTitle = findViewById(R.id.tv_recommendation_title);
        cardRecommendation = findViewById(R.id.card_recommendation);
        tvRecommendedCategory = findViewById(R.id.tv_recommended_category);
        tvRecommendedExercises = findViewById(R.id.tv_recommended_exercises);

        // Obsługa nastrojów
        findViewById(R.id.card_mood_happy).setOnClickListener(v -> generateRecommendation(0.8f, 0.8f));
        findViewById(R.id.card_mood_sad).setOnClickListener(v -> generateRecommendation(0.4f, 0.4f));
        findViewById(R.id.card_mood_very_sad).setOnClickListener(v -> generateRecommendation(0.2f, 0.2f));

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
        try {
            int count = db.exerciseDao().getCount();
            Log.d(TAG, "Aktualna liczba ćwiczeń w bazie: " + count);

            // Zawsze odświeżamy bazę dla pewności (debug)
            Log.d(TAG, "Odświeżam bazę ćwiczeń z CSV...");
            db.exerciseDao().deleteAll();
            List<Exercise> exercises = CsvImporter.loadExercisesFromCsv(this);
            if (!exercises.isEmpty()) {
                db.exerciseDao().insertAll(exercises);
                int newCount = db.exerciseDao().getCount();
                Log.d(TAG, "Zaimportowano " + exercises.size() + " ćwiczeń. Nowy stan bazy: " + newCount);
                runOnUiThread(() -> Toast.makeText(this, "Baza ćwiczeń gotowa (" + newCount + ")", Toast.LENGTH_SHORT).show());
            } else {
                Log.e(TAG, "Nie zaimportowano żadnych ćwiczeń!");
                runOnUiThread(() -> Toast.makeText(this, "Błąd importu bazy ćwiczeń!", Toast.LENGTH_LONG).show());
            }
        } catch (Exception e) {
            Log.e(TAG, "Błąd bazy danych", e);
        }
    }

    private void generateRecommendation(float energyLevel, float difficultyPref) {
        if (modelRunner == null) {
            Log.e(TAG, "modelRunner is null");
            Toast.makeText(this, "Model nie jest gotowy", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Toast.makeText(this, "Generuję...", Toast.LENGTH_SHORT).show();
            
            // 12 cech zgodnie z metadata.json / rekomendator.py
            // FEATURES = ["wplyw_na_sile_num", "wplyw_na_elastycznosc_num", "wplyw_na_kardio_num",
            //             "wplyw_na_postawe_num", "intensywnosc_num", "poziom_trudnosci_num",
            //             "wspomagane_krzeslem_bin", "mozna_w_lozku_bin", "mozna_siedzac_bin",
            //             "wymaga_stania_bin", "wymaga_podlogi_bin", "zrodlo_enc"]
            float[] moodFeatures = new float[12];
            
            // Mapowanie nastroju na cechy wejściowe modelu
            // Seniorzy: 1=bardzo niska/łatwy, 2=niska/średni, 3=wysoka/trudny
            float intens = energyLevel < 0.4f ? 1.0f : 2.0f;
            float diff = difficultyPref < 0.4f ? 1.0f : 2.0f;

            moodFeatures[0] = 2.0f; // wplyw_na_sile
            moodFeatures[1] = 2.0f; // wplyw_na_elastycznosc
            moodFeatures[2] = 2.0f; // wplyw_na_kardio
            moodFeatures[3] = 2.0f; // wplyw_na_postawe
            moodFeatures[4] = intens; // intensywnosc_num
            moodFeatures[5] = diff;   // poziom_trudnosci_num
            
            moodFeatures[6] = 0.0f; // wspomagane_krzeslem_bin
            moodFeatures[7] = 0.0f; // mozna_w_lozku_bin
            moodFeatures[8] = 1.0f; // mozna_siedzac_bin
            moodFeatures[9] = 1.0f; // wymaga_stania_bin
            moodFeatures[10] = 0.0f; // wymaga_podlogi_bin
            moodFeatures[11] = 0.0f; // zrodlo_enc
            
            new Thread(() -> {
                try {
                    Log.d(TAG, "Wykonuję predict dla cech: " + java.util.Arrays.toString(moodFeatures));
                    float[] probs = modelRunner.predict(moodFeatures);

                    if (probs == null || probs.length == 0) {
                        Log.e(TAG, "Prawdopodobieństwa są puste");
                        runOnUiThread(() -> Toast.makeText(this, "Model nie zwrócił wyników", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    List<String> classes = modelRunner.getClasses();
                    int maxIdx = 0;
                    for (int i = 1; i < probs.length; i++) {
                        if (probs[i] > probs[maxIdx]) maxIdx = i;
                    }

                    String recommendedCategory = (classes != null && maxIdx < classes.size()) 
                        ? classes.get(maxIdx) 
                        : "mieszana";

                    Log.d(TAG, "Rekomendowana kategoria: " + recommendedCategory);
                    
                    // Pobranie ćwiczeń z bazy w wątku tła
                    List<Exercise> exercises = db.exerciseDao().getByCategory(recommendedCategory);
                    Log.d(TAG, "Znaleziono " + exercises.size() + " ćwiczeń dla: " + recommendedCategory);

                    runOnUiThread(() -> displayRecommendation(recommendedCategory, exercises));

                } catch (Exception e) {
                    Log.e(TAG, "Błąd w wątku rekomendacji", e);
                    runOnUiThread(() -> Toast.makeText(this, "Błąd: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
            
        } catch (Exception e) {
            Log.e(TAG, "Błąd podczas predict", e);
            Toast.makeText(this, "Błąd: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void displayRecommendation(String category, List<Exercise> exercises) {
        tvRecommendationTitle.setVisibility(View.VISIBLE);
        cardRecommendation.setVisibility(View.VISIBLE);
        
        String categoryDisplay = category.substring(0, 1).toUpperCase() + category.substring(1);
        tvRecommendedCategory.setText("Kategoria: " + categoryDisplay);
        
        StringBuilder exercisesText = new StringBuilder();
        int count = 0;
        for (Exercise e : exercises) {
            exercisesText.append("• ").append(e.name).append("\n");
            count++;
            if (count >= 5) break;
        }
        
        if (exercisesText.length() == 0) {
            tvRecommendedExercises.setText("Nie znaleziono ćwiczeń w kategorii: " + categoryDisplay + ". Dzisiaj postaw na relaks.");
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