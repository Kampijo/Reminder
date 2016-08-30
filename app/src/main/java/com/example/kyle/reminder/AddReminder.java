package com.example.kyle.reminder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import java.util.ArrayList;

public class AddReminder extends AppCompatActivity {
    private EditText editText;
    private Intent intent;
    private ArrayList<String> reminders;
    private ReminderData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
        intent = getIntent();
        editText = (EditText) findViewById(R.id.editText);
        data = new ReminderData(getApplicationContext());

    }

    @Override
    public void onBackPressed(){

        String reminder = editText.getText().toString();
        editText.getText().clear();
        reminders = intent.getStringArrayListExtra("reminders");
        data.getEditor().putString(reminder, reminder);
        data.getEditor().commit();
        reminders.add(reminder);
        startActivity(new Intent(this, MainActivity.class));
        finish();

    }
}
