package edu.hm.cs.ig.passbutler.entry;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.gui.PreAuthActivity;
import edu.hm.cs.ig.passbutler.util.FileUtil;
import edu.hm.cs.ig.passbutler.util.NavigationUtil;
import edu.hm.cs.ig.passbutler.util.ServiceUtil;

public class LogoActivity extends PreAuthActivity {

    private static final String TAG = LogoActivity.class.getName();
    private static final int DELAY_IN_MILLIS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        // Disable screenshots
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        ServiceUtil.startSyncMergerService(getApplicationContext());

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(FileUtil.internalStorageFileExists(LogoActivity.this, getString(R.string.accounts_file_path))) {
                    Log.i(TAG, "Account file exists.");
                    NavigationUtil.goToUnlockActivity(LogoActivity.this);
                }
                else {
                    Log.i(TAG, "Account file does not exist.");
                    NavigationUtil.goToCreatePersistenceActivity(LogoActivity.this);
                }
            }
        }, DELAY_IN_MILLIS);
    }

    @Override
    public void onBackPressed() {
        // Do nothing here.
    }
}
