package edu.hm.cs.ig.passbutler.entry;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.data.BroadcastFileObserver;
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

        TextView appTitleTextView = findViewById(R.id.app_title_text_view);
        Typeface type = Typeface.createFromAsset(getAssets(), getString(R.string.font_dancing_script_regular_path));
        appTitleTextView.setTypeface(type);

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

        ServiceUtil.startSyncMergerService(getApplicationContext());
        BroadcastFileObserver.init(
                getApplicationContext(),
                getString(R.string.account_list_handler_loader_reload_action),
                FileUtil.combinePaths(getFilesDir().getAbsolutePath(), getString(R.string.accounts_file_path)),
                FileObserver.MODIFY);
    }

    @Override
    public void onBackPressed() {
        // Do nothing here.
    }
}
