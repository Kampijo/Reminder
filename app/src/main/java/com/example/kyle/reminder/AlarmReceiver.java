package com.example.kyle.reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;

/**
 * Created by kyle on 07/09/16.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    private final int HOURLY = 1, DAILY = 2, WEEKLY = 3, MONTHLY = 4, YEARLY = 5;

    @Override
    public void onReceive(Context context, Intent intent) {

        int id = intent.getIntExtra("id", 0);
        String title = intent.getStringExtra("title");
        String msg = intent.getStringExtra("msg");

        reminderDatabase database = new reminderDatabase(context);
        Cursor cursor = database.getItem(id);
        cursor.moveToFirst();

        int frequency = cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_FREQUENCY));
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TIME)));

        if (frequency > 0) {
            if (frequency == HOURLY) {
                time.add(Calendar.HOUR, 1);

            } else if (frequency == DAILY) {
                time.add(Calendar.DATE, 1);

            } else if (frequency == WEEKLY) {
                time.add(Calendar.DATE, 7);
            } else if (frequency == MONTHLY) {
                time.add(Calendar.MONTH, 1);

            } else if (frequency == YEARLY) {
                time.add(Calendar.YEAR, 1);

            }
            database.updateTime(id, time.getTimeInMillis());
            Intent setAlarm = new Intent(context, AlarmService.class);
            setAlarm.putExtra("id", id);
            setAlarm.setAction(AlarmService.CREATE);
            context.startService(setAlarm);
        }

        Intent result = new Intent(context, createOrEditAlert.class);
        result.putExtra("ID", id);
        PendingIntent clicked = PendingIntent.getActivity(context, id, result,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification n = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_calendar_check_black_48dp)
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(clicked)
                .setAutoCancel(true)
                .build();


        n.defaults |= Notification.DEFAULT_VIBRATE;
        n.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        n.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, n);


    }

}
