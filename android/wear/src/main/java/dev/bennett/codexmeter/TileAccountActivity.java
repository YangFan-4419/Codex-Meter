package dev.bennett.codexmeter;

import android.app.Activity;
import android.os.Bundle;
import android.app.AlertDialog;
import dev.bennett.codexmeter.wear.WearUsageState;

/** A tap configures only this tile. OAuth and refresh remain phone-owned. */
public final class TileAccountActivity extends Activity {
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        WearTheme.apply(this);
        String service = getIntent().getStringExtra("tile_service");
        int id = getIntent().getIntExtra("tile_id", -1);
        java.util.List<Class<? extends CodexTileService>> providers = java.util.List.of(
                UsageOverviewTileService.class, FiveHourTileService.class,
                WeeklyTileService.class, ResetCountdownTileService.class, MonitorStatusTileService.class);
        Class<? extends CodexTileService> provider = providers.stream()
                .filter(type -> type.getName().equals(service)).findFirst().orElse(null);
        if (provider == null || id < 0) { finish(); return; }
        TileAccountContext tile = new TileAccountContext(this, service, id);
        WearUsageState state = WearPreferences.accountState(this);
        if (state == null || state.accounts.isEmpty()) { finish(); return; }
        String bound = getSharedPreferences("tile_account_bindings", 0).getString(tile.key(), "");
        String[] labels = new String[state.accounts.size() + 1];
        labels[0] = "Follow watch selection";
        int selected = 0;
        for (int i = 0; i < state.accounts.size(); i++) {
            labels[i + 1] = state.accounts.get(i).displayName;
            if (bound.equals(state.accounts.get(i).accountId)) selected = i + 1;
        }
        new AlertDialog.Builder(this).setTitle("This tile’s account")
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    tile.bind(which == 0 ? "" : state.accounts.get(which - 1).accountId);
                    androidx.wear.tiles.TileService.getUpdater(this).requestUpdate(provider);
                    dialog.dismiss(); finish();
                }).setOnCancelListener(dialog -> finish()).show();
    }
}
