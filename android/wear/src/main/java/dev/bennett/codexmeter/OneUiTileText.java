package dev.bennett.codexmeter;

import android.content.Context;
import androidx.wear.protolayout.ColorBuilders;
import androidx.wear.protolayout.DimensionBuilders;
import androidx.wear.protolayout.LayoutElementBuilders;
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement;
import androidx.wear.protolayout.ProtoLayoutScope;

/** Native text stays sharp at the renderer's screen density and accessibility font scale. */
final class OneUiTileText {
    OneUiTileText(Context context, ProtoLayoutScope scope) {
        // Keep the shared tile API; the renderer now owns font metrics and resources.
    }

    LayoutElement element(String value, float sizeSp, int color, int weight) {
        return new LayoutElementBuilders.Text.Builder()
                .setText(value == null ? "" : value)
                .setFontStyle(new LayoutElementBuilders.FontStyle.Builder()
                        .setSize(DimensionBuilders.sp(sizeSp))
                        .setColor(ColorBuilders.argb(color))
                        .setWeight(weight)
                        .setPreferredFontFamilies("sec", "sans-serif")
                        .build())
                .setMaxLines(1)
                .build();
    }
}
