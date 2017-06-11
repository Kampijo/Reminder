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
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class CreateOrEditAlert extends AppCompatActivity {

    private SimpleAdapter adapter;
    private ReminderDataHelper database;
    private EditText content, title;
    private String time, date;
    private int id, repeatMode;
    private Map<String, String> alarmTime, alarmDate, alarmRepeat;
    private DateFormat timeFormat, dateFormat;
    private Calendar alertTime;
    private String[] repeatModes;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_alert);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter("DELETED");
        broadcastManager.registerReceiver(deleteReceiver, filter);

        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
        alarmTime = new HashMap<String, String>();
        alarmDate = new HashMap<String, String>();
        alarmRepeat = new HashMap<String, String>();

        database = new ReminderDataHelper(this);
        content = (EditText) findViewById(R.id.alertContent);
        title = (EditText) findViewById(R.id.alertTitle);

        repeatModes = new String[]{"None", "Hourly", "Daily", "Weekly", "Monthly", "Yearly"};
        repeatMode = 0;

        Intent intent = getIntent();
        id = intent.getIntExtra("ID", 0);
        alertTime = Calendar.getInstance();

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        this.setSupportActionBar(toolbar);

        timeFormat = new SimpleDateFormat("hh:mm aa");
        dateFormat = new SimpleDateFormat("dd/MM/yy");

        // If item exists, then set time and date list items to the time and date stored in alert
        if (id > 0) {
            Cursor cursor = database.getItem(id);
            cursor.moveToFirst();
            String contentString = cursor.getString(cursor.getColumnIndex
                    (ReminderDataHelper.DB_COLUMN_CONTENT));
            String titleString = cursor.getString(cursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TITLE));
            content.setText(contentString);
            title.setText(titleString);

            long timeInMilliseconds = cursor.getLong(cursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TIME));
            repeatMode = cursor.getInt(cursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_FREQUENCY));
            alertTime.setTimeInMillis(timeInMilliseconds);
            DateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

            time = timeFormat.format(alertTime.getTime());
            date = dateFormat.format(alertTime.getTime());
            getSupportActionBar().setTitle("Edit Alert");
            cursor.close();

            // Otherwise, set time and date list items to system time
        } else {
            Calendar current = Calendar.getInstance();
            time = timeFormat.format(current.getTime());
            date = dateFormat.format(current.getTime());
            alertTime.setTimeInMillis(current.getTimeInMillis());
            getSupportActionBar().setTitle("Create Alert");

        }

        alarmTime.put("title", "Time");
        alarmTime.put("subtext", time);
        alarmDate.put("title", "Date");
        alarmDate.put("subtext", date);
        alarmRepeat.put("title", "Repeat");
        alarmRepeat.put("subtext", repeatModes[repeatMode]);


        mapList.add(alarmTime);
        mapList.add(alarmDate);
        mapList.add(alarmRepeat);
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
        return new TimePickerDialog(CreateOrEditAlert.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        alertTime.set(Calendar.HOUR_OF_DAY, hour);
                        alertTime.set(Calendar.MINUTE, minute);
                        alertTime.set(Calendar.SECOND, 0);
                        time = timeFormat.format(alertTime.getTime());
                        alarmTime.put("subtext", time);
                        adapter.notifyDataSetChanged();
                    }
                }, alertTime.get(Calendar.HOUR_OF_DAY), alertTime.get(Calendar.MINUTE), false);
    }

    // date picker
    private DatePickerDialog datePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(CreateOrEditAlert.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        alertTime.set(Calendar.YEAR, year);
                        alertTime.set(Calendar.MONTH, month);
                        alertTime.set(Calendar.DAY_OF_MONTH, day);
                        date = dateFormat.format(alertTime.getTime());
                        alarmDate.put("subtext", date);
                        adapter.notifyDataSetChanged();
                    }
                }, alertTime.get(Calendar.YEAR), alertTime.get(Calendar.MONTH), alertTime.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
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
                            Intent cancelPrevious = new Intent(CreateOrEditAlert.this,
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
                            Intent delete = new Intent(CreateOrEditAlert.this, AlarmService.class);
                            delete.putExtra("id", deleteId);
                            delete.setAction(AlarmService.DELETE);
                            startService(delete);
                        } else {
                            terminateActivity();
                        }
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
                        // set label to selected repeat mode
                        alarmRepeat.put("subtext", repeatModes[repeatMode]);
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
        NavUtils.navigateUpFromSameTask(this);
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
