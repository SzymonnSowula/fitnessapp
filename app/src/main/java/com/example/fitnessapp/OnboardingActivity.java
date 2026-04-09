package com.example.fitnessapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private int currentStep = 0;

    private ViewPager2 viewPager;
    private View[] dots;
    private Button btnNext;

    private final int[] bgColors = {R.color.blue_primary, R.color.purple_bg, R.color.orange_bg};
    private final int[] icons = {R.drawable.ic_onboarding_1, R.drawable.ic_onboarding_2, R.drawable.ic_onboarding_3};
    private final int[] titles = {R.string.onboarding_1_title, R.string.onboarding_2_title, R.string.onboarding_3_title};
    private final int[] descs = {R.string.onboarding_1_desc, R.string.onboarding_2_desc, R.string.onboarding_3_desc};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btn_next);

        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3)
        };

        OnboardingAdapter adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentStep = position;
                updateStep();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentStep < dots.length - 1) {
                viewPager.setCurrentItem(currentStep + 1);
            } else {
                startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void updateStep() {
        int activeDotColor = ContextCompat.getColor(this, R.color.blue_primary);
        int inactiveDotColor = ContextCompat.getColor(this, R.color.gray_dot);

        for (int i = 0; i < dots.length; i++) {
            int colorToSet = (i == currentStep) ? activeDotColor : inactiveDotColor;
            dots[i].setBackgroundTintList(ColorStateList.valueOf(colorToSet));
        }

        if (currentStep == dots.length - 1) {
            btnNext.setText(R.string.start_now);
        } else {
            btnNext.setText(R.string.next);
        }
    }

    private class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OnboardingViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        class OnboardingViewHolder extends RecyclerView.ViewHolder {
            private final FrameLayout iconContainer;
            private final ImageView onboardingIcon;
            private final TextView onboardingTitle;
            private final TextView onboardingDesc;

            public OnboardingViewHolder(@NonNull View itemView) {
                super(itemView);
                iconContainer = itemView.findViewById(R.id.icon_container);
                onboardingIcon = itemView.findViewById(R.id.onboarding_icon);
                onboardingTitle = itemView.findViewById(R.id.onboarding_title);
                onboardingDesc = itemView.findViewById(R.id.onboarding_desc);
            }

            public void bind(int position) {
                iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), bgColors[position])));
                onboardingIcon.setImageResource(icons[position]);
                onboardingTitle.setText(titles[position]);
                onboardingDesc.setText(descs[position]);
            }
        }
    }
}