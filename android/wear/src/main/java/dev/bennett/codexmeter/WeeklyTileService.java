package dev.bennett.codexmeter;

import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters;
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement;
import androidx.wear.protolayout.ProtoLayoutScope;

public final class WeeklyTileService extends CodexTileService {
    @Override
    protected LayoutElement tileLayout(android.content.Context context, DeviceParameters deviceParameters, ProtoLayoutScope scope) {
        // Free-tier accounts report a monthly window instead of a weekly one; the tile
        // follows whichever long-cadence window the subscription currently has.
        return CodexTileLayouts.progress(context, deviceParameters,
                CodexTileLayouts.longWindowLabel(context),
                CodexTileLayouts.longWindow(context), scope);
    }
}
