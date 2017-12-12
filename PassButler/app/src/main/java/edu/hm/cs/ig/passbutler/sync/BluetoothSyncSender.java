package edu.hm.cs.ig.passbutler.sync;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.data.SyncContract;
import edu.hm.cs.ig.passbutler.util.SyncContentProviderUtil;
import edu.hm.cs.ig.passbutler.util.FileUtil;
import edu.hm.cs.ig.passbutler.util.SqlUtil;

/**
 * Created by dennis on 05.12.17.
 */

public class BluetoothSyncSender {

    public static final String TAG = BluetoothSyncSender.class.getName();
    private Context context;
    private ContentResolver contentResolver;
    private List<String> filesToSync;
    private BluetoothAdapter bluetoothAdapter;
    private UUID bluetoothChannelUuid;

    BluetoothSyncSender(Context context, String... filesToSync) {
        Log.i(TAG, "Initializing " + BluetoothSyncSender.class.getSimpleName() + ".");
        this.context = context;
        this.contentResolver = context.getContentResolver();
        this.filesToSync = new ArrayList<>(Arrays.asList(filesToSync));
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Log.e(TAG, "Cannot initialize "
                    + BluetoothSyncSender.class.getSimpleName()
                    + " since the device does not support Bluetooth.");
            throw new UnsupportedOperationException(BluetoothSyncSender.class.getSimpleName()
                    + " cannot be used on a device that does not support Bluetooth.");
        }
        bluetoothChannelUuid = UUID.fromString(context.getString(R.string.bluetooth_sync_channel_uuid));
    }

    public void syncAllDevices() {
        Log.i(TAG, "Starting synchronization to all available devices.");
        Set<BluetoothDevice> syncDevices = getSyncDevices();
        for (BluetoothDevice syncDevice : syncDevices) {
            if(syncSingleDevice(syncDevice)) {
                Log.i(TAG, "Successfully synced to device with hardware address " + syncDevice.getAddress() + ".");
            }
            else {
                Log.i(TAG, "Failed to sync to device with hardware address " + syncDevice.getAddress() + ".");
            }
        }
        Log.i(TAG, "Synchronization of all available devices finished.");
    }

    public boolean syncSingleDevice(BluetoothDevice syncDevice) {
        String hardwareAddress = syncDevice.getAddress();
        Log.i(TAG, "Starting synchronization to device with hardware address " + hardwareAddress + ".");

        // Check if there is actually data to sync.
        List<String> filesRequiredToSync;
        filesRequiredToSync = getFilesRequiredToSync(syncDevice.getAddress());
        if(filesRequiredToSync.isEmpty()) {
            Log.i(TAG, "No files require synchronization.");
            return true;
        }
        Log.i(TAG, "There are files that require synchronization.");

        // Check if devices are paired.
        if(syncDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            Log.e(TAG, "Synchronization failed. There is no bond to device with hardware address " + hardwareAddress + ".");
            Toast.makeText(context, "@string/missing_bluetooth_bond_error_msg", Toast.LENGTH_SHORT).show();
            return false;
        }
        Log.i(TAG, "Bond to device with hardware address " + hardwareAddress + " found.");

        try(BluetoothConnection connection = new BluetoothConnection(context, syncDevice, bluetoothChannelUuid)) {
            // Start file sync.
            Log.i(TAG, "Sending start code.");
            connection.writeInt(context.getResources().getInteger(R.integer.bluetooth_sync_start));
            Log.i(TAG, "Sending own hardware address.");
            connection.writeString(bluetoothAdapter.getAddress());
            boolean continueSync = false;
            try {
                Log.i(TAG, "Receiving permission for synchronization.");
                continueSync = connection.readBoolean();
            } catch (IOException e) {
                Log.e(TAG, "Synchronization failed. Could not receive permission for data transfer from device with hardware address " + hardwareAddress + ".");
                return false;
            }
            if(!continueSync) {
                Log.e(TAG, "Synchronization failed. Device with hardware address " + hardwareAddress + " rejected data transfer.");
                return false;
            }

            // Sync every required file.
            Set<ContentValues> contentProviderUpdates = new HashSet<>();
            for (int i = 0; i < filesRequiredToSync.size(); i++) {
                Log.i(TAG, "Starting to process file " + i + " that requires sync.");
                Log.i(TAG, "Sending signal to proceed with another file.");
                connection.writeInt(context.getResources().getInteger(R.integer.bluetooth_sync_continue));
                String filePath = filesRequiredToSync.get(i);
                Log.i(TAG, "Sending file path " + filePath + ".");
                connection.writeString(filePath);
                Date lastModified = SyncContentProviderUtil.getLastModified(context, filePath);
                Log.i(TAG, "Sending date of last modification " + lastModified + ".");
                connection.writeLong(lastModified.getTime());
                File file = FileUtil.getFromInternalStorage(context, filePath);
                if(!file.exists()) {
                    Log.e(TAG, "Synchronization of file " + filePath + " failed. File does not exist.");
                    return false;
                }
                try {
                    Log.i(TAG, "Sending file content.");
                    connection.writeFile(file);
                } catch (IOException e) {
                    Log.e(TAG, "Synchronization of file " + filePath + " failed. I/O error while writing file content to device with hardware address " + hardwareAddress + ".");
                    return false;
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(SyncContract.SentSyncItemEntry.COLUMN_FILE_PATH, filePath);
                contentValues.put(SyncContract.SentSyncItemEntry.COLUMN_DESTINATION_HARDWARE_ADDRESS, syncDevice.getAddress());
                contentValues.put(SyncContract.SentSyncItemEntry.COLUMN_LAST_SENT_VERSION_TIMESTAMP, lastModified.getTime());
                contentProviderUpdates.add(contentValues);
            }

            // Finish synchronization and update meta data of sent files.
            Log.i(TAG, "Sending signal that all files that require sync have been processed.");
            connection.writeInt(context.getResources().getInteger(R.integer.bluetooth_sync_end));
            boolean syncCompleted = false;
            try {
                syncCompleted = connection.readBoolean();
            } catch (IOException e) {
                Log.e(TAG, "Synchronization failed. Could not receive confirmation for completion from device with hardware address " + hardwareAddress + ".");
                return false;
            }
            if(syncCompleted) {
                Log.i(TAG, "Updating meta data of sent files.");
                for(ContentValues contentValues : contentProviderUpdates) {
                    contentResolver.insert(SyncContract.SentSyncItemEntry.CONTENT_URI, contentValues);
                }
            }
            return syncCompleted;
        } catch (IOException e) {
            Log.e(TAG, "Synchronization failed. Could not connect to device with hardware address " + hardwareAddress + ".");
            return false;
        }
    }

    private Set<BluetoothDevice> getSyncDevices() {
        Log.i(TAG, "Determining remote Bluetooth devices requested for synchronization.");
        Set<BluetoothDevice> syncDevices = new HashSet<>();
        try(Cursor cursor = contentResolver.query(
                SyncContract.BluetoothSyncDeviceEntry.CONTENT_URI,
                null,
                null,
                null,
                null)) {
            int hardwareAddressIndex = cursor.getColumnIndex(
                    SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_HARDWARE_ADDRESS);
            while(cursor.moveToNext()) {
                String hardwareAddress = cursor.getString(hardwareAddressIndex);
                syncDevices.add(bluetoothAdapter.getRemoteDevice(hardwareAddress));
            }
        }
        return syncDevices;
    }

    private List<String> getFilesRequiredToSync(String destinationHardwareAddress) {
        Log.i(TAG, "Determining files that are required to sync to device with hardware address " + destinationHardwareAddress + ".");
        List<String> filePaths = new ArrayList<>();
        try(Cursor cursor = contentResolver.query(
                SyncContract.DataSourceEntry.CONTENT_URI,
                null,
                null,
                null,
                null)) {
            int filePathIndex = cursor.getColumnIndex(
                    SyncContract.DataSourceEntry.COLUMN_FILE_PATH);
            int lastModifiedTimestampIndex = cursor.getColumnIndex(
                    SyncContract.DataSourceEntry.COLUMN_LAST_MODIFIED_TIMESTAMP);
            while(cursor.moveToNext()) {
                String filePath = cursor.getString(filePathIndex);
                Date lastModified = new Date(cursor.getLong(lastModifiedTimestampIndex));
                if(isFileRequiredToSync(destinationHardwareAddress, filePath, lastModified)) {
                    filePaths.add(filePath);
                }
            }
        }
        return filePaths;
    }

    private boolean isFileRequiredToSync(String destinationHardwareAddress, String filePath, Date lastModified) {
        Log.i(TAG, "Checking if file " + filePath + " is required to sync to device with hardware address " + destinationHardwareAddress + ".");
        if(!this.filesToSync.contains(filePath)) {
            Log.i(TAG, "File " + filePath + " was not specified to sync.");
            return false;
        }
        Date lastSentVersion = SyncContentProviderUtil.getLastSent(context, destinationHardwareAddress, filePath);
        if(lastSentVersion == null) {
            Log.i(TAG, "File " + filePath + " is required to sync because it was never sent before.");
            return true;
        }
        if(lastModified.before(lastSentVersion)) {
            Log.wtf(TAG, "The last sent version of the file "
                    + filePath
                    + " is younger than the date of its last modification.");
            throw new IllegalStateException(
                    "The last sent version of a file cannot be younger than the date of its last modification.");
        }
        if(lastModified.equals(lastSentVersion)) {
            Log.i(TAG, "File " + filePath + " is not required to sync to device with hardware address " + destinationHardwareAddress + ".");
            return false;
        }
        Log.i(TAG, "File " + filePath + " is required to sync to device with hardware address " + destinationHardwareAddress + ".");
        return true;
    }
}
