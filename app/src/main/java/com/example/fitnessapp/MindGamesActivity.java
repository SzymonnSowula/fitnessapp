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

        // Easy - go to EasyGamesActivity
        CardView cardEasy = findViewById(R.id.card_mood_easy);
        cardEasy.setOnClickListener(v -> {
            startActivity(new Intent(MindGamesActivity.this, EasyGamesActivity.class));
        });

        // Medium - go to MediumGamesActivity
        CardView cardMedium = findViewById(R.id.card_mood_medium);
        cardMedium.setOnClickListener(v -> {
            startActivity(new Intent(MindGamesActivity.this, MediumGamesActivity.class));
        });

        // Hard - go to HardGamesActivity
        CardView cardHard = findViewById(R.id.card_mood_hard);
        cardHard.setOnClickListener(v -> {
            startActivity(new Intent(MindGamesActivity.this, HardGamesActivity.class));
        });
    }
}
