package com.example.fitnessapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnDoRegister = findViewById(R.id.btn_do_register);
        btnDoRegister.setOnClickListener(v -> {
            Toast.makeText(this, "Rejestracja pomyślna!", Toast.LENGTH_SHORT).show();
            finish(); // Powrót do ekranu logowania
        });

        findViewById(R.id.tv_back_to_login).setOnClickListener(v -> finish());
    }
}