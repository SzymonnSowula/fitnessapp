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

        // Memory - Go to level selection
        CardView cardMemory = findViewById(R.id.card_memory_selection);
        cardMemory.setOnClickListener(v -> {
            startActivity(new Intent(MindGamesActivity.this, EasyGamesActivity.class));
        });

        // Kolory - Go to level selection
        CardView cardColors = findViewById(R.id.card_colors_selection);
        cardColors.setOnClickListener(v -> {
            startActivity(new Intent(MindGamesActivity.this, ColorTapActivity.class));
        });

        // Liquid Sort
        CardView cardLiquid = findViewById(R.id.card_liquid_selection);
        cardLiquid.setOnClickListener(v -> {
            startActivity(new Intent(MindGamesActivity.this, LiquidSortActivity.class));
        });
    }
}
