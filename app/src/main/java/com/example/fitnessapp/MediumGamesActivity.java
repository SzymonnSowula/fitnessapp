package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MediumGamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medium_games);

        NavbarHelper.initNavbar(this);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Memory 2x4 - 4 pairs
        CardView cardMemory2x4 = findViewById(R.id.card_memory_2x4);
        cardMemory2x4.setOnClickListener(v -> {
            Intent intent = new Intent(MediumGamesActivity.this, MemoryGameActivity.class);
            intent.putExtra("EXTRA_COLUMNS", 2);
            intent.putExtra("EXTRA_ROWS", 4);
            startActivity(intent);
        });

        // Memory 4x4 - 8 pairs
        CardView cardMemory4x4 = findViewById(R.id.card_memory_4x4_medium);
        cardMemory4x4.setOnClickListener(v -> {
            Intent intent = new Intent(MediumGamesActivity.this, MemoryGameActivity.class);
            intent.putExtra("EXTRA_COLUMNS", 4);
            intent.putExtra("EXTRA_ROWS", 4);
            startActivity(intent);
        });

        // Kolory Medium - 3-4 colors, medium speed
        CardView cardColorsMedium = findViewById(R.id.card_colors_medium);
        cardColorsMedium.setOnClickListener(v -> {
            Intent intent = new Intent(MediumGamesActivity.this, ColorTapActivity.class);
            intent.putExtra(ColorTapActivity.EXTRA_DIFFICULTY, GameConstants.DIFFICULTY_MEDIUM);
            startActivity(intent);
        });
    }
}
