package com.example.fitnessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecommendationListActivity extends AppCompatActivity {

    private AppDatabase db;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ExerciseAdapter adapter;
    private VoiceNavigator voiceNavigator;
    private List<Exercise> currentExercises = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        voiceNavigator = new VoiceNavigator(this, new VoiceNavigator.VoiceCallback() {
            @Override
            public void onVoiceCommand(String command) {
                runOnUiThread(() -> handleVoiceCommand(command));
            }
        });

        voiceNavigator.setup();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.recommendations_title);
        }

        db = AppDatabase.getDatabase(this);
        recyclerView = findViewById(R.id.recycler_recommendations);
        progressBar = findViewById(R.id.progress_recommendations);
        tvEmpty = findViewById(R.id.tv_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(new ArrayList<>(), exercise -> {
            Intent intent = new Intent(RecommendationListActivity.this, ExerciseDetailActivity.class);
            intent.putExtra("exercise_id", exercise.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        String category = getIntent().getStringExtra("category");
        if (category == null) category = "mieszana";

        float maxDifficulty = getIntent().getFloatExtra("maxDifficulty", 2.0f);
        float maxIntensity = getIntent().getFloatExtra("maxIntensity", 2.0f);

        SharedPreferences prefs = getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE);
        Set<String> userConditions = prefs.getStringSet("conditions", new HashSet<>());

        String finalCategory = category;
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            List<Exercise> allExercises = db.exerciseDao().getAll();
            
            ExerciseRecommender.UserProfile profile = new ExerciseRecommender.UserProfile();
            profile.cel = finalCategory;
            profile.intensywnosc = (int)maxIntensity;
            profile.trudnosc = (int)maxDifficulty;
            profile.schorzenia = userConditions;
            
            // SYNCHRONIZACJA KLUCZY Z ONBOARDINGIEM
            profile.mozeStac = prefs.getBoolean("can_stand", true);
            profile.mozePodloge = prefs.getBoolean("can_exercise_floor", false);
            profile.krzeslo = prefs.getBoolean("needs_chair", false);
            profile.lozko = prefs.getBoolean("can_exercise_bed", false);
            profile.mozeSiedzac = prefs.getBoolean("can_exercise_sitting", true);

            List<Exercise> recommended = ExerciseRecommender.recommend(allExercises, profile, 5);
            currentExercises = recommended;

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (recommended.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText(R.string.no_exercises_found);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.update(recommended);
                    voiceNavigator.speakDelayed("Przygotowałam 5 najlepszych ćwiczeń. Możesz powiedzieć 'czytaj', aby poznać listę.", 500);
                }
            });
        }).start();
    }

    private void handleVoiceCommand(String command) {
        if (command == null) return;
        String cmd = command.toLowerCase();
        
        if (cmd.contains("czytaj") || cmd.contains("read")) {
            readExercises();
        } else if (cmd.contains("następne") || cmd.contains("next")) {
            scrollNext();
        } else if (cmd.contains("back") || cmd.contains("powrót")) {
            onBackPressed();
        } else if (cmd.contains("exit") || cmd.contains("wyjdź")) {
            finish();
        } else if (cmd.contains("stop")) {
            voiceNavigator.stopSpeaking();
        } else if (cmd.contains("help") || cmd.contains("pomoc")) {
            voiceNavigator.speak("Powiedz 'czytaj', aby usłyszeć listę, lub 'następne', aby przewinąć.");
        }
    }

    private void readExercises() {
        if (currentExercises.isEmpty()) {
            voiceNavigator.speak("Lista jest pusta.");
            return;
        }
        StringBuilder sb = new StringBuilder("Twoje ćwiczenia to: ");
        for (int i = 0; i < currentExercises.size(); i++) {
            sb.append(i + 1).append(". ").append(currentExercises.get(i).name).append(". ");
        }
        voiceNavigator.speak(sb.toString());
    }

    private void scrollNext() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int lastVisible = layoutManager.findLastVisibleItemPosition();
            if (lastVisible < adapter.getItemCount() - 1) {
                recyclerView.smoothScrollToPosition(lastVisible + 1);
                voiceNavigator.speak("Przewijam listę.");
            } else {
                voiceNavigator.speak("To już koniec listy.");
            }
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
        if (voiceNavigator != null) voiceNavigator.cleanup();
    }

    private static class ExerciseAdapter extends RecyclerView.Adapter<ExerciseViewHolder> {
        interface OnExerciseClick { void onClick(Exercise exercise); }
        private final OnExerciseClick clickListener;
        private final List<Exercise> data;

        ExerciseAdapter(List<Exercise> data, OnExerciseClick clickListener) {
            this.data = data;
            this.clickListener = clickListener;
        }

        void update(List<Exercise> newData) {
            data.clear();
            data.addAll(newData);
            notifyDataSetChanged();
        }

        @Override
        public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
            return new ExerciseViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ExerciseViewHolder holder, int position) {
            holder.bind(data.get(position), clickListener);
        }

        @Override
        public int getItemCount() { return data.size(); }
    }

    private static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvSubtitle;

        ExerciseViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ex_name);
            tvSubtitle = itemView.findViewById(R.id.tv_ex_subtitle);
        }

        void bind(Exercise e, ExerciseAdapter.OnExerciseClick click) {
            tvName.setText(e.name);
            tvSubtitle.setText(itemView.getContext().getString(R.string.exercise_subtitle_fmt, 
                e.category != null ? e.category : "", (int)e.poziomTrudnosciNum, (int)e.intensywnoscNum));
            itemView.setOnClickListener(v -> click.onClick(e));
        }
    }
}
