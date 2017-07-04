package com.example.kyle.reminder;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
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

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

  private ReminderDataHelper mDataHelper;
  private TextView mEmptyView;
  private RecyclerView mRecyclerView;
  private RecyclerView.LayoutManager mLayoutManager;
  private ReminderAdapter mAdapter;
  private String mType;
  private MultiSelector mMultiSelector;
  private ModalMultiSelectorCallback mActionModeCallBack;
  private Cursor mCursor;

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri uri;
    switch(mType){
      case "All":
        uri = ReminderContract.All.CONTENT_URI;
        break;
      case "Alerts":
        uri = ReminderContract.Alerts.CONTENT_URI;
        break;
      case "Notes":
        uri = ReminderContract.Notes.CONTENT_URI;
        break;
      default:
        return null;
    }
    return new CursorLoader(getActivity(), uri, null, null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mCursor = data;
    updateData(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {

  }

  public interface EditListener {
    void startEditActivity(View v);
  }

  private EditListener mEditListener;


  @Override
  public void onCreate(Bundle savedInstanceState) {
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
        String type = mCursor.getString(mCursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TYPE));
        if (type.equalsIgnoreCase("alert")) {
          intent = new Intent(getContext(), CreateOrEditAlert.class);
        } else {
          intent = new Intent(getContext(), CreateOrEditNote.class);
        }
        intent.putExtra("id", mCursor.getInt(mCursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_ID)));
        getContext().startActivity(intent);

      }
    };
    mType = getArguments().getString("Type");

    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.activity_main_fragment, container, false);
  }

  public void onStart() {
    super.onStart();
    mDataHelper = new ReminderDataHelper(getContext());

    setupFAB();

    mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.reminder_list);
    mEmptyView = (TextView) getActivity().findViewById(R.id.empty);
    mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    mRecyclerView.setLayoutManager(mLayoutManager);
  }

  public void onResume() {
    super.onResume();
    getLoaderManager().restartLoader(0, null, this);
  }

  private void setupFAB() {
    FloatingActionMenu floatingActionMenu = (FloatingActionMenu) getActivity().findViewById(R.id.floating_menu);
    floatingActionMenu.setClosedOnTouchOutside(true);
    FloatingActionButton addAlert = (FloatingActionButton) getActivity().findViewById(R.id.add_alert);
    FloatingActionButton addNote = (FloatingActionButton) getActivity().findViewById(R.id.add_note);

    addAlert.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(view.getContext(), CreateOrEditAlert.class));
      }
    });
    addNote.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(view.getContext(), CreateOrEditNote.class));
      }
    });

  }

  // checks if specified item mType has any members in mDataHelper and sets emptyView
  private void emptyCheck(String type) {
    if (mDataHelper.isEmpty(type)) {
      mEmptyView.setVisibility(View.VISIBLE);
      mRecyclerView.setVisibility(View.GONE);
    } else {
      mEmptyView.setVisibility(View.GONE);
      mRecyclerView.setVisibility(View.VISIBLE);
    }
  }


  private AlertDialog deleteDialog(final ActionMode actionMode) {
    return new AlertDialog.Builder(getContext())
            .setTitle("Confirm")
            .setMessage("Do you want to delete?")

            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface dialog, int i) {
                Uri uri;
                List<Integer> positions = mMultiSelector.getSelectedPositions();
                for (int j = 0; j < positions.size(); j++) {
                  int position = positions.get(j);
                  mCursor.moveToPosition(position);
                  int id = mCursor.getInt(mCursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_ID));
                  int deleteId = id;
                  uri = ContentUris.withAppendedId(ReminderContract.All.CONTENT_URI,
                          deleteId);
                  Cursor cursor = getContext().getContentResolver().query(uri,
                          null, null, null, null);
                  cursor.moveToFirst();

                  // if the selectors item for deletion is an alert, cancel the alarm
                  if ((cursor.getString(cursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TYPE))
                          .equals("alert"))) {
                    Intent delete = new Intent(getContext(), AlarmService.class);
                    delete.putExtra(AlarmService.ID_KEY, deleteId);
                    delete.setAction(AlarmService.DELETE);
                    getContext().startService(delete);
                    // otherwise just delete note and notify mAdapter
                  } else {
                    uri = ContentUris.withAppendedId(ReminderContract.Alerts.CONTENT_URI,
                            deleteId);
                    getContext().getContentResolver().delete(uri, null, null);
                  }
                }
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

  private void updateData(Cursor data) {
    Log.d("development", "updating data");
    mAdapter = new ReminderAdapter(getContext(), data);
    mAdapter.setHasStableIds(true);
    mAdapter.setMultiSelector(mMultiSelector);
    mAdapter.setModalMultiSelectorCallback(mActionModeCallBack);
    mAdapter.setEditListener(mEditListener);
    mRecyclerView.swapAdapter(mAdapter, false);
    emptyCheck(mType);
  }

}
