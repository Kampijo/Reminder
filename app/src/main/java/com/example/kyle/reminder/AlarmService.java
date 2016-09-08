package com.example.kyle.reminder;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by kyle on 07/09/16.
 */
public class AlarmService extends IntentService {

    public static final String CREATE = "CREATE";
    public static final String CANCEL = "CANCEL";
    private reminderDatabase database;

    private IntentFilter matcher;

    public AlarmService() {
        super("IntentService");
        matcher = new IntentFilter();
        matcher.addAction(CREATE);
        matcher.addAction(CANCEL);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        int id = intent.getIntExtra("id", 0);

        if (matcher.matchAction(action)) {
            Log.i("action", action);
            execute(action, id);
        }
    }

    private void execute(String action, int id) {
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        database = new reminderDatabase(this);
        Cursor cursor = database.getItem(id);
        cursor.moveToFirst();

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("id", cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_ID)));
        intent.putExtra("msg", cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_CONTENT)));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int year = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_YEAR));
        int day = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_DAY));
        int month = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_MONTH));
        int hour = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_HOUR));
        int minute = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_MINUTE));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        Log.i("time", calendar.getTime().toString());
        if (CREATE.equals(action)) {
            alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        } else if (CANCEL.equals(action)) {

            //if alarm is cancelled, then delete alert from database and send broadcast of completion

            alarm.cancel(pendingIntent);
            database.deleteItem(id);
            Intent finishIntent = new Intent("FINISHED");
            LocalBroadcastManager.getInstance(this).sendBroadcast(finishIntent);
        }
        database.close();
        cursor.close();
    }

}