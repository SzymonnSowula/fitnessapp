package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Nawigacja dolna
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_start) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_assistant) {
                Toast.makeText(this, "Asystent (Wkrótce)", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });

<<<<<<< HEAD
        // Wylogowanie
        Button btnLogout = findViewById(R.id.btn_logout_settings);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // Czyścimy też lokalny onboarding przy wylogowaniu, aby umożliwić ponowny flow (zgodnie z logout w MainActivity)
            getSharedPreferences("FitnessAppPrefs", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
            finishAffinity();
        });
=======
>>>>>>> 2d2d0cc024b430d33800098146014eaf445487b9

        // Obsługa kliknięć w opcje (toast dla demonstracji)
        findViewById(R.id.tv_change_name).setOnClickListener(v -> 
            Toast.makeText(this, R.string.change_name, Toast.LENGTH_SHORT).show());
        findViewById(R.id.tv_change_password).setOnClickListener(v -> 
            Toast.makeText(this, R.string.change_password, Toast.LENGTH_SHORT).show());
        findViewById(R.id.tv_notification_settings).setOnClickListener(v -> 
            Toast.makeText(this, R.string.notification_settings, Toast.LENGTH_SHORT).show());
        findViewById(R.id.tv_add_condition).setOnClickListener(v -> 
            Toast.makeText(this, R.string.add_condition, Toast.LENGTH_SHORT).show());
    }
}
