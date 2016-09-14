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

/**
 * Created by kyle on 07/09/16.
 *
 * Service used to create alarms.
 */
public class AlarmService extends IntentService {

    public static final String CREATE = "CREATE";
    public static final String CANCEL = "CANCEL";
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
        reminderDatabase database = new reminderDatabase(this);
        Cursor cursor = database.getItem(id);
        cursor.moveToFirst();

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("id", cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_ID)));
        intent.putExtra("title", cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TITLE)));
        intent.putExtra("msg", cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_CONTENT)));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        long timeInMilliseconds = cursor.getLong(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TIME));

        if (CREATE.equals(action)) {
            alarm.set(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);

        } else if (CANCEL.equals(action)) {
            //cancel alarm
            alarm.cancel(pendingIntent);
            database.deleteItem(id);
            Intent finishIntent = new Intent("FINISHED");
            LocalBroadcastManager.getInstance(this).sendBroadcast(finishIntent);
        }
        database.close();
        cursor.close();
    }

}