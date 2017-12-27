package edu.hm.cs.ig.passbutler.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Date;

import edu.hm.cs.ig.passbutler.data.FileMetaData;
import edu.hm.cs.ig.passbutler.data.SyncContract;

/**
 * Created by dennis on 10.12.17.
 */

public class SyncContentProviderUtil {

    public static final String TAG = SyncContentProviderUtil.class.getName();

    public static void persistFileMetaData(Context context, FileMetaData fileMetaData) {
        Log.i(TAG, "Persisting meta data of file " + fileMetaData.getFilePath() + " in table " + SyncContract.DataSourceEntry.TABLE_NAME + ".");
        ContentValues contentValues = new ContentValues();
        contentValues.put(SyncContract.DataSourceEntry.COLUMN_FILE_PATH, fileMetaData.getFilePath());
        contentValues.put(SyncContract.DataSourceEntry.COLUMN_LAST_MODIFIED_TIMESTAMP, fileMetaData.getLastModified().getTime());
        contentValues.put(SyncContract.DataSourceEntry.COLUMN_FILE_HASH, fileMetaData.getFileHash());
        context.getContentResolver().insert(SyncContract.DataSourceEntry.CONTENT_URI, contentValues);
        Log.i(TAG, "Persisted meta data of file " + fileMetaData.getFilePath() + " in table " + SyncContract.DataSourceEntry.TABLE_NAME + ".");
    }

    public static Date getLastSent(Context context, String hardwareAddress, String filePath) {
        Log.i(TAG, "Retrieving date of last sending of file " + filePath + " to device with hardware address " + hardwareAddress +".");
        Date lastSent = getColumnDateValue(
                context,
                SyncContract.SentSyncItemEntry.CONTENT_URI,
                SyncContract.SentSyncItemEntry.COLUMN_LAST_SENT_VERSION_TIMESTAMP,
                SqlUtil.createSelectionString(
                        SyncContract.SentSyncItemEntry.COLUMN_DESTINATION_HARDWARE_ADDRESS,
                        SyncContract.SentSyncItemEntry.COLUMN_FILE_PATH),
                new String[]{hardwareAddress, filePath},
                null);
        Log.i(TAG, "Date of last sending is " + lastSent + ".");
        return lastSent;
    }

    public static String getHardwareAddress(Context context, String hardwareAddressParam) {
        Log.i(TAG, "Retrieving hardware address.");
        String hardwareAddress = getColumnStringValue(
                context,
                SyncContract.BluetoothSyncDeviceEntry.CONTENT_URI,
                SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_HARDWARE_ADDRESS,
                SqlUtil.createSelectionString(SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_HARDWARE_ADDRESS),
                new String[]{hardwareAddressParam},
                null);
        Log.i(TAG, "Hardware address is " + hardwareAddress + ".");
        return hardwareAddress;
    }

    public static String getFileHash(Context context, String filePath) {
        Log.i(TAG, "Retrieving hash of file " + filePath + ".");
        String fileHash = getColumnStringValue(
                context,
                SyncContract.DataSourceEntry.CONTENT_URI,
                SyncContract.DataSourceEntry.COLUMN_FILE_HASH,
                SqlUtil.createSelectionString(SyncContract.DataSourceEntry.COLUMN_FILE_PATH),
                new String[]{filePath},
                null);
        Log.i(TAG, "Hash value is " + fileHash + ".");
        return fileHash;
    }

    public static Date getLastModified(Context context, String filePath) {
        Log.i(TAG, "Retrieving date of last modification of file " + filePath + ".");
        Date lastModified = getColumnDateValue(
                context,
                SyncContract.DataSourceEntry.CONTENT_URI,
                SyncContract.DataSourceEntry.COLUMN_LAST_MODIFIED_TIMESTAMP,
                SqlUtil.createSelectionString(SyncContract.DataSourceEntry.COLUMN_FILE_PATH),
                new String[]{filePath},
                null);
        Log.i(TAG, "Date of last modification is " + lastModified + ".");
        return lastModified;
    }

