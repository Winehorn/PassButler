package edu.hm.cs.ig.passbutler.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dennis on 03.12.17.
 */

public class SyncContract {

    public static final String AUTHORITY = "edu.hm.cs.ig.passbutler";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_BLUETOOTH_SYNC_DEVICES = "bluetooth-sync-devices";
    public static final String PATH_SYNC_ITEMS = "sync-items";

    public static final class BluetoothSyncDeviceEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BLUETOOTH_SYNC_DEVICES).build();
        public static final String TABLE_NAME = "bluetooth_sync_devices";
        public static final String COLUMN_DEVICE_NAME = "device_name";
        public static final String COLUMN_DEVICE_HARDWARE_ADDRESS = "device_hardware_address";
    }

    public static final class SyncItemEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SYNC_ITEMS).build();
        public static final String TABLE_NAME = "sync_items";
        public static final String COLUMN_SOURCE_UUID = "source_uuid";
        public static final String COLUMN_FILE_HASH = "file_hash";
        public static final String COLUMN_LAST_EDITED_UNIX_TIME = "last_edited_unix_time";
    }
}
