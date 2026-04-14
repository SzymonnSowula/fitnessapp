package com.example.fitnessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SingleExerciseActivity extends AppCompatActivity {

    public static final String EXTRA_MOOD_TYPE = "mood_type";
    public static final int MOOD_HAPPY = 0;      // Czuję się dobrze - najtrudniejsze
    public static final int MOOD_SAD = 1;        // Jestem zmęczony - umiarkowane
    public static final int MOOD_VERY_SAD = 2;   // Nie czuję się dobrze - najłatwiejsze

    private AppDatabase db;
    private List<Exercise> exercises = new ArrayList<>();
    private int currentIndex = 0;

    private TextView tvProgress;
    private ProgressBar progressExercise;
    private TextView tvExerciseCategory;
    private TextView tvExerciseName;
    private TextView tvDifficulty;
    private TextView tvIntensity;
    private TextView tvDescription;
    private TextView tvContraindicationsLabel;
    private TextView tvContraindications;
    private Button btnNext;
    private Button btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_exercise);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ćwiczenia");
        }

        initViews();

        int moodType = getIntent().getIntExtra(EXTRA_MOOD_TYPE, MOOD_SAD);

        db = AppDatabase.getDatabase(this);

        SharedPreferences prefs = getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE);
        Set<String> userConditions = prefs.getStringSet("conditions", new HashSet<>());

        new Thread(() -> {
            List<Exercise> list = loadExercisesForMood(moodType);

            // Filtruj przeciwwskazania i ogranicz do max 5
            if (list != null && !list.isEmpty()) {
                List<Exercise> filtered = new ArrayList<>();
                for (Exercise e : list) {
                    if (filtered.size() >= 5) break;

                    boolean isSafe = true;
                    if (e.przeciwwskazania != null && !e.przeciwwskazania.trim().isEmpty()
                            && !e.przeciwwskazania.equalsIgnoreCase("brak")) {
                        for (String condition : userConditions) {
                            if (e.przeciwwskazania.toLowerCase().contains(condition.toLowerCase())) {
                                isSafe = false;
                                break;
                            }
                        }
                    }
                    if (isSafe) {
                        filtered.add(e);
                    }
                }
                exercises = filtered;
            }

            runOnUiThread(() -> {
                if (exercises.isEmpty()) {
                    Toast.makeText(this, "Brak ćwiczeń dla tego nastroju", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    showExercise(currentIndex);
                }
            });
        }).start();

        btnNext.setOnClickListener(v -> {
            if (currentIndex < exercises.size() - 1) {
                currentIndex++;
                showExercise(currentIndex);
            } else {
                Toast.makeText(this, "To już ostatnie ćwiczenie!", Toast.LENGTH_SHORT).show();
                btnNext.setEnabled(false);
            }
        });

        btnFinish.setOnClickListener(v -> finish());
    }

    private List<Exercise> loadExercisesForMood(int moodType) {
        switch (moodType) {
            case MOOD_HAPPY:
                // Najtrudniejsze i najbardziej intensywne
                return db.exerciseDao().getHardestByCategory("sila", 3.0f, 3.0f);
            case MOOD_VERY_SAD:
                // Najłatwiejsze i najmniej intensywne
                return db.exerciseDao().getEasiestByCategory("mobilnosc", 3.0f, 3.0f);
            case MOOD_SAD:
            default:
                // Umiarkowane - bliskie preferencjom
                return db.exerciseDao().getByCategorySortedByMood("mieszana", 1.5f, 1.5f);
        }
    }

    private void initViews() {
        tvProgress = findViewById(R.id.tv_progress);
        progressExercise = findViewById(R.id.progress_exercise);
        tvExerciseCategory = findViewById(R.id.tv_exercise_category);
        tvExerciseName = findViewById(R.id.tv_exercise_name);
        tvDifficulty = findViewById(R.id.tv_difficulty);
        tvIntensity = findViewById(R.id.tv_intensity);
        tvDescription = findViewById(R.id.tv_description);
        tvContraindicationsLabel = findViewById(R.id.tv_contraindications_label);
        tvContraindications = findViewById(R.id.tv_contraindications);
        btnNext = findViewById(R.id.btn_next);
        btnFinish = findViewById(R.id.btn_finish);
    }

    private void showExercise(int index) {
        Exercise e = exercises.get(index);

        tvProgress.setText(String.format("Ćwiczenie %d z %d", index + 1, exercises.size()));
        progressExercise.setMax(exercises.size());
        progressExercise.setProgress(index + 1);

        String categoryDisplay = e.category != null ? e.category.toUpperCase() : "INNE";
        tvExerciseCategory.setText(categoryDisplay);

        tvExerciseName.setText(e.name);
        tvDifficulty.setText(getStars((int) e.poziomTrudnosciNum));
        tvIntensity.setText(getStars((int) e.intensywnoscNum));

        String desc = e.opis != null && !e.opis.trim().isEmpty() ? e.opis : "Brak opisu dla tego ćwiczenia.";
        tvDescription.setText(desc);

        if (e.przeciwwskazania != null && !e.przeciwwskazania.trim().isEmpty()
                && !e.przeciwwskazania.equalsIgnoreCase("brak")) {
            tvContraindicationsLabel.setVisibility(View.VISIBLE);
            tvContraindications.setVisibility(View.VISIBLE);
            tvContraindications.setText(e.przeciwwskazania);
        } else {
            tvContraindicationsLabel.setVisibility(View.GONE);
            tvContraindications.setVisibility(View.GONE);
        }

        // Aktualizuj przycisk
        if (index >= exercises.size() - 1) {
            btnNext.setText("Ostatnie ćwiczenie");
            btnNext.setEnabled(true);
        } else {
            btnNext.setText("Następne ćwiczenie");
            btnNext.setEnabled(true);
        }
    }

    private String getStars(int value) {
        if (value <= 0) return "☆☆☆☆☆";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < value ? "★" : "☆");
        }
        return sb.toString();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
