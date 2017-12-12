package edu.hm.cs.ig.passbutler.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dennis on 03.12.17.
 */

public class SyncContract {

    public static final String AUTHORITY = "edu.hm.cs.ig.passbutler";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_DATA_SOURCES = "data-sources";
    public static final String PATH_BLUETOOTH_SYNC_DEVICES = "bluetooth-sync-devices";
    public static final String PATH_RECEIVED_SYNC_ITEMS = "received-sync-items";
    public static final String PATH_SENT_SYNC_ITEMS = "sent-sync-items";

    public static final class DataSourceEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DATA_SOURCES).build();
        public static final String TABLE_NAME = "data_sources";
        public static final String COLUMN_FILE_PATH = "file_path";
        public static final String COLUMN_FILE_HASH = "file_hash";
        public static final String COLUMN_LAST_MODIFIED_TIMESTAMP = "last_modified_timestamp";
    }

    public static final class BluetoothSyncDeviceEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BLUETOOTH_SYNC_DEVICES).build();
        public static final String TABLE_NAME = "bluetooth_sync_devices";
        public static final String COLUMN_DEVICE_NAME = "device_name";
        public static final String COLUMN_DEVICE_HARDWARE_ADDRESS = "device_hardware_address";
    }

    public static final class ReceivedSyncItemEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECEIVED_SYNC_ITEMS).build();
        public static final String TABLE_NAME = "received_sync_items";
        public static final String COLUMN_SOURCE_HARDWARE_ADDRESS = "source_hardware_address";
        public static final String COLUMN_FILE_PATH = "file_path";
        public static final String COLUMN_LAST_RECEIVED_VERSION_TIMESTAMP = "last_received_version_timestamp";
        public static final String COLUMN_LAST_INCORPORATED_VERSION_TIMESTAMP = "last_incorporated_version_timestamp";
    }

    public static final class SentSyncItemEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SENT_SYNC_ITEMS).build();
        public static final String TABLE_NAME = "sent_sync_items";
        public static final String COLUMN_DESTINATION_HARDWARE_ADDRESS = "destination_hardware_address";
        public static final String COLUMN_FILE_PATH = "file_path";
        public static final String COLUMN_LAST_SENT_VERSION_TIMESTAMP = "last_sent_version_timestamp";
    }
}
