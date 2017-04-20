package com.example.kyle.reminder;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by kyle on 22/09/16.
 */

public class reminderAdapter extends RecyclerView.Adapter<reminderAdapter.ViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private reminderDatabase database;
    private RecyclerView mRecyclerView;
    //public View.OnClickListener mListener = new reminderClickListener();
    //public View.OnLongClickListener mLongListener = new reminderLongClickListener();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm, MMM d ''yy");

    private MultiSelector multiSelector = new MultiSelector();

    private ModalMultiSelectorCallback mActionModeCallback
            = new ModalMultiSelectorCallback(multiSelector) {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            ((Activity)mContext).getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_del_item) {
                // delete the items
                deleteDialog(actionMode).show();
                return true;
            }
            return false;
        }
        @Override
        public void onDestroyActionMode(ActionMode actionMode){
            multiSelector.clearSelections();
            multiSelector.setSelectable(false);
        }
    };

    public class ViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView title;
        public TextView content;
        public TextView time;
        public ImageView icon;


        public ViewHolder(View view) {

            super(view, multiSelector);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            title = (TextView) view.findViewById(R.id.title);
            content = (TextView) view.findViewById(R.id.reminder);
            time = (TextView) view.findViewById(R.id.timeLabel);
            icon = (ImageView) view.findViewById(R.id.icon);

        }
        @Override
        public void onClick(View view){
            // if not in selection mode, go to detail screen
            if (!multiSelector.tapSelection(ViewHolder.this)) {
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
        @Override
        public boolean onLongClick(View view){
            if (!multiSelector.isSelectable()) {
                ((AppCompatActivity) mContext).startSupportActionMode(mActionModeCallback);
                Log.d("LONG CLICK", "LONG CLICKED");
                multiSelector.setSelectable(true);
                multiSelector.setSelected(ViewHolder.this, true);
                return true;
            }
            return false;
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
         //   reminderView.setOnClickListener(mListener);
          //  reminderView.setOnLongClickListener(mLongListener);

            // Return a new holder
            ViewHolder viewHolder = new ViewHolder(reminderView);
            return viewHolder;
        }
    }

    // Populating the items in the holder
    @Override
    public void onBindViewHolder(reminderAdapter.ViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);
        String type = mCursor.getString(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_TYPE));
        if (type.equalsIgnoreCase("alert")) {
            viewHolder.time.setText(timeFormat.format(mCursor.getLong(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_TIME))));
            viewHolder.icon.setImageResource(R.drawable.ic_bell_ring_grey600_18dp);
            viewHolder.time.setVisibility(View.VISIBLE);
            viewHolder.icon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.time.setVisibility(View.GONE);
            viewHolder.icon.setVisibility(View.GONE);
        }
        viewHolder.title.setText(mCursor.getString(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_TITLE)));
        viewHolder.content.setText(mCursor.getString(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_CONTENT)));
        viewHolder.setSelectionModeBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.selectors, null));
    }

    private AlertDialog deleteDialog(final ActionMode actionMode) {

        return new AlertDialog.Builder(mContext)
                .setTitle("Confirm")
                .setMessage("Do you want to delete?")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int i) {
                        List<Integer> positions = multiSelector.getSelectedPositions();
                        Log.d("POSITIONS", positions.toString());
                        for(int j = 0; j < positions.size(); j++) {
                            int position = positions.get(j);
                            mCursor.moveToPosition(position);
                            int id = mCursor.getInt(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_ID));
                            int deleteId = id;
                            Cursor cursor = database.getItem(id);
                            cursor.moveToFirst();

                            // remove the soon to be deleted view from RecyclerView and notify
                            mRecyclerView.removeViewAt(position);

                            // if the selectors item for deletion is an alert, cancel the alarm
                            if ((cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TYPE)).equals("alert"))) {
                                Intent delete = new Intent(mContext, AlarmService.class);
                                delete.putExtra("id", deleteId);
                                delete.putExtra("deletedFromMain", true);
                                delete.setAction(AlarmService.DELETE);
                                mContext.startService(delete);

                                // otherwise just delete note and notify adapter
                            } else {
                                database.deleteItem(deleteId);
                            }
                        }
                        // sends refresh signal to Main UI
                        Intent refresh = new Intent("REFRESH");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(refresh);
                        dialog.dismiss();
                        actionMode.finish();
                        multiSelector.clearSelections();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();

                    }
                })
                .create();

    }

    public int getItemCount() {
        mCursor = database.getAllItems();
        return mCursor.getCount();
    }


}


