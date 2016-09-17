package com.example.kyle.reminder;

import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by kyle on 16/09/16.
 */
public class reminderCursorAdapter extends SimpleCursorAdapter {

    private int layout;
    private final LayoutInflater inflater;

    public reminderCursorAdapter(Context context, int layout, Cursor cursor,
                                 String[] from, int[] to){
        super(context, layout, cursor, from, to, 0);
        this.layout = layout;
        this.inflater = LayoutInflater.from(context);

    }

    public void bindView(View view, Context context, Cursor cursor){

        super.bindView(view, context, cursor);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView content = (TextView) view.findViewById(R.id.reminder);
        ImageView imageView = (ImageView) view.findViewById(R.id.icon);

        title.setText(cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TITLE)));
        content.setText(cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_CONTENT)));

        String type = cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TYPE));
        if(type.equalsIgnoreCase("alert")){
            imageView.setImageResource(R.drawable.ic_bell_grey600_48dp);
        } else {
            imageView.setImageResource(R.drawable.ic_note_grey600_48dp);
        }

    }

    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return inflater.inflate(layout, parent, false);
    }
}
