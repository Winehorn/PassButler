package edu.hm.cs.ig.passbutler.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import afu.org.checkerframework.checker.oigj.qual.O;
import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.security.AutoLocker;

/**
 * Created by dennis on 16.12.17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {
    private String TAG = this.getClass().getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_settings);
        findPreference(getString(R.string.shared_prefs_auto_lock_time_key)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.pref_number_of_attempts_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return onChangeNumberOfAttempts(preference, newValue);
            }
        });
        findPreference(getString(R.string.pref_attempt_lock_time_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return onChangeAttemptTime(preference, newValue);
            }
        });
        findPreference(getString(R.string.pref_overdue_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return onChangeOverdueTime(preference, newValue);
            }
        });
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if (preference.getTitle().equals(getString(R.string.shared_prefs_auto_lock_time_title))) {
                String value = preferenceScreen.getSharedPreferences().getString(
                        preference.getKey(),
                        getString(R.string.shared_prefs_auto_lock_time_summary_default));
                updatePreferenceSummary(preference, value);
            }
        }
    }

    private void updatePreferenceSummary(Preference preference, String summary) {
        if (preference instanceof EditTextPreference) {
            preference.setSummary(summary);
        } else {
            throw new IllegalArgumentException("The preference must have a valid type");
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Toast errorMessage = Toast.makeText(
                getContext(),
                String.format(
                        getString(R.string.auto_lock_time_error_msg),
                        getResources().getInteger(R.integer.shared_prefs_auto_lock_time_min),
                        getResources().getInteger(R.integer.shared_prefs_auto_lock_time_max)),
                Toast.LENGTH_SHORT);
        if (preference.getKey().equals(getString(R.string.shared_prefs_auto_lock_time_key))) {
            String autoLockTimeInMinutesString = ((String) (newValue)).trim();
            if (autoLockTimeInMinutesString == null) {
                autoLockTimeInMinutesString = getString(R.string.shared_prefs_auto_lock_time_default);
            }
            try {
                int autoLockTimeInMinutes = Integer.parseInt(autoLockTimeInMinutesString);
                if (autoLockTimeInMinutes < getResources().getInteger(R.integer.shared_prefs_auto_lock_time_min)
                        || autoLockTimeInMinutes > getResources().getInteger(R.integer.shared_prefs_auto_lock_time_max)) {
                    errorMessage.show();
                    return false;
                }
            } catch (NumberFormatException e) {
                errorMessage.show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (key.equals(getString(R.string.shared_prefs_auto_lock_enabled_key))) {
            boolean autoLockEnabled = sharedPreferences.getBoolean(
                    key,
                    getResources().getBoolean(R.bool.shared_prefs_auto_lock_enabled_default));
            if (autoLockEnabled) {
                final int autoLockTimeInMinutes = Integer.parseInt(sharedPreferences.getString(
                        getString(R.string.shared_prefs_auto_lock_time_key),
                        getString(R.string.shared_prefs_auto_lock_time_default)));
                AutoLocker.getInstance().start(getContext(), autoLockTimeInMinutes);
            } else {
                AutoLocker.getInstance().cancel();
            }
        } else if (key.equals(getString(R.string.shared_prefs_auto_lock_time_key))) {

            boolean isAutoLockEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(
                    getString(R.string.shared_prefs_auto_lock_enabled_key),
                    getResources().getBoolean(R.bool.shared_prefs_auto_lock_enabled_default));
            final int autoLockTimeInMinutes = Integer.parseInt(sharedPreferences.getString(
                    key,
                    getString(R.string.shared_prefs_auto_lock_time_default)));
            if (isAutoLockEnabled) {
                AutoLocker.getInstance().reset(getContext(), autoLockTimeInMinutes);
            }
            updatePreferenceSummary(preference, String.valueOf(autoLockTimeInMinutes));
        }
    }

    private boolean onChangeNumberOfAttempts(Preference preference, Object newValue) {
        String numberOfAttemptsString = ((String) (newValue)).trim();
        try {
            int numberOfAttempts = Integer.parseInt(numberOfAttemptsString);
            if (numberOfAttempts < 1) {
                Toast.makeText(getContext(), "Number of Attempts has to be an integer greater than 1.", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Number of Attempts has to be an integer greater than 1.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean onChangeAttemptTime(Preference preference, Object newValue) {
        String attemptTimeString = ((String) (newValue)).trim();
        try {
            int attemptTime = Integer.parseInt(attemptTimeString);
            if (attemptTime < 1) {
                Toast.makeText(getContext(), "Attempt Time has to be an integer greater than 1.", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Attempt Time has to be an integer greater than 1.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean onChangeOverdueTime(Preference preference, Object newValue) {
        String overdueTimeString = ((String) (newValue)).trim();
        Log.i(TAG, "onPreferenceChange: overdueTimeString:" + overdueTimeString);
        try {
            int overdueTime = Integer.parseInt(overdueTimeString);
            if (overdueTime < 1) {
                Toast.makeText(getContext(), "Reminder Time has to be an integer greater than 1.", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Reminder Time has to be an integer greater than 1.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
