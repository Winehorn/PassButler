package edu.hm.cs.ig.passbutler.security;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import javax.security.auth.DestroyFailedException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.util.NavigationUtil;

/**
 * Created by dennis on 16.12.17.
 */

public class AutoLocker {

    public static final String TAG = AutoLocker.class.getName();
    private static final AutoLocker instance = new AutoLocker();
    private Context context;
    private CountDownTimer countDownTimer;

    private AutoLocker() { }

    public static AutoLocker getInstance() {
        return instance;
    }

    public void start(Context context, final long autoLockTimeInMinutes) {
        Log.i(TAG, "Starting auto lock countdown.");
        this.context = context;
        final long autoLockTimeInMillis = TimeUnit.MINUTES.toMillis(autoLockTimeInMinutes);
        countDownTimer = new CountDownTimer(autoLockTimeInMillis, autoLockTimeInMillis) {
            public void onTick(long millisUntilFinished) {
                AutoLocker.this.onTick(millisUntilFinished);
            }

            public void onFinish() {
                AutoLocker.this.onFinish();
            }
        }.start();
    }

    public void reset(Context context, final long newAutoLockTimeInMinutes) {
        Log.i(TAG, "Resetting auto lock countdown.");
        cancel();
        start(context, newAutoLockTimeInMinutes);
    }

    public void expireImmediately() {
        Log.i(TAG, "Expiring auto lock countdown immediately.");
        cancel();
        onFinish();
    }

    public void cancel() {
        if(countDownTimer != null) {
            Log.i(TAG, "Canceling auto lock countdown.");
            countDownTimer.cancel();
        }
    }

    private void onTick(final long millisUntilFinished) { }

    private void onFinish() {
        Log.i(TAG, "Auto lock countdown has expired.");

        // TODO: clear backstack

        NavigationUtil.goToUnlockActivity(context);
    }
}
