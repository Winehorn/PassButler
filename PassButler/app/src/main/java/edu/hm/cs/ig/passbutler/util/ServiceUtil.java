package edu.hm.cs.ig.passbutler.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.Date;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.sync.BluetoothSyncReceiverIntentService;
import edu.hm.cs.ig.passbutler.sync.BluetoothSyncSenderJobService;
import edu.hm.cs.ig.passbutler.sync.SyncFileMerger;
import edu.hm.cs.ig.passbutler.sync.SyncFileMergerIntentService;

/**
 * Created by dennis on 10.12.17.
 */

public class ServiceUtil {

    private static final String TAG = ServiceUtil.class.getName();
    private static boolean isSyncSenderServiceInitialized;
    private static boolean isSyncReceiverServiceInitialized;
    private static boolean isSyncMergerServiceInitialized;

    public synchronized static void startSyncServices(final Context context) {
        startSyncMergerService(context);
        startSyncReceiverService(context);
        startSyncSenderService(context);
    }

    public synchronized static void cancelSyncServices(final Context context) {
        cancelSyncSenderService(context);
        cancelSyncReceiverService(context);
        cancelSyncMergerService(context);
    }

    public synchronized static void startSyncSenderService(final Context context) {
        if(isSyncSenderServiceInitialized) {
            return;
        }
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job bluetoothSyncSenderJob = dispatcher.newJobBuilder()
                .setService(BluetoothSyncSenderJobService.class)
                .setTag(BluetoothSyncSenderJobService.JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        context.getResources().getInteger(R.integer.bluetooth_sync_interval_sec),
                        context.getResources().getInteger(R.integer.bluetooth_sync_interval_sec) + context.getResources().getInteger(R.integer.bluetooth_sync_flextime_sec)))
                .setReplaceCurrent(true)
                .build();
        dispatcher.schedule(bluetoothSyncSenderJob);
        isSyncSenderServiceInitialized = true;
    }

    public synchronized static void cancelSyncSenderService(final Context context) {
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        dispatcher.cancel(BluetoothSyncSenderJobService.JOB_TAG);
        isSyncSenderServiceInitialized = false;
    }

    public synchronized static void startSyncReceiverService(final Context context) {
        if(isSyncReceiverServiceInitialized) {
            return;
        }
        Intent intent = new Intent(context, BluetoothSyncReceiverIntentService.class);
        context.startService(intent);
        isSyncReceiverServiceInitialized = true;
    }

    public synchronized static void cancelSyncReceiverService(final Context context) {
        Intent intent = new Intent(context, BluetoothSyncReceiverIntentService.class);
        context.stopService(intent);
        isSyncReceiverServiceInitialized = false;
    }

    public synchronized static void startSyncMergerService(final Context context) {
        if(isSyncMergerServiceInitialized) {
            return;
        }
        Intent intent = new Intent(context, SyncFileMergerIntentService.class);
        context.startService(intent);
        isSyncMergerServiceInitialized = true;
    }

    public synchronized static void cancelSyncMergerService(final Context context) {
        Intent intent = new Intent(context, SyncFileMergerIntentService.class);
        context.stopService(intent);
        isSyncReceiverServiceInitialized = false;
    }

    public static void notifyMergeAll(Context context) {
        Message message = getHandler().obtainMessage(SyncFileMerger.MERGE_ALL);
        message.sendToTarget();
    }

    public static void notifyMergeSingle(Context context, String filePath, String remoteDeviceHardwareAddress, Date lastModified) {
        Message message = getHandler().obtainMessage(SyncFileMerger.MERGE_SINGLE);
        Bundle bundle = new Bundle();
        bundle.putString(context.getString(R.string.bundle_key_file_path), filePath);
        bundle.putString(context.getString(R.string.bundle_key_hardware_address), remoteDeviceHardwareAddress);
        bundle.putLong(context.getString(R.string.bundle_key_last_received_version_timestamp), lastModified.getTime());
        bundle.putLong(context.getString(
                R.string.bundle_key_last_incorporated_version_timestamp),
                SyncContentProviderUtil.getLastIncorporated(context, remoteDeviceHardwareAddress, filePath).getTime());
        bundle.putLong(context.getString(R.string.bundle_key_merge_date), new Date().getTime());
        message.setData(bundle);
        message.sendToTarget();
    }

    private static Handler getHandler() {
        Handler handler = SyncFileMergerIntentService.syncFileMerger.getHandler();
        if(handler == null) {
            Log.wtf(TAG, "No running " + SyncFileMergerIntentService.class.getSimpleName() + " found.");
            throw new IllegalStateException("There must be a running " + SyncFileMergerIntentService.class.getSimpleName() + ".");
        }
        return handler;
    }
}
