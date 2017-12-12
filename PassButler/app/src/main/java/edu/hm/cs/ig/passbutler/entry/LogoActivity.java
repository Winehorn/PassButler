package edu.hm.cs.ig.passbutler.entry;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.util.FileUtil;
import edu.hm.cs.ig.passbutler.util.ServiceUtil;

public class LogoActivity extends AppCompatActivity {

    private static final String TAG = LogoActivity.class.getName();
    private static final int DELAY_IN_MILLIS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        // Disable screenshots
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        final Handler handler = new Handler();
        ServiceUtil.startSyncMergerService(getApplicationContext());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(FileUtil.internalStorageFileExists(LogoActivity.this, getString(R.string.accounts_file_path))) {
                    Intent intent = new Intent(
                            LogoActivity.this,
                            UnlockActivity.class);
                    startActivity(intent);
                    Log.i(TAG, "Account file exists. Proceeding to " + UnlockActivity.class.getSimpleName() + ".");
                }
                else {
                    Intent intent = new Intent(
                            LogoActivity.this,
                            CreatePersistenceActivity.class);
                    startActivity(intent);
                    Log.i(TAG, "Account file does not exist. Proceeding to " + CreatePersistenceActivity.class.getSimpleName() + ".");
                }
            }
        }, DELAY_IN_MILLIS);
    }
}
