package com.example.kyle.reminder;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.URI;

/**
 * Created by kyle on 10/06/17.
 */

public class reminderContentProvider extends ContentProvider {

    private static final int NOTE = 1;
    private static final int NOTE_ID = 2;

    private static final int ALERT = 3;
    private static final int ALERT_ID = 4;

    private static final UriMatcher URI_MATCHER;
    private reminderDatabase mDatabase;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(reminderContract.AUTHORITY, reminderContract.PATH_NOTE, NOTE);
        URI_MATCHER.addURI(reminderContract.AUTHORITY, reminderContract.PATH_NOTE_ID, NOTE_ID);
        URI_MATCHER.addURI(reminderContract.AUTHORITY, reminderContract.PATH_ALERT, ALERT);
        URI_MATCHER.addURI(reminderContract.AUTHORITY, reminderContract.PATH_ALERT_ID, ALERT_ID);
    }

    @Override
    public boolean onCreate() {
        mDatabase = new reminderDatabase(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case NOTE:
                return reminderContract.Notes.CONTENT_TYPE;
            case NOTE_ID:
                return reminderContract.Notes.CONTENT_ITEM_TYPE;
            case ALERT:
                return reminderContract.Alerts.CONTENT_TYPE;
            case ALERT_ID:
                return reminderContract.Alerts.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        if(URI_MATCHER.match(uri) != NOTE && URI_MATCHER.match(uri) != ALERT){
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

        SQLiteDatabase db = mDatabase.getWritableDatabase();
        if(URI_MATCHER.match(uri) == NOTE){
            long id = db.insert(reminderContract.Notes.TABLE_NAME, null, contentValues);
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        } else {
            long id = db.insert(reminderContract.Alerts.TABLE_NAME, null, contentValues);
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
