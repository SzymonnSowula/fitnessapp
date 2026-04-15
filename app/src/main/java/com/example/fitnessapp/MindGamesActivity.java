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

        CardView cardMemory = findViewById(R.id.card_memory);
        CardView card2048 = findViewById(R.id.card_2048);

        cardMemory.setOnClickListener(v -> {
            startActivity(new Intent(MindGamesActivity.this, MemoryGameActivity.class));
        });

        card2048.setOnClickListener(v -> {
            startActivity(new Intent(MindGamesActivity.this, Game2048Activity.class));
        });
    }
}