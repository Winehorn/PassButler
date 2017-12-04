package edu.hm.cs.ig.passbutler;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import edu.hm.cs.ig.passbutler.data.AccountListHandler;
import edu.hm.cs.ig.passbutler.data.FileUtil;

public class LogoActivity extends AppCompatActivity {

    private static final String TAG = LogoActivity.class.getName();
    private static final int DELAY_IN_MILLIS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(FileUtil.fileExists(LogoActivity.this, getString(R.string.accounts_file_name))) {
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
