package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ImageButton;
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
    private VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Dodanie sprawdzenia uprawnień
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
        }

        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });
        voiceNavigator.setup();

        mAuth = FirebaseAuth.getInstance();

        TextView tvWelcome = findViewById(R.id.tv_welcome_title);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(KEY_USER_NAME, "");
        if (!name.isEmpty() && tvWelcome != null) {
            tvWelcome.setText(getString(R.string.welcome_personalized, name));
        }

        NavbarHelper.initNavbar(this);

        modelRunner = new ModelRunner();
        db = AppDatabase.getDatabase(this);
        try {
            modelRunner.init(this);
            new Thread(this::loadExerciseDatabase).start();
        } catch (Exception e) {
            Log.e(TAG, "Błąd inicjalizacji", e);
            Toast.makeText(this, "Błąd ładowania systemu rekomendacji", Toast.LENGTH_SHORT).show();
        }

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

voiceNavigator.speakDelayed("Witaj! Jak się dzisiaj czujesz?", 500);

        // Help button listener
        ImageButton btnHelp = findViewById(R.id.btn_help);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> VoiceHelpDialog.show(this));
        }
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "next":
                voiceNavigator.speak("Czuję się dobrze - trudniejsze ćwiczenia. Jestem zmęczony - umiarkowane. Nie czuję się dobrze - łatwe ćwiczenia.");
                break;
            case "exercises":
            case "read":
            case "repeat":
                voiceNavigator.speak("Wybierz nastrój aby otrzymać rekomendację ćwiczeń.");
                break;
            case "mood_happy":
                voiceNavigator.speak("Świetnie! Przygotowałam dla Ciebie zestaw intensywniejszych ćwiczeń.");
                navigateToMood(SingleExerciseActivity.MOOD_HAPPY);
                break;
            case "mood_sad":
                voiceNavigator.speak("Rozumiem. Spróbujmy łagodnych ćwiczeń na rozruszanie.");
                navigateToMood(SingleExerciseActivity.MOOD_SAD);
                break;
            case "mood_very_sad":
                voiceNavigator.speak("Pamiętaj, że ruch poprawia humor. Przygotowałam bardzo proste ćwiczenia.");
                navigateToMood(SingleExerciseActivity.MOOD_VERY_SAD);
                break;
            case "stop":
                voiceNavigator.stopSpeaking();
                break;
            case "help":
                voiceNavigator.speak("Powiedz 'dobrze' aby wybrać trudniejsze ćwiczenia, 'zmęczony' dla umiarkowanych, lub 'nie dobrze' dla łatwych.");
                break;
        }
    }

    private void navigateToMood(int moodType) {
        Intent intent = new Intent(this, SingleExerciseActivity.class);
        intent.putExtra(SingleExerciseActivity.EXTRA_MOOD_TYPE, moodType);
        startActivity(intent);
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
        if (voiceNavigator != null) {
            voiceNavigator.cleanup();
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
