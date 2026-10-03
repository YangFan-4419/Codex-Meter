package dev.bennett.codexmeter;

import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters;
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement;
import androidx.wear.protolayout.ProtoLayoutScope;

public final class UsageOverviewTileService extends CodexTileService {
    @Override
    protected LayoutElement tileLayout(android.content.Context context, DeviceParameters deviceParameters, ProtoLayoutScope scope) {
        return CodexTileLayouts.overview(context, deviceParameters, scope);
    }
}
