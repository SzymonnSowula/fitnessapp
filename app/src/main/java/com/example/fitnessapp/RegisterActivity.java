package com.example.fitnessapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Zauważ: usunięty import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnDoRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicjalizacja widoków
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnDoRegister = findViewById(R.id.btn_do_register);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        // 1. Logika przycisku REJESTRACJI (Tymczasowa / Symulacja)
        btnDoRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, R.string.error_password_too_short, Toast.LENGTH_SHORT).show();
                return;
            }

            // TYMCZASOWA REJESTRACJA LOKALNA:
            // Udajemy, że rejestracja się powiodła. Gdy stworzymy lokalną bazę danych (Room),
            // w tym miejscu dodamy kod zapisujący użytkownika do pamięci telefonu.
            Toast.makeText(this, R.string.success_account_created, Toast.LENGTH_LONG).show();
            finish(); // Zamyka ekran rejestracji i wraca do logowania
        });

        // 2. Powrót do logowania
        tvBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }
}