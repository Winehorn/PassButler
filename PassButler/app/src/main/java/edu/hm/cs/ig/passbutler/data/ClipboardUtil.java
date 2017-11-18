package edu.hm.cs.ig.passbutler.data;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Florian Kraus on 18.11.2017.
 */

// TODO: check why clip label randomly changes to "host clipboard"

public class ClipboardUtil {
    private static final String TAG = ClipboardUtil.class.getName();
    private final Context mContext;
    private final ClipboardManager clipboardManager;

    public ClipboardUtil(Context mContext) {
        this.mContext = mContext;
        this.clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public void copyAndDelete(final String label, String text, int seconds) {
        Handler handler = new Handler(Looper.getMainLooper());

        // Create ClipData
        ClipData clip = ClipData.newPlainText(label, text);
        // TODO: handle possible NullPointerException
        clipboardManager.setPrimaryClip(clip);

        Log.d(TAG, "Clipboard1: " + clipboardManager.getPrimaryClipDescription().getLabel().toString());
        Log.d(TAG, "Clipboard2: " + clipboardManager.getPrimaryClipDescription().getLabel().toString());
        // Overwrite clipboard after given time
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    clearClipboard(label);
                    Toast.makeText(mContext, "Clipboard has been cleared!", Toast.LENGTH_LONG).show();
                //}
            }
        }, seconds * 1000);
    }

    private void clearClipboard() {

        ClipData clip = ClipData.newPlainText("", "");
        // TODO: handle possible NullPointerException
        clipboardManager.setPrimaryClip(clip);
    }

    private void clearClipboard(String label) {

        Log.d(TAG, "Got Label:" + label);
        Log.d(TAG, "Clipboardlabel: " + clipboardManager.getPrimaryClip().toString());
        if(clipboardManager.getPrimaryClipDescription().getLabel().toString().equals(label)) {
            Log.d(TAG, "Deleting Clipboard!");
            ClipData clip = ClipData.newPlainText("", "");
            // TODO: handle possible NullPointerException
            clipboardManager.setPrimaryClip(clip);
        }

    }
}
