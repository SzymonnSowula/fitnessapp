package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // 1. Deklaracja zmiennej autoryzacji
    private FirebaseAuth mAuth;
    private static final String PREFS_NAME = "FitnessAppPrefs";
    private static final String KEY_USER_NAME = "user_name";

    private ModelRunner modelRunner;
    private AppDatabase db;
    private volatile boolean dbReady = false;

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
//        tvRecommendationTitle = findViewById(R.id.tv_recommendation_title);
//        cardRecommendation = findViewById(R.id.card_recommendation);
//        tvRecommendedCategory = findViewById(R.id.tv_recommended_category);
//        tvRecommendedExercises = findViewById(R.id.tv_recommended_exercises);

        // Obsługa nastrojów - teraz otwieramy SingleExerciseActivity z jednym ćwiczeniem
        findViewById(R.id.card_mood_happy).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SingleExerciseActivity.class);
            intent.putExtra("mood_type", SingleExerciseActivity.MOOD_HAPPY);
            startActivity(intent);
        });
        findViewById(R.id.card_mood_sad).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SingleExerciseActivity.class);
            intent.putExtra("mood_type", SingleExerciseActivity.MOOD_SAD);
            startActivity(intent);
        });
        findViewById(R.id.card_mood_very_sad).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SingleExerciseActivity.class);
            intent.putExtra("mood_type", SingleExerciseActivity.MOOD_VERY_SAD);
            startActivity(intent);
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

            // Odświeżamy bazę – w jednej transakcji, żeby uniknąć okna z pustą bazą
            Log.d(TAG, "Odświeżam bazę ćwiczeń z CSV...");
            List<Exercise> exercises = CsvImporter.loadExercisesFromCsv(this);
            if (!exercises.isEmpty()) {
                db.exerciseDao().replaceAll(exercises);
                int newCount = db.exerciseDao().getCount();
                Log.d(TAG, "Zaimportowano " + exercises.size() + " ćwiczeń. Nowy stan bazy: " + newCount);

                // Statystyki kategorii
                String[] categories = {"kardio", "mieszana", "mobilnosc", "postura", "rownowaga", "sila"};
                for (String cat : categories) {
                    int catCount = db.exerciseDao().getByCategory(cat).size();
                    Log.d(TAG, "Kategoria '" + cat + "': " + catCount + " ćwiczeń");
                }

                dbReady = true;
                runOnUiThread(() -> Toast.makeText(this, "Baza ćwiczeń gotowa (" + newCount + ")", Toast.LENGTH_SHORT).show());
            } else {
                Log.e(TAG, "Nie zaimportowano żadnych ćwiczeń!");
                // Nawet jak CSV puste, oznacz gotowość żeby nie blokować UI
                dbReady = true;
                runOnUiThread(() -> Toast.makeText(this, "Błąd importu bazy ćwiczeń!", Toast.LENGTH_LONG).show());
            }
        } catch (Throwable t) {
            Log.e(TAG, "Błąd bazy danych", t);
            dbReady = true; // Nie blokuj UI nawet przy błędzie
        }
    }

    /**
     * Mapowanie nastroju na parametry rekomendacji.
     * Zwraca: [kategoria_fallback, maxDifficulty, maxIntensity, prefDifficulty, prefIntensity]
     */
    private String[] moodToParams(float energyLevel) {
        if (energyLevel >= 0.7f) {
            // Dobrze – pełna aktywność, trudniejsze ćwiczenia
            return new String[]{"sila", "3.0", "3.0", "2.0", "2.0"};
        } else if (energyLevel >= 0.4f) {
            // Średnio – umiarkowane ćwiczenia
            return new String[]{"mieszana", "2.0", "2.0", "1.5", "1.5"};
        } else {
            // Słabo – delikatne ćwiczenia, niska trudność
            return new String[]{"mobilnosc", "1.0", "1.0", "1.0", "1.0"};
        }
    }

    private void generateRecommendation(float energyLevel, float difficultyPref) {
        // Parametry nastroju – zawsze dostępne jako fallback
        String[] params = moodToParams(energyLevel);
        String fallbackCategory = params[0];
        float maxDifficulty = Float.parseFloat(params[1]);
        float maxIntensity = Float.parseFloat(params[2]);
        float prefDifficulty = Float.parseFloat(params[3]);
        float prefIntensity = Float.parseFloat(params[4]);

        if (!dbReady) {
            Toast.makeText(this, "Baza ćwiczeń się ładuje, spróbuj za chwilę...", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Generuję...", Toast.LENGTH_SHORT).show();

        if (modelRunner == null) {
            Log.w(TAG, "modelRunner is null – używam fallback kategorii");
            openRecommendation(fallbackCategory, maxDifficulty, maxIntensity, prefDifficulty, prefIntensity);
            return;
        }

        try {
            float[] moodFeatures = new float[12];

            // --- POBIERANIE DANYCH Z ONBOARDINGU ---
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            // Konwersja boolean (true/false) na float (1.0f / 0.0f) dla modelu ONNX
            float canStand = prefs.getBoolean("can_stand", true) ? 1.0f : 0.0f;
            float canExerciseFloor = prefs.getBoolean("can_exercise_floor", true) ? 1.0f : 0.0f;
            float needsChair = prefs.getBoolean("needs_chair", false) ? 1.0f : 0.0f;
            float canExerciseBed = prefs.getBoolean("can_exercise_bed", false) ? 1.0f : 0.0f;
            float canExerciseSitting = prefs.getBoolean("can_exercise_sitting", false) ? 1.0f : 0.0f;
            // ----------------------------------------

            // Dynamiczne mapowanie nastroju na parametry intensywności
            float intens, diff, sile, mobil, kardio, post;

            if (energyLevel < 0.3f) { // Bardzo słabo
                intens = 1.0f; diff = 1.0f; sile = 1.0f; mobil = 2.0f; kardio = 1.0f; post = 2.0f;
            } else if (energyLevel < 0.6f) { // Średnio
                intens = 2.0f; diff = 1.0f; sile = 2.0f; mobil = 2.0f; kardio = 2.0f; post = 2.0f;
            } else { // Dobrze
                intens = 2.0f; diff = 2.0f; sile = 3.0f; mobil = 2.0f; kardio = 2.0f; post = 2.0f;
            }

            // Pakowanie tablicy Features (kolejność musi zgadzać się z modelem Pythona!)
            moodFeatures[0] = sile;   // wplyw_na_sile
            moodFeatures[1] = mobil;  // wplyw_na_elastycznosc
            moodFeatures[2] = kardio; // wplyw_na_kardio
            moodFeatures[3] = post;   // wplyw_na_postawe
            moodFeatures[4] = intens; // intensywnosc_num
            moodFeatures[5] = diff;   // poziom_trudnosci_num

            // Zamiast wpisywać na sztywno, używamy preferencji użytkownika!
            moodFeatures[6] = needsChair;         // wspomagane_krzeslem_bin
            moodFeatures[7] = canExerciseBed;     // mozna_w_lozku_bin
            moodFeatures[8] = canExerciseSitting; // mozna_siedzac_bin
            moodFeatures[9] = canStand;           // wymaga_stania_bin
            moodFeatures[10] = canExerciseFloor;  // wymaga_podlogi_bin
            moodFeatures[11] = 0.0f;              // zrodlo_enc (zakładam, że zostaje 0)

            final String finalFallbackCategory = fallbackCategory;
            final float finalMaxDiff = maxDifficulty;
            final float finalMaxIntens = maxIntensity;
            final float finalPrefDiff = prefDifficulty;
            final float finalPrefIntens = prefIntensity;

            new Thread(() -> {
                try {
                    Log.d(TAG, "Wykonuję predict dla cech: " + java.util.Arrays.toString(moodFeatures));
                    float[] probs = modelRunner.predict(moodFeatures);

                    String recommendedCategory = finalFallbackCategory;

                    if (probs != null && probs.length > 0) {
                        List<String> classes = modelRunner.getClasses();
                        int maxIdx = 0;
                        for (int i = 1; i < probs.length; i++) {
                            if (probs[i] > probs[maxIdx]) maxIdx = i;
                        }
                        if (classes != null && maxIdx < classes.size()) {
                            recommendedCategory = classes.get(maxIdx);
                        }
                        Log.d(TAG, "Rekomendowana kategoria (model): " + recommendedCategory);
                    } else {
                        Log.w(TAG, "Model nie zwrócił wyników – używam fallback: " + finalFallbackCategory);
                    }

                    String finalCategory = recommendedCategory;
                    runOnUiThread(() -> openRecommendation(finalCategory, finalMaxDiff, finalMaxIntens, finalPrefDiff, finalPrefIntens));

                } catch (Throwable t) {
                    Log.e(TAG, "Błąd modelu – fallback: " + finalFallbackCategory, t);
                    runOnUiThread(() -> openRecommendation(finalFallbackCategory, finalMaxDiff, finalMaxIntens, finalPrefDiff, finalPrefIntens));
                }
            }).start();

        } catch (Throwable t) {
            Log.e(TAG, "Błąd podczas predict – fallback", t);
            openRecommendation(fallbackCategory, maxDifficulty, maxIntensity, prefDifficulty, prefIntensity);
        }
    }

    private void openRecommendation(String category, float maxDifficulty, float maxIntensity, float prefDifficulty, float prefIntensity) {
        Intent intent = new Intent(MainActivity.this, RecommendationListActivity.class);
        intent.putExtra("category", category);
        intent.putExtra("maxDifficulty", maxDifficulty);
        intent.putExtra("maxIntensity", maxIntensity);
        intent.putExtra("prefDifficulty", prefDifficulty);
        intent.putExtra("prefIntensity", prefIntensity);
        startActivity(intent);
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