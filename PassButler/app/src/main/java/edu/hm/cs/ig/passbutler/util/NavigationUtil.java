package edu.hm.cs.ig.passbutler.util;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.account_detail.AccountDetailActivity;
import edu.hm.cs.ig.passbutler.account_list.AccountListActivity;
import edu.hm.cs.ig.passbutler.entry.CreatePersistenceActivity;
import edu.hm.cs.ig.passbutler.entry.UnlockActivity;
import edu.hm.cs.ig.passbutler.password.PasswordGeneratorActivity;
import edu.hm.cs.ig.passbutler.security.AutoLocker;
import edu.hm.cs.ig.passbutler.settings.SettingsActivity;
import edu.hm.cs.ig.passbutler.sync.SyncActivity;

/**
 * Created by dennis on 12.12.17.
 */

public class NavigationUtil {

    private static final String TAG = NavigationUtil.class.getName();

    public static void goToHomeScreen(Context context) {
        Log.i(TAG, "Navigating to home screen.");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void goToCreatePersistenceActivity(Context context) {
        Log.i(TAG, "Navigating to " + CreatePersistenceActivity.class.getSimpleName() + ".");
        Intent intent = new Intent(context, CreatePersistenceActivity.class);
        context.startActivity(intent);
    }

    public static void goToUnlockActivity(Context context) {
        Log.i(TAG, "Navigating to " + UnlockActivity.class.getSimpleName() + ".");
        AutoLocker.getInstance().cancel();
        Intent intent = new Intent(context, UnlockActivity.class);
        context.startActivity(intent);
    }

    public static void goToAccountListActivity(Context context) {
        Log.i(TAG, "Navigating to " + AccountListActivity.class.getSimpleName() + ".");
        boolean isAutoLockEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.shared_prefs_auto_lock_enabled_key),
                context.getResources().getBoolean(R.bool.shared_prefs_auto_lock_enabled_default));
        if(isAutoLockEnabled) {
            final int autoLockTimeInMinutes = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(
                    context.getString(R.string.shared_prefs_auto_lock_time_key),
                    context.getString(R.string.shared_prefs_auto_lock_time_default)));
            AutoLocker.getInstance().start(context, autoLockTimeInMinutes);
        }
        Intent intent = new Intent(context, AccountListActivity.class);
        context.startActivity(intent);
    }

    public static void goToAccountDetailActivity(Context context, String accountName, boolean createNewAccountItem) {
        Log.i(TAG, "Navigating to " + AccountDetailActivity.class.getSimpleName() + ".");
        Intent intent = new Intent(context, AccountDetailActivity.class);
        intent.putExtra(context.getString(R.string.bundle_key_account_name), accountName);
        intent.putExtra(context.getString(R.string.bundle_key_create_new_account_item), createNewAccountItem);
        context.startActivity(intent);
    }

    public static void goToPasswordGeneratorActivity(Context context) {
        Log.i(TAG, "Navigating to " + PasswordGeneratorActivity.class.getSimpleName() + ".");
        Intent intent = new Intent(context, PasswordGeneratorActivity.class);
        context.startActivity(intent);
    }

    public static void goToSyncActivity(Context context) {
        Log.i(TAG, "Navigating to " + SyncActivity.class.getSimpleName() + ".");
        Intent intent = new Intent(context, SyncActivity.class);
        context.startActivity(intent);
    }

    public static void goToSettingsActivity(Context context) {
        Log.i(TAG, "Navigating to " + SettingsActivity.class.getSimpleName() + ".");
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }
}
