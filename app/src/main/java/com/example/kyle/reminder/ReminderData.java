package com.example.kyle.reminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by kyle on 29/08/16.
 */
public class ReminderData {
    private SharedPreferences data;
    private SharedPreferences.Editor editor;

    public ReminderData(Context context){
        data = PreferenceManager.getDefaultSharedPreferences(context);
        editor = data.edit();

    }
    public SharedPreferences getData(){
        return data;
    }
    public SharedPreferences.Editor getEditor(){
        return editor;
    }
}
