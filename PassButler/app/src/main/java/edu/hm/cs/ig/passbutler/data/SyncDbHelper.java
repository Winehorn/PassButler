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

        final String CREATE_TABLE_DATA_SOURCES =
                "CREATE TABLE "  + SyncContract.DataSourceEntry.TABLE_NAME               + " (" +
                        SyncContract.DataSourceEntry._ID                                 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SyncContract.DataSourceEntry.COLUMN_FILE_PATH                    + " TEXT NOT NULL, " +
                        SyncContract.DataSourceEntry.COLUMN_FILE_HASH                    + " TEXT NOT NULL, " +
                        SyncContract.DataSourceEntry.COLUMN_LAST_MODIFIED_TIMESTAMP      + " INTEGER NOT NULL, " +
                        " UNIQUE (" + SyncContract.DataSourceEntry.COLUMN_FILE_PATH      + ") ON CONFLICT REPLACE);";
        db.execSQL(CREATE_TABLE_DATA_SOURCES);

        final String CREATE_TABLE_BLUETOOTH_SYNC_DEVICES =
                "CREATE TABLE "  + SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME                 + " (" +
                SyncContract.BluetoothSyncDeviceEntry._ID                                           + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_NAME                            + " TEXT NOT NULL, " +
                SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_HARDWARE_ADDRESS                + " TEXT NOT NULL, " +
                " UNIQUE (" + SyncContract.BluetoothSyncDeviceEntry.COLUMN_DEVICE_HARDWARE_ADDRESS  + ") ON CONFLICT REPLACE);";
        db.execSQL(CREATE_TABLE_BLUETOOTH_SYNC_DEVICES);

        final String CREATE_TABLE_RECEIVED_SYNC_ITEMS =
                "CREATE TABLE "  + SyncContract.ReceivedSyncItemEntry.TABLE_NAME                    + " (" +
                SyncContract.ReceivedSyncItemEntry._ID                                              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SyncContract.ReceivedSyncItemEntry.COLUMN_SOURCE_HARDWARE_ADDRESS                   + " TEXT NOT NULL, " +
                SyncContract.ReceivedSyncItemEntry.COLUMN_FILE_PATH                                 + " TEXT NOT NULL, " +
                SyncContract.ReceivedSyncItemEntry.COLUMN_LAST_RECEIVED_VERSION_TIMESTAMP           + " INTEGER NOT NULL, " +
                SyncContract.ReceivedSyncItemEntry.COLUMN_LAST_INCORPORATED_VERSION_TIMESTAMP       + " INTEGER NOT NULL, " +
                " UNIQUE (" + SyncContract.ReceivedSyncItemEntry.COLUMN_SOURCE_HARDWARE_ADDRESS     + ", "
                        + SyncContract.ReceivedSyncItemEntry.COLUMN_FILE_PATH                       + ") ON CONFLICT REPLACE);";
        db.execSQL(CREATE_TABLE_RECEIVED_SYNC_ITEMS);

        final String CREATE_TABLE_SENT_SYNC_ITEMS =
                "CREATE TABLE "  + SyncContract.SentSyncItemEntry.TABLE_NAME                        + " (" +
                SyncContract.SentSyncItemEntry._ID                                                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SyncContract.SentSyncItemEntry.COLUMN_DESTINATION_HARDWARE_ADDRESS                  + " TEXT NOT NULL, " +
                SyncContract.SentSyncItemEntry.COLUMN_FILE_PATH                                     + " TEXT NOT NULL, " +
                SyncContract.SentSyncItemEntry.COLUMN_LAST_SENT_VERSION_TIMESTAMP                   + " INTEGER NOT NULL, " +
                " UNIQUE (" + SyncContract.SentSyncItemEntry.COLUMN_DESTINATION_HARDWARE_ADDRESS    + ", "
                + SyncContract.SentSyncItemEntry.COLUMN_FILE_PATH                                   + ") ON CONFLICT REPLACE);";
        db.execSQL(CREATE_TABLE_SENT_SYNC_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SyncContract.DataSourceEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SyncContract.ReceivedSyncItemEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SyncContract.SentSyncItemEntry.TABLE_NAME);
        onCreate(db);
    }
}
