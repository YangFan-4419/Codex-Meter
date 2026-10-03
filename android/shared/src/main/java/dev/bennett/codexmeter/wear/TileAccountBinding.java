package dev.bennett.codexmeter.wear;

/** Per-instance binding resolution uses only normalized account IDs, never credentials. */
public final class TileAccountBinding {
    private TileAccountBinding() { }
    public static String key(String service, int tileId) { return service + ":" + tileId; }
    public static String resolve(String bound, WearUsageState state, String watchSelection) {
        if (bound != null && !bound.isEmpty() && state != null)
            for (WearAccount account : state.accounts)
                if (bound.equals(account.accountId)) return bound;
        return watchSelection == null ? "" : watchSelection;
    }
}
