package com.example.kyle.reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;

/**
 * Created by kyle on 07/09/16.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

  private static final int HOURLY = 1, DAILY = 2, WEEKLY = 3, MONTHLY = 4, YEARLY = 5;

  @Override
  public void onReceive(Context context, Intent intent) {

    int id = intent.getIntExtra(AlarmService.ID_KEY, 0);
    String title = intent.getStringExtra(AlarmService.TITLE_KEY);
    String msg = intent.getStringExtra(AlarmService.MESSAGE_KEY);

    ReminderDataHelper database = new ReminderDataHelper(context);
    Cursor cursor = database.getItem(id);
    cursor.moveToFirst();

    int frequency = cursor.getInt(cursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_FREQUENCY));
    Calendar time = Calendar.getInstance();
    time.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TIME)));

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
      setAlarm.putExtra(AlarmService.ID_KEY, id);
      setAlarm.setAction(AlarmService.CREATE);
      context.startService(setAlarm);
    }

    Intent result = new Intent(context, CreateOrEditAlert.class);
    result.putExtra(AlarmService.ID_KEY, id);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(CreateOrEditAlert.class);
    stackBuilder.addNextIntent(result);
    PendingIntent clicked = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
    bigStyle.setBigContentTitle(title);
    bigStyle.bigText(msg);
    Notification n = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_calendar_check_black_48dp)
            .setContentTitle(title)
            .setContentText(msg)
            .setPriority(Notification.PRIORITY_MAX)
            .setWhen(0)
            .setStyle(bigStyle)
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
