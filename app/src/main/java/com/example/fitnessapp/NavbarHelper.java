package com.example.fitnessapp;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import android.graphics.Color;

public class NavbarHelper {

    public static void initNavbar(Activity activity) {
        View homeTab = activity.findViewById(R.id.nav_home);
        View planTab = activity.findViewById(R.id.nav_plan);
        View settingsTab = activity.findViewById(R.id.nav_settings);

        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                if (!(activity instanceof ChoiceActivity)) {
                    activity.startActivity(new Intent(activity, ChoiceActivity.class));
                    if (!(activity instanceof MainActivity)) activity.finish();
                }
            });
        }

        if (planTab != null) {
            planTab.setOnClickListener(v -> {
                if (!(activity instanceof MainActivity)) {
                    activity.startActivity(new Intent(activity, MainActivity.class));
                    activity.finish();
                }
            });
        }

        if (settingsTab != null) {
            settingsTab.setOnClickListener(v -> {
                if (!(activity instanceof SettingsActivity)) {
                    activity.startActivity(new Intent(activity, SettingsActivity.class));
                    if (!(activity instanceof MainActivity)) activity.finish();
                }
            });
        }

        highlightCurrentTab(activity);
    }

    private static void highlightCurrentTab(Activity activity) {
        int activeColor = Color.parseColor("#004A99");
        int inactiveColor = Color.parseColor("#64748B");
        int activeBg = Color.parseColor("#DBEAFE");
        int inactiveBg = Color.TRANSPARENT;

        if (activity instanceof ChoiceActivity) {
            setTabActive(activity, R.id.nav_home_indicator, R.id.nav_home_icon, R.id.nav_home_text, activeColor, activeBg);
            setTabInactive(activity, R.id.nav_plan_indicator, R.id.nav_plan_icon, R.id.nav_plan_text, inactiveColor, inactiveBg);
            setTabInactive(activity, R.id.nav_settings_indicator, R.id.nav_settings_icon, R.id.nav_settings_text, inactiveColor, inactiveBg);
        } else if (activity instanceof MainActivity) {
            setTabInactive(activity, R.id.nav_home_indicator, R.id.nav_home_icon, R.id.nav_home_text, inactiveColor, inactiveBg);
            setTabActive(activity, R.id.nav_plan_indicator, R.id.nav_plan_icon, R.id.nav_plan_text, activeColor, activeBg);
            setTabInactive(activity, R.id.nav_settings_indicator, R.id.nav_settings_icon, R.id.nav_settings_text, inactiveColor, inactiveBg);
        } else if (activity instanceof SettingsActivity) {
            setTabInactive(activity, R.id.nav_home_indicator, R.id.nav_home_icon, R.id.nav_home_text, inactiveColor, inactiveBg);
            setTabInactive(activity, R.id.nav_plan_indicator, R.id.nav_plan_icon, R.id.nav_plan_text, inactiveColor, inactiveBg);
            setTabActive(activity, R.id.nav_settings_indicator, R.id.nav_settings_icon, R.id.nav_settings_text, activeColor, activeBg);
        }
    }

    private static void setTabActive(Activity activity, int indicatorId, int iconId, int textId, int color, int bgColor) {
        CardView indicator = activity.findViewById(indicatorId);
        ImageView icon = activity.findViewById(iconId);
        TextView text = activity.findViewById(textId);

        if (indicator != null) indicator.setCardBackgroundColor(bgColor);
        if (icon != null) icon.setColorFilter(color);
        if (text != null) {
            text.setTextColor(color);
            text.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private static void setTabInactive(Activity activity, int indicatorId, int iconId, int textId, int color, int bgColor) {
        CardView indicator = activity.findViewById(indicatorId);
        ImageView icon = activity.findViewById(iconId);
        TextView text = activity.findViewById(textId);

        if (indicator != null) indicator.setCardBackgroundColor(bgColor);
        if (icon != null) icon.setColorFilter(color);
        if (text != null) {
            text.setTextColor(color);
            text.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }
}