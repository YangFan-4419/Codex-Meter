package dev.bennett.codexmeter;

/** Remaining allowance bands shared by phone configuration and cached Wear rendering. */
public final class QuotaColorBands {
    public static final int DEFAULT_LOW = 20;
    public static final int DEFAULT_SUFFICIENT = 50;
    public static final int LOW_COLOR = 0xFFFF6B6B;
    public static final int MEDIUM_COLOR = 0xFFFFC56E;
    public static final int SUFFICIENT_COLOR = 0xFF6B6EE0;
    public final int low;
    public final int sufficient;

    public QuotaColorBands(int low, int sufficient) {
        boolean valid = isValid(low, sufficient);
        this.low = valid ? low : DEFAULT_LOW;
        this.sufficient = valid ? sufficient : DEFAULT_SUFFICIENT;
    }

    public static boolean isValid(int low, int sufficient) {
        return low >= 1 && low < sufficient && sufficient <= 99;
    }

    public int color(int remainingPercent) {
        return remainingPercent < low ? LOW_COLOR
                : remainingPercent < sufficient ? MEDIUM_COLOR : SUFFICIENT_COLOR;
    }
}
