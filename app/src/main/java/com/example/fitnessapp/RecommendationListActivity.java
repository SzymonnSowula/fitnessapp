package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecommendationListActivity extends AppCompatActivity {

    private AppDatabase db;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ExerciseAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

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

        String finalCategory = category;
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            List<Exercise> list = db.exerciseDao().getByCategory(finalCategory);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (list == null || list.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText(getString(R.string.no_exercises_for_category, finalCategory));
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.update(list);
                }
            });
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Adapter wewnętrzny do prostego wyświetlania ćwiczeń
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
            Exercise e = data.get(position);
            holder.bind(e, clickListener);
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
            String cat = e.category == null ? "" : e.category;
            tvSubtitle.setText(itemView.getContext().getString(R.string.exercise_subtitle_fmt,
                    cat, (int)e.poziomTrudnosciNum, (int)e.intensywnoscNum));
            itemView.setOnClickListener(v -> click.onClick(e));
        }
    }
}
