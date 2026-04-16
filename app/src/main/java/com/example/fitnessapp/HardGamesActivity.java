package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HardGamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hard_games);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Memory 4x4 - 8 pairs
        CardView cardMemory4x4 = findViewById(R.id.card_memory_4x4);
        cardMemory4x4.setOnClickListener(v -> {
            Intent intent = new Intent(HardGamesActivity.this, MemoryGameActivity.class);
            intent.putExtra("EXTRA_COLUMNS", 4);
            intent.putExtra("EXTRA_ROWS", 4);
            startActivity(intent);
        });

        // Memory 4x5 - 10 pairs
        CardView cardMemory4x5 = findViewById(R.id.card_memory_4x5);
        cardMemory4x5.setOnClickListener(v -> {
            Intent intent = new Intent(HardGamesActivity.this, MemoryGameActivity.class);
            intent.putExtra("EXTRA_COLUMNS", 4);
            intent.putExtra("EXTRA_ROWS", 5);
            startActivity(intent);
        });

        // Kolory Hard - 4-6 colors, fast
        CardView cardColorsHard = findViewById(R.id.card_colors_hard);
        cardColorsHard.setOnClickListener(v -> {
            Intent intent = new Intent(HardGamesActivity.this, ColorTapActivity.class);
            intent.putExtra(ColorTapActivity.EXTRA_DIFFICULTY, ColorTapActivity.DIFFICULTY_HARD);
            startActivity(intent);
        });
    }
}
