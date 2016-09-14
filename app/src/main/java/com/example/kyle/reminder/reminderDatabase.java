package com.example.kyle.reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kyle on 05/09/16.
 *
 * SQLite database for storing notes/alerts
 */
public class reminderDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "reminderData.db";
    private static final int DATABASE_VERSION = 1;
    public static final String DB_TABLE_NAME = "reminders";
    public static final String DB_COLUMN_ID = "_id";
    public static final String DB_COLUMN_TYPE = "type";
    public static final String DB_COLUMN_TITLE = "title";
    public static final String DB_COLUMN_CONTENT = "content";
    public static final String DB_COLUMN_TIME = "time";

    public reminderDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DB_TABLE_NAME + "(" +
                DB_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                DB_COLUMN_TYPE + " TEXT, " +
                DB_COLUMN_TITLE + " TEXT, " +
                DB_COLUMN_CONTENT + " TEXT, " +
                DB_COLUMN_TIME + " LONG)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertNote(String title, String content) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DB_COLUMN_TYPE, "note");
        values.put(DB_COLUMN_TITLE, title);
        values.put(DB_COLUMN_CONTENT, content);
        db.insert(DB_TABLE_NAME, null, values);
        return true;
    }

    public long insertAlert(String title, String content, long time) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DB_COLUMN_TYPE, "alert");
        values.put(DB_COLUMN_TITLE, title);
        values.put(DB_COLUMN_CONTENT, content);
        values.put(DB_COLUMN_TIME, time);
        return db.insert(DB_TABLE_NAME, null, values);
    }

    public boolean updateNote(Integer id, String title, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DB_COLUMN_CONTENT, note);
        values.put(DB_COLUMN_TITLE, title);
        db.update(DB_TABLE_NAME, values, DB_COLUMN_ID + " = ? ",
                new String[]{Integer.toString(id)});
        return true;
    }

    public boolean updateAlert(Integer id, String title, String note, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DB_COLUMN_CONTENT, note);
        values.put(DB_COLUMN_TITLE, title);
        values.put(DB_COLUMN_TIME, time);
        db.update(DB_TABLE_NAME, values, DB_COLUMN_ID + " = ? ",
                new String[]{Integer.toString(id)});
        return true;
    }

    public Cursor getItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DB_TABLE_NAME + " WHERE " +
                DB_COLUMN_ID + "=?", new String[]{Integer.toString(id)});
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DB_TABLE_NAME, null);
    }

    public Integer deleteItem(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DB_TABLE_NAME,
                DB_COLUMN_ID + " = ? ",
                new String[]{Integer.toString(id)});
    }

}
