package edu.hm.cs.ig.passbutler.data;

import android.content.Context;
import android.content.Intent;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by dennis on 17.11.17.
 */

/*
 * Reasons for this class being a strange semi singleton and rules for its usage:
 *
 * - No 2 instances of FileObserver should observe the same file. If one instance is stopped or
 * destroyed by the garbage collection (which calls stopWatching on destruction) ALL OTHER
 * observers that point to the same file also stop working. So in this app only 1 instance of
 * FileObserver is used over its whole lifetime.
 * For more details see http://grokbase.com/t/gg/android-developers/12a3s507xa/issues-with-fileobserver
 *
 * - The single instance of FileObserver should never be stopped by calling stopWatching. This is
 * irreversible and the observer cannot start to watch again afterwards. So pausing the observer
 * when an activity stops and starting it again when the activity is restarted (or similar things)
 * are NOT possible. If the callback of an observer is called with the event code 32768 means that
 * the observe has stopped working.
 * For more details see https://stackoverflow.com/questions/10305054/android-fileobserver-passing-undocumented-event-32768
 *
 * - Starting to observe a file that does not yet exist (e.g. on startup in the LogoActivity) will
 * not work even if the file is created afterwards. But starting it multiple times at a point of
 * time where the file is sure to be created is OK.
 */
public class BroadcastFileObserver extends FileObserver {

    public static final String TAG = BroadcastFileObserver.class.getName();
    private static BroadcastFileObserver instance;
    private final Context context;
    private final String action;

    private BroadcastFileObserver(Context context, String action, String path, int mask) {
        super(path, mask);
        this.context = context;
        this.action = action;
    }

    public synchronized static void init(Context context, String action, String path, int mask) {
        if(instance == null) {
            instance = new BroadcastFileObserver(context, action, path, mask);
        }
        else {
            throw new UnsupportedOperationException("A " + BroadcastFileObserver.class.getSimpleName() + " can be initialized only once.");
        }
    }

    public synchronized static BroadcastFileObserver getInstance() {
        if(instance != null) {
            return instance;
        }
        else {
            throw new UnsupportedOperationException("A " + BroadcastFileObserver.class.getSimpleName() + " must be initialized prior to getting an instance.");
        }
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        if(event == FileObserver.MODIFY) {
            Intent intent = new Intent(action);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
