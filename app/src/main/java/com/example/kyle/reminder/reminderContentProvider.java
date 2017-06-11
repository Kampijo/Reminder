package com.example.kyle.reminder;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by kyle on 10/06/17.
 */

public class reminderContentProvider extends ContentProvider {

    private static final int NOTE = 1;
    private static final int NOTE_ID = 2;

    private static final int ALERT = 3;
    private static final int ALERT_ID = 4;

    private static final UriMatcher URI_MATCHER;
    private reminderDataHelper mOpenHelper;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(reminderContract.AUTHORITY, reminderContract.PATH_NOTE, NOTE);
        URI_MATCHER.addURI(reminderContract.AUTHORITY, reminderContract.PATH_NOTE_ID, NOTE_ID);
        URI_MATCHER.addURI(reminderContract.AUTHORITY, reminderContract.PATH_ALERT, ALERT);
        URI_MATCHER.addURI(reminderContract.AUTHORITY, reminderContract.PATH_ALERT_ID, ALERT_ID);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new reminderDataHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        switch(URI_MATCHER.match(uri)) {
            case NOTE:
                builder.setTables(reminderContract.Notes.TABLE_NAME);
                builder.appendWhere(reminderContract.Notes.TYPE + " = " +
                        reminderContract.PATH_NOTE);
                break;
            case NOTE_ID:
                builder.setTables(reminderContract.Notes.TABLE_NAME);
                builder.appendWhere(reminderContract.Notes._ID + " = " +
                        uri.getLastPathSegment());
                break;
            case ALERT:
                builder.setTables(reminderContract.Alerts.TABLE_NAME);
                builder.appendWhere(reminderContract.Alerts.TYPE + " = " +
                        reminderContract.PATH_ALERT);
                break;
            case ALERT_ID:
                builder.setTables(reminderContract.Alerts.TABLE_NAME);
                builder.appendWhere(reminderContract.Alerts._ID + " = " +
                        uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI: " + uri);
        }
        Cursor cursor =
                builder.query(
                        db,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
        return cursor;
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

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
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
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int delCount = 0;
        String id, where;
        switch (URI_MATCHER.match(uri)) {
            case NOTE:
                delCount = db.delete(
                        reminderContract.Notes.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case NOTE_ID:
                id = uri.getLastPathSegment();
                where = reminderContract.Notes._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = db.delete(
                        reminderContract.Notes.TABLE_NAME,
                        where,
                        selectionArgs);
                break;
            case ALERT:
                delCount = db.delete(
                        reminderContract.Alerts.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case ALERT_ID:
                id = uri.getLastPathSegment();
                where = reminderContract.Alerts._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = db.delete(
                        reminderContract.Alerts.TABLE_NAME,
                        where,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // notify all listeners of changes:
        if (delCount > 0 ){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int updateCount = 0;
        String id, where;
        switch (URI_MATCHER.match(uri)) {
            case NOTE:
                updateCount = db.update(
                        reminderContract.Notes.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case NOTE_ID:
                id = uri.getLastPathSegment();
                where = reminderContract.Notes._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(
                        reminderContract.Notes.TABLE_NAME,
                        values,
                        where,
                        selectionArgs);
                break;
            case ALERT:
                updateCount = db.update(
                        reminderContract.Notes.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case ALERT_ID:
                id = uri.getLastPathSegment();
                where = reminderContract.Alerts._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(
                        reminderContract.Alerts.TABLE_NAME,
                        values,
                        where,
                        selectionArgs);
                break;
            default:
                // no support for updating photos or entities
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // notify all listeners of changes:
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }
}
