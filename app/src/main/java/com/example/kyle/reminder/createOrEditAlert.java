package com.example.kyle.reminder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;

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
    private String time, date;
    private static int hour, minute, day, month, year;
    private int id;
    private Map<String, String> item1, item2;
    private DateFormat df, df1;
    private Intent intent;
    private Calendar alertTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_alert);

        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
        item1 = new HashMap<String, String>();
        item2 = new HashMap<String, String>();

        database = new reminderDatabase(this);
        editText = (EditText) findViewById(R.id.editText2);

        intent = getIntent();
        id = intent.getIntExtra("alertID", 0);
        alertTime = Calendar.getInstance();

        df = new SimpleDateFormat("hh:mm aa");
        df1 = new SimpleDateFormat("dd/MM/yy");

        if(id > 0){
            Cursor cursor = database.getItem(id);
            cursor.moveToFirst();
            String note = cursor.getString(cursor.getColumnIndex
                    (reminderDatabase.DB_COLUMN_CONTENT));
            editText.setText(note);

            hour = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_HOUR));
            minute = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_MINUTE));

            day = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_DAY));
            month = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_MONTH));
            year = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_YEAR));

            alertTime.set(year, month, day, hour, minute);
            DateFormat df = new SimpleDateFormat("hh:mm aa");
            DateFormat df1 = new SimpleDateFormat("dd/MM/yy");

            time = df.format(alertTime.getTime());
            date = df1.format(alertTime.getTime());
        }
        else {
            Calendar current = Calendar.getInstance();

            time = df.format(current.getTime());
            date = df1.format(current.getTime());

            hour = current.get(Calendar.HOUR_OF_DAY);
            minute = current.get(Calendar.MINUTE);

            day = current.get(Calendar.DAY_OF_MONTH);
            month = current.get(Calendar.MONTH);
            year = current.get(Calendar.YEAR);

        }

        item1.put("title", "Time");
        item1.put("subtext", time);
        item2.put("title", "Date");
        item2.put("subtext", date);


        mapList.add(item1);
        mapList.add(item2);
        adapter = new SimpleAdapter(this, mapList, android.R.layout.simple_list_item_2,
                new String[]{"title", "subtext"}, new int[]{android.R.id.text1, android.R.id.text2});
        ListView listView = (ListView) findViewById(R.id.listView2);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0){
                    TimePickerDialog timePicker = timePicker();
                    timePicker.show();
                }
                else if(i == 1){
                    DatePickerDialog datePicker = datePicker();
                    datePicker.show();
                }

            }
        });



    }
    public void onBackPressed(){
        Intent intent = new Intent(this, MainActivity.class);
        String note = editText.getText().toString();
        editText.getText().clear();
        if(id > 0){
            database.updateAlert(id, note, hour, minute, day, month, year);
        }
        else {
            database.insertAlert(note, hour, minute, day, month, year);
        }
        database.close();
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
    private TimePickerDialog timePicker(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(createOrEditAlert.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        createOrEditAlert.hour = hour;
                        createOrEditAlert.minute = minute;
                        alertTime.set(createOrEditAlert.year, createOrEditAlert.month,
                                createOrEditAlert.day, hour, minute);
                        time = df.format(alertTime.getTime());
                        item1.put("subtext", time);
                        adapter.notifyDataSetChanged();
                    }
                }, hour, minute, false);
       return timePickerDialog;
    }
    private DatePickerDialog datePicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(createOrEditAlert.this,
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                createOrEditAlert.year = year;
                createOrEditAlert.month = month;
                createOrEditAlert.day = day;
                alertTime.set(year, month, day, createOrEditAlert.hour, createOrEditAlert.minute);
                date = df1.format(alertTime.getTime());
                item2.put("subtext", date);
                adapter.notifyDataSetChanged();
            }
        }, year, month, day);
        return datePickerDialog;
    }



}
