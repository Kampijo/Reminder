package com.example.kyle.reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kyle on 05/09/16.
 */
public class noteDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "noteData.db";
    private static final int DATABASE_VERSION = 1;
    public static final String DB_TABLE_NAME = "notes";
    public static final String DB_COLUMN_ID = "_id";
    public static final String DB_COLUMN_CONTENT = "content";

    public noteDatabase(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + DB_TABLE_NAME + "(" +
                DB_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                DB_COLUMN_CONTENT + " TEXT)"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_NAME);
        onCreate(db);
    }
    public boolean insertNote(String note){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DB_COLUMN_CONTENT, note);
        db.insert(DB_TABLE_NAME, null, values);
        return true;
    }
    public Cursor getNote(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + DB_TABLE_NAME + " WHERE " +
                DB_COLUMN_ID + "=?", new String[] { Integer.toString(id) } );
        return res;
    }
    public Cursor getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + DB_TABLE_NAME, null );
        return res;
    }
    public Integer deleteNote(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DB_TABLE_NAME,
                DB_COLUMN_ID + " = ? ",
                new String[] { Integer.toString(id) });
    }

}
