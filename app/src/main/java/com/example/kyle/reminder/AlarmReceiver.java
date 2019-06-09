package com.example.kyle.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.ContentValues.TAG;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by kyle on 07/09/16.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

  private static final int HOURLY = 1, DAILY = 2, WEEKLY = 3, MONTHLY = 4, YEARLY = 5;

  @Override
  public void onReceive(Context context, Intent intent) {
    int id = intent.getIntExtra(ReminderParams.ID, -1);
    String title = intent.getStringExtra(ReminderParams.TITLE);
    String msg = intent.getStringExtra(ReminderParams.CONTENT);

    if (context == null) {
      return;
    }

    ContentResolver contentResolver = context.getContentResolver();
    if (contentResolver == null) {
      return;
    }

    Uri uri = ContentUris.withAppendedId(ReminderContract.All.CONTENT_URI, id);
    Cursor cursor = contentResolver.query(uri,
            null, null, null, null);

    if (cursor == null || !cursor.moveToFirst()) {
      return;
    }

    int frequency = cursor.getInt(cursor.getColumnIndex(ReminderParams.FREQUENCY));
    Calendar time = Calendar.getInstance();
    time.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(ReminderParams.TIME)));
    cursor.close();

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

      ContentValues values = new ContentValues();
      values.put(ReminderContract.Alerts.TIME, time.getTimeInMillis());
      uri = ContentUris.withAppendedId(ReminderContract.Alerts.CONTENT_URI, id);
      context.getContentResolver().update(uri, values, null, null);

      Intent setAlarm = new Intent(context, AlarmService.class);
      setAlarm.putExtra(ReminderParams.ID, id);
      setAlarm.setAction(AlarmService.CREATE);
      context.startService(setAlarm);
    }

    Intent result = new Intent(context, CreateOrEditAlert.class);
    result.putExtra(ReminderParams.ID, id);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(CreateOrEditAlert.class);
    stackBuilder.addNextIntent(result);
    PendingIntent clicked = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
    bigStyle.setBigContentTitle(title);
    bigStyle.bigText(msg);

    //original
    /*Notification n = new NotificationCompat.Builder(context)
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

    notificationManager.notify(id, n);*/

    //DavidKongKK 2019.6.9:Fixed issue:Alert not working due to notification channel
    String channelId = "my_channel_01";
    String name="Alarm Notification";
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    Notification notification=null;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel mChannel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW);
      notificationManager.createNotificationChannel(mChannel);
      notification = new Notification.Builder(context)
              .setChannelId(channelId)
              .setContentTitle(title)
              .setContentText(msg)
              .setSmallIcon(R.mipmap.ic_launcher).build();
    } else {
      NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
              .setContentTitle(title)
              .setContentText(msg)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setOngoing(true);
      notification = notificationBuilder.build();
    }
    notificationManager.notify(id, notification);


  }

}
