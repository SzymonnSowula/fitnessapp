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

    private int currentStep = 1;
    private FrameLayout iconContainer;
    private ImageView onboardingIcon;
    private TextView onboardingTitle;
    private TextView onboardingDesc;
    private View dot1, dot2, dot3;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        iconContainer = findViewById(R.id.icon_container);
        onboardingIcon = findViewById(R.id.onboarding_icon);
        onboardingTitle = findViewById(R.id.onboarding_title);
        onboardingDesc = findViewById(R.id.onboarding_desc);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        btnNext = findViewById(R.id.btn_next);

        btnNext.setOnClickListener(v -> {
            if (currentStep < 3) {
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

        if (currentStep == 2) {
            iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_bg)));
            onboardingIcon.setImageResource(R.drawable.ic_onboarding_2);
            onboardingTitle.setText(R.string.onboarding_2_title);
            onboardingDesc.setText(R.string.onboarding_2_desc);
            
            dot1.setBackgroundTintList(ColorStateList.valueOf(inactiveDotColor));
            dot2.setBackgroundTintList(ColorStateList.valueOf(activeDotColor));
            dot3.setBackgroundTintList(ColorStateList.valueOf(inactiveDotColor));
            
            btnNext.setText(R.string.next);
        } else if (currentStep == 3) {
            iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange_bg)));
            onboardingIcon.setImageResource(R.drawable.ic_onboarding_3);
            onboardingTitle.setText(R.string.onboarding_3_title);
            onboardingDesc.setText(R.string.onboarding_3_desc);
            
            dot1.setBackgroundTintList(ColorStateList.valueOf(inactiveDotColor));
            dot2.setBackgroundTintList(ColorStateList.valueOf(inactiveDotColor));
            dot3.setBackgroundTintList(ColorStateList.valueOf(activeDotColor));
            
            btnNext.setText(R.string.start_now);
        }
    }
}