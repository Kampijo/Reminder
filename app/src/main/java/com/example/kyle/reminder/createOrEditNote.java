package com.example.kyle.reminder;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class createOrEditNote extends AppCompatActivity {
    private EditText editText, editText2;
    private reminderDatabase database;
    private int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_note);
        database = new reminderDatabase(this);

        Intent intent = getIntent();
        id = intent.getIntExtra("noteID", 0);

        editText = (EditText) findViewById(R.id.noteContent);
        editText2 = (EditText) findViewById(R.id.noteTitle);

        if (id > 0) {
            Cursor cursor = database.getItem(id);
            cursor.moveToFirst();
            String content = cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_CONTENT));
            String title = cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TITLE));
            editText.setText(content);
            editText2.setText(title);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {

        String content = editText.getText().toString();
        String title = editText2.getText().toString();
        saveDialog(id, title, content).show();

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
                deleteDialog(id).show();
            case R.id.action_settings:
                break;
            case android.R.id.home:
                terminateActivity();
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
                            database.deleteItem(deleteId);
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

    private AlertDialog saveDialog(int id, String title, String content) {
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
                            database.updateNote(saveId, saveTitle, saveMessage);
                        } else {
                            database.insertNote(saveTitle, saveMessage);
                        }
                        terminateActivity();
                        database.close();
                        editText.getText().clear();
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        terminateActivity();
                        database.close();
                        editText.getText().clear();
                        dialog.dismiss();

                    }
                })
                .create();
    }

    private void terminateActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


}
