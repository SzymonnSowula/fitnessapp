package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class EasyGamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_games);

        NavbarHelper.initNavbar(this);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Memory 2x3 - 3 pairs
        CardView cardMemory2x3 = findViewById(R.id.card_memory_2x3);
        cardMemory2x3.setOnClickListener(v -> {
            Intent intent = new Intent(EasyGamesActivity.this, MemoryGameActivity.class);
            intent.putExtra("EXTRA_COLUMNS", 2);
            intent.putExtra("EXTRA_ROWS", 3);
            startActivity(intent);
        });

        // Memory 3x4 - 6 pairs
        CardView cardMemory3x4 = findViewById(R.id.card_memory_3x4);
        cardMemory3x4.setOnClickListener(v -> {
            Intent intent = new Intent(EasyGamesActivity.this, MemoryGameActivity.class);
            intent.putExtra("EXTRA_COLUMNS", 3);
            intent.putExtra("EXTRA_ROWS", 4);
            startActivity(intent);
        });

        // Kolory Easy - 2-3 colors, slow
        CardView cardColorsEasy = findViewById(R.id.card_colors_easy);
        cardColorsEasy.setOnClickListener(v -> {
            Intent intent = new Intent(EasyGamesActivity.this, ColorTapActivity.class);
            intent.putExtra(ColorTapActivity.EXTRA_DIFFICULTY, ColorTapActivity.DIFFICULTY_EASY);
            startActivity(intent);
        });
    }
}
