package com.example.kyle.reminder;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

public class seeNote extends AppCompatActivity {
    private int id = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_note);

        noteDatabase database = new noteDatabase(this);
        Intent intent = getIntent();
        id = intent.getIntExtra("noteID", 0);
        Cursor cursor = database.getNote(id);
        cursor.moveToFirst();
        String note = cursor.getString(cursor.getColumnIndex(noteDatabase.DB_COLUMN_CONTENT));
        System.out.println(note);
        TextView textView = new TextView(this);
        textView.setText(note);
        textView.setTextSize(40);



        ViewGroup layout = (ViewGroup) findViewById(R.id.see_note);
        layout.addView(textView);

    }


}
