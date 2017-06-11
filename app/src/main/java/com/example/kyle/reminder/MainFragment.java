package com.example.kyle.reminder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.List;

/**
 * Created by kyle on 27/04/17.
 */

public class MainFragment extends Fragment {

    private reminderDatabase database;
    private TextView empty;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private reminderAdapter adapter;
    private String type;
    private MultiSelector mMultiSelector;
    private ModalMultiSelectorCallback mActionModeCallBack;
    private Cursor mCursor;

    public interface EditListener {
        void startEditActivity(View v);
    }

    private EditListener mEditListener;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mMultiSelector = new MultiSelector();

        mActionModeCallBack = new ModalMultiSelectorCallback(mMultiSelector) {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                super.onCreateActionMode(actionMode, menu);
                (getActivity()).getMenuInflater().inflate(R.menu.menu_main, menu);
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
            public void onDestroyActionMode(ActionMode actionMode) {
                mMultiSelector.clearSelections();
                mMultiSelector.setSelectable(false);
            }
        };

        mEditListener = new EditListener() {
            @Override
            public void startEditActivity(View v) {
                int position = mRecyclerView.getChildAdapterPosition(v);
                mCursor.moveToPosition(position);
                Intent intent;
                String type = mCursor.getString(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_TYPE));
                if (type.equalsIgnoreCase("alert")) {
                    intent = new Intent(getContext(), createOrEditAlert.class);
                } else {
                    intent = new Intent(getContext(), createOrEditNote.class);
                }
                intent.putExtra("ID", mCursor.getInt(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_ID)));
                getContext().startActivity(intent);

            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_main_fragment, container, false);
    }

    public void onStart() {
        super.onStart();
        database = new reminderDatabase(getContext());
        type = getArguments().getString("Type");
        final Cursor cursor = getCursor(type);
        mCursor = cursor;

        setupFAB();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter filter = new IntentFilter("REFRESH");
        broadcastManager.registerReceiver(deleteReceiver, filter);

        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.reminderList);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter = new reminderAdapter(getContext(), cursor);
        adapter.setMultiSelector(mMultiSelector);
        adapter.setModalMultiSelectorCallback(mActionModeCallBack);
        adapter.setEditListener(mEditListener);
        mRecyclerView.setAdapter(adapter);

        empty = (TextView) getActivity().findViewById(R.id.empty);
        emptyCheck(this.type);

        adapter.notifyDataSetChanged();
        mRecyclerView.invalidate();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    private Cursor getCursor(String type) {
        Cursor cursor;

        if (type.equals("All")) cursor = database.getAllItems();
        else if (type.equals("Alerts")) cursor = database.getAllAlerts();
        else cursor = database.getAllNotes();

        return cursor;
    }

    private void setupFAB() {
        FloatingActionMenu floatingActionMenu = (FloatingActionMenu) getActivity().findViewById(R.id.floatingMenu);
        floatingActionMenu.setClosedOnTouchOutside(true);
        FloatingActionButton addAlert = (FloatingActionButton) getActivity().findViewById(R.id.add_alert);
        FloatingActionButton addNote = (FloatingActionButton) getActivity().findViewById(R.id.add_note);

        addAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), createOrEditAlert.class));
            }
        });
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), createOrEditNote.class));
            }
        });

    }

    // checks if specified item type has any members in database and sets emptyView
    private void emptyCheck(String type) {
        if (database.isEmpty(type)) {
            empty.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private AlertDialog deleteDialog(final ActionMode actionMode) {

        return new AlertDialog.Builder(getContext())
                .setTitle("Confirm")
                .setMessage("Do you want to delete?")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int i) {
                        List<Integer> positions = mMultiSelector.getSelectedPositions();
                        for (int j = 0; j < positions.size(); j++) {
                            int position = positions.get(j);
                            mCursor.moveToPosition(position);
                            int id = mCursor.getInt(mCursor.getColumnIndex(reminderDatabase.DB_COLUMN_ID));
                            int deleteId = id;
                            Cursor cursor = database.getItem(id);
                            cursor.moveToFirst();

                            // if the selectors item for deletion is an alert, cancel the alarm
                            if ((cursor.getString(cursor.getColumnIndex(reminderDatabase.DB_COLUMN_TYPE)).equals("alert"))) {
                                Intent delete = new Intent(getContext(), AlarmService.class);
                                delete.putExtra("id", deleteId);
                                delete.putExtra("deletedFromMain", true);
                                delete.setAction(AlarmService.DELETE);
                                getContext().startService(delete);
                                // otherwise just delete note and notify adapter
                            } else {
                                database.deleteItem(deleteId);
                            }
                        }
                        // sends refresh signal to Main UI
                        Intent refresh = new Intent("REFRESH");
                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(refresh);
                        dialog.dismiss();
                        actionMode.finish();
                        mMultiSelector.clearSelections();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();

                    }
                })
                .create();

    }

    private BroadcastReceiver deleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("REFRESH")) {
                emptyCheck(type);
                adapter.notifyDataSetChanged();
                mRecyclerView.invalidate();
            }
        }
    };

}
