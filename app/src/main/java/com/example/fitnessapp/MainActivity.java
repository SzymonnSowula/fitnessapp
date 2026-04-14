package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.SharedPreferences;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // 1. Deklaracja zmiennej autoryzacji
    private FirebaseAuth mAuth;
    private static final String PREFS_NAME = "FitnessAppPrefs";
    private static final String KEY_USER_NAME = "user_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Inicjalizacja Firebase (musi być przed jakimkolwiek użyciem mAuth!)
        mAuth = FirebaseAuth.getInstance();

        // Spersonalizowane powitanie
        TextView tvWelcome = findViewById(R.id.tv_welcome_message);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(KEY_USER_NAME, "");
        if (!name.isEmpty()) {
            tvWelcome.setText(getString(R.string.welcome_personalized, name) + "\nJak się dzisiaj czujesz?");
        }

        // Obsługa czatu
        EditText etChatInput = findViewById(R.id.et_chat_input);
        // W przyszłości tutaj dodamy obsługę wysyłania wiadomości do AI

        // Nawigacja dolna
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_start);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_start) {
                return true;
            } else if (itemId == R.id.nav_assistant) {
                Toast.makeText(this, "Asystent (Wkrótce)", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                // Nie używamy tu finish(), aby użytkownik mógł wrócić do MainActivity
                return true;
            }
            return false;
        });

        // Wylogowanie - teraz przekierowuje do SplashActivity (która zainicjuje nowe anonimowe konto)
        Button btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                // Czyścimy lokalny onboarding przy wylogowaniu
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                finishAffinity();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Jeśli nikt nie jest zalogowany - odsyłamy do SplashActivity
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }
    }
}