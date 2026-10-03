package dev.bennett.codexmeter;

import android.content.Context;
import android.content.ContextWrapper;
import dev.bennett.codexmeter.wear.WearUsageState;

/** Read-only account view for one tile instance; never changes watch-wide selection. */
final class TileAccountContext extends ContextWrapper {
    final String service;
    final int tileId;
    TileAccountContext(Context base, String service, int tileId) {
        super(base); this.service = service; this.tileId = tileId;
    }
    String selection() {
        String id = getSharedPreferences("tile_account_bindings", 0).getString(key(), "");
        WearUsageState state = WearPreferences.accountState(this);
        return dev.bennett.codexmeter.wear.TileAccountBinding.resolve(id, state,
                WearPreferences.selectedAccountId(getBaseContext()));
    }
    String key() { return dev.bennett.codexmeter.wear.TileAccountBinding.key(service, tileId); }
    void bind(String id) {
        if (!getSharedPreferences("tile_account_bindings", 0).edit().putString(key(), id).commit())
            throw new IllegalStateException("Could not save tile account");
    }
}
