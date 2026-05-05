package com.example.fitnessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
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

    private TextView tvProgress, tvPercentage, tvExerciseNameStep1, tvExerciseNameStep2, tvDifficultyIcons, tvDifficultyText, tvDescription, tvContraindicationsLabel, tvContraindications;
    private View layoutStep1, layoutStep2;
    private ProgressBar progressExercise;
    private Button btnNext, btnFinish;
    private VoiceNavigator voiceNavigator;
    private VideoView videoView;
    private int videoPosition = 0;
    private boolean isProcessingAction = false;
    private boolean isStep2 = false;
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
            
            if (moodType == MOOD_HAPPY) profile.samopoczucie = 3;
            else if (moodType == MOOD_VERY_SAD) profile.samopoczucie = 1;
            else profile.samopoczucie = 2;

            profile.mozeStac = prefs.getBoolean("can_stand", true);
            profile.mozePodloge = prefs.getBoolean("can_exercise_floor", false);
            profile.krzeslo = prefs.getBoolean("needs_chair", false);
            profile.lozko = prefs.getBoolean("can_exercise_bed", false);
            profile.mozeSiedzac = prefs.getBoolean("can_exercise_sitting", true);
            
            profile.intensywnosc = (int) prefs.getFloat("maxIntensity", 3.0f);
            profile.trudnosc = (int) prefs.getFloat("maxDifficulty", 3.0f);
            profile.cel = prefs.getString("user_goal", "mieszana");
            profile.schorzenia = userConditions;

            exercises = ExerciseRecommender.recommend(allExercises, profile, 5);

            runOnUiThread(() -> {
                if (exercises.isEmpty()) {
                    Toast.makeText(this, R.string.no_safe_exercises, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    showExercise(currentIndex);
                    exerciseStartTime = System.currentTimeMillis();
                    voiceNavigator.speakDelayed(getString(R.string.training_plan_ready, exercises.get(0).name), 500);
                }
            });
        });

        btnNext.setOnClickListener(v -> {
            if (isProcessingAction) return;
            isProcessingAction = true;
            voiceNavigator.stopSpeaking();

            if (!isStep2) {
                isStep2 = true;
                updateStepVisibility();
                voiceNavigator.speak(getString(R.string.step2_announcement));
                mainHandler.postDelayed(() -> isProcessingAction = false, 500);
            } else {
                saveCurrentExercise();
                if (currentIndex < exercises.size() - 1) {
                    currentIndex++;
                    isStep2 = false;
                    showExercise(currentIndex);
                    mainHandler.postDelayed(() -> isProcessingAction = false, 1000);
                } else {
                    finish();
                }
            }
        });

        btnFinish.setOnClickListener(v -> {
            if (isProcessingAction) return;
            isProcessingAction = true;
            voiceNavigator.stopSpeaking();

            if (isStep2) {
                isStep2 = false;
                updateStepVisibility();
                mainHandler.postDelayed(() -> isProcessingAction = false, 500);
            } else {
                if (currentIndex == 0) {
                    saveCurrentExercise();
                    finish();
                } else {
                    currentIndex--;
                    isStep2 = true;
                    showExercise(currentIndex);
                    mainHandler.postDelayed(() -> isProcessingAction = false, 1000);
                }
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
        tvExerciseNameStep1 = findViewById(R.id.tv_exercise_name_step1);
        tvExerciseNameStep2 = findViewById(R.id.tv_exercise_name_step2);
        layoutStep1 = findViewById(R.id.layout_step1);
        layoutStep2 = findViewById(R.id.layout_step2);
        tvDifficultyIcons = findViewById(R.id.tv_difficulty_icons);
        tvDifficultyText = findViewById(R.id.tv_difficulty_text);
        tvDescription = findViewById(R.id.tv_description);
        tvContraindicationsLabel = findViewById(R.id.tv_contraindications_label);
        tvContraindications = findViewById(R.id.tv_contraindications);
        btnNext = findViewById(R.id.btn_next);
        btnFinish = findViewById(R.id.btn_finish);
        videoView = findViewById(R.id.video_view);

        android.widget.ImageButton btnHelp = findViewById(R.id.btn_help);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> VoiceHelpDialog.show(this));
        }
    }

    private void updateStepVisibility() {
        if (isStep2) {
            layoutStep1.setVisibility(View.GONE);
            layoutStep2.setVisibility(View.VISIBLE);
            btnNext.setText(currentIndex >= exercises.size() - 1 ? getString(R.string.finish) : getString(R.string.next));
            btnFinish.setText(R.string.back_button);
            if (videoView != null) videoView.start();
        } else {
            layoutStep1.setVisibility(View.VISIBLE);
            layoutStep2.setVisibility(View.GONE);
            btnNext.setText(R.string.video_button);
            btnFinish.setText(currentIndex == 0 ? getString(R.string.finish) : getString(R.string.previous));
            if (videoView != null) videoView.pause();
            if (exercises != null && currentIndex < exercises.size()) {
                Exercise e = exercises.get(currentIndex);
                voiceNavigator.speak(e.name + ". " + (e.opis != null ? e.opis : ""));
            }
        }
    }

    private void showExercise(int index) {
        exerciseStartTime = System.currentTimeMillis();
        Exercise e = exercises.get(index);
        tvProgress.setText(getString(R.string.exercise_step_fmt, index + 1, exercises.size()));
        tvPercentage.setText(((index + 1) * 100 / exercises.size()) + "%");
        progressExercise.setProgress((index + 1) * 100 / exercises.size());
        tvExerciseNameStep1.setText(e.name);
        tvExerciseNameStep2.setText(e.name);
        tvDifficultyIcons.setText(getDifficultyIcons((int) e.poziomTrudnosciNum));
        tvDifficultyText.setText(getDifficultyText((int) e.poziomTrudnosciNum));
        tvDescription.setText(e.opis != null ? e.opis : getString(R.string.no_description_short));
        if (e.przeciwwskazania != null && !e.przeciwwskazania.equalsIgnoreCase("brak")) {
            tvContraindicationsLabel.setVisibility(View.VISIBLE);
            tvContraindications.setVisibility(View.VISIBLE);
            tvContraindications.setText(e.przeciwwskazania);
        } else {
            tvContraindicationsLabel.setVisibility(View.GONE);
            tvContraindications.setVisibility(View.GONE);
        }
        updateStepVisibility();
    }

    private String getDifficultyIcons(int v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append(i < v ? "● " : "○ ");
        return sb.toString();
    }

    private String getDifficultyText(int v) {
        return v <= 1 ? getString(R.string.difficulty_easy) : (v <= 2 ? getString(R.string.difficulty_medium) : getString(R.string.difficulty_hard));
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
            if (videoPosition > 0) videoView.seekTo(videoPosition);
            if (isStep2) videoView.start();
        });
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_video_load, Toast.LENGTH_SHORT).show();
                    videoView.setVisibility(View.GONE);
                });
            }
        });
    }

    private File copyAssetToCache(Context context, String assetName, String outFileName) throws IOException {
        File outFile = new File(context.getCacheDir(), outFileName);
        if (outFile.exists()) return outFile;
        try (InputStream is = context.getAssets().open(assetName);
             FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) fos.write(buffer, 0, read);
            fos.flush();
        }
        return outFile;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (videoView != null && isStep2) {
            mainHandler.postDelayed(() -> {
                if (videoView != null && isStep2) {
                    videoView.seekTo(videoPosition);
                    videoView.start();
                }
            }, 300);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) {
            videoPosition = videoView.getCurrentPosition();
            videoView.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mainHandler.removeCallbacksAndMessages(null);
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
        voiceNavigator.speak(getString(R.string.exercise_saved));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceNavigator != null) voiceNavigator.cleanup();
    }
}
