package edu.hm.cs.ig.passbutler.password;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Florian Kraus on 20.12.2017.
 */


public class PassphraseDB extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "passphrase.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_WORDS = "words";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_WORD = "word";

    public PassphraseDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // you can use an alternate constructor to specify a database location
        // (such as a folder on the sd card)
        // you must ensure that this folder is available and you have permission
        // to write to it
        //super(context, DATABASE_NAME, context.getExternalFilesDir(null).getAbsolutePath(), null, DATABASE_VERSION);

    }

    public String getWordById(int id) throws CursorIndexOutOfBoundsException {
        String word = "";
        String[] columns = new String[]{COLUMN_WORD};
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        Cursor cursor;

        try (SQLiteDatabase db = getReadableDatabase()) {
            cursor = db.query(
                    TABLE_WORDS, columns, selection,
                    selectionArgs, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD));
                cursor.close();
            }
        }
        return word;
    }

}

