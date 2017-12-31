package com.example.kyle.reminder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import java.util.Locale;
import java.util.Map;

public class CreateOrEditAlert extends AppCompatActivity {
  private SimpleAdapter mAdapter;
  private EditText mContent, mTitle;
  private String mTime, mDate;
  private int mRepeatMode;
  private Map<String, String> mAlarmTime, mAlarmDate, mAlarmRepeat;
  private Calendar mAlertTime;
  private ContentResolver mContentResolver;
  private ReminderItem mData;

  private static final String NONE = "None";
  private static final String HOURLY = "Hourly";
  private static final String DAILY = "Daily";
  private static final String WEEKLY = "Weekly";
  private static final String MONTHLY = "Monthly";
  private static final String YEARLY = "Yearly";

  private static final String[] REPEAT_MODES =
          new String[]{NONE, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY};

  private static final DateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm aa", Locale.CANADA);
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy", Locale.CANADA);

  private static final String ITEM_TITLE = "header";
  private static final String ITEM_CONTENT = "content";

  private static final String TIME_SETTING = "Time";
  private static final String DATE_SETTING = "Date";
  private static final String REPEAT_SETTING = "Repeat";

  private static final int TIME_POSITION = 0;
  private static final int DATE_POSITION = 1;
  private static final int REPEAT_POSITION = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_or_edit_alert);

    mContentResolver = getContentResolver();

    List<Map<String, String>> mapList = new ArrayList<>();
    mAlarmTime = new HashMap<>();
    mAlarmDate = new HashMap<>();
    mAlarmRepeat = new HashMap<>();

    mContent = findViewById(R.id.alert_content);
    mTitle = findViewById(R.id.alert_title);

    mRepeatMode = 0;

    Intent intent = getIntent();
    mData = intent.getParcelableExtra("data");
    mAlertTime = Calendar.getInstance();

    Toolbar toolbar = findViewById(R.id.tool_bar);
    this.setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();

    if (mData != null) {
      mTitle.setText(mData.getTitle());
      mContent.setText(mData.getContent());
      mAlertTime.setTimeInMillis(mData.getTimeInMillis());
      mRepeatMode = mData.getFrequency();

      mTime = TIME_FORMAT.format(mAlertTime.getTime());
      mDate = DATE_FORMAT.format(mAlertTime.getTime());
      setActionBarTitle(actionBar, this.getString(R.string.action_bar_edit_alert));
    } else {
      mData = new ReminderItem();
      Calendar current = Calendar.getInstance();
      mTime = TIME_FORMAT.format(current.getTime());
      mDate = DATE_FORMAT.format(current.getTime());
      mAlertTime.setTimeInMillis(current.getTimeInMillis());

      mData.setTimeInMillis(current.getTimeInMillis());
      mData.setFrequency(mRepeatMode);
      setActionBarTitle(actionBar, this.getString(R.string.action_bar_create_alert));
    }

    mAlarmTime.put(ITEM_TITLE, TIME_SETTING);
    mAlarmTime.put(ITEM_CONTENT, mTime);
    mAlarmDate.put(ITEM_TITLE, DATE_SETTING);
    mAlarmDate.put(ITEM_CONTENT, mDate);
    mAlarmRepeat.put(ITEM_TITLE, REPEAT_SETTING);
    mAlarmRepeat.put(ITEM_CONTENT, REPEAT_MODES[mRepeatMode]);

    mapList.add(mAlarmTime);
    mapList.add(mAlarmDate);
    mapList.add(mAlarmRepeat);

    mAdapter = new SimpleAdapter(this, mapList, android.R.layout.simple_list_item_2,
            new String[]{ITEM_TITLE, ITEM_CONTENT}, new int[]{
        android.R.id.text1, android.R.id.text2});
    ListView listView = findViewById(R.id.alert_settings);
    listView.setAdapter(mAdapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
          case TIME_POSITION:
            TimePickerDialog timePicker = getTimePicker();
            timePicker.show();
            break;
          case DATE_POSITION:
            DatePickerDialog datePicker = getDatePicker();
            datePicker.show();
            break;
          case REPEAT_POSITION:
            createRepeatDialog().show();
            break;
          default:
            Log.e(this.getClass().getName(), "Out of bounds setting position.");
            break;
        }
      }
    });
  }

  @Override
  public void onBackPressed() {
    promptSave();
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
        createDeleteDialog(mData).show();
        break;
      case android.R.id.home:
        promptSave();
        break;
      default:
        break;
    }
    return true;
  }

  private TimePickerDialog getTimePicker() {
    return new TimePickerDialog(CreateOrEditAlert.this, new TimePickerDialog.OnTimeSetListener() {
              @Override
              public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                mAlertTime.set(Calendar.HOUR_OF_DAY, hour);
                mAlertTime.set(Calendar.MINUTE, minute);
                mAlertTime.set(Calendar.SECOND, 0);
                mTime = TIME_FORMAT.format(mAlertTime.getTime());
                mAlarmTime.put(ITEM_CONTENT, mTime);
                mData.setTimeInMillis(mAlertTime.getTimeInMillis());
                mAdapter.notifyDataSetChanged();
              }
            }, mAlertTime.get(Calendar.HOUR_OF_DAY), mAlertTime.get(Calendar.MINUTE), false);
  }

  private DatePickerDialog getDatePicker() {
    DatePickerDialog datePicker = new DatePickerDialog(CreateOrEditAlert.this,
        new DatePickerDialog.OnDateSetListener() {
              @Override
              public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                mAlertTime.set(Calendar.YEAR, year);
                mAlertTime.set(Calendar.MONTH, month);
                mAlertTime.set(Calendar.DAY_OF_MONTH, day);
                mDate = DATE_FORMAT.format(mAlertTime.getTime());
                mAlarmDate.put(ITEM_CONTENT, mDate);
                mData.setTimeInMillis(mAlertTime.getTimeInMillis());
                mAdapter.notifyDataSetChanged();
              }
            }, mAlertTime.get(Calendar.YEAR), mAlertTime.get(Calendar.MONTH),
            mAlertTime.get(Calendar.DAY_OF_MONTH));
    datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    return datePicker;
  }

  private AlertDialog createSaveDialog(final ReminderItem item) {
    return new AlertDialog.Builder(this)
        .setTitle(R.string.confirm)
        .setMessage(R.string.save_prompt)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int i) {
            saveAlert(item);
            terminateActivity();
            dialog.dismiss();
          }
        })
        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int i) {
            terminateActivity();
            dialog.dismiss();
          }
        })
        .create();
  }

  private AlertDialog createDeleteDialog(final ReminderItem item) {
    return new AlertDialog.Builder(this)
            .setTitle(R.string.confirm)
            .setMessage(R.string.delete_prompt)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int i) {
                deleteAlert(item);
              }
            })
            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
              }
            })
            .create();

  }

  private AlertDialog createRepeatDialog() {
    final int prevRepeat = mRepeatMode;
    return new AlertDialog.Builder(this)
            .setTitle(R.string.repeat)
            .setSingleChoiceItems(REPEAT_MODES, 0, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int i) {
                mRepeatMode = i;
              }
            })
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int i) {
                mAlarmRepeat.put(ITEM_CONTENT, REPEAT_MODES[mRepeatMode]);
                mData.setFrequency(mRepeatMode);
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
              }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int i) {
                mRepeatMode = prevRepeat;
                mData.setFrequency(mRepeatMode);
              }
            })
            .create();
  }

  private void createAlarm(int id) {
    Intent alarm = new Intent(this, AlarmService.class);
    alarm.putExtra(ReminderParams.ID, id);
    alarm.setAction(AlarmService.CREATE);
    startService(alarm);
  }

  private void terminateActivity() {
    NavUtils.navigateUpFromSameTask(this);
  }

  private void promptSave() {
    mData.setTitle(mTitle.getText().toString());
    mData.setContent(mContent.getText().toString());
    createSaveDialog(mData).show();
  }

  private void setActionBarTitle(ActionBar actionBar, String title) {
    if (actionBar != null) {
      actionBar.setTitle(title);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void deleteAlert(ReminderItem item) {
    if (item != null) {
      Intent delete = new Intent(CreateOrEditAlert.this, AlarmService.class);
      delete.putExtra(ReminderParams.ID, item.getId());
      delete.setAction(AlarmService.DELETE);
      Uri uri = ContentUris.withAppendedId(ReminderContract.Notes.CONTENT_URI, item.getId());
      mContentResolver.delete(uri, null, null);
      startService(delete);
      terminateActivity();
    } else {
      terminateActivity();
    }
  }

  private void saveAlert(final ReminderItem item) {
    if (item.getId() > 0) {
      Intent cancelPrevious = new Intent(CreateOrEditAlert.this,
          AlarmService.class);
      cancelPrevious.putExtra(ReminderParams.ID, item.getId());
      cancelPrevious.setAction(AlarmService.CANCEL);
      startService(cancelPrevious);
      ContentValues values = new ContentValues();
      values.put(ReminderContract.Alerts.TITLE, item.getTitle());
      values.put(ReminderContract.Alerts.CONTENT, item.getContent());
      values.put(ReminderContract.Alerts.TIME, item.getTimeInMillis());
      values.put(ReminderContract.Alerts.FREQUENCY, item.getFrequency());
      Uri uri = ContentUris.withAppendedId(ReminderContract.Alerts.CONTENT_URI, item.getId());
      mContentResolver.update(uri, values, null, null);
      createAlarm(item.getId());
    } else {
      ContentValues values = new ContentValues();
      values.put(ReminderContract.Alerts.TYPE, ReminderType.ALERT.getName());
      values.put(ReminderContract.Alerts.TITLE, item.getTitle());
      values.put(ReminderContract.Alerts.CONTENT, item.getContent());
      values.put(ReminderContract.Alerts.TIME, item.getTimeInMillis());
      values.put(ReminderContract.Alerts.FREQUENCY, item.getFrequency());
      Uri uri = mContentResolver.insert(ReminderContract.Notes.CONTENT_URI,
          values);
      if (uri != null) {
        createAlarm(Integer.parseInt(uri.getLastPathSegment()));
      }
    }
  }
}
