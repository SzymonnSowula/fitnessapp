package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MindGamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mind_games);

        NavbarHelper.initNavbar(this);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Memory - Start with easiest level
        CardView cardMemory = findViewById(R.id.card_memory);
        cardMemory.setOnClickListener(v -> {
            Intent intent = new Intent(MindGamesActivity.this, MemoryGameActivity.class);
            intent.putExtra("EXTRA_COLUMNS", 2);
            intent.putExtra("EXTRA_ROWS", 3);
            startActivity(intent);
        });

        // Kolory - Start with easiest level
        CardView cardColors = findViewById(R.id.card_colors);
        cardColors.setOnClickListener(v -> {
            Intent intent = new Intent(MindGamesActivity.this, ColorTapActivity.class);
            intent.putExtra(ColorTapActivity.EXTRA_DIFFICULTY, ColorTapActivity.DIFFICULTY_EASY);
            startActivity(intent);
        });
    }
}
