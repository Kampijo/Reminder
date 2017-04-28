package com.example.kyle.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

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

        setupFAB();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter filter = new IntentFilter("REFRESH");
        broadcastManager.registerReceiver(deleteReceiver, filter);

        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.reminderList);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter = new reminderAdapter(getContext(), cursor, mRecyclerView);
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
