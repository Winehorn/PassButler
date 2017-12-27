package edu.hm.cs.ig.passbutler.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.NoSuchPaddingException;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.data.AccountListHandler;
import edu.hm.cs.ig.passbutler.data.SyncContract;
import edu.hm.cs.ig.passbutler.security.KeyHolder;
import edu.hm.cs.ig.passbutler.security.MissingKeyException;
import edu.hm.cs.ig.passbutler.util.SyncContentProviderUtil;
import edu.hm.cs.ig.passbutler.util.FileUtil;
import edu.hm.cs.ig.passbutler.util.SqlUtil;

/**
 * Created by dennis on 09.12.17.
 */

public class SyncFileMerger {

    public static final String TAG = SyncFileMerger.class.getName();
    public static final int MERGE_ALL = 0;
    public static final int MERGE_SINGLE = 1;
    private Context context;
    private Handler handler;
    private ContentResolver contentResolver;

    SyncFileMerger(final Context context, Looper looper) {
        Log.i(TAG, "Initializing " + SyncFileMerger.class.getSimpleName() + ".");
        this.context = context;
        this.handler = new Handler(looper) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case MERGE_ALL: {
                        Log.i(TAG, "Received message to merge all available files.");
                        mergeAll(new Date());
                        break;
                    }
                    case MERGE_SINGLE: {
                        Log.i(TAG, "Received message to merge a single file.");
                        Bundle bundle= inputMessage.getData();
                        String filePath = bundle.getString(context.getString(R.string.bundle_key_file_path));
                        String hardwareAddress = bundle.getString(context.getString(R.string.bundle_key_hardware_address));
                        Log.i(TAG, "File to merge is " + filePath + " from remote device with hardware address " + hardwareAddress + ".");
                        Date lastReceivedVersion = new Date(
                                bundle.getLong(context.getString(R.string.bundle_key_last_received_version_timestamp)));
                        Date lastIncorporatedVersion = new Date(
                                bundle.getLong(context.getString(R.string.bundle_key_last_incorporated_version_timestamp)));
                        Date mergeDate = new Date(
                                bundle.getLong(context.getString(R.string.bundle_key_merge_date)));
                        mergeSingle(filePath, hardwareAddress, lastReceivedVersion, lastIncorporatedVersion, mergeDate);
                        break;
                    }
                    default: {
                        super.handleMessage(inputMessage);
                        break;
                    }
                }
            }
        };
        this.contentResolver = context.getContentResolver();
    }

    public Handler getHandler() {
        return this.handler;
    }

    public void mergeAll(Date mergeDate) {
        Log.i(TAG, "Starting to merge all available files.");
        Cursor cursor = contentResolver.query(
                SyncContract.ReceivedSyncItemEntry.CONTENT_URI,
                null,
                null,
                null,
                SyncContract.ReceivedSyncItemEntry.COLUMN_LAST_RECEIVED_VERSION_TIMESTAMP);
        int filePathIndex = cursor.getColumnIndex(SyncContract.ReceivedSyncItemEntry.COLUMN_FILE_PATH);
        int hardwareAddressIndex = cursor.getColumnIndex(SyncContract.ReceivedSyncItemEntry.COLUMN_SOURCE_HARDWARE_ADDRESS);
        int lastReceivedVersionIndex = cursor.getColumnIndex(SyncContract.ReceivedSyncItemEntry.COLUMN_LAST_RECEIVED_VERSION_TIMESTAMP);
        int lastIncorporatedVersionIndex = cursor.getColumnIndex(SyncContract.ReceivedSyncItemEntry.COLUMN_LAST_INCORPORATED_VERSION_TIMESTAMP);
        while (cursor.moveToNext()) {
            String filePath = cursor.getString(filePathIndex);
            String hardwareAddress = cursor.getString(hardwareAddressIndex);
            Date lastReceivedVersionTimestamp = new Date(cursor.getLong(lastReceivedVersionIndex));
            Date lastIncorporatedVersionTimestamp = new Date(cursor.getLong(lastIncorporatedVersionIndex));
            if(isMergeRequired(filePath, hardwareAddress, lastReceivedVersionTimestamp)) {
                mergeSingle(filePath, hardwareAddress, lastReceivedVersionTimestamp, lastIncorporatedVersionTimestamp, mergeDate);
            }
        }
        Log.i(TAG, "Finished to merge all available sync items.");

        // TODO sort order correct?
    }

    public boolean mergeSingle(
            String filePath,
            String hardwareAddress,
            Date lastReceivedTimeStamp,
            Date lastIncorporatedTimestamp,
            Date mergeDate) {
        Log.i(TAG, "Starting to merge file "
                + filePath + " from device with hardware address "
                + hardwareAddress + " received on " +
                lastReceivedTimeStamp + ".");
        try {
            KeyHolder keyHolder = KeyHolder.getInstance();
            if(!lastReceivedTimeStamp.after(lastIncorporatedTimestamp)) {
                Log.i(TAG, "Merge aborted. Nothing has changed since last merge.");
                return false;
            }
            AccountListHandler localData;
            try {
                Log.i(TAG, "Reading local version of file to merge.");
                localData = AccountListHandler.getFromInternalStorage(
                        context,
                        filePath,
                        keyHolder.getKey());
            }
            catch (JSONException
                    | NoSuchAlgorithmException
                    | InvalidKeyException
                    | NoSuchPaddingException
                    | IOException e) {
                Log.e(TAG, "Merge failed. Could not read local data.");
                return false;
            }
            AccountListHandler syncData;
            try {
                Log.i(TAG, "Reading version of file to merge from remote device.");
                syncData = AccountListHandler.getFromInternalStorage(
                        context,
                        FileUtil.combinePaths(context.getString(R.string.sync_directory_name), hardwareAddress, filePath),
                        keyHolder.getKey());
            }
            catch (JSONException
                    | NoSuchAlgorithmException
                    | InvalidKeyException
                    | NoSuchPaddingException
                    | IOException e) {
                Log.e(TAG, "Merge failed. Could not read data from sync item.");
                // TODO: In this case the password was changed on remote device. What to do?
                return false;
            }
            boolean dataChanged = localData.merge(context, filePath, syncData, lastReceivedTimeStamp, mergeDate);
            if(dataChanged) {
                Log.i(TAG, "Saving merged data to internal storage.");
                localData.saveToInternalStorage(
                        context,
                        filePath,
                        lastReceivedTimeStamp,
                        keyHolder.getKey(),
                        true);
                Log.i(TAG, "Saving changed meta data.");
                ContentValues contentValues = new ContentValues();
                contentValues.put(
                        SyncContract.ReceivedSyncItemEntry.COLUMN_LAST_INCORPORATED_VERSION_TIMESTAMP,
                        lastIncorporatedTimestamp.getTime());
                int updatedRows = contentResolver.update(
                        SyncContract.ReceivedSyncItemEntry.CONTENT_URI,
                        contentValues,
                        SqlUtil.createSelectionString(
                                SyncContract.ReceivedSyncItemEntry.COLUMN_SOURCE_HARDWARE_ADDRESS,
                                SyncContract.ReceivedSyncItemEntry.COLUMN_FILE_PATH),
                        new String[]{hardwareAddress, filePath});
                if(updatedRows < 1) {
                    throw new IllegalStateException("There must be a row for a received sync item to update.");
                }
                contentValues = new ContentValues();
                contentValues.put(
                        SyncContract.DataSourceEntry.COLUMN_LAST_MODIFIED_TIMESTAMP,
                        mergeDate.getTime());
                updatedRows = contentResolver.update(
                        SyncContract.DataSourceEntry.CONTENT_URI,
                        contentValues,
                        SqlUtil.createSelectionString(SyncContract.DataSourceEntry.COLUMN_FILE_PATH),
                        new String[]{filePath});
                if(updatedRows < 1) {
                    throw new IllegalStateException("There must be a row for a data source to update.");
                }
                Log.i(TAG, "Finished to merge file "
                        + filePath + " from device with hardware address "
                        + hardwareAddress + " received on " +
                        lastReceivedTimeStamp + ".");
            }
            return dataChanged;
        }
        catch (MissingKeyException e) {
            Log.e(TAG, "Merge failed. Decryption key is not available.");
            return false;
        }
    }

    private boolean isMergeRequired(String filePath, String hardwareAddress, Date lastReceivedVersionTimestamp) {
        Log.i(TAG, "Checking if file " + filePath + " from device with hardware address " + hardwareAddress + " needs to be merged.");
        Date lastModifiedTimestamp = SyncContentProviderUtil.getLastModified(context, filePath);
        if(lastReceivedVersionTimestamp.after(lastModifiedTimestamp)) {
            Log.i(TAG, "The file needs to be merged.");
            return true;
        }
        Log.i(TAG, "The file does not need to be merged.");
        return false;
    }
}
