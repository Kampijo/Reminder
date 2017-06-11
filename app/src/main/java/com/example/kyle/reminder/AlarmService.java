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

  public static final String ID_KEY = "id";
  public static final String TITLE_KEY = "title";
  public static final String MESSAGE_KEY = "msg";

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
    int id = intent.getIntExtra(CreateOrEditAlert.ID_KEY, 0);
    boolean deletedFromMain = intent.getBooleanExtra("deletedFromMain", false);

    if (matcher.matchAction(action)) {
      execute(action, id, deletedFromMain);
    }
  }

  private void execute(String action, int id, boolean deletedFromMain) {

    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    Uri uri = ContentUris.withAppendedId(ReminderContract.Alerts.CONTENT_URI,
            id);
    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
    cursor.moveToFirst();

    Intent intent = new Intent(this, AlarmReceiver.class);
    intent.putExtra(ID_KEY, cursor.getInt(cursor.getColumnIndex(ReminderContract.Alerts._ID)));
    intent.putExtra(TITLE_KEY, cursor.getString(
            cursor.getColumnIndex(ReminderContract.Alerts.TITLE)));
    intent.putExtra(MESSAGE_KEY, cursor.getString(
            cursor.getColumnIndex(ReminderContract.Alerts.CONTENT)));

    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);

    long timeInMilliseconds = cursor.getLong(cursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TIME));

    if (CREATE.equals(action)) {
      alarm.setExact(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);

    } else if (DELETE.equals(action)) {
      alarm.cancel(pendingIntent);
      NotificationManager notificationManager =
              (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      getContentResolver().delete(uri, null, null);
      notificationManager.cancel(id);

      // if deleted from the main screen, then broadcast refresh signal
      if (deletedFromMain) {
        Intent refresh = new Intent("REFRESH");
        LocalBroadcastManager.getInstance(this).sendBroadcast(refresh);
        // otherwise, send deleted signal to exit alert detail screen
      } else {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("DELETED"));
      }

    } else if (CANCEL.equals(action)) {
      alarm.cancel(pendingIntent);
    }
    cursor.close();
  }

}
