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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    // Trackables dla step 2 (checkbox card views)
    private CardView cardQ1, cardQ2, cardQ3, cardQ4, cardQ5;
    private ImageView ivQ1, ivQ2, ivQ3, ivQ4, ivQ5;
    private CardView btnAllPositions;

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
                startActivity(new Intent(OnboardingActivity.this, ChoiceActivity.class));
                finish();
            }
        });
    }

    private void setupStep2ClickHandlers(View step2View) {
        // Przycisk "Mogę ćwiczyć w każdej pozycji"
        btnAllPositions = step2View.findViewById(R.id.btn_all_positions);
        
        cardQ1 = step2View.findViewById(R.id.card_q1);
        cardQ2 = step2View.findViewById(R.id.card_q2);
        cardQ3 = step2View.findViewById(R.id.card_q3);
        cardQ4 = step2View.findViewById(R.id.card_q4);
        cardQ5 = step2View.findViewById(R.id.card_q5);

        ivQ1 = step2View.findViewById(R.id.iv_q1);
        ivQ2 = step2View.findViewById(R.id.iv_q2);
        ivQ3 = step2View.findViewById(R.id.iv_q3);
        ivQ4 = step2View.findViewById(R.id.iv_q4);
        ivQ5 = step2View.findViewById(R.id.iv_q5);

        // Przycisk zaznacza wszystkie opcje
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

        // Klikalne karty
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
                // Dla step 2 dane są już aktualizowane przez click handlery
                // Wczytaj aktualne wartości z tagów kart
                canStand = "checked".equals(cardQ1.getTag());
                canExerciseFloor = "checked".equals(cardQ2.getTag());
                needsChair = "checked".equals(cardQ3.getTag());
                canExerciseBed = "checked".equals(cardQ4.getTag());
                canExerciseSitting = "checked".equals(cardQ5.getTag());
                break;
            case 2:
                int checkedGoalId = ((RadioGroup) holder.itemView.findViewById(R.id.rg_goal)).getCheckedRadioButtonId();
                if (checkedGoalId == R.id.rb_goal_strength) goal = "siła";
                else if (checkedGoalId == R.id.rb_goal_balance) goal = "równowaga";
                else if (checkedGoalId == R.id.rb_goal_mobility) goal = "mobilność";
                else if (checkedGoalId == R.id.rb_goal_cardio) goal = "kardio";
                else if (checkedGoalId == R.id.rb_goal_posture) goal = "postura";
                else if (checkedGoalId == R.id.rb_goal_mixed) goal = "mieszana";
                break;
            case 3:
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
        editor.putString("goal", goal);
        editor.putStringSet("conditions", conditions);
        editor.apply();

        // Dodatkowa synchronizacja z Firestore
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("onboardingCompleted", true);
            updates.put("name", userName);
            updates.put("can_stand", canStand);
            updates.put("can_exercise_floor", canExerciseFloor);
            updates.put("needs_chair", needsChair);
            updates.put("can_exercise_bed", canExerciseBed);
            updates.put("can_exercise_sitting", canExerciseSitting);
            updates.put("goal", goal);
            updates.put("conditions", new ArrayList<>(conditions));

            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .update(updates)
                    .addOnFailureListener(e -> {
                        android.util.Log.e("Onboarding", "Błąd synchronizacji Firestore: " + e.getMessage());
                    });
        }

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
            if (position == 1) {
                // Step 2 - setup click handlers
                setupStep2ClickHandlers(holder.itemView);
            } else if (position == 3) {
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
