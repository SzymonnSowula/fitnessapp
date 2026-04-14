package com.example.fitnessapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ExerciseDetailActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

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
        TextView tvFlags = findViewById(R.id.tv_flags);
        TextView tvImpacts = findViewById(R.id.tv_impacts);

        new Thread(() -> {
            Exercise e = db.exerciseDao().getById(id);
            runOnUiThread(() -> {
                if (e == null) { finish(); return; }
                tvName.setText(e.name);
                tvCategory.setText(getString(R.string.exercise_category_fmt, e.category));
                tvDifficulty.setText(getString(R.string.exercise_difficulty_fmt, (int)e.poziomTrudnosciNum));
                tvIntensity.setText(getString(R.string.exercise_intensity_fmt, (int)e.intensywnoscNum));

                String flags = getString(R.string.exercise_flags_fmt,
                        e.wspomaganeKrzeslemBin > 0 ? getString(R.string.yes) : getString(R.string.no),
                        e.moznaWLozkuBin > 0 ? getString(R.string.yes) : getString(R.string.no),
                        e.moznaSiedzacBin > 0 ? getString(R.string.yes) : getString(R.string.no),
                        e.wymagaStaniaBin > 0 ? getString(R.string.yes) : getString(R.string.no),
                        e.wymagaPodlogiBin > 0 ? getString(R.string.yes) : getString(R.string.no)
                );
                tvFlags.setText(flags);

                String impacts = getString(R.string.exercise_impacts_fmt,
                        (int)e.wplywNaSileNum,
                        (int)e.wplywNaElastycznoscNum,
                        (int)e.wplywNaKardioNum,
                        (int)e.wplywNaPostaweNum
                );
                tvImpacts.setText(impacts);
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
}
