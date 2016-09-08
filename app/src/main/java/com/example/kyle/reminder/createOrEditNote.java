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
import android.widget.TextView;


public class createOrEditNote extends AppCompatActivity {
    private EditText editText;
    private reminderDatabase database;
    private int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_note);
        database = new reminderDatabase(this);
        Intent intent = getIntent();
        id = intent.getIntExtra("noteID", 0);
        editText = (EditText) findViewById(R.id.editText);
        if (id > 0) {
            Cursor cursor = database.getItem(id);
            cursor.moveToFirst();
            String note = cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_CONTENT));
            editText.setText(note, TextView.BufferType.EDITABLE);
        }


    }

    @Override
    public void onBackPressed() {

        String reminder = editText.getText().toString();
        AlertDialog save = saveDialog(id, reminder);
        save.show();

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
                //if item exists, delete and go back to MainActivity. Otherwise, just go back.
                if (id > 0) {
                    database.deleteItem(id);
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            case R.id.action_settings:
                break;
            default:
                break;
        }

        return true;
    }

    private AlertDialog saveDialog(int id, String reminder) {
        final int saveId = id;
        final String saveMessage = reminder;

        AlertDialog saveConfirm = new AlertDialog.Builder(this)

                .setTitle("Confirm")
                .setMessage("Do you want to save?")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //if note exists, update. Otherwise insert new note.
                        if (saveId > 0) {
                            database.updateNote(saveId, saveMessage);
                        } else {
                            database.insertNote(saveMessage);
                        }
                        startActivity(new Intent(createOrEditNote.this, MainActivity.class));
                        finish();
                        database.close();
                        editText.getText().clear();
                        dialog.dismiss();
                    }

                })


                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(createOrEditNote.this, MainActivity.class));
                        finish();
                        database.close();
                        editText.getText().clear();
                        dialog.dismiss();

                    }
                })
                .create();
        return saveConfirm;

    }

}
