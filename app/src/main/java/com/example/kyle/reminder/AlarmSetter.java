package com.example.kyle.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import java.util.Calendar;

/**
 * Created by kyle on 07/09/16.
 * <p/>
 * Gets alarms from ReminderDataHelper and sets alarms on reboot
 */

public class AlarmSetter extends BroadcastReceiver {

    @Override
    // once phone reboot complete, set back all alarms
    public void onReceive(Context context, Intent intent) {
        ReminderDataHelper database = new ReminderDataHelper(context);
        Cursor cursor = database.getAllItems();
        try {
            while (cursor.moveToNext()) {

                String type = cursor.getString(cursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TYPE));
                long time = cursor.getLong(cursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TIME));

                if (type.equals("alert") && time > Calendar.getInstance().getTimeInMillis()) {
                    Intent service = new Intent(context, AlarmService.class);
                    service.setAction(AlarmService.CREATE);
                    service.putExtra("id",
                            cursor.getInt(cursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_ID)));
                    context.startService(service);
                }
            }
        } finally {
            cursor.close();
        }

    }

}

