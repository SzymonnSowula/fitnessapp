package com.example.fitnessapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnDoRegister;
    private TextView tvBackToLogin; // Dodana zmienna dla tekstu powrotu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // Podpięcie zmiennych pod dokładne ID z Twojego pliku XML
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnDoRegister = findViewById(R.id.btn_do_register);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        // 1. Logika TWORZENIA KONTA
        btnDoRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Hasło musi mieć co najmniej 6 znaków", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Rejestracja pomyślna!", Toast.LENGTH_SHORT).show();
                            finish(); // Zamyka ekran rejestracji i cofa do logowania
                        } else {
                            String errorMessage = (task.getException() != null) ? task.getException().getMessage() : "Nieznany błąd";
                            Toast.makeText(this, "Błąd: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 2. Logika POWROTU DO LOGOWANIA
        tvBackToLogin.setOnClickListener(v -> {
            // Po prostu zamykamy ten ekran, pod spodem cały czas czeka ekran logowania
            finish();
        });
    }
}