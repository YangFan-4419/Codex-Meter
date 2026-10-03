package dev.bennett.codexmeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Additive account management using the existing SESL page and card components. */
public final class AccountsActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean busy;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (AppConstants.ACTION_OAUTH_READY.equals(intent.getAction())) {
                String url = intent.getStringExtra(AppConstants.EXTRA_AUTH_URL);
                if (url != null && !url.isEmpty()) startActivity(new Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse(url)));
            } else {
                Toast.makeText(AccountsActivity.this,
                        intent.getStringExtra(AppConstants.EXTRA_MESSAGE), Toast.LENGTH_LONG).show();
                rebuild();
            }
        }
    };

    @Override protected void onCreate(Bundle state) {
        Ui.applySelectedTheme(this);
        super.onCreate(state);
        rebuild();
    }

    @Override protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(AppConstants.ACTION_OAUTH_READY);
        filter.addAction(AppConstants.ACTION_OAUTH_RESULT);
        androidx.core.content.ContextCompat.registerReceiver(this, receiver, filter,
                AppConstants.INTERNAL_PERMISSION, null,
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    @Override protected void onResume() { super.onResume(); rebuild(); }
    @Override protected void onStop() { unregisterReceiver(receiver); super.onStop(); }
    @Override protected void onDestroy() { executor.shutdown(); super.onDestroy(); }

    private void rebuild() {
        boolean dark = Ui.isDark(this);
        LinearLayout content = Ui.installPage(this, "Accounts", true).content;
        androidx.swiperefreshlayout.widget.SwipeRefreshLayout pull = findViewById(R.id.dashboard_refresh);
        pull.setEnabled(false);
        LinearLayout tools = Ui.cardGroup(this, dark);
        content.addView(tools);
        android.view.View all = Ui.actionRow(this, busy ? "Refreshing…" : "Refresh all accounts",
                "Update usage for your connected accounts", 0,
                view -> run(() -> UsageApi.refreshAllAndCache(getApplicationContext())));
        all.setEnabled(!busy);
        tools.addView(all);
        android.view.View add = Ui.actionRow(this, "Add account", "Sign in to another account", 0,
                view -> startForegroundService(new Intent(this, OAuthService.class)
                        .setAction(OAuthService.ACTION_START).putExtra("add_account", true)));
        add.setEnabled(!busy);
        tools.addView(add);
        String selected = AccountRepository.selectedId(this);
        for (AccountProfile account : AccountRepository.accounts(this)) {
            LinearLayout card = Ui.card(this, dark);
            content.addView(Ui.sectionTitle(this, account.label
                    + (account.id.equals(selected) ? " · Selected" : ""), dark));
            content.addView(card);
            UsageSnapshot snapshot = AccountRepository.snapshot(this, account.id);
            String quota = snapshot == null ? "No usage yet" : "5h: " + remaining(snapshot.fiveHour)
                    + " · " + (snapshot.longWindowIsMonthly() ? "Monthly" : "Weekly")
                    + ": " + remaining(snapshot.longWindow()) + "\n" + snapshot.planType;
            card.addView(Ui.text(this, quota, 16f, Ui.mainText(dark)));
            String session = account.isAuthenticated() ? "Connected" : "Sign-in required";
            if (snapshot != null) session += " · Updated " + android.text.format.DateUtils
                    .getRelativeTimeSpanString(snapshot.fetchedAtMillis, System.currentTimeMillis(),
                            android.text.format.DateUtils.MINUTE_IN_MILLIS);
            Ui.addSpacer(card, 8);
            card.addView(Ui.text(this, session, 13f, Ui.mainText(dark)));
            String error = AccountRepository.usagePreferences(this, account.id).getString("last_error", "");
            if (!error.isEmpty()) card.addView(Ui.text(this, error, 14f, Ui.mainText(dark)));
            Ui.addSpacer(card, 16);
            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            card.addView(actions);
            Button refresh = Ui.button(this, "Refresh", false, dark);
            refresh.setEnabled(!busy);
            refresh.setOnClickListener(view -> run(() -> UsageApi.refreshAndCache(this, account.id)));
            LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(0, -2, 1f);
            refreshParams.setMarginEnd(Ui.dp(this, 12));
            actions.addView(refresh, refreshParams);
            Button more = Ui.button(this, "More", false, dark);
            more.setContentDescription("More actions for " + account.label);
            more.setEnabled(!busy);
            actions.addView(more, new LinearLayout.LayoutParams(0, -2, 1f));
            more.setOnClickListener(view -> {
                PopupMenu menu = new PopupMenu(this, more);
                if (!account.id.equals(selected)) menu.getMenu().add(0, 1, 0, "Use this account");
                menu.getMenu().add(0, 2, 1, "Rename");
                menu.getMenu().add(0, 3, 2, "Remove / sign out");
                menu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) run(() -> AccountRepository.select(this, account.id));
                    else if (item.getItemId() == 2) rename(account);
                    else if (item.getItemId() == 3) remove(account);
                    return true;
                });
                menu.show();
            });
        }
    }

    private void rename(AccountProfile account) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(account.label);
        input.setFilters(new android.text.InputFilter[] { new android.text.InputFilter.LengthFilter(40) });
        new AlertDialog.Builder(this).setTitle("Account label").setView(input)
                .setNegativeButton("Cancel", null).setPositiveButton("Save", (dialog, which) ->
                        run(() -> AccountRepository.rename(this, account.id, input.getText().toString()))).show();
    }

    private void remove(AccountProfile account) {
        new AlertDialog.Builder(this)
                .setTitle("Remove " + account.label + "?")
                .setMessage("Removes this account’s encrypted session and cached usage.")
                .setNegativeButton("Cancel", null).setPositiveButton("Remove", (dialog, which) ->
                        run(() -> {
                            AuthTokens tokens = AccountRepository.remove(this, account.id);
                            OAuthClient.revokeBestEffort(this, tokens);
                        })).show();
    }

    private static String remaining(UsageWindow window) {
        return window == null ? "—" : window.remainingPercent() + "% remaining";
    }

    private void run(Operation operation) {
        busy = true;
        rebuild();
        Context app = getApplicationContext();
        executor.execute(() -> {
            String error = "";
            try { operation.run(); WidgetRenderer.updateAll(app); }
            catch (Exception exception) { error = "Account operation failed. Please try again."; }
            String message = error;
            runOnUiThread(() -> {
                busy = false;
                if (!isFinishing() && !isDestroyed()) {
                    if (!message.isEmpty()) Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    rebuild();
                }
            });
        });
    }

    private interface Operation { void run() throws Exception; }
}
