package com.example.fitnessapp.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

/**
 * Utility class for screen dimension calculations and responsive UI scaling.
 * Follows Android_UI_Standards from BRAIN.
 */
public class ScreenUtils {

    private ScreenUtils() { }

    /**
     * Get the DisplayMetrics for the current context.
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(metrics);
        }
        return metrics;
    }

    /**
     * Convert dp to pixels.
     */
    public static int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * Convert pixels to dp.
     */
    public static float pxToDp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    /**
     * Convert sp to pixels.
     */
    public static int spToPx(Context context, float sp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * Get screen width in pixels.
     */
    public static int getScreenWidthPx(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    /**
     * Get screen height in pixels.
     */
    public static int getScreenHeightPx(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    /**
     * Check if the device is a tablet (smallest width >= 600dp).
     */
    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }
}