    public static Date getLastReceived(Context context, String hardwareAddress, String filePath) {
        Log.i(TAG, "Retrieving date of last reception of file " + filePath + " from device with hardware address " + hardwareAddress +".");
        Date lastReceived = getColumnDateValue(
                context,
                SyncContract.ReceivedSyncItemEntry.CONTENT_URI,
                SyncContract.ReceivedSyncItemEntry.COLUMN_LAST_RECEIVED_VERSION_TIMESTAMP,
                SqlUtil.createSelectionString(
                        SyncContract.ReceivedSyncItemEntry.COLUMN_SOURCE_HARDWARE_ADDRESS,
                        SyncContract.ReceivedSyncItemEntry.COLUMN_FILE_PATH),
                new String[]{hardwareAddress, filePath},
                null);
        Log.i(TAG, "Date of last reception is " + lastReceived + ".");
        return lastReceived;
    }

    public static Date getLastIncorporated(Context context, String hardwareAddress, String filePath) {
        Log.i(TAG, "Retrieving date of last incorporation of file " + filePath + " from device with hardware address " + hardwareAddress +".");
        Date lastIncorporated = getColumnDateValue(
                context,
                SyncContract.ReceivedSyncItemEntry.CONTENT_URI,
                SyncContract.ReceivedSyncItemEntry.COLUMN_LAST_INCORPORATED_VERSION_TIMESTAMP,
                SqlUtil.createSelectionString(
                        SyncContract.ReceivedSyncItemEntry.COLUMN_SOURCE_HARDWARE_ADDRESS,
                        SyncContract.ReceivedSyncItemEntry.COLUMN_FILE_PATH),
                new String[]{hardwareAddress, filePath},
                null);
        if(lastIncorporated == null) {
            lastIncorporated = new Date(0L);
        }
        Log.i(TAG, "Date of last incorporation is " + lastIncorporated + ".");
        return lastIncorporated;
    }

    private static Date getColumnDateValue(
            Context context,
            Uri contentUri,
            String column,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        Log.i(TAG, "Retrieving value of column " + column + " from URI " + contentUri+ ".");
        ContentResolver contentResolver = context.getContentResolver();
        try(Cursor cursor = contentResolver.query(
                contentUri,
                new String[]{column},
                selection,
                selectionArgs,
                sortOrder)) {
            if(cursor.getCount() < 1) {
                Log.i(TAG, "Could not retrieve value. No matching row found.");
                return null;
            }
            if(cursor.getCount() > 1) {
                Log.wtf(TAG, "There was more than 1 entry found in the table.");
                throw new IllegalStateException(
                        "There must be at most 1 entry in the table.");
            }
            int columnIndex = cursor.getColumnIndex(column);
            cursor.moveToFirst();
            Date ret = new Date(cursor.getLong(columnIndex));
            Log.i(TAG, "Retrieved value is " + ret + ".");
            return ret;
        }
    }

    private static String getColumnStringValue(
            Context context,
            Uri contentUri,
            String column,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        Log.i(TAG, "Retrieving value of column " + column + " from URI " + contentUri+ ".");
        ContentResolver contentResolver = context.getContentResolver();
        try(Cursor cursor = contentResolver.query(
                contentUri,
                new String[]{column},
                selection,
                selectionArgs,
                sortOrder)) {
            if(cursor.getCount() < 1) {
                Log.i(TAG, "Could not retrieve value. No matching row found.");
                return null;
            }
            if(cursor.getCount() > 1) {
                Log.wtf(TAG, "There was more than 1 entry found in the table.");
                throw new IllegalStateException(
                        "There must be at most 1 entry in the table.");
            }
            int columnIndex = cursor.getColumnIndex(column);
            cursor.moveToFirst();
            String ret = cursor.getString(columnIndex);
            Log.i(TAG, "Retrieved value is " + ret + ".");
            return ret;
        }
    }
}
