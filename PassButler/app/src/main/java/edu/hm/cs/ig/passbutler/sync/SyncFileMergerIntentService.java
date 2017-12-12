package edu.hm.cs.ig.passbutler.sync;

import android.app.IntentService;
import android.content.Intent;
import android.os.Looper;
import android.widget.Toast;

import edu.hm.cs.ig.passbutler.util.ServiceUtil;

/**
 * Created by dennis on 11.12.17.
 */

public class SyncFileMergerIntentService extends IntentService {

    public static final String SERVICE_NAME = "bluetooth_sync_merger_service";
    public static SyncFileMerger syncFileMerger;

    public SyncFileMergerIntentService() {
        super(SERVICE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        syncFileMerger = new SyncFileMerger(getApplicationContext(), Looper.myLooper());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO: entfernen
        Toast.makeText(this, "Merger started", Toast.LENGTH_SHORT).show();

        ServiceUtil.notifyMergeAll(getApplicationContext());
        Looper.loop();
    }
}
