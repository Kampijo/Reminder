package com.example.kyle.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

/**
 * Created by kyle on 07/09/16.
 *
 * Gets alarms from reminderDatabase and sets alarms on reboot
 */

public class AlarmSetter extends BroadcastReceiver {

    @Override
    // once phone reboot complete, set back all alarms
    public void onReceive(Context context, Intent intent) {
        reminderDatabase database = new reminderDatabase(context);
        Cursor cursor = database.getAllItems();
        try {
            while (cursor.moveToNext()) {
                String type =
                        cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TYPE));

                if (type.equals("alert")) {
                    Intent service = new Intent(context, AlarmService.class);
                    service.setAction(AlarmService.CREATE);
                    service.putExtra("id",
                            cursor.getInt(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_ID)));
                    context.startService(service);
                }
            }
        } finally {
            cursor.close();
        }

    }

}

