package com.example.fitnessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

public class EditConditionsActivity extends AppCompatActivity {

    private CheckBox cbNoConditions;
    private ViewGroup container;
    private Set<String> conditions = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_conditions);

        cbNoConditions = findViewById(R.id.cb_no_conditions);
        container = findViewById(R.id.conditions_container);

        loadCurrentConditions();

        // Logic for "No conditions" checkbox
        cbNoConditions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof CheckBox && child.getId() != R.id.cb_no_conditions) {
                        ((CheckBox) child).setChecked(false);
                    }
                }
            }
        });

        // Logic for other checkboxes
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof CheckBox && child.getId() != R.id.cb_no_conditions) {
                ((CheckBox) child).setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) cbNoConditions.setChecked(false);
                });
            }
        }

        findViewById(R.id.btn_save_conditions).setOnClickListener(v -> saveConditions());
    }

    private void loadCurrentConditions() {
        SharedPreferences prefs = getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE);
        Set<String> savedConditions = prefs.getStringSet("conditions", new HashSet<>());

        if (savedConditions.isEmpty()) {
            cbNoConditions.setChecked(true);
        } else {
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox cb = (CheckBox) child;
                    if (savedConditions.contains(cb.getText().toString())) {
                        cb.setChecked(true);
                    }
                }
            }
        }
    }

    private void saveConditions() {
        conditions.clear();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
                if (cb.isChecked() && cb.getId() != R.id.cb_no_conditions) {
                    conditions.add(cb.getText().toString());
                }
            }
        }

        // Save to SharedPreferences only
        SharedPreferences prefs = getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putStringSet("conditions", conditions).apply();

        Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT).show();
        finish();
    }
}
