package com.example.kyle.reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

/**
 * Created by kyle on 07/09/16.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int id = intent.getIntExtra("id", 0);
        String msg = intent.getStringExtra("msg");

        Notification n = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_event_note_black_48dp)
                .setContentTitle("Reminder")
                .setContentText(msg)
                .build();

        n.defaults |= Notification.DEFAULT_VIBRATE;
        n.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        n.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, n);


    }

}
