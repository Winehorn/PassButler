package edu.hm.cs.ig.passbutler.gui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.security.AutoLocker;

/**
 * Created by dennis on 16.12.17.
 */

public class PostAuthActivity extends PassButlerActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
            boolean isAutoLockEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                    getString(R.string.shared_prefs_auto_lock_enabled_key),
                    getResources().getBoolean(R.bool.shared_prefs_auto_lock_enabled_default));
            if(isAutoLockEnabled) {
                final int autoLockTimeInMinutes = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(
                        getString(R.string.shared_prefs_auto_lock_time_key),
                        getString(R.string.shared_prefs_auto_lock_time_default)));

                AutoLocker.getInstance().reset(this, autoLockTimeInMinutes);
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
