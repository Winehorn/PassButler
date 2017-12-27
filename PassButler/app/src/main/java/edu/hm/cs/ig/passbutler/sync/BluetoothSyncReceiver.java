package edu.hm.cs.ig.passbutler.sync;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.data.SyncContract;
import edu.hm.cs.ig.passbutler.util.FileUtil;
import edu.hm.cs.ig.passbutler.util.ServiceUtil;
import edu.hm.cs.ig.passbutler.util.SqlUtil;
import edu.hm.cs.ig.passbutler.util.SyncContentProviderUtil;

/**
 * Created by dennis on 07.12.17.
 */

public class BluetoothSyncReceiver {
    public static final String TAG = BluetoothSyncReceiver.class.getName();
    private Context context;
    private ContentResolver contentResolver;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isReceiving;
    private BluetoothServerSocket serverSocket;

    public BluetoothSyncReceiver(Context context) {
        Log.i(TAG, "Initializing " + BluetoothSyncReceiver.class.getSimpleName() + ".");
        this.context = context;
        this.contentResolver = context.getContentResolver();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Log.e(TAG, "Cannot initialize "
                    + BluetoothSyncReceiver.class.getSimpleName()
                    + " since the device does not support Bluetooth.");
            throw new UnsupportedOperationException(BluetoothSyncReceiver.class.getSimpleName()
                    + " cannot be used on a device that does not support Bluetooth.");
        }
    }

    public void continuousReceiveSync() {
        Log.i(TAG, "Started continuous receiving of bluetooth sync connections.");
        isReceiving = true;
        while(isReceiving) {
            Log.i(TAG, "Starting new iteration of continuous sync request receiving.");
            boolean syncSuccessful = receiveSync();
            if(syncSuccessful) {
                Log.i(TAG, "Current iteration of continuous sync request receiving was successful.");
            }
            else {
                Log.i(TAG, "Current iteration of continuous sync request receiving failed.");
            }
        }
        Log.i(TAG, "Stopped continuous receiving of bluetooth sync connections.");
    }

    public boolean receiveSync() {
        try {
            Log.i(TAG, "Creating new server socket.");
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    context.getString(R.string.bluetooth_sync_channel_name),
                    UUID.fromString(context.getString(R.string.bluetooth_sync_channel_uuid)));
        }
        catch (IOException e) {
            Log.e(TAG, "I/O error while creating server socket.");
            return false;
        }
        BluetoothSocket socket;
        try {
            Log.i(TAG, "Accepting incoming connection to server socket.");
            socket = serverSocket.accept();
        }
        catch (IOException e) {
            Log.e(TAG, "I/O error while accepting connection to server socket.");
            return false;
        }
        if (socket != null) {
            try {
                Log.i(TAG, "Closing server socket for further sync requests.");
                closeServerSocket();
            }
            catch (IOException e) {
                Log.e(TAG, "I/O error while closing server socket for further sync requests.");
                return false;
            }
        }
        try(BluetoothConnection connection = new BluetoothConnection(context, socket)) {
            return processSyncRequest(connection);
        } catch (IOException e) {
            Log.e(TAG, "I/O error while creating wrapper for Bluetooth connection.");
            return false;
        }
    }

    private boolean processSyncRequest(BluetoothConnection connection) {
        Log.i(TAG, "Starting processing synchronization request from remote Bluetooth device.");

        // Check start code.
        int startCode;
        try {
            Log.i(TAG, "Receiving start code from remote Bluetooth device.");
            startCode = connection.readInt();
            Log.i(TAG, "Received start code from remote Bluetooth device.");
        } catch (IOException e) {
            Log.e(TAG, "Synchronization failed. I/O error while receiving sync start code from remote Bluetooth device.");
            return false;
        }
        if(startCode != context.getResources().getInteger(R.integer.bluetooth_sync_start)) {
            Log.e(TAG, "Synchronization failed. Start code did not match expected value.");
            return false;
        }
        Log.i(TAG, "Start code is valid.");

        // Get hardware address from remote device.
        String remoteDeviceHardwareAddress;
        try {
            Log.i(TAG, "Receiving hardware address of remote Bluetooth device.");
            remoteDeviceHardwareAddress = connection.readString();
            Log.i(TAG, "Hardware address of remote Bluetooth device is " + remoteDeviceHardwareAddress + ".");
        } catch (IOException e) {
            Log.e(TAG, "Synchronization failed. I/O error while receiving hardware address of remote Bluetooth device.");
            return false;
        }

        // Check if sync is allowed.
        Log.i(TAG, "Checking if remote Bluetooth device is allowed to sync.");
        if(isSyncAllowed(remoteDeviceHardwareAddress)) {
            Log.i(TAG, "Allowing remote Bluetooth device to sync.");
            connection.writeBoolean(true);
        }
        else {
            Log.i(TAG, "Synchronization failed. Remote Bluetooth device is not allowed to sync.");
            connection.writeBoolean(false);
            return false;
        }

        // Receive files.
        try {
            while(doExistMoreFilesToSync(connection)) {
                // File path
                String filePath;
                try {
                    Log.i(TAG, "Receiving file path from remote Bluetooth device.");
                    filePath = connection.readString();
                    Log.i(TAG, "Received file path from remote Bluetooth device.");
                } catch (IOException e) {
                    Log.e(TAG, "Synchronization failed. I/O error while receiving file path from remote Bluetooth device.");
                    return false;
                }

                // Date of last modification
                Date lastModified;
                try {
                    Log.i(TAG, "Receiving date of last modification from remote Bluetooth device.");
                    lastModified = new Date(connection.readLong());
                    Log.i(TAG, "Received date of last modification from remote Bluetooth device.");
                } catch (IOException e) {
                    Log.e(TAG, "Synchronization failed. I/O error while receiving date of last modification from remote Bluetooth device.");
                    return false;
                }

                // File content
                byte[] fileContent;
                try {
                    Log.i(TAG, "Receiving file content from remote Bluetooth device.");
                    fileContent = connection.readFileContent();
                    Log.i(TAG, "Received file content from remote Bluetooth device.");
                } catch (IOException e) {
                    Log.e(TAG, "Synchronization failed. I/O error while receiving file content from remote Bluetooth device.");
                    return false;
                }

                if(isFileRequiredToSync(remoteDeviceHardwareAddress, filePath, lastModified)) {
                    // Save received data.
                    updateSyncItem(remoteDeviceHardwareAddress, filePath, lastModified, fileContent);

                    // Send message to file merge thread.
                    ServiceUtil.notifyMergeSingle(context, filePath, remoteDeviceHardwareAddress, lastModified);
                }
            }
        }
        catch(IllegalStateException e) {
            Log.e(TAG, "Synchronization failed. Illegal state error while checking if there are more pending file to sync.");
            return false;
        }
        catch(IOException e) {
            Log.e(TAG, "Synchronization failed. I/O error while checking if there are more pending file to sync.");
            return false;
        }

        Log.i(TAG, "Sending synchronization completion confirmation to remote Bluetooth device.");
        connection.writeBoolean(true);

        Log.i(TAG, "Synchronization request from remote Bluetooth device successfully processed.");
        return true;
    }

    private boolean isSyncAllowed(String remoteDeviceHardwareAddress) {
        Log.i(TAG, "Checking if sync from remote Bluetooth device with hardware address " + remoteDeviceHardwareAddress + " is allowed.");
        String hardwareAddress = SyncContentProviderUtil.getHardwareAddress(context, remoteDeviceHardwareAddress);
        if(hardwareAddress == null) {
            Log.i(TAG, "Sync from remote Bluetooth device is not allowed.");
            return false;
        }
        Log.i(TAG, "Sync from remote Bluetooth device is allowed.");
        return true;
    }

    private boolean doExistMoreFilesToSync(BluetoothConnection connection) throws IOException, IllegalStateException {
        int continueCode;
        try {
            Log.i(TAG, "Receiving continue code from remote Bluetooth device.");
            continueCode = connection.readInt();
            Log.i(TAG, "Received continue code from remote Bluetooth device.");
        } catch (IOException e) {
            Log.e(TAG, "I/O error while receiving sync continue code.");
            throw e;
        }
        if(continueCode == context.getResources().getInteger(R.integer.bluetooth_sync_continue)) {
            Log.i(TAG, "There are more pending files to sync.");
            return true;
        }
        else if(continueCode == context.getResources().getInteger(R.integer.bluetooth_sync_end)) {
            Log.i(TAG, "There are no more pending files to sync.");
            return false;
        }
        else {
            Log.e(TAG, "Continue code did not match one of the expected values.");
            throw new IllegalStateException("The continue code must have a valid value.");
        }
    }

    private boolean isFileRequiredToSync(String remoteDeviceHardwareAddress, String filePath, Date newLastModified) {
        Log.i(TAG, "Checking if file " + filePath + " from remote Bluetooth device with hardware address " + remoteDeviceHardwareAddress + " is required to sync.");
        Date lastReceivedVersion = SyncContentProviderUtil.getLastReceived(context, remoteDeviceHardwareAddress, filePath);
        if(lastReceivedVersion == null) {
            Log.i(TAG, "File " + filePath + " from remote Bluetooth device with hardware address " + remoteDeviceHardwareAddress + " is required to sync because it was never received before.");
            return true;
        }
        if(newLastModified.before(lastReceivedVersion)) {
            Log.wtf(TAG, "The last received version of the file "
                    + filePath
                    + " is younger than the date of its last modification.");
            throw new IllegalStateException(
                    "The last received version of a file cannot be younger than the date of its last modification.");
        }
        if(newLastModified.equals(lastReceivedVersion)) {
            Log.i(TAG, "File " + filePath + " from remote Bluetooth device with hardware address " + remoteDeviceHardwareAddress + " is not required to sync.");
            return false;
        }
        Log.i(TAG, "File " + filePath + " from remote Bluetooth device with hardware address " + remoteDeviceHardwareAddress + " is required to sync.");
        return true;
    }

    private void updateSyncItem(String remoteDeviceHardwareAddress, String filePath, Date newLastModified, byte[] fileContent) {
        Log.i(TAG, "Updating data for file " + filePath
                + " from remote Bluetooth device with hardware address " + remoteDeviceHardwareAddress
                + " in table " + SyncContract.ReceivedSyncItemEntry.TABLE_NAME + ".");
        ContentValues contentValues = new ContentValues();
        contentValues.put(SyncContract.ReceivedSyncItemEntry.COLUMN_SOURCE_HARDWARE_ADDRESS, remoteDeviceHardwareAddress);
        contentValues.put(SyncContract.ReceivedSyncItemEntry.COLUMN_FILE_PATH, filePath);
        contentValues.put(SyncContract.ReceivedSyncItemEntry.COLUMN_LAST_RECEIVED_VERSION_TIMESTAMP, newLastModified.getTime());
        int updatedRows = contentResolver.update(
                SyncContract.ReceivedSyncItemEntry.CONTENT_URI,
                contentValues,
                SqlUtil.createSelectionString(
                        SyncContract.ReceivedSyncItemEntry.COLUMN_SOURCE_HARDWARE_ADDRESS,
                        SyncContract.ReceivedSyncItemEntry.COLUMN_FILE_PATH),
                new String[]{remoteDeviceHardwareAddress, filePath});
        if(updatedRows < 1) {
            contentValues.put(SyncContract.ReceivedSyncItemEntry.COLUMN_LAST_INCORPORATED_VERSION_TIMESTAMP, 0L);
            contentResolver.insert(SyncContract.ReceivedSyncItemEntry.CONTENT_URI, contentValues);
        }
        Log.i(TAG, "Update complete.");
        Log.i(TAG, "Writing sync item to internal storage.");
        String syncFilePath = FileUtil.combinePaths(
                context.getString(R.string.sync_directory_name),
                remoteDeviceHardwareAddress,
                filePath);
        FileUtil.writeToInternalStorage(context, syncFilePath, fileContent);
        Log.i(TAG, "Sync item has been written to internal storage.");
    }

    public void stopContinuousReceiveSync() {
        Log.i(TAG, "Stopping continuous receiving of bluetooth sync connections.");
        this.isReceiving = false;
    }

    private void closeServerSocket() throws IOException {
        Log.i(TAG, "Closing server socket.");
        if(serverSocket != null) {
            try {
                serverSocket.close();
                Log.i(TAG, "Successfully closed server socket.");
            }
            catch (IOException e) {
                Log.e(TAG, "I/O error while closing server socket.");
                throw e;
            }
        }
    }
}
