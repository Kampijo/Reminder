package com.example.kyle.reminder;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        if(id > 0) {
            Cursor cursor = database.getNote(id);
            cursor.moveToFirst();
            String note = cursor.getString(cursor.getColumnIndex(database.DB_COLUMN_CONTENT));
            editText.setText(note, TextView.BufferType.EDITABLE);
        }


    }

    @Override
    public void onBackPressed(){

        String reminder = editText.getText().toString();
        editText.getText().clear();
        if(id > 0){
            database.updateNote(id, reminder);
        }
        else {
            database.insertNote(reminder);
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();

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
                if(id > 0){
                    database.deleteNote(id);
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                else{
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
}
