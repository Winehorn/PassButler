package edu.hm.cs.ig.passbutler.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dennis on 03.12.17.
 */

public class SyncDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "syncDb.db";
    private static final int VERSION = 1;

    SyncDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_BLUETOOTH_SYNC_DEVICES =
                "CREATE TABLE "  + SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME + " (" +
                SyncContract.BluetoothSyncDeviceEntry._ID                               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_NAME                + " TEXT NOT NULL, " +
                SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_HARDWARE_ADDRESS    + " TEXT NOT NULL, " +
                " UNIQUE (" + SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_HARDWARE_ADDRESS + ") ON CONFLICT REPLACE);";
        db.execSQL(CREATE_TABLE_BLUETOOTH_SYNC_DEVICES);
        final String CREATE_TABLE_SYNC_ITEMS =
                "CREATE TABLE "  + SyncContract.SyncItemEntry.TABLE_NAME + " (" +
                SyncContract.SyncItemEntry._ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SyncContract.SyncItemEntry.COLUMN_SOURCE_UUID   + " TEXT NOT NULL, " +
                SyncContract.SyncItemEntry.COLUMN_FILE_HASH     + " TEXT NOT NULL, " +
                SyncContract.SyncItemEntry.COLUMN_LAST_EDITED_UNIX_TIME + " INTEGER NOT NULL, " +
                " UNIQUE (" + SyncContract.SyncItemEntry.COLUMN_SOURCE_UUID + ") ON CONFLICT REPLACE);";
        db.execSQL(CREATE_TABLE_SYNC_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SyncContract.SyncItemEntry.TABLE_NAME);
        onCreate(db);
    }
}
