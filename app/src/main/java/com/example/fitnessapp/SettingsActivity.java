package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Inicjalizacja Navbar
        NavbarHelper.initNavbar(this);

        // Wylogowanie
        Button btnLogout = findViewById(R.id.btn_logout_settings);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
            finishAffinity();
        });

        // Obsługa kliknięć w opcje
        findViewById(R.id.tv_change_name).setOnClickListener(v -> 
            Toast.makeText(this, "Funkcja zmiany imienia będzie dostępna wkrótce", Toast.LENGTH_SHORT).show());
        
        findViewById(R.id.tv_edit_conditions).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, EditConditionsActivity.class));
        });
    }
}