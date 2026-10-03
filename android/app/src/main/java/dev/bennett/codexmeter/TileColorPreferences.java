package dev.bennett.codexmeter;

import android.content.Context;
import android.content.SharedPreferences;
import dev.bennett.codexmeter.wear.PhoneWearSync;

/** Phone-owned, global Tile appearance; contains only percentage thresholds. */
public final class TileColorPreferences {
    private TileColorPreferences() { }
    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences("codex_meter_tile_colors_v1", 0);
    }
    public static QuotaColorBands get(Context context) {
        SharedPreferences prefs = prefs(context);
        return new QuotaColorBands(prefs.getInt("low", QuotaColorBands.DEFAULT_LOW),
                prefs.getInt("sufficient", QuotaColorBands.DEFAULT_SUFFICIENT));
    }
    public static boolean dynamicColorsEnabled(Context context) {
        return prefs(context).getBoolean("watch_dynamic_colors", false);
    }
    public static void setDynamicColorsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean("watch_dynamic_colors", enabled).apply();
        PhoneWearSync.pushSettings(context);
    }
    public static void save(Context context, int low, int sufficient) {
        if (!QuotaColorBands.isValid(low, sufficient))
            throw new IllegalArgumentException("Require 1 <= low < sufficient <= 99");
        prefs(context).edit().putInt("low", low).putInt("sufficient", sufficient).apply();
        PhoneWearSync.pushSettings(context);
    }
}
