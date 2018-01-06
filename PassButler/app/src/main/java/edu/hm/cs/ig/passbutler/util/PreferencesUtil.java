package edu.hm.cs.ig.passbutler.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

import edu.hm.cs.ig.passbutler.R;

/**
 * Created by dennis on 06.12.17.
 */

public class PreferencesUtil {
    public static UUID getUserUuid(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(!prefs.contains(context.getString(R.string.shared_prefs_uuid_key))) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(context.getString(
                    R.string.shared_prefs_uuid_key),
                    UUID.randomUUID().toString());
            editor.apply();
        }
        UUID uuid = UUID.fromString(prefs.getString(
                context.getString(R.string.shared_prefs_uuid_key),
                null));
        return uuid;
    }

    public static boolean getBluetoothSyncEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.shared_prefs_bluetooth_sync_enabled_key),
                context.getResources().getBoolean(R.bool.shared_prefs_bluetooth_sync_enabled_default));
    }

    public static void setBluetoothSyncEnabled(Context context, boolean newIsEnabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(context.getString(R.string.shared_prefs_bluetooth_sync_enabled_key), newIsEnabled);
        editor.apply();
    }

    public static boolean getAutoSyncEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.shared_prefs_auto_sync_enabled_key),
                context.getResources().getBoolean(R.bool.shared_prefs_auto_sync_enabled_default));
    }

    public static void setAutoSyncEnabled(Context context, boolean newIsEnabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(context.getString(R.string.shared_prefs_auto_sync_enabled_key), newIsEnabled);
        editor.apply();
    }

    public static int getStringPrefAsInt(Context context, String key, String defaultVal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.valueOf(prefs.getString(key, defaultVal));
    }
}
