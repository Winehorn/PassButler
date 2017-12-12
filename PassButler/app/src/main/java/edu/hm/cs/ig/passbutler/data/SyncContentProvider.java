package edu.hm.cs.ig.passbutler.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;

import edu.hm.cs.ig.passbutler.util.SqlUtil;

/**
 * Created by dennis on 03.12.17.
 */

public class SyncContentProvider extends ContentProvider {

    public static final String PATH_WILDCARD_INT = "/#";
    public static final String PATH_WILDCARD_STRING = "/*";
    public static final int DATA_SOURCES_ALL = 100;
    public static final int DATA_SOURCES_SINGLE = 101;
    public static final int BLUETOOTH_SYNC_DEVICES_ALL = 200;
    public static final int BLUETOOTH_SYNC_DEVICES_SINGLE = 201;
    public static final int RECEIVED_SYNC_ITEMS_ALL = 300;
    public static final int RECEIVED_SYNC_ITEMS_SINGLE = 301;
    public static final int SENT_SYNC_ITEMS_ALL = 400;
    public static final int SENT_SYNC_ITEMS_SINGLE = 401;
    public static final UriMatcher uriMatcher = buildUriMatcher();
    private SyncDbHelper syncDbHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(
                SyncContract.AUTHORITY,
                SyncContract.PATH_DATA_SOURCES,
                DATA_SOURCES_ALL);
        uriMatcher.addURI(
                SyncContract.AUTHORITY,
                SyncContract.PATH_DATA_SOURCES + PATH_WILDCARD_INT,
                DATA_SOURCES_SINGLE);
        uriMatcher.addURI(
                SyncContract.AUTHORITY,
                SyncContract.PATH_BLUETOOTH_SYNC_DEVICES,
                BLUETOOTH_SYNC_DEVICES_ALL);
        uriMatcher.addURI(
                SyncContract.AUTHORITY,
                SyncContract.PATH_BLUETOOTH_SYNC_DEVICES + PATH_WILDCARD_INT,
                BLUETOOTH_SYNC_DEVICES_SINGLE);
        uriMatcher.addURI(
                SyncContract.AUTHORITY,
                SyncContract.PATH_RECEIVED_SYNC_ITEMS,
                RECEIVED_SYNC_ITEMS_ALL);
        uriMatcher.addURI(
                SyncContract.AUTHORITY,
                SyncContract.PATH_RECEIVED_SYNC_ITEMS + PATH_WILDCARD_INT,
                RECEIVED_SYNC_ITEMS_SINGLE);
        uriMatcher.addURI(
                SyncContract.AUTHORITY,
                SyncContract.PATH_SENT_SYNC_ITEMS,
                SENT_SYNC_ITEMS_ALL);
        uriMatcher.addURI(
                SyncContract.AUTHORITY,
                SyncContract.PATH_SENT_SYNC_ITEMS + PATH_WILDCARD_INT,
                SENT_SYNC_ITEMS_SINGLE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        syncDbHelper = new SyncDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {
        final SQLiteDatabase dataBase = syncDbHelper.getReadableDatabase();
        int match = uriMatcher.match(uri);
        Cursor ret;
        switch (match) {
            case DATA_SOURCES_ALL: {
                ret = dataBase.query(
                        SyncContract.DataSourceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case DATA_SOURCES_SINGLE: {
                String id = uri.getPathSegments().get(1);
                String newSelection = SyncContract.DataSourceEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX;
                String[] newSelectionArgs = new String[]{id};
                ret = dataBase.query(
                        SyncContract.DataSourceEntry.TABLE_NAME,
                        projection,
                        newSelection,
                        newSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case BLUETOOTH_SYNC_DEVICES_ALL: {
                ret = dataBase.query(
                        SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case BLUETOOTH_SYNC_DEVICES_SINGLE: {
                String id = uri.getPathSegments().get(1);
                String newSelection = SyncContract.BluetoothSyncDeviceEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX;
                String[] newSelectionArgs = new String[]{id};
                ret = dataBase.query(
                        SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME,
                        projection,
                        newSelection,
                        newSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case RECEIVED_SYNC_ITEMS_ALL: {
                ret = dataBase.query(
                        SyncContract.ReceivedSyncItemEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case RECEIVED_SYNC_ITEMS_SINGLE: {
                String id = uri.getPathSegments().get(1);
                String newSelection = SyncContract.ReceivedSyncItemEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX;
                String[] newSelectionArgs = new String[]{id};
                ret = dataBase.query(
                        SyncContract.ReceivedSyncItemEntry.TABLE_NAME,
                        projection,
                        newSelection,
                        newSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case SENT_SYNC_ITEMS_ALL: {
                ret = dataBase.query(
                        SyncContract.SentSyncItemEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case SENT_SYNC_ITEMS_SINGLE: {
                String id = uri.getPathSegments().get(1);
                String newSelection = SyncContract.SentSyncItemEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX;
                String[] newSelectionArgs = new String[]{id};
                ret = dataBase.query(
                        SyncContract.SentSyncItemEntry.TABLE_NAME,
                        projection,
                        newSelection,
                        newSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default: {
                throw new UnsupportedOperationException("The URI " + uri + " is unknown.");
            }
        }
        ret.setNotificationUri(getContext().getContentResolver(), uri);
        return ret;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase database = syncDbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        Uri ret;
        switch (match) {
            case DATA_SOURCES_ALL: {
                long id = database.insert(
                        SyncContract.DataSourceEntry.TABLE_NAME,
                        null,
                        values);
                if(id > 0) {
                    ret = ContentUris.withAppendedId(SyncContract.DataSourceEntry.CONTENT_URI, id);
                }
                else {
                    throw new SQLiteException("Failed to insert row into " + uri + ".");
                }
                break;
            }
            case BLUETOOTH_SYNC_DEVICES_ALL: {
                long id = database.insert(
                        SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME,
                        null,
                        values);
                if(id > 0) {
                    ret = ContentUris.withAppendedId(SyncContract.BluetoothSyncDeviceEntry.CONTENT_URI, id);
                }
                else {
                    throw new SQLiteException("Failed to insert row into " + uri + ".");
                }
                break;
            }
            case RECEIVED_SYNC_ITEMS_ALL: {
                long id = database.insert(
                        SyncContract.ReceivedSyncItemEntry.TABLE_NAME,
                        null,
                        values);
                if(id > 0) {
                    ret = ContentUris.withAppendedId(SyncContract.ReceivedSyncItemEntry.CONTENT_URI, id);
                }
                else {
                    throw new SQLiteException("Failed to insert row into " + uri + ".");
                }
                break;
            }
            case SENT_SYNC_ITEMS_ALL: {
                long id = database.insert(
                        SyncContract.SentSyncItemEntry.TABLE_NAME,
                        null,
                        values);
                if(id > 0) {
                    ret = ContentUris.withAppendedId(SyncContract.SentSyncItemEntry.CONTENT_URI, id);
                }
                else {
                    throw new SQLiteException("Failed to insert row into " + uri + ".");
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("The URI " + uri + " is unknown.");
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ret;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase database = syncDbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int tasksDeleted;
        switch (match) {
            case DATA_SOURCES_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksDeleted = database.delete(
                        SyncContract.DataSourceEntry.TABLE_NAME,
                        SyncContract.DataSourceEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX,
                        new String[]{id});
                break;
            }
            case BLUETOOTH_SYNC_DEVICES_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksDeleted = database.delete(
                        SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME,
                        SyncContract.BluetoothSyncDeviceEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX,
                        new String[]{id});
                break;
            }
            case RECEIVED_SYNC_ITEMS_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksDeleted = database.delete(
                        SyncContract.ReceivedSyncItemEntry.TABLE_NAME,
                        SyncContract.ReceivedSyncItemEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX,
                        new String[]{id});
                break;
            }
            case SENT_SYNC_ITEMS_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksDeleted = database.delete(
                        SyncContract.SentSyncItemEntry.TABLE_NAME,
                        SyncContract.SentSyncItemEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX,
                        new String[]{id});
                break;
            }
            default: {
                throw new UnsupportedOperationException("The URI " + uri + " is unknown.");
            }
        }
        if (tasksDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return tasksDeleted;
    }

    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues values,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {
        final SQLiteDatabase database = syncDbHelper.getWritableDatabase();
        int tasksUpdated;
        int match = uriMatcher.match(uri);

        switch (match) {
            case DATA_SOURCES_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksUpdated = database.update(
                        SyncContract.DataSourceEntry.TABLE_NAME,
                        values,
                        SyncContract.DataSourceEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX,
                        new String[]{id});
                break;
            }
            case BLUETOOTH_SYNC_DEVICES_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksUpdated = database.update(
                        SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME,
                        values,
                        SyncContract.BluetoothSyncDeviceEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX,
                        new String[]{id});
                break;
            }
            case RECEIVED_SYNC_ITEMS_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksUpdated = database.update(
                        SyncContract.ReceivedSyncItemEntry.TABLE_NAME,
                        values,
                        SyncContract.ReceivedSyncItemEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX,
                        new String[]{id});
                break;
            }
            case SENT_SYNC_ITEMS_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksUpdated = database.update(
                        SyncContract.SentSyncItemEntry.TABLE_NAME,
                        values,
                        SyncContract.SentSyncItemEntry._ID + SqlUtil.SELECTION_PLACEHOLDER_SUFFIX,
                        new String[]{id});
                break;
            }
            default: {
                throw new UnsupportedOperationException("The URI " + uri + " is unknown.");
            }
        }
        if (tasksUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return tasksUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // This method is not needed for PassButler.
        throw new NotImplementedException("The method getType is not implemented.");
    }
}
