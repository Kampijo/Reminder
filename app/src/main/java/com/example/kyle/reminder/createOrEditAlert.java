package com.example.kyle.reminder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class createOrEditAlert extends AppCompatActivity {

    private SimpleAdapter adapter;
    private reminderDatabase database;
    private EditText editText;
    private int hour, minute, am_pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_alert);

        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
        Map<String, String> item = new HashMap<String, String>();
        Calendar current = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("hh:mm aa");
        String date = df.format(current.getTime());
        hour = current.get(Calendar.HOUR);
        minute = current.get(Calendar.MINUTE);
        am_pm = current.get(Calendar.AM_PM);
        database = new reminderDatabase(this);
        editText = (EditText) findViewById(R.id.editText2);
        item.put("title", "Time");
        item.put("time", date);
        mapList.add(item);
        adapter = new SimpleAdapter(this, mapList, android.R.layout.simple_list_item_2,
                new String[]{"title", "time"}, new int[]{android.R.id.text1, android.R.id.text2});
        ListView listView = (ListView) findViewById(R.id.listView2);
        listView.setAdapter(adapter);

    }
    public void onBackPressed(){
        Intent intent = new Intent(this, MainActivity.class);
        String note = editText.getText().toString();
        editText.getText().clear();
        database.insertAlert(note, hour, minute, am_pm);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_or_edit_alert, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                break;
            default:
                break;
        }

        return true;
    }
}
