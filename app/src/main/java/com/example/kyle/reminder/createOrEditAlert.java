package com.example.kyle.reminder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
    private EditText editText, editText2;
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
        editText = (EditText) findViewById(R.id.alertContent);
        editText2 = (EditText) findViewById(R.id.alertTitle);

        intent = getIntent();
        id = intent.getIntExtra("alertID", 0);
        alertTime = Calendar.getInstance();

        df = new SimpleDateFormat("hh:mm aa");
        df1 = new SimpleDateFormat("dd/MM/yy");

        // If item exists, then set time and date list items to the time and date stored in alert
        if (id > 0) {
            Cursor cursor = database.getItem(id);
            cursor.moveToFirst();
            String content = cursor.getString(cursor.getColumnIndex
                    (reminderDatabase.DB_COLUMN_CONTENT));
            String title = cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TITLE));
            editText.setText(content);
            editText2.setText(title);

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
            cursor.close();

            // Otherwise, set time and date list items to system time
        } else {
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
        ListView listView = (ListView) findViewById(R.id.alertSettings);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //if first item in list (the time), then show timePickerDialog
                if (i == 0) {
                    TimePickerDialog timePicker = timePicker();
                    timePicker.show();
                    //if second item in list (the date), then show datePicker dialog
                } else if (i == 1) {
                    DatePickerDialog datePicker = datePicker();
                    datePicker.show();
                }

            }
        });

    }

    @Override
    public void onBackPressed() {

        String content = editText.getText().toString();
        String title = editText2.getText().toString();
        if(!(alertTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())) {
            AlertDialog save = saveDialog(id, title, content, hour, minute, day, month, year);
            save.show();
        }
        else{
            AlertDialog error = errorDialog();
            error.show();
        }
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

    private TimePickerDialog timePicker() {
        return new TimePickerDialog(createOrEditAlert.this,
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
    }

    private DatePickerDialog datePicker() {
        return new DatePickerDialog(createOrEditAlert.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        createOrEditAlert.year = year;
                        createOrEditAlert.month = month;
                        createOrEditAlert.day = day;
                        alertTime.set(year, month, day, createOrEditAlert.hour,
                                createOrEditAlert.minute);
                        date = df1.format(alertTime.getTime());
                        item2.put("subtext", date);
                        adapter.notifyDataSetChanged();
                    }
                }, year, month, day);
    }

    private AlertDialog saveDialog(int id, String title, String content, int hour, int minute, int day,
                                   int month, int year) {
        final int saveId = id;
        final int saveHour = hour;
        final int saveMinute = minute;
        final int saveDay = day;
        final int saveMonth = month;
        final int saveYear = year;
        final String saveMessage = content;
        final String saveTitle = title;


        return new AlertDialog.Builder(this)

                .setTitle("Confirm")
                .setMessage("Do you want to save?")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int i) {
                        // if item exists, cancel previous alarm, update alert, then set new alarm
                        if (saveId > 0) {

                            Intent cancelPrevious = new Intent(createOrEditAlert.this,
                                    AlarmService.class);
                            cancelPrevious.putExtra("id", saveId);
                            cancelPrevious.setAction(AlarmService.CANCEL);
                            startService(cancelPrevious);
                            database.updateAlert(saveId, saveTitle, saveMessage, saveHour, saveMinute, saveDay,
                                    saveMonth, saveYear);
                            createAlarm(saveId);

                            // creates alarm for new alert
                        } else {
                            createAlarm((int) database.insertAlert(saveTitle, saveMessage, saveHour,
                                    saveMinute, saveDay, saveMonth, saveYear));
                        }
                        startActivity(new Intent(createOrEditAlert.this, MainActivity.class));
                        finish();
                        dialog.dismiss();
                    }

                })


                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        startActivity(new Intent(createOrEditAlert.this, MainActivity.class));
                        finish();
                        database.close();
                        editText.getText().clear();
                        dialog.dismiss();

                    }
                })
                .create();

    }

    private AlertDialog errorDialog(){

        return new AlertDialog.Builder(this)
                .setMessage("There is no such thing as time travel.")
                .setTitle("Error")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    // creates an alarm
    private void createAlarm(int id) {
        Intent alarm = new Intent(this, AlarmService.class);
        alarm.putExtra("id", id);
        alarm.setAction(AlarmService.CREATE);
        startService(alarm);
        database.close();
        editText.getText().clear();
    }


}
