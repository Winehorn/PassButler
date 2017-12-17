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
            editor.commit();
        }
        UUID uuid = UUID.fromString(prefs.getString(
                context.getString(R.string.shared_prefs_uuid_key),
                null));
        return uuid;
    }
}