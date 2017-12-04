package edu.hm.cs.ig.passbutler.sync;

import android.view.MenuItem;

/**
 * Created by dennis on 04.12.17.
 */

public interface BluetoothSyncDeviceAdapterOnMenuItemClickHandler {
    boolean onMenuItemClick(MenuItem item, String deviceId);
}
