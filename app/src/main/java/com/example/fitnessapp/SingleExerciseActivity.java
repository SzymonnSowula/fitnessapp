package com.example.fitnessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp.utils.AppExecutors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private VideoView videoView;
    private int videoPosition = 0;
    private boolean isProcessingAction = false;
    private long exerciseStartTime;
    private final Handler mainHandler = new Handler(android.os.Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_exercise);

        initViews();
        setupVideoView();
        voiceNavigator = new VoiceNavigator(this, command -> runOnUiThread(() -> handleVoiceCommand(command)));
        voiceNavigator.setup();
        voiceNavigator.startListening();

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        int moodType = getIntent().getIntExtra(EXTRA_MOOD_TYPE, MOOD_SAD);
        db = AppDatabase.getDatabase(this);
        SharedPreferences prefs = getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE);
        Set<String> userConditions = prefs.getStringSet("conditions", new HashSet<>());

        AppExecutors.getInstance().diskIO().execute(() -> {
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
                    exerciseStartTime = System.currentTimeMillis();
                    voiceNavigator.speakDelayed("Przygotowałam plan treningowy. Jeśli potrzebujesz pomocy, powiedz: POMOC. Ćwiczenie 1: " + exercises.get(0).name, 500);
                }
            });
        });

        btnNext.setOnClickListener(v -> {
            if (isProcessingAction) return;
            isProcessingAction = true;
            voiceNavigator.stopSpeaking();

            // Save current exercise to history
            saveCurrentExercise();

            if (currentIndex < exercises.size() - 1) {
                currentIndex++;
                showExercise(currentIndex);
                voiceNavigator.speak("Ćwiczenie " + (currentIndex + 1) + ": " + exercises.get(currentIndex).name);
                mainHandler.postDelayed(() -> isProcessingAction = false, 1000);
            } else {
                finish();
            }
        });

        btnFinish.setOnClickListener(v -> {
            if (isProcessingAction) return;
            isProcessingAction = true;
            voiceNavigator.stopSpeaking();
            if (currentIndex == 0) {
                saveCurrentExercise();
                finish();
            } else {
                currentIndex--;
                showExercise(currentIndex);
                voiceNavigator.speak("Poprzednie ćwiczenie: " + exercises.get(currentIndex).name);
                mainHandler.postDelayed(() -> isProcessingAction = false, 1000);
            }
        });
    }

    private void handleVoiceCommand(String command) {
        if (command == null || isProcessingAction) return;
        
        String cleanCommand = command.toLowerCase(Locale.ROOT);
        switch (cleanCommand) {
            case "next": 
            case "następne": 
            case "next_exercise":
                btnNext.performClick();
                break;
                
            case "previous": 
            case "poprzednie": 
            case "previous_exercise":
                btnFinish.performClick();
                break;
                
            case "back": 
            case "powrót": 
                onBackPressed(); 
                break;
                
            case "read": 
            case "czytaj": 
            case "read_description":
                voiceNavigator.speak(tvDescription.getText().toString()); 
                break;

            case "finish":
                onBackPressed();
                break;

            case "stop": 
                voiceNavigator.stopSpeaking(); 
                break;

            case "help":
            case "pomoc":
                voiceNavigator.speak(VoiceCommands.getExerciseHelpText());
                break;
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
        videoView = findViewById(R.id.video_view);
    }

    private void showExercise(int index) {
        exerciseStartTime = System.currentTimeMillis();
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

    private void setupVideoView() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                File videoFile = copyAssetToCache(this, "exercise_placeholder.mp4", "exercise_placeholder.mp4");
                runOnUiThread(() -> {
                    videoView.setVideoURI(Uri.fromFile(videoFile));
                    MediaController mediaController = new MediaController(this);
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);
                    videoView.setOnPreparedListener(mp -> {
                        mp.setLooping(true);
                        if (videoPosition > 0) {
                            videoView.seekTo(videoPosition);
                        }
                        videoView.start();
                    });
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Nie można wczytać wideo", Toast.LENGTH_SHORT).show();
                    videoView.setVisibility(View.GONE);
                });
            }
        });
    }

    private File copyAssetToCache(Context context, String assetName, String outFileName) throws IOException {
        File outFile = new File(context.getCacheDir(), outFileName);
        if (outFile.exists()) {
            return outFile;
        }
        try (InputStream is = context.getAssets().open(assetName);
             FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
        }
        return outFile;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (videoView != null) {
            mainHandler.postDelayed(() -> {
                if (videoView != null) {
                    videoView.seekTo(videoPosition);
                    videoView.start();
                }
            }, 300);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoView != null) {
            videoPosition = videoView.getCurrentPosition();
            videoView.pause();
        }
    }

    private void saveCurrentExercise() {
        if (exercises == null || currentIndex >= exercises.size()) return;
        Exercise e = exercises.get(currentIndex);
        long duration = (System.currentTimeMillis() - exerciseStartTime) / 1000;
        int mood = getIntent().getIntExtra(EXTRA_MOOD_TYPE, MOOD_SAD);

        AppExecutors.getInstance().diskIO().execute(() -> {
            ExerciseSession session = new ExerciseSession();
            session.exerciseId = e.id;
            session.exerciseName = e.name;
            session.category = e.category;
            session.moodType = mood;
            session.durationSeconds = (int) duration;
            session.completedAt = System.currentTimeMillis();
            AppDatabase.getDatabase(SingleExerciseActivity.this).exerciseSessionDao().insert(session);
        });

        voiceNavigator.speak("Ćwiczenie zapisane w historii");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceNavigator != null) voiceNavigator.cleanup();
    }
}
