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

public class MainActivity extends AppCompatActivity {

    // 1. Deklaracja zmiennej autoryzacji
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Inicjalizacja Firebase (musi być przed jakimkolwiek użyciem mAuth!)
        mAuth = FirebaseAuth.getInstance();

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

        // Wylogowanie
        Button btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) { // Zabezpieczenie na wypadek braku przycisku w XML
            btnLogout.setOnClickListener(v -> {
                // 3. Złota zasada: Najpierw wylogowujemy z bazy Firebase!
                mAuth.signOut();

                // Następnie przenosimy do ekranu logowania i zamykamy MainActivity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            });
        }
    }

    // 4. Prawidłowe miejsce na sprawdzanie, czy użytkownik jest zalogowany
    @Override
    protected void onStart() {
        super.onStart();

        // Pobieramy aktualnego użytkownika
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Jeśli jest nullem, to znaczy, że nie jest zalogowany
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Zamykamy MainActivity, żeby nie miał do niego dostępu
        }
    }
}