package dev.bennett.codexmeter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

/** Uses Wear's local system palette only when both app and platform opt in. */
final class WearTheme {
    private WearTheme() { }

    static boolean isDynamic(Context context) {
        if (!WearPreferences.dynamicColorsEnabled(context) || Build.VERSION.SDK_INT < 35)
            return false;
        try {
            // Same platform availability gate as AndroidX Wear Material 3.
            return Settings.Global.getInt(context.getContentResolver(),
                    "dynamic_color_theme_enabled", 0) == 1;
        } catch (SecurityException unavailable) {
            return false;
        }
    }

    static void apply(Activity activity) {
        activity.setTheme(isDynamic(activity) ? R.style.AppTheme_DynamicColors : R.style.AppTheme);
    }

    static int primary(Context context, int fallback) {
        return Build.VERSION.SDK_INT >= 35 && isDynamic(context)
                ? context.getColor(android.R.color.system_primary_fixed) : fallback;
    }

    static int container(Context context, int fallback) {
        return Build.VERSION.SDK_INT >= 35 && isDynamic(context)
                ? context.getColor(android.R.color.system_primary_container_dark) : fallback;
    }

    static int color(Context context, int attribute, int fallbackResource) {
        android.util.TypedValue value = new android.util.TypedValue();
        return context.getTheme().resolveAttribute(attribute, value, true)
                ? value.data : context.getColor(fallbackResource);
    }

    static int signature(Context context) {
        return primary(context, 0);
    }
}
