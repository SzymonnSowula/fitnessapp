package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obsługa czatu
        EditText etChatInput = findViewById(R.id.et_chat_input);
        // W przyszłości tutaj dodamy obsługę wysyłania wiadomości do AI

        // Nawigacja dolna
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_chat) {
                Toast.makeText(this, "Czat AI (Wkrótce)", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_camera) {
                Toast.makeText(this, "Aparat AI (Wkrótce)", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Profil (Wkrótce)", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Wylogowanie
        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }
}