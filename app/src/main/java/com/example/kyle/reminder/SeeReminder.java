package com.example.kyle.reminder;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class SeeReminder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_reminder);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.message);
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);
     /*   EditText editText = new EditText(this);
        editText.setTextSize(40);
        editText.setId(R.id.text_box);
        editText.setText(message);
        editText.setBackgroundColor(Color.TRANSPARENT);*/



        ViewGroup layout = (ViewGroup) findViewById(R.id.see_reminder);
        layout.addView(textView);

    }


}
