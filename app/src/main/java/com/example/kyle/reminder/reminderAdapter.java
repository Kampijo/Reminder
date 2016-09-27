package com.example.kyle.reminder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;


/**
 * Created by kyle on 22/09/16.
 */

public class reminderAdapter extends RecyclerView.Adapter<reminderAdapter.ViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private reminderDatabase database;
    private RecyclerView mRecyclerView;
    public View.OnClickListener mListener = new reminderClickListener();
    public View.OnLongClickListener mLongListener = new reminderLongClickListener();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("MMM d ''yy");

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView content;
        public TextView time;
        public ImageView icon;

        public ViewHolder(View view) {

            super(view);
            title = (TextView) view.findViewById(R.id.title);
            content = (TextView) view.findViewById(R.id.reminder);
            time = (TextView) view.findViewById(R.id.timeLabel);
            icon = (ImageView) view.findViewById(R.id.icon);

        }
    }

    public reminderAdapter(Context context, Cursor cursor, RecyclerView recyclerView) {
        mContext = context;
        mCursor = cursor;
        mRecyclerView = recyclerView;
        database = new reminderDatabase(mContext);
    }

    private Context getContext() {
        return mContext;
    }

    // inflating layout from XML and returning the holder
    @Override
    public reminderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (database.isEmpty()) {
            View emptyView = parent.findViewById(R.id.empty);
            return new ViewHolder(emptyView);
        } else {

            // Inflate the custom layout
            View reminderView = inflater.inflate(R.layout.list_item_layout, parent, false);

            // set click listener for every view holder
            reminderView.setOnClickListener(mListener);
            reminderView.setOnLongClickListener(mLongListener);

            // Return a new holder
            ViewHolder viewHolder = new ViewHolder(reminderView);
            return viewHolder;
        }
    }

    // Populating the items in the holder
    @Override
    public void onBindViewHolder(reminderAdapter.ViewHolder viewHolder, int id) {
        mCursor.moveToPosition(id);

        viewHolder.title.setText(mCursor.getString(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_TITLE)));
        viewHolder.content.setText(mCursor.getString(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_CONTENT)));

        String type = mCursor.getString(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_TYPE));
        if (type.equalsIgnoreCase("alert")) {
            viewHolder.time.setText(timeFormat.format(mCursor.getLong(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_TIME))));
            viewHolder.icon.setImageResource(R.drawable.ic_bell_grey600_48dp);
        }
    }

    public int getItemCount() {
        mCursor = database.getAllItems();
        return mCursor.getCount();
    }

    private AlertDialog deleteDialog(int id, final int position) {
        final int deleteId = id;
        final Cursor cursor = database.getItem(id);
        cursor.moveToFirst();

        return new AlertDialog.Builder(mContext)
                .setTitle("Confirm")
                .setMessage("Do you want to delete?")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int i) {

                        // remove the soon to be deleted view from RecyclerView and notify
                        mRecyclerView.removeViewAt(position);

                        // if the selected item for deletion is an alert, cancel the alarm
                        if ((cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TYPE)).equals("alert"))) {
                            Intent delete = new Intent(mContext, AlarmService.class);
                            delete.putExtra("id", deleteId);
                            delete.putExtra("deletedFromMain", true);
                            delete.setAction(AlarmService.DELETE);
                            mContext.startService(delete);

                            // otherwise just delete note and notify adapter
                        } else {

                            database.deleteItem(deleteId);

                            // sends refresh signal to Main UI
                            Intent refresh = new Intent("REFRESH");
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(refresh);
                        }
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();

                    }
                })
                .create();

    }

    // short click listener for viewing notes/alerts
    class reminderClickListener implements View.OnClickListener {
        public void onClick(View view) {
            int position = mRecyclerView.getChildAdapterPosition(view);
            mCursor.moveToPosition(position);
            Intent intent;
            String type = mCursor.getString(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_TYPE));
            if (type.equalsIgnoreCase("alert")) {
                intent = new Intent(mContext, createOrEditAlert.class);
            } else {
                intent = new Intent(mContext, createOrEditNote.class);
            }
            intent.putExtra("ID", mCursor.getInt(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_ID)));
            mContext.startActivity(intent);
        }
    }

    class reminderLongClickListener implements View.OnLongClickListener {
        public boolean onLongClick(View view) {
            int position = mRecyclerView.getChildAdapterPosition(view);
            mCursor.moveToPosition(position);
            int id = mCursor.getInt(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_ID));
            deleteDialog(id, position).show();
            return true;
        }
    }

}


