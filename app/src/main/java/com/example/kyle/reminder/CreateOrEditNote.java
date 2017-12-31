package com.example.kyle.reminder;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class CreateOrEditNote extends AppCompatActivity {
  private EditText mTitle, mContent;
  private ContentResolver mContentResolver;
  private ReminderItem mData;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_or_edit_note);
    mContentResolver = getContentResolver();

    Intent intent = getIntent();
    mData = intent.getParcelableExtra("data");

    mContent = findViewById(R.id.note_content);
    mTitle = findViewById(R.id.note_title);

    Toolbar toolbar = findViewById(R.id.tool_bar);
    this.setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();

    if (mData != null) {
      mTitle.setText(mData.getTitle());
      mContent.setText(mData.getContent());
      setActionBarTitle(actionBar, this.getString(R.string.action_bar_edit_note));
    } else {
      mData = new ReminderItem();
      setActionBarTitle(actionBar, this.getString(R.string.action_bar_create_note));
    }
  }

  @Override
  public void onBackPressed() {
    promptSave();
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
        deleteDialog(mData).show();
        break;

      case android.R.id.home:
        promptSave();
        break;

      default:
        break;
    }

    return true;
  }

  private AlertDialog deleteDialog(final ReminderItem item) {
    return new AlertDialog.Builder(this)
        .setTitle("Confirm")
        .setMessage("Do you want to delete?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int i) {
            deleteNote(item);
          }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog, int i) {
dialog.dismiss();

}
})
        .create();
  }

  private AlertDialog saveDialog(final ReminderItem item) {

    return new AlertDialog.Builder(this)
        .setTitle("Confirm")
        .setMessage("Do you want to save?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            saveNote(item);
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

  private void promptSave() {
    mData.setTitle(mTitle.getText().toString());
    mData.setContent(mContent.getText().toString());
    saveDialog(mData).show();
  }

  private void setActionBarTitle(ActionBar actionBar, String title) {
    if (actionBar != null) {
      actionBar.setTitle(title);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void saveNote(ReminderItem item) {
    if (item.getId() > 0) {
      ContentValues values = new ContentValues();
      values.put(ReminderContract.Notes.TITLE, item.getTitle());
      values.put(ReminderContract.Notes.CONTENT, item.getContent());
      Uri uri = ContentUris.withAppendedId(ReminderContract.Notes.CONTENT_URI, item.getId());
      mContentResolver.update(uri, values, null, null);
    } else {
      ContentValues values = new ContentValues();
      values.put(ReminderContract.Notes.TYPE, ReminderType.NOTE.getName());
      values.put(ReminderContract.Notes.TITLE, item.getTitle());
      values.put(ReminderContract.Notes.CONTENT, item.getContent());
      mContentResolver.insert(ReminderContract.Notes.CONTENT_URI, values);
    }
  }

  private void deleteNote(ReminderItem item) {
    if (item != null) {
      Uri uri = ContentUris.withAppendedId(ReminderContract.Notes.CONTENT_URI, item.getId());
      mContentResolver.delete(uri, null, null);
    }
    terminateActivity();
  }
}
