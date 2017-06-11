package com.example.kyle.reminder;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import java.text.SimpleDateFormat;

/**
 * Created by kyle on 22/09/16.
 */

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

  private Context mContext;
  private Cursor mCursor;
  private ReminderDataHelper mDatabase;
  private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm, MMM d ''yy");

  private MultiSelector mMultiSelector;
  private ModalMultiSelectorCallback mActionModeCallback;
  private MainFragment.EditListener mEditListener;

  public class ViewHolder extends SwappingHolder implements View.OnClickListener,
          View.OnLongClickListener {
    public TextView title;
    public TextView content;
    public TextView time;
    public ImageView icon;

    public ViewHolder(View view) {

      super(view, mMultiSelector);
      view.setOnClickListener(this);
      view.setOnLongClickListener(this);
      title = (TextView) view.findViewById(R.id.title);
      content = (TextView) view.findViewById(R.id.reminder);
      time = (TextView) view.findViewById(R.id.timeLabel);
      icon = (ImageView) view.findViewById(R.id.icon);

    }

    @Override
    public void onClick(View view) {
      // if not in selection mode, go to detail screen
      if (!mMultiSelector.tapSelection(ViewHolder.this)) {
        mEditListener.startEditActivity(view);
      }
    }

    @Override
    public boolean onLongClick(View view) {
      if (!mMultiSelector.isSelectable()) {
        ((AppCompatActivity) mContext).startSupportActionMode(mActionModeCallback);
        mMultiSelector.setSelectable(true);
        mMultiSelector.setSelected(ViewHolder.this, true);
        return true;
      }
      return false;
    }
  }

  public ReminderAdapter(Context context, Cursor cursor) {
    mContext = context;
    mCursor = cursor;
    mDatabase = new ReminderDataHelper(mContext);
  }

  // inflating layout from XML and returning the holder
  @Override
  public ReminderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);

    if (mDatabase.isEmpty("All")) {
      View emptyView = parent.findViewById(R.id.empty);
      return new ViewHolder(emptyView);
    } else {

      // Inflate the custom layout
      View reminderView = inflater.inflate(R.layout.list_item_layout, parent, false);

      // Return a new holder
      ViewHolder viewHolder = new ViewHolder(reminderView);
      return viewHolder;
    }
  }

  // Populating the items in the holder
  @Override
  public void onBindViewHolder(ReminderAdapter.ViewHolder viewHolder, int position) {
    mCursor.moveToPosition(position);
    String type = mCursor.getString(mCursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TYPE));
    if (type.equalsIgnoreCase("alert")) {
      viewHolder.time.setText(timeFormat.format(mCursor.getLong(mCursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TIME))));
      viewHolder.icon.setImageResource(R.drawable.ic_bell_ring_grey_18dp);
      viewHolder.time.setVisibility(View.VISIBLE);
      viewHolder.icon.setVisibility(View.VISIBLE);
    } else {
      viewHolder.time.setVisibility(View.GONE);
      viewHolder.icon.setVisibility(View.GONE);
    }
    viewHolder.title.setText(mCursor.getString(mCursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_TITLE)));
    viewHolder.content.setText(mCursor.getString(mCursor.getColumnIndex(ReminderDataHelper.DB_COLUMN_CONTENT)));
    viewHolder.setSelectionModeBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.selectors, null));
  }


  public int getItemCount() {
    return mCursor.getCount();
  }

  public void setMultiSelector(MultiSelector l) {
    mMultiSelector = l;
  }

  public void setModalMultiSelectorCallback(ModalMultiSelectorCallback l) {
    mActionModeCallback = l;
  }

  public void setEditListener(MainFragment.EditListener l) {
    mEditListener = l;
  }

}


