package com.example.fitnessapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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

    // Dane tymczasowe zbierane podczas onboardingu
    private String userName = "";
    private boolean canStand = false, canExerciseFloor = false, needsChair = false, canExerciseBed = false, canExerciseSitting = false;
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
                findViewById(R.id.dot4)
        };

        OnboardingAdapter adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // Blokujemy swipe

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentStep = position;
                updateStepUI();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentStep < dots.length - 1) {
                viewPager.setCurrentItem(currentStep + 1);
            } else {
                completeOnboarding();
            }
        });
    }

    private void updateStepUI() {
        int activeDotColor = ContextCompat.getColor(this, R.color.blue_primary);
        int inactiveDotColor = ContextCompat.getColor(this, R.color.gray_dot);

        for (int i = 0; i < dots.length; i++) {
            int colorToSet = (i == currentStep) ? activeDotColor : inactiveDotColor;
            dots[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorToSet));
        }

        if (currentStep == dots.length - 1) {
            btnNext.setText(R.string.finish);
        } else {
            btnNext.setText(R.string.next);
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
        editor.putString("goal", goal);
        editor.putStringSet("conditions", conditions);
        editor.apply();

        startActivity(new Intent(OnboardingActivity.this, ChoiceActivity.class));
        finish();
    }

    private void updateCheckboxCard(CardView card, ImageView indicator, boolean isChecked) {
        if (isChecked) {
            card.setCardBackgroundColor(0xFF057A32); // Success green
            indicator.setImageResource(R.drawable.bg_indicator_active);
            card.setTag("checked");
        } else {
            card.setCardBackgroundColor(0xFFDBEAFE); // Light blue
            indicator.setImageResource(R.drawable.bg_indicator_inactive);
            card.setTag("unchecked");
        }
    }

    private class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

        private final int[] layouts = {
                R.layout.item_onboarding_step1,
                R.layout.item_onboarding_step2,
                R.layout.item_onboarding_step4,
                R.layout.item_onboarding_step5
        };

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new OnboardingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            if (position == 0) {
                // Step 1 – imię
                EditText etName = holder.itemView.findViewById(R.id.et_name);
                etName.setText(userName);
                if (holder.nameWatcher != null) {
                    etName.removeTextChangedListener(holder.nameWatcher);
                }
                holder.nameWatcher = new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        userName = s.toString().trim();
                    }
                    @Override public void afterTextChanged(Editable s) {}
                };
                etName.addTextChangedListener(holder.nameWatcher);

            } else if (position == 1) {
                // Step 2 – pozycje
                CardView cardQ1 = holder.itemView.findViewById(R.id.card_q1);
                CardView cardQ2 = holder.itemView.findViewById(R.id.card_q2);
                CardView cardQ3 = holder.itemView.findViewById(R.id.card_q3);
                CardView cardQ4 = holder.itemView.findViewById(R.id.card_q4);
                CardView cardQ5 = holder.itemView.findViewById(R.id.card_q5);
                ImageView ivQ1 = holder.itemView.findViewById(R.id.iv_q1);
                ImageView ivQ2 = holder.itemView.findViewById(R.id.iv_q2);
                ImageView ivQ3 = holder.itemView.findViewById(R.id.iv_q3);
                ImageView ivQ4 = holder.itemView.findViewById(R.id.iv_q4);
                ImageView ivQ5 = holder.itemView.findViewById(R.id.iv_q5);
                CardView btnAllPositions = holder.itemView.findViewById(R.id.btn_all_positions);

                updateCheckboxCard(cardQ1, ivQ1, canStand);
                updateCheckboxCard(cardQ2, ivQ2, canExerciseFloor);
                updateCheckboxCard(cardQ3, ivQ3, needsChair);
                updateCheckboxCard(cardQ4, ivQ4, canExerciseBed);
                updateCheckboxCard(cardQ5, ivQ5, canExerciseSitting);

                btnAllPositions.setOnClickListener(v -> {
                    canStand = true;
                    canExerciseFloor = true;
                    needsChair = true;
                    canExerciseBed = true;
                    canExerciseSitting = true;
                    updateCheckboxCard(cardQ1, ivQ1, canStand);
                    updateCheckboxCard(cardQ2, ivQ2, canExerciseFloor);
                    updateCheckboxCard(cardQ3, ivQ3, needsChair);
                    updateCheckboxCard(cardQ4, ivQ4, canExerciseBed);
                    updateCheckboxCard(cardQ5, ivQ5, canExerciseSitting);
                });

                cardQ1.setOnClickListener(v -> {
                    canStand = !canStand;
                    updateCheckboxCard(cardQ1, ivQ1, canStand);
                });
                cardQ2.setOnClickListener(v -> {
                    canExerciseFloor = !canExerciseFloor;
                    updateCheckboxCard(cardQ2, ivQ2, canExerciseFloor);
                });
                cardQ3.setOnClickListener(v -> {
                    needsChair = !needsChair;
                    updateCheckboxCard(cardQ3, ivQ3, needsChair);
                });
                cardQ4.setOnClickListener(v -> {
                    canExerciseBed = !canExerciseBed;
                    updateCheckboxCard(cardQ4, ivQ4, canExerciseBed);
                });
                cardQ5.setOnClickListener(v -> {
                    canExerciseSitting = !canExerciseSitting;
                    updateCheckboxCard(cardQ5, ivQ5, canExerciseSitting);
                });

            } else if (position == 2) {
                // Step 3 – cel
                RadioGroup rgGoal = holder.itemView.findViewById(R.id.rg_goal);
                int goalId;
                switch (goal) {
                    case "równowaga": goalId = R.id.rb_goal_balance; break;
                    case "mobilność": goalId = R.id.rb_goal_mobility; break;
                    case "kardio": goalId = R.id.rb_goal_cardio; break;
                    case "postura": goalId = R.id.rb_goal_posture; break;
                    case "mieszana": goalId = R.id.rb_goal_mixed; break;
                    default: goalId = R.id.rb_goal_strength; break;
                }
                rgGoal.check(goalId);
                rgGoal.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.rb_goal_strength) goal = "siła";
                    else if (checkedId == R.id.rb_goal_balance) goal = "równowaga";
                    else if (checkedId == R.id.rb_goal_mobility) goal = "mobilność";
                    else if (checkedId == R.id.rb_goal_cardio) goal = "kardio";
                    else if (checkedId == R.id.rb_goal_posture) goal = "postura";
                    else if (checkedId == R.id.rb_goal_mixed) goal = "mieszana";
                });

            } else if (position == 3) {
                // Step 4 – schorzenia
                CheckBox cbNoConditions = holder.itemView.findViewById(R.id.cb_no_conditions);
                ViewGroup container = holder.itemView.findViewById(R.id.conditions_container);

                // Ustaw stany checkboxów
                cbNoConditions.setChecked(conditions.isEmpty());
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof CheckBox && child.getId() != R.id.cb_no_conditions) {
                        CheckBox cb = (CheckBox) child;
                        cb.setChecked(conditions.contains(cb.getText().toString()));
                    }
                }

                cbNoConditions.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        conditions.clear();
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
                        CheckBox cb = (CheckBox) child;
                        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                cbNoConditions.setChecked(false);
                                conditions.add(cb.getText().toString());
                            } else {
                                conditions.remove(cb.getText().toString());
                            }
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
            TextWatcher nameWatcher;

            public OnboardingViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
