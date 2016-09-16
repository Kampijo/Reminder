package com.example.kyle.reminder;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by kyle on 07/09/16.
 * <p/>
 * Service used to create alarms.
 */
public class AlarmService extends IntentService {

    public static final String CREATE = "CREATE";
    public static final String CANCEL = "CANCEL";
    public static final String DELETE = "DELETE";
    private IntentFilter matcher;

    public AlarmService() {
        super("IntentService");
        matcher = new IntentFilter();
        matcher.addAction(CREATE);
        matcher.addAction(CANCEL);
        matcher.addAction(DELETE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        int id = intent.getIntExtra("id", 0);
        boolean deleteFromMain = intent.getBooleanExtra("deleteFromMain", false);

        if (matcher.matchAction(action)) {
            execute(action, id, deleteFromMain);
        }
    }

    private void execute(String action, int id, boolean deleteFromMain) {

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

        } else if (DELETE.equals(action)) {

            alarm.cancel(pendingIntent);
            database.deleteItem(id);
            if (!deleteFromMain) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("DELETED"));
            } else {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("FINISHED"));
            }
        } else if (CANCEL.equals(action)) {
            alarm.cancel(pendingIntent);
        }
        database.close();
        cursor.close();
    }

}