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
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.HashSet;
import java.util.Set;

public class OnboardingActivity extends AppCompatActivity {

    private int currentStep = 0;
    private ViewPager2 viewPager;
    private View[] dots;
    private Button btnNext;

    private static final String PREFS_NAME = "FitnessAppPrefs";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String KEY_USER_NAME = "user_name";

    // Pamięć tymczasowa na dane podczas onboardingu
    private String userName = "";
    private boolean canStand = false, canExerciseFloor = false, needsChair = false, canExerciseBed = false, canExerciseSitting = false;
    private int intensity = 1, difficulty = 1;
    private String goal = "siła";
    private Set<String> conditions = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btn_next);

        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4),
                findViewById(R.id.dot5)
        };

        OnboardingAdapter adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // Blokujemy swipe, aby wymusić stepper

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentStep = position;
                updateStepUI();
            }
        });

        btnNext.setOnClickListener(v -> {
            saveCurrentStepData();
            if (currentStep < dots.length - 1) {
                viewPager.setCurrentItem(currentStep + 1);
            } else {
                completeOnboarding();
                startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void saveCurrentStepData() {
        OnboardingAdapter.OnboardingViewHolder holder = (OnboardingAdapter.OnboardingViewHolder) 
                ((RecyclerView) viewPager.getChildAt(0)).findViewHolderForAdapterPosition(currentStep);
        
        if (holder == null) return;

        switch (currentStep) {
            case 0:
                EditText etName = holder.itemView.findViewById(R.id.et_name);
                if (etName != null) userName = etName.getText().toString().trim();
                break;
            case 1:
                canStand = ((SwitchMaterial) holder.itemView.findViewById(R.id.sw_q1)).isChecked();
                canExerciseFloor = ((SwitchMaterial) holder.itemView.findViewById(R.id.sw_q2)).isChecked();
                needsChair = ((SwitchMaterial) holder.itemView.findViewById(R.id.sw_q3)).isChecked();
                canExerciseBed = ((SwitchMaterial) holder.itemView.findViewById(R.id.sw_q4)).isChecked();
                canExerciseSitting = ((SwitchMaterial) holder.itemView.findViewById(R.id.sw_q5)).isChecked();
                break;
            case 2:
                intensity = ((RadioButton) holder.itemView.findViewById(R.id.rb_intensity_1)).isChecked() ? 1 : 2;
                difficulty = ((RadioButton) holder.itemView.findViewById(R.id.rb_difficulty_1)).isChecked() ? 1 : 2;
                break;
            case 3:
                int checkedGoalId = ((RadioGroup) holder.itemView.findViewById(R.id.rg_goal)).getCheckedRadioButtonId();
                if (checkedGoalId == R.id.rb_goal_strength) goal = "siła";
                else if (checkedGoalId == R.id.rb_goal_balance) goal = "równowaga";
                else if (checkedGoalId == R.id.rb_goal_mobility) goal = "mobilność";
                else if (checkedGoalId == R.id.rb_goal_cardio) goal = "kardio";
                else if (checkedGoalId == R.id.rb_goal_posture) goal = "postura";
                else if (checkedGoalId == R.id.rb_goal_mixed) goal = "mieszana";
                break;
            case 4:
                conditions.clear();
                ViewGroup container = holder.itemView.findViewById(R.id.conditions_container);
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof CheckBox) {
                        CheckBox cb = (CheckBox) child;
                        if (cb.isChecked() && cb.getId() != R.id.cb_no_conditions) {
                            conditions.add(cb.getText().toString());
                        }
                    }
                }
                break;
        }
    }

    private void completeOnboarding() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, true);
        editor.putString(KEY_USER_NAME, userName);
        editor.putBoolean("can_stand", canStand);
        editor.putBoolean("can_exercise_floor", canExerciseFloor);
        editor.putBoolean("needs_chair", needsChair);
        editor.putBoolean("can_exercise_bed", canExerciseBed);
        editor.putBoolean("can_exercise_sitting", canExerciseSitting);
        editor.putInt("intensity", intensity);
        editor.putInt("difficulty", difficulty);
        editor.putString("goal", goal);
        editor.putStringSet("conditions", conditions);
        editor.apply();

        startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
        finish();
    }

    private void updateStepUI() {
        int activeDotColor = ContextCompat.getColor(this, R.color.blue_primary);
        int inactiveDotColor = ContextCompat.getColor(this, R.color.gray_dot);

        for (int i = 0; i < dots.length; i++) {
            int colorToSet = (i == currentStep) ? activeDotColor : inactiveDotColor;
            dots[i].setBackgroundTintList(ColorStateList.valueOf(colorToSet));
        }

        if (currentStep == dots.length - 1) {
            btnNext.setText(R.string.finish);
        } else {
            btnNext.setText(R.string.next);
        }
    }

    private class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

        private final int[] layouts = {
                R.layout.item_onboarding_step1,
                R.layout.item_onboarding_step2,
                R.layout.item_onboarding_step3,
                R.layout.item_onboarding_step4,
                R.layout.item_onboarding_step5
        };

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OnboardingViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            if (position == 4) {
                CheckBox cbNoConditions = holder.itemView.findViewById(R.id.cb_no_conditions);
                ViewGroup container = holder.itemView.findViewById(R.id.conditions_container);
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
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof CheckBox && child.getId() != R.id.cb_no_conditions) {
                        ((CheckBox) child).setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) cbNoConditions.setChecked(false);
                        });
                    }
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return layouts[position];
        }

        @Override
        public int getItemCount() {
            return layouts.length;
        }

        class OnboardingViewHolder extends RecyclerView.ViewHolder {
            public OnboardingViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}