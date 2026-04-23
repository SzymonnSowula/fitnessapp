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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private TextView tvPercentage;
    private ProgressBar progressExercise;
    private TextView tvExerciseName;
    private TextView tvDifficultyIcons;
    private TextView tvDifficultyText;
    private TextView tvDescription;
    private TextView tvContraindicationsLabel;
    private TextView tvContraindications;
    private Button btnNext;
    private Button btnFinish;

    private VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_exercise);

        initViews();

        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });

        voiceNavigator.setup();

        // Hide standard ActionBar if exists since we have custom header
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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
                    voiceNavigator.speakDelayed("Ćwiczenie 1 z " + exercises.size() + ". " + exercises.get(0).name, 500);
                }
            });
        }).start();

        btnNext.setOnClickListener(v -> {
            if (currentIndex < exercises.size() - 1) {
                currentIndex++;
                showExercise(currentIndex);
                voiceNavigator.speak("Ćwiczenie " + (currentIndex + 1) + " z " + exercises.size() + ". " + exercises.get(currentIndex).name);
            } else {
                finish();
            }
        });

        btnFinish.setOnClickListener(v -> finish());
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "next":
            case "next_exercise":
                if (currentIndex < exercises.size() - 1) {
                    btnNext.performClick();
                }
                break;
            case "back":
                onBackPressed();
                break;
            case "exit":
            case "finish":
                finish();
                break;
            case "read":
            case "read_description":
            case "read_more":
                voiceNavigator.speak(tvDescription.getText().toString());
                break;
            case "repeat":
                voiceNavigator.speak(tvExerciseName.getText().toString() + ". " + tvDescription.getText().toString());
                break;
            case "stop":
                voiceNavigator.stopSpeaking();
                break;
            case "help":
                voiceNavigator.speak(VoiceCommands.getExerciseHelpText());
                break;
        }
    }

    private List<Exercise> loadExercisesForMood(int moodType) {
        switch (moodType) {
            case MOOD_HAPPY:
                return db.exerciseDao().getHardestByCategory("sila", 3.0f, 3.0f);
            case MOOD_VERY_SAD:
                return db.exerciseDao().getEasiestByCategory("mobilnosc", 3.0f, 3.0f);
            case MOOD_SAD:
            default:
                return db.exerciseDao().getByCategorySortedByMood("mieszana", 1.5f, 1.5f);
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

        int currentNum = index + 1;
        int totalNum = exercises.size();
        int progress = (int) (((float) currentNum / totalNum) * 100);

        tvProgress.setText(String.format("ĆWICZENIE %d Z %d", currentNum, totalNum));
        tvPercentage.setText(String.format("%d%%", progress));
        progressExercise.setProgress(progress);

        tvExerciseName.setText(e.name);

        int diff = (int) e.poziomTrudnosciNum;
        tvDifficultyIcons.setText(getDifficultyIcons(diff));
        tvDifficultyText.setText(getDifficultyText(diff));

        int diffColor = getDifficultyColor(diff);
        tvDifficultyIcons.setTextColor(diffColor);
        tvDifficultyText.setTextColor(diffColor);

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

        if (index >= exercises.size() - 1) {
            btnNext.setText("ZAKOŃCZ TRENING");
            btnFinish.setVisibility(View.GONE);
        } else {
            btnNext.setText("NASTĘPNE ĆWICZENIE");
            btnFinish.setVisibility(View.VISIBLE);
        }
    }

    private String getDifficultyIcons(int value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < value ? "⤢ " : "⤢ ");
        }
        return sb.toString().trim();
    }

    private String getDifficultyText(int value) {
        if (value <= 2) return "Łatwe";
        if (value <= 3) return "Średnie";
        return "Trudne";
    }

    private int getDifficultyColor(int value) {
        if (value <= 2) return 0xFF057A32;
        if (value <= 3) return 0xFF994A00;
        return 0xFFEF4444;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceNavigator != null) {
            voiceNavigator.cleanup();
        }
    }
}
