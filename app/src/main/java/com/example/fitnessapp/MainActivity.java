package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private static final String PREFS_NAME = "FitnessAppPrefs";
    private static final String KEY_USER_NAME = "user_name";

    private ModelRunner modelRunner;
    private AppDatabase db;
    private volatile boolean dbReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Spersonalizowane powitanie
        TextView tvWelcome = findViewById(R.id.tv_welcome_title);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(KEY_USER_NAME, "");
        if (!name.isEmpty() && tvWelcome != null) {
            tvWelcome.setText(getString(R.string.welcome_personalized, name));
        }

        // Inicjalizacja Navbar
        NavbarHelper.initNavbar(this);

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

        // Obsługa nastrojów
        findViewById(R.id.card_mood_happy).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SingleExerciseActivity.class);
            intent.putExtra(SingleExerciseActivity.EXTRA_MOOD_TYPE, SingleExerciseActivity.MOOD_HAPPY);
            startActivity(intent);
        });
        findViewById(R.id.card_mood_sad).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SingleExerciseActivity.class);
            intent.putExtra(SingleExerciseActivity.EXTRA_MOOD_TYPE, SingleExerciseActivity.MOOD_SAD);
            startActivity(intent);
        });
        findViewById(R.id.card_mood_very_sad).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SingleExerciseActivity.class);
            intent.putExtra(SingleExerciseActivity.EXTRA_MOOD_TYPE, SingleExerciseActivity.MOOD_VERY_SAD);
            startActivity(intent);
        });
    }

    private void loadExerciseDatabase() {
        try {
            int count = db.exerciseDao().getCount();
            Log.d(TAG, "Aktualna liczba ćwiczeń w bazie: " + count);

            Log.d(TAG, "Odświeżam bazę ćwiczeń z CSV...");
            List<Exercise> exercises = CsvImporter.loadExercisesFromCsv(this);
            if (!exercises.isEmpty()) {
                db.exerciseDao().replaceAll(exercises);
                int newCount = db.exerciseDao().getCount();
                Log.d(TAG, "Zaimportowano " + exercises.size() + " ćwiczeń. Nowy stan bazy: " + newCount);
                dbReady = true;
            } else {
                Log.e(TAG, "Nie zaimportowano żadnych ćwiczeń!");
                dbReady = true;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Błąd bazy danych", t);
            dbReady = true;
        }
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
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }
    }
}