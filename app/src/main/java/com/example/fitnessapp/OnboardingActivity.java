package com.example.fitnessapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class OnboardingActivity extends AppCompatActivity {

    private int currentStep = 0; // Zmiana na indeksowanie od 0

    private FrameLayout iconContainer;
    private ImageView onboardingIcon;
    private TextView onboardingTitle;
    private TextView onboardingDesc;
    private View[] dots; // Zamiast 3 oddzielnych zmiennych
    private Button btnNext;
=
    private final int[] bgColors = {R.color.blue_primary, R.color.purple_bg, R.color.orange_bg};
    private final int[] icons = {R.drawable.ic_onboarding_1, R.drawable.ic_onboarding_2, R.drawable.ic_onboarding_3};
    private final int[] titles = {R.string.onboarding_1_title, R.string.onboarding_2_title, R.string.onboarding_3_title};
    private final int[] descs = {R.string.onboarding_1_desc, R.string.onboarding_2_desc, R.string.onboarding_3_desc};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        iconContainer = findViewById(R.id.icon_container);
        onboardingIcon = findViewById(R.id.onboarding_icon);
        onboardingTitle = findViewById(R.id.onboarding_title);
        onboardingDesc = findViewById(R.id.onboarding_desc);
        btnNext = findViewById(R.id.btn_next);
=
        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3)
        };

        btnNext.setOnClickListener(v -> {
            if (currentStep < dots.length - 1) { // Dynamiczne sprawdzanie limitu na podstawie ilości kropek
                currentStep++;
                updateStep();
            } else {
                startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void updateStep() {
        int activeDotColor = ContextCompat.getColor(this, R.color.blue_primary);
        int inactiveDotColor = ContextCompat.getColor(this, R.color.gray_dot);
=
        iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, bgColors[currentStep])));
        onboardingIcon.setImageResource(icons[currentStep]);
        onboardingTitle.setText(titles[currentStep]);
        onboardingDesc.setText(descs[currentStep]);
=
        for (int i = 0; i < dots.length; i++) {
            int colorToSet = (i == currentStep) ? activeDotColor : inactiveDotColor;
            dots[i].setBackgroundTintList(ColorStateList.valueOf(colorToSet));
        }
=
        if (currentStep == dots.length - 1) {
            btnNext.setText(R.string.start_now);
        } else {
            btnNext.setText(R.string.next);
        }
    }
}