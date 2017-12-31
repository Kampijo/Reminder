package com.example.kyle.reminder;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

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
    super("AlarmService");
    matcher = new IntentFilter();
    matcher.addAction(CREATE);
    matcher.addAction(CANCEL);
    matcher.addAction(DELETE);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    String action = intent.getAction();
    int id = intent.getIntExtra(ReminderParams.ID, 0);
    if (matcher.matchAction(action)) {
      execute(action, id);
    }
  }

  private void execute(String action, int id) {

    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    Uri uri = ContentUris.withAppendedId(ReminderContract.Alerts.CONTENT_URI,
            id);
    Cursor cursor = getContentResolver().query(uri, null, null, null, null);

    if (cursor == null || !cursor.moveToFirst()) {
      return;
    }

    Intent intent = new Intent(this, AlarmReceiver.class);
    intent.putExtra(ReminderParams.ID, cursor.getInt(cursor.getColumnIndex(ReminderParams.ID)));
    intent.putExtra(ReminderParams.TITLE, cursor.getString(cursor.getColumnIndex(
        ReminderParams.TITLE)));
    intent.putExtra(ReminderParams.CONTENT, cursor.getString(cursor.getColumnIndex(
        ReminderParams.CONTENT)));

    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);

    long timeInMilliseconds = cursor.getLong(cursor.getColumnIndex(ReminderParams.TIME));

    if (CREATE.equals(action)) {
      alarm.setExact(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);

    } else if (DELETE.equals(action)) {
      alarm.cancel(pendingIntent);
      NotificationManager notificationManager =
              (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      getContentResolver().delete(uri, null, null);
      notificationManager.cancel(id);

    } else if (CANCEL.equals(action)) {
      alarm.cancel(pendingIntent);
    }
    cursor.close();
  }

}
