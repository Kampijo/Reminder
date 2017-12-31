package com.example.kyle.reminder;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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

public class ReminderContentProvider extends ContentProvider {

  private static final int NOTE = 1;
  private static final int NOTE_ID = 2;

  private static final int ALERT = 3;
  private static final int ALERT_ID = 4;

  private static final int ALL = 5;
  private static final int ALL_ID = 6;


  private static final UriMatcher URI_MATCHER;
  private ReminderDataHelper mOpenHelper;

  static {
    URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    URI_MATCHER.addURI(ReminderContract.AUTHORITY, ReminderContract.PATH_NOTE, NOTE);
    URI_MATCHER.addURI(ReminderContract.AUTHORITY, ReminderContract.PATH_NOTE_ID, NOTE_ID);
    URI_MATCHER.addURI(ReminderContract.AUTHORITY, ReminderContract.PATH_ALERT, ALERT);
    URI_MATCHER.addURI(ReminderContract.AUTHORITY, ReminderContract.PATH_ALERT_ID, ALERT_ID);
    URI_MATCHER.addURI(ReminderContract.AUTHORITY, ReminderContract.PATH_ALL, ALL);
    URI_MATCHER.addURI(ReminderContract.AUTHORITY, ReminderContract.PATH_ALL_ID, ALL_ID);
  }

  @Override
  public boolean onCreate() {
    mOpenHelper = new ReminderDataHelper(getContext());
    return false;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                      @Nullable String[] selectionArgs, @Nullable String sortOrder) {
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

    switch (URI_MATCHER.match(uri)) {
      case NOTE:
        builder.setTables(ReminderContract.Notes.TABLE_NAME);
        builder.appendWhere(ReminderContract.Notes.TYPE + " = '" +
                ReminderContract.PATH_NOTE + "'");
        break;
      case NOTE_ID:
        builder.setTables(ReminderContract.Notes.TABLE_NAME);
        builder.appendWhere(ReminderContract.Notes._ID + " = " +
                uri.getLastPathSegment());
        break;
      case ALERT:
        builder.setTables(ReminderContract.Alerts.TABLE_NAME);
        builder.appendWhere(ReminderContract.Alerts.TYPE + " = '" +
                ReminderContract.PATH_ALERT + "'");
        break;
      case ALERT_ID:
        builder.setTables(ReminderContract.Alerts.TABLE_NAME);
        builder.appendWhere(ReminderContract.Alerts._ID + " = " +
                uri.getLastPathSegment());
        break;
      case ALL:
        builder.setTables(ReminderContract.Alerts.TABLE_NAME);
        break;
      case ALL_ID:
        builder.setTables(ReminderContract.All.TABLE_NAME);
        builder.appendWhere(ReminderContract.All._ID + " = " +
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
    cursor.setNotificationUri(getContext().getContentResolver(), ReminderContract.BASE_CONTENT_URI);
    return cursor;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    switch (URI_MATCHER.match(uri)) {
      case NOTE:
        return ReminderContract.Notes.CONTENT_TYPE;
      case NOTE_ID:
        return ReminderContract.Notes.CONTENT_ITEM_TYPE;
      case ALERT:
        return ReminderContract.Alerts.CONTENT_TYPE;
      case ALERT_ID:
        return ReminderContract.Alerts.CONTENT_ITEM_TYPE;
      case ALL:
        return ReminderContract.All.CONTENT_TYPE;
      case ALL_ID:
        return ReminderContract.All.CONTENT_ITEM_TYPE;
      default:
        return null;
    }
  }


  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
    Context context = getContext();
    if (context == null) {
      return null;
    }

    ContentResolver contentResolver = context.getContentResolver();
    if (contentResolver == null) {
      return null;
    }

    if (URI_MATCHER.match(uri) != NOTE && URI_MATCHER.match(uri) != ALERT) {
      throw new IllegalArgumentException(
              "Unsupported URI for insertion: " + uri);
    }

    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    if (URI_MATCHER.match(uri) == NOTE) {
      long id = db.insert(ReminderContract.Notes.TABLE_NAME, null, contentValues);
      contentResolver.notifyChange(uri, null);
      return ContentUris.withAppendedId(uri, id);
    } else {
      long id = db.insert(ReminderContract.Alerts.TABLE_NAME, null, contentValues);
      contentResolver.notifyChange(uri, null);
      return ContentUris.withAppendedId(uri, id);
    }
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    String id;
    String where;
    int delCount;

    Context context = getContext();
    if (context == null) {
      return 0;
    }

    ContentResolver contentResolver = context.getContentResolver();
    if (contentResolver == null) {
      return 0;
    }

    switch (URI_MATCHER.match(uri)) {
      case NOTE:
        delCount = db.delete(
                ReminderContract.Notes.TABLE_NAME,
                selection,
                selectionArgs);
        break;
      case NOTE_ID:
        id = uri.getLastPathSegment();
        where = ReminderContract.Notes._ID + " = " + id;
        if (!TextUtils.isEmpty(selection)) {
          where += " AND " + selection;
        }
        delCount = db.delete(
                ReminderContract.Notes.TABLE_NAME,
                where,
                selectionArgs);
        break;
      case ALERT:
        delCount = db.delete(
                ReminderContract.Alerts.TABLE_NAME,
                selection,
                selectionArgs);
        break;
      case ALERT_ID:
        id = uri.getLastPathSegment();
        where = ReminderContract.Alerts._ID + " = " + id;
        if (!TextUtils.isEmpty(selection)) {
          where += " AND " + selection;
        }
        delCount = db.delete(
                ReminderContract.Alerts.TABLE_NAME,
                where,
                selectionArgs);
        break;
      default:
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }
    // notify all listeners of changes:
    contentResolver.notifyChange(uri, null);
    return delCount;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values,
                    @Nullable String selection, @Nullable String[] selectionArgs) {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int updateCount;
    String id, where;
    switch (URI_MATCHER.match(uri)) {
      case NOTE:
        updateCount = db.update(
                ReminderContract.Notes.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        break;
      case NOTE_ID:
        id = uri.getLastPathSegment();
        where = ReminderContract.Notes._ID + " = " + id;
        if (!TextUtils.isEmpty(selection)) {
          where += " AND " + selection;
        }
        updateCount = db.update(
                ReminderContract.Notes.TABLE_NAME,
                values,
                where,
                selectionArgs);
        break;
      case ALERT:
        updateCount = db.update(
                ReminderContract.Notes.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        break;
      case ALERT_ID:
        id = uri.getLastPathSegment();
        where = ReminderContract.Alerts._ID + " = " + id;
        if (!TextUtils.isEmpty(selection)) {
          where += " AND " + selection;
        }
        updateCount = db.update(
                ReminderContract.Alerts.TABLE_NAME,
                values,
                where,
                selectionArgs);
        break;
      default:
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }
    // notify all listeners of changes:
    if (updateCount > 0) {
      Context context = getContext();
      if (context == null) {
        return 0;
      }

      ContentResolver contentResolver = context.getContentResolver();
      if (contentResolver == null) {
        return 0;
      }
      contentResolver.notifyChange(uri, null);
    }
    return updateCount;
  }
}
