package com.example.fitnessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

public class ExerciseDetailActivity extends AppCompatActivity {

    private static final String TAG = "ExerciseDetail";

    private AppDatabase db;
    private Exercise currentExercise;
    private VoiceNavigator voiceNavigator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });
        voiceNavigator.setup();
        voiceNavigator.startListening();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.exercise_details_title);
        }

        db = AppDatabase.getDatabase(this);

        int id = getIntent().getIntExtra("exercise_id", -1);
        if (id <= 0) {
            finish();
            return;
        }

        TextView tvName = findViewById(R.id.tv_name);
        TextView tvCategory = findViewById(R.id.tv_category);
        TextView tvDifficulty = findViewById(R.id.tv_difficulty);
        TextView tvIntensity = findViewById(R.id.tv_intensity);
        TextView tvDescription = findViewById(R.id.tv_description);
        TextView tvContraindicationsLabel = findViewById(R.id.tv_contraindications_label);
        TextView tvContraindications = findViewById(R.id.tv_contraindications);
        TextView tvFlags = findViewById(R.id.tv_flags);
        TextView tvImpacts = findViewById(R.id.tv_impacts);

        new Thread(() -> {
            currentExercise = db.exerciseDao().getById(id);
            runOnUiThread(() -> {
                if (currentExercise == null) {
                    finish();
                    return;
                }

                tvName.setText(currentExercise.name);
                tvCategory.setText(getString(R.string.exercise_category_fmt, currentExercise.category));
                tvDifficulty.setText(getString(R.string.exercise_difficulty_fmt, (int) currentExercise.poziomTrudnosciNum));
                tvIntensity.setText(getString(R.string.exercise_intensity_fmt, (int) currentExercise.intensywnoscNum));

                if (currentExercise.opis != null && !currentExercise.opis.trim().isEmpty()) {
                    tvDescription.setText(currentExercise.opis);
                }

                if (currentExercise.przeciwwskazania != null && !currentExercise.przeciwwskazania.trim().isEmpty() && !currentExercise.przeciwwskazania.equalsIgnoreCase("brak")) {
                    tvContraindicationsLabel.setVisibility(View.VISIBLE);
                    tvContraindications.setVisibility(View.VISIBLE);
                    tvContraindications.setText(currentExercise.przeciwwskazania);
                }

                String flags = getString(R.string.exercise_flags_fmt,
                        currentExercise.wspomaganeKrzeslemBin > 0 ? getString(R.string.yes) : getString(R.string.no),
                        currentExercise.moznaWLozkuBin > 0 ? getString(R.string.yes) : getString(R.string.no),
                        currentExercise.moznaSiedzacBin > 0 ? getString(R.string.yes) : getString(R.string.no),
                        currentExercise.wymagaStaniaBin > 0 ? getString(R.string.yes) : getString(R.string.no),
                        currentExercise.wymagaPodlogiBin > 0 ? getString(R.string.yes) : getString(R.string.no)
                );
                tvFlags.setText(flags);

                String impacts = getString(R.string.exercise_impacts_fmt,
                        (int) currentExercise.wplywNaSileNum,
                        (int) currentExercise.wplywNaElastycznoscNum,
                        (int) currentExercise.wplywNaKardioNum,
                        (int) currentExercise.wplywNaPostaweNum
                );
                tvImpacts.setText(impacts);

                voiceNavigator.speakDelayed("Szczegóły ćwiczenia: " + currentExercise.name, 500);
            });
        }).start();
    }

    private void handleVoiceCommand(String command) {
        switch (command) {
            case "back":
                onBackPressed();
                break;
            case "exit":
                finish();
                break;
            case "read":
            case "read_description":
            case "read_more":
                if (currentExercise != null) {
                    String text = currentExercise.name + ". ";
                    if (currentExercise.opis != null && !currentExercise.opis.trim().isEmpty()) {
                        text += currentExercise.opis;
                    }
                    voiceNavigator.speak(text);
                }
                break;
            case "repeat":
                if (currentExercise != null) {
                    voiceNavigator.speak(currentExercise.name);
                }
                break;
            case "stop":
                voiceNavigator.stopSpeaking();
                break;
            case "help":
                voiceNavigator.speak(VoiceCommands.getExerciseHelpText());
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
