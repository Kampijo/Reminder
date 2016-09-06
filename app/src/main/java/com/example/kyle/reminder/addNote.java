package com.example.kyle.reminder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;



public class addNote extends AppCompatActivity {
    private EditText editText;
    private noteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        database = new noteDatabase(this);
        editText = (EditText) findViewById(R.id.editText);


    }

    @Override
    public void onBackPressed(){

        String reminder = editText.getText().toString();
        editText.getText().clear();
        database.insertNote(reminder);
        startActivity(new Intent(this, MainActivity.class));
        finish();

    }
}
