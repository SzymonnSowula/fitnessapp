package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Zauważ, że usunęliśmy import FirebaseAuth

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private LinearLayout btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicjalizacja widoków
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnGoogle = findViewById(R.id.btn_google);

        // 1. Logika przycisku LOGOWANIA (Przygotowana pod logowanie lokalne)
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            // TYMCZASOWE LOGOWANIE LOKALNE:
            // Żebyś mógł wejść do aplikacji, zanim zrobimy bazę danych.
            // Wpisz 'admin' w polu email i 'admin' w polu hasło.
            if (email.equals("admin") && password.equals("admin")) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish(); // Zamykamy ekran logowania, żeby użytkownik nie mógł do niego wrócić przyciskiem "Wstecz"
            } else {
                Toast.makeText(this, "Błędne dane! Spróbuj wpisać admin / admin", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Logika przycisku REJESTRACJI
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // 3. Logika przycisku GOOGLE
        // Skoro rezygnujemy z Google, ten przycisk docelowo powinieneś usunąć z pliku XML (activity_login.xml).
        // Na razie zostawiamy, żeby aplikacja się nie sypała, ale zmieniamy mu komunikat.
        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Zdecydowaliśmy się na logowanie lokalne (offline)!", Toast.LENGTH_SHORT).show();
        });
    }
}