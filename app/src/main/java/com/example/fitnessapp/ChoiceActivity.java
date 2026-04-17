package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        // Inicjalizacja Navbar
        NavbarHelper.initNavbar(this);

        CardView cardBody = findViewById(R.id.card_body);
        CardView cardMind = findViewById(R.id.card_mind);

        cardBody.setOnClickListener(v -> {
            // Przenosi do wyboru "Jak się dzisiaj czujesz?" (MainActivity)
            startActivity(new Intent(ChoiceActivity.this, MainActivity.class));
        });

        cardMind.setOnClickListener(v -> {
            startActivity(new Intent(ChoiceActivity.this, MindGamesActivity.class));
        });
    }
}