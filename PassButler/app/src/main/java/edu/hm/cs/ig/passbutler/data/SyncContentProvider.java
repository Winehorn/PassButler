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

/**
 * Created by dennis on 03.12.17.
 */

public class SyncContentProvider extends ContentProvider {

    public static final String PATH_WILDCARD_INT = "/#";
    public static final String PATH_WILDCARD_STRING = "/*";
    public static final String SELECTION_PLACEHOLDER_SUFFIX = "=?";
    public static final int BLUETOOTH_SYNC_DEVICES_ALL = 100;
    public static final int BLUETOOTH_SYNC_DEVICES_SINGLE = 101;
    public static final int SYNC_ITEMS_ALL = 200;
    public static final int SYNC_ITEMS_SINGLE = 201;
    public static final UriMatcher uriMatcher = buildUriMatcher();
    private SyncDbHelper syncDbHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
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
                SyncContract.PATH_SYNC_ITEMS,
                SYNC_ITEMS_ALL);
        uriMatcher.addURI(
                SyncContract.AUTHORITY,
                SyncContract.PATH_SYNC_ITEMS + PATH_WILDCARD_INT,
                SYNC_ITEMS_SINGLE);
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
                String newSelection = SyncContract.BluetoothSyncDeviceEntry._ID + SELECTION_PLACEHOLDER_SUFFIX;
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
            case SYNC_ITEMS_ALL: {
                ret = dataBase.query(
                        SyncContract.SyncItemEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case SYNC_ITEMS_SINGLE: {
                String id = uri.getPathSegments().get(1);
                String newSelection = SyncContract.SyncItemEntry._ID + SELECTION_PLACEHOLDER_SUFFIX;
                String[] newSelectionArgs = new String[]{id};
                ret = dataBase.query(
                        SyncContract.SyncItemEntry.TABLE_NAME,
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
            case SYNC_ITEMS_ALL: {
                long id = database.insert(
                        SyncContract.SyncItemEntry.TABLE_NAME,
                        null,
                        values);
                if(id > 0) {
                    ret = ContentUris.withAppendedId(SyncContract.SyncItemEntry.CONTENT_URI, id);
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
            case BLUETOOTH_SYNC_DEVICES_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksDeleted = database.delete(
                        SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME,
                        SyncContract.BluetoothSyncDeviceEntry._ID + SELECTION_PLACEHOLDER_SUFFIX,
                        new String[]{id});
                break;
            }
            case SYNC_ITEMS_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksDeleted = database.delete(
                        SyncContract.SyncItemEntry.TABLE_NAME,
                        SyncContract.SyncItemEntry._ID + SELECTION_PLACEHOLDER_SUFFIX,
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
            case BLUETOOTH_SYNC_DEVICES_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksUpdated = database.update(
                        SyncContract.BluetoothSyncDeviceEntry.TABLE_NAME,
                        values,
                        SyncContract.BluetoothSyncDeviceEntry._ID + SELECTION_PLACEHOLDER_SUFFIX,
                        new String[]{id});
                break;
            }
            case SYNC_ITEMS_SINGLE: {
                String id = uri.getPathSegments().get(1);
                tasksUpdated = database.update(
                        SyncContract.SyncItemEntry.TABLE_NAME,
                        values,
                        SyncContract.SyncItemEntry._ID + SELECTION_PLACEHOLDER_SUFFIX,
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
