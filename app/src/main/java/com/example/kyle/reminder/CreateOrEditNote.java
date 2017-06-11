package com.example.kyle.reminder;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class CreateOrEditNote extends AppCompatActivity {
  private EditText mTitle, mContent;
  private int mID = 0;
  private ContentResolver mContentResolver;

  private static final String ID_KEY = "id";

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_or_edit_note);
    mContentResolver = getContentResolver();

    Intent intent = getIntent();
    mID = intent.getIntExtra(ID_KEY, 0);

    mContent = (EditText) findViewById(R.id.noteContent);
    mTitle = (EditText) findViewById(R.id.noteTitle);

    Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
    this.setSupportActionBar(toolbar);

    if (mID > 0) {
      Uri uri = ContentUris.withAppendedId(ReminderContract.Notes.CONTENT_URI,
              mID);
      Cursor cursor = mContentResolver.query(uri, null, null, null, null);
      cursor.moveToFirst();
      String titleString = cursor.getString(cursor.getColumnIndex(ReminderContract.Notes.TITLE));
      String contentString = cursor.getString(cursor.getColumnIndex(ReminderContract.Notes.CONTENT));
      mContent.setText(contentString);
      mTitle.setText(titleString);
      getSupportActionBar().setTitle("Edit Note");
    } else {
      getSupportActionBar().setTitle("Create Note");
    }

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public void onBackPressed() {
    saveNote();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_create_or_edit_note, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.id.action_del_note:
        deleteDialog(mID).show();
        break;

      case android.R.id.home:
        saveNote();
        break;

      default:
        break;
    }

    return true;
  }

  private AlertDialog deleteDialog(int id) {

    final int deleteId = id;

    return new AlertDialog.Builder(this)
            .setTitle("Confirm")
            .setMessage("Do you want to delete?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface dialog, int i) {
                if (deleteId > 0) {
                  Uri uri = ContentUris.withAppendedId(ReminderContract.Notes.CONTENT_URI,
                          deleteId);
                  mContentResolver.delete(uri, null, null);
                }
                terminateActivity();
              }

            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();

              }
            })
            .create();

  }

  private AlertDialog saveDialog(int id, String title, final String content) {
    final int saveId = id;
    final String saveMessage = content;
    final String saveTitle = title;

    return new AlertDialog.Builder(this)

            .setTitle("Confirm")
            .setMessage("Do you want to save?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface dialog, int whichButton) {
                //if note exists, update. Otherwise insert new note.
                if (saveId > 0) {
                  ContentValues values = new ContentValues();
                  values.put(ReminderContract.Notes.TITLE, saveTitle);
                  values.put(ReminderContract.Notes.CONTENT, saveMessage);
                  Uri uri = ContentUris.withAppendedId(ReminderContract.Notes.CONTENT_URI,
                          saveId);
                  mContentResolver.update(uri, values, null, null);
                } else {
                  ContentValues values = new ContentValues();
                  values.put(ReminderContract.Notes.TYPE, ReminderContract.PATH_NOTE);
                  values.put(ReminderContract.Notes.TITLE, saveTitle);
                  values.put(ReminderContract.Notes.CONTENT, saveMessage);
                  mContentResolver.insert(ReminderContract.Notes.CONTENT_URI, values);
                }
                terminateActivity();
                dialog.dismiss();
              }

            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                terminateActivity();
                dialog.dismiss();

              }
            })
            .create();
  }

  private void terminateActivity() {
    NavUtils.navigateUpFromSameTask(this);
  }

  private void saveNote() {
    String contentString = mContent.getText().toString();
    String titleString = mTitle.getText().toString();
    saveDialog(mID, titleString, contentString).show();
  }

}
