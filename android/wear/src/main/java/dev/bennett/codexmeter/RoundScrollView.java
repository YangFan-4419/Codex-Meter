package dev.bennett.codexmeter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.ScrollView;

/** Round-screen emphasis computed only on scroll/layout, with no animation timer. */
public final class RoundScrollView extends ScrollView {
    public RoundScrollView(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @Override protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        updateEmphasis();
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateEmphasis();
    }

    private void updateEmphasis() {
        if (getHeight() == 0 || getChildCount() == 0
                || !(getChildAt(0) instanceof ViewGroup)) return;
        ViewGroup items = (ViewGroup) getChildAt(0);
        AccessibilityManager accessibility = getContext().getSystemService(AccessibilityManager.class);
        boolean scale = getResources().getConfiguration().isScreenRound()
                && (accessibility == null || !accessibility.isTouchExplorationEnabled());
        float half = getHeight() / 2f;
        for (int i = 0; i < items.getChildCount(); i++) {
            View item = items.getChildAt(i);
            if (item.getVisibility() == View.GONE) continue;
            float center = items.getTop() + item.getTop() + item.getHeight() / 2f - getScrollY();
            float distance = Math.abs(center - half) / half;
            // Central third stays full size; smoothly shrink toward the round edges.
            float edge = Math.max(0f, Math.min(1f, (distance - 0.35f) / 0.65f));
            float factor = scale ? 1f - 0.2f * edge * edge : 1f;
            item.setPivotX(item.getWidth() / 2f);
            item.setPivotY(item.getHeight() / 2f);
            item.setScaleX(factor);
            item.setScaleY(factor);
        }
    }
}
