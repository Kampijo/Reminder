package com.example.kyle.reminder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
    private EditText content, title;
    private String time, date;
    private int id, repeatMode;
    private Map<String, String> item1, item2, item3;
    private DateFormat df, df1;
    private Calendar alertTime;
    private String[] repeatModes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_alert);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter("DELETED");
        broadcastManager.registerReceiver(deleteReceiver, filter);

        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
        item1 = new HashMap<String, String>();
        item2 = new HashMap<String, String>();
        item3 = new HashMap<String, String>();

        database = new reminderDatabase(this);
        content = (EditText) findViewById(R.id.alertContent);
        title = (EditText) findViewById(R.id.alertTitle);

        repeatModes = new String[]{"None", "Hourly", "Daily", "Monthly", "Yearly"};
        repeatMode = 0;

        Intent intent = getIntent();
        id = intent.getIntExtra("ID", 0);
        alertTime = Calendar.getInstance();


        df = new SimpleDateFormat("hh:mm aa");
        df1 = new SimpleDateFormat("dd/MM/yy");

        // If item exists, then set time and date list items to the time and date stored in alert
        if (id > 0) {
            Cursor cursor = database.getItem(id);
            cursor.moveToFirst();
            String contentString = cursor.getString(cursor.getColumnIndex
                    (reminderDatabase.DB_COLUMN_CONTENT));
            String titleString = cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TITLE));
            content.setText(contentString);
            title.setText(titleString);

            long timeInMilliseconds = cursor.getLong(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TIME));
            repeatMode = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_FREQUENCY));
            alertTime.setTimeInMillis(timeInMilliseconds);
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
            alertTime.setTimeInMillis(current.getTimeInMillis());

        }

        item1.put("title", "Time");
        item1.put("subtext", time);
        item2.put("title", "Date");
        item2.put("subtext", date);
        item3.put("title", "Repeat");
        item3.put("subtext", repeatModes[repeatMode]);


        mapList.add(item1);
        mapList.add(item2);
        mapList.add(item3);
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
                } else {
                    repeatDialog().show();
                }

            }
        });

        getSupportActionBar().setTitle("Create Alert");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onBackPressed() {
        saveAlert();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_or_edit_alert, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_del_alert:
                deleteDialog(id).show();
                break;

            case android.R.id.home:
                saveAlert();
                break;

            default:
                break;

        }

        return true;
    }

    // time picker
    private TimePickerDialog timePicker() {
        return new TimePickerDialog(createOrEditAlert.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        alertTime.set(Calendar.HOUR_OF_DAY, hour);
                        alertTime.set(Calendar.MINUTE, minute);
                        time = df.format(alertTime.getTime());
                        item1.put("subtext", time);
                        adapter.notifyDataSetChanged();
                    }
                }, alertTime.get(Calendar.HOUR_OF_DAY), alertTime.get(Calendar.MINUTE), false);
    }

    // date picker
    private DatePickerDialog datePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(createOrEditAlert.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        alertTime.set(Calendar.YEAR, year);
                        alertTime.set(Calendar.MONTH, month);
                        alertTime.set(Calendar.DAY_OF_MONTH, day);
                        date = df1.format(alertTime.getTime());
                        item2.put("subtext", date);
                        adapter.notifyDataSetChanged();
                    }
                }, alertTime.get(Calendar.YEAR), alertTime.get(Calendar.MONTH), alertTime.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        return datePicker;
    }

    private AlertDialog saveDialog(int id, String title, String content, long time) {
        final int saveId = id;
        final long saveTime = time;
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
                            database.updateAlert(saveId, saveTitle, saveMessage, saveTime, repeatMode);
                            createAlarm(saveId);

                            // creates alarm for new alert
                        } else {
                            createAlarm((int) database.insertAlert(saveTitle, saveMessage, saveTime, repeatMode));
                        }
                        terminateActivity();
                        dialog.dismiss();
                    }

                })


                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        terminateActivity();
                        database.close();
                        dialog.dismiss();

                    }
                })
                .create();
    }

    private AlertDialog deleteDialog(int id) {

        final int deleteId = id;
        return new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Do you want to delete?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {

                        if (deleteId > 0) {
                            // delete the alarm
                            Intent delete = new Intent(createOrEditAlert.this, AlarmService.class);
                            delete.putExtra("id", deleteId);
                            delete.setAction(AlarmService.DELETE);
                            startService(delete);
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

    // set repeat mode from None, Hour, Daily, Monthly, Yearly
    private AlertDialog repeatDialog() {
        final int prevRepeat = repeatMode;
        return new AlertDialog.Builder(this)
                .setTitle("Repeat")
                .setSingleChoiceItems(repeatModes, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        repeatMode = i;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        item3.put("subtext", repeatModes[repeatMode]);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        repeatMode = prevRepeat;
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
    }

    // go back to main activity
    private void terminateActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // saves the alert (Handles case where if time set to before current, just set immediate alert)
    private void saveAlert() {
        String contentString = content.getText().toString();
        String titleString = title.getText().toString();
        if (!(alertTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())) {
            saveDialog(id, titleString, contentString, alertTime.getTimeInMillis()).show();

        } else {
            saveDialog(id, titleString, contentString, Calendar.getInstance().getTimeInMillis()).show();
        }
    }

    // once item is deleted, we can safely exit the activity
    private BroadcastReceiver deleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("DELETED")) {
                terminateActivity();
            }
        }
    };



}
