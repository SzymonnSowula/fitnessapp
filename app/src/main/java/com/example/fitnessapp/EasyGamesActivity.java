package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EasyGamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Redirect to the standard flow: Instruction -> Difficulty -> Game
        // This activity is kept for backward compatibility but now redirects
        int gameType = getIntent().getIntExtra(GameInstructionActivity.EXTRA_GAME_TYPE, GameInstructionActivity.GAME_MEMORY);

        Intent intent = new Intent(this, GameInstructionActivity.class);
        intent.putExtra(GameInstructionActivity.EXTRA_GAME_TYPE, gameType);
        startActivity(intent);
        finish();
    }
}
