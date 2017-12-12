package edu.hm.cs.ig.passbutler.sync;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by dennis on 10.12.17.
 */

public class BluetoothSyncReceiverIntentService extends IntentService {

    public static final String TAG = BluetoothSyncReceiverIntentService.class.getName();
    public static final String SERVICE_NAME = "bluetooth_sync_receiver_service";
    public static BluetoothSyncReceiver bluetoothSyncReceiver;

    public BluetoothSyncReceiverIntentService() {
        super(SERVICE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothSyncReceiver = new BluetoothSyncReceiver(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO: entfernen
        Toast.makeText(this, "Listening", Toast.LENGTH_LONG).show();

        bluetoothSyncReceiver.continuousReceiveSync();
    }
}
