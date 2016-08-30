package com.example.kyle.reminder;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> reminders = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private ReminderData data;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = new ReminderData(this);
        data.getEditor().commit();

        ListView listView = (ListView) findViewById(R.id.listView);
        loadReminders();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, reminders);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(MainActivity.this, SeeReminder.class);
                intent.putExtra("item", item);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);
                AlertDialog confirm = AskOption(i, item);
                confirm.show();

                return true;
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_add selected
            case R.id.action_add:
                Intent intent = new Intent(this, AddReminder.class);
                intent.putStringArrayListExtra("reminders", reminders);
                startActivity(intent);
                break;
            // action with ID action_settings was selected
            case R.id.action_settings:

                break;
            default:
                break;
        }

        return true;
    }



    private void update(int i, String s){
        reminders.remove(i);
        data.getEditor().remove(s);
        data.getEditor().commit();
        adapter.notifyDataSetChanged();
    }

    private void loadReminders(){
        Map<String, ?> savedReminders = data.getData().getAll();
        for(Map.Entry<String, ?> entry: savedReminders.entrySet()){
            reminders.add(entry.getKey());
        }
    }


    private AlertDialog AskOption(int i, String s) {
        final int deleteIndex = i;
        final String deleteItem = s;
        AlertDialog deleteConfirm = new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Confirm")
                .setMessage("Do you want to delete?")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        update(deleteIndex, deleteItem);
                        dialog.dismiss();
                    }

                })


                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();
        return deleteConfirm;

    }
}

