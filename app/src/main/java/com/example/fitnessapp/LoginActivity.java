package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    // Zaktualizowane nazwy zmiennych pod nowe przyciski
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private LinearLayout btnGoogle; // Twój przycisk Google to LinearLayout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // UWAGA: Zaktualizowane ID, aby pasowały do Twojego nowego pliku XML!
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnGoogle = findViewById(R.id.btn_google);

        // 1. Logika przycisku LOGOWANIA
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Przejście do głównego ekranu po udanym logowaniu
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Błąd logowania: Sprawdź dane!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 2. Logika przycisku REJESTRACJI
        btnRegister.setOnClickListener(v -> {
            // Przenosi użytkownika do ekranu rejestracji
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // 3. Logika przycisku GOOGLE (Tymczasowa)
        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Logowanie Google dodamy wkrótce!", Toast.LENGTH_SHORT).show();
        });
    }
}