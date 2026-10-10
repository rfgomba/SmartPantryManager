package com.smartpantry.manager.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Reads and writes the user's settings using SharedPreferences
 */
public final class AppSettings {

    private static final String PREFS_NAME = "smart_pantry_settings";

    private static final String KEY_EXCLUDE_EXPIRED = "exclude_expired";
    private static final String KEY_SHOW_ALMOST_THERE = "show_almost_there";
    private static final String KEY_HIGHLIGHT_EXPIRING = "highlight_expiring";
    private static final String KEY_WARN_DAYS = "warn_days";

    public static final int DEFAULT_WARN_DAYS = 3;

    private AppSettings() {
        // Utility class: no instances
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isExcludeExpired(Context context) {
        return prefs(context).getBoolean(KEY_EXCLUDE_EXPIRED, true);
    }

    public static void setExcludeExpired(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_EXCLUDE_EXPIRED, value).apply();
    }

    public static boolean isShowAlmostThere(Context context) {
        return prefs(context).getBoolean(KEY_SHOW_ALMOST_THERE, true);
    }

    public static void setShowAlmostThere(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_SHOW_ALMOST_THERE, value).apply();
    }

    public static boolean isHighlightExpiring(Context context) {
        return prefs(context).getBoolean(KEY_HIGHLIGHT_EXPIRING, true);
    }

    public static void setHighlightExpiring(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_HIGHLIGHT_EXPIRING, value).apply();
    }

    public static int getWarnDays(Context context) {
        return prefs(context).getInt(KEY_WARN_DAYS, DEFAULT_WARN_DAYS);
    }

    public static void setWarnDays(Context context, int days) {
        prefs(context).edit().putInt(KEY_WARN_DAYS, days).apply();
    }
}
