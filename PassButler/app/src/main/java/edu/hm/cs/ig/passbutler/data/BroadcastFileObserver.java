package edu.hm.cs.ig.passbutler.data;

import android.content.Context;
import android.content.Intent;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by dennis on 17.11.17.
 */

public class BroadcastFileObserver extends FileObserver {

    private final Context context;
    private final String action;

    public BroadcastFileObserver(Context context, String action, String path, int mask) {
        super(path, mask);
        this.context = context;
        this.action = action;
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        if(event == FileObserver.MODIFY) {
            Intent intent = new Intent(action);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
