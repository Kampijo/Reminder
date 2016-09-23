package com.example.kyle.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;


public class MainActivity extends AppCompatActivity {

    private reminderDatabase database;
    private TextView empty;
    // private SimpleCursorAdapter cursorAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private reminderAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // sets listView in mainActivity to contents of database
        database = new reminderDatabase(this);
        final Cursor cursor = database.getAllItems();

        //broadcastManager to wait for AlarmService to finish
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter("REFRESH");
        broadcastManager.registerReceiver(deleteReceiver, filter);


        mRecyclerView = (RecyclerView) findViewById(R.id.reminderList);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter = new reminderAdapter(this, cursor, mRecyclerView);
        mRecyclerView.setAdapter(adapter);

        empty = (TextView) findViewById(R.id.empty);
        emptyCheck();

        adapter.notifyDataSetChanged();


        FloatingActionMenu floatingActionMenu = (FloatingActionMenu) findViewById(R.id.floatingMenu);
        floatingActionMenu.setClosedOnTouchOutside(true);
        FloatingActionButton addAlert = (FloatingActionButton) findViewById(R.id.add_alert);
        FloatingActionButton addNote = (FloatingActionButton) findViewById(R.id.add_note);

        addAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, createOrEditAlert.class));
                finish();
            }
        });
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, createOrEditNote.class));
                finish();
            }
        });
    }


    // checks if RecyclerView is empty and sets emptyView
    private void emptyCheck(){
        if(database.isEmpty()){
            empty.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    //receives signal of deletion and then refreshes UI
    private BroadcastReceiver deleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("REFRESH")) {
                emptyCheck();
                int position = intent.getIntExtra("position", 0);
                // notify item removal
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, 1);
            }
        }
    };
}

