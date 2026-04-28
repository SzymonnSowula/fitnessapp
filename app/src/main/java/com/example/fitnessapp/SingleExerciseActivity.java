package com.example.fitnessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SingleExerciseActivity extends AppCompatActivity {

    public static final String EXTRA_MOOD_TYPE = "mood_type";
    public static final int MOOD_HAPPY = 0;      
    public static final int MOOD_SAD = 1;        
    public static final int MOOD_VERY_SAD = 2;   

    private AppDatabase db;
    private List<Exercise> exercises = new ArrayList<>();
    private int currentIndex = 0;

    private TextView tvProgress, tvPercentage, tvExerciseName, tvDifficultyIcons, tvDifficultyText, tvDescription, tvContraindicationsLabel, tvContraindications;
    private ProgressBar progressExercise;
    private Button btnNext, btnFinish;
    private VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_exercise);

        initViews();
        voiceNavigator = new VoiceNavigator(this, command -> runOnUiThread(() -> handleVoiceCommand(command)));
        voiceNavigator.setup();
        voiceNavigator.startListening();

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        int moodType = getIntent().getIntExtra(EXTRA_MOOD_TYPE, MOOD_SAD);
        db = AppDatabase.getDatabase(this);
        SharedPreferences prefs = getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE);
        Set<String> userConditions = prefs.getStringSet("conditions", new HashSet<>());

        new Thread(() -> {
            List<Exercise> allExercises = db.exerciseDao().getAll();
            ExerciseRecommender.UserProfile profile = new ExerciseRecommender.UserProfile();
            
            // Mapowanie nastroju
            if (moodType == MOOD_HAPPY) profile.samopoczucie = 3;
            else if (moodType == MOOD_VERY_SAD) profile.samopoczucie = 1;
            else profile.samopoczucie = 2;

            // Synchronizacja z SharedPreferences
            profile.mozeStac = prefs.getBoolean("can_stand", true);
            profile.mozePodloge = prefs.getBoolean("can_exercise_floor", false);
            profile.krzeslo = prefs.getBoolean("needs_chair", false);
            profile.lozko = prefs.getBoolean("can_exercise_bed", false);
            profile.mozeSiedzac = prefs.getBoolean("can_exercise_sitting", true);
            
            profile.intensywnosc = (int) prefs.getFloat("maxIntensity", 3.0f);
            profile.trudnosc = (int) prefs.getFloat("maxDifficulty", 3.0f);
            profile.cel = prefs.getString("user_goal", "mieszana");
            profile.schorzenia = userConditions;

            // WYWOŁANIE REKOMENDATORA
            exercises = ExerciseRecommender.recommend(allExercises, profile, 5);

            runOnUiThread(() -> {
                if (exercises.isEmpty()) {
                    Toast.makeText(this, "Brak bezpiecznych ćwiczeń dla Twojego profilu", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    showExercise(currentIndex);
                    voiceNavigator.speakDelayed("Przygotowałam plan. Ćwiczenie 1: " + exercises.get(0).name, 500);
                }
            });
        }).start();

        btnNext.setOnClickListener(v -> {
            voiceNavigator.stopSpeaking();
            if (currentIndex < exercises.size() - 1) {
                currentIndex++;
                showExercise(currentIndex);
                voiceNavigator.speak("Ćwiczenie " + (currentIndex + 1) + ": " + exercises.get(currentIndex).name);
            } else {
                finish();
            }
        });

        btnFinish.setOnClickListener(v -> {
            voiceNavigator.stopSpeaking();
            if (currentIndex == 0) {
                finish();
            } else {
                currentIndex--;
                showExercise(currentIndex);
                voiceNavigator.speak("Poprzednie ćwiczenie: " + exercises.get(currentIndex).name);
            }
        });
    }

    private void handleVoiceCommand(String command) {
        if (command == null) return;
        switch (command.toLowerCase(Locale.ROOT)) {
            case "next": case "następne": if (currentIndex < exercises.size() - 1) btnNext.performClick(); break;
            case "back": case "powrót": onBackPressed(); break;
            case "read": case "czytaj": voiceNavigator.speak(tvDescription.getText().toString()); break;
            case "stop": voiceNavigator.stopSpeaking(); break;
        }
    }

    private void initViews() {
        tvProgress = findViewById(R.id.tv_progress);
        tvPercentage = findViewById(R.id.tv_percentage);
        progressExercise = findViewById(R.id.progress_exercise);
        tvExerciseName = findViewById(R.id.tv_exercise_name);
        tvDifficultyIcons = findViewById(R.id.tv_difficulty_icons);
        tvDifficultyText = findViewById(R.id.tv_difficulty_text);
        tvDescription = findViewById(R.id.tv_description);
        tvContraindicationsLabel = findViewById(R.id.tv_contraindications_label);
        tvContraindications = findViewById(R.id.tv_contraindications);
        btnNext = findViewById(R.id.btn_next);
        btnFinish = findViewById(R.id.btn_finish);
    }

    private void showExercise(int index) {
        Exercise e = exercises.get(index);
        tvProgress.setText("ĆWICZENIE " + (index + 1) + " Z " + exercises.size());
        tvPercentage.setText(((index + 1) * 100 / exercises.size()) + "%");
        progressExercise.setProgress((index + 1) * 100 / exercises.size());
        tvExerciseName.setText(e.name);
        tvDifficultyIcons.setText(getDifficultyIcons((int) e.poziomTrudnosciNum));
        tvDifficultyText.setText(getDifficultyText((int) e.poziomTrudnosciNum));
        tvDescription.setText(e.opis != null ? e.opis : "Brak opisu.");
        
        if (e.przeciwwskazania != null && !e.przeciwwskazania.equalsIgnoreCase("brak")) {
            tvContraindicationsLabel.setVisibility(View.VISIBLE);
            tvContraindications.setVisibility(View.VISIBLE);
            tvContraindications.setText(e.przeciwwskazania);
        } else {
            tvContraindicationsLabel.setVisibility(View.GONE);
            tvContraindications.setVisibility(View.GONE);
        }

        // Logika przycisków nawigacji
        if (index == 0) {
            btnFinish.setText("ZAKOŃCZ");
        } else {
            btnFinish.setText("POPRZEDNIE");
        }

        if (index >= exercises.size() - 1) {
            btnNext.setText("ZAKOŃCZ");
        } else {
            btnNext.setText("NASTĘPNE");
        }
    }

    private String getDifficultyIcons(int v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append(i < v ? "● " : "○ ");
        return sb.toString();
    }

    private String getDifficultyText(int v) {
        return v <= 1 ? "Łatwe" : (v <= 2 ? "Średnie" : "Trudne");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceNavigator != null) voiceNavigator.cleanup();
    }
}
