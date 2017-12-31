package com.example.kyle.reminder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.multiselector.MultiSelector;

import java.util.List;

/**
 * Created by kyle on 22/09/16.
 */

public class ReminderAdapter extends RecyclerView.Adapter<ReminderViewHolder> {


  private MultiSelector mMultiSelector;
  private ReminderViewHolder.OnClickListener mOnClickListener;
  private ReminderViewHolder.OnLongClickListener mOnLongClickListener;
  private List<ReminderItem> mReminderItems;

  public ReminderAdapter(List<ReminderItem> items,
                         ReminderViewHolder.OnClickListener clickListener,
                         ReminderViewHolder.OnLongClickListener longClickListener) {
    mReminderItems = items;
    mOnClickListener = clickListener;
    mOnLongClickListener = longClickListener;
  }

  @Override
  public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);

    View reminderView = inflater.inflate(R.layout.list_item_layout, parent, false);
    return new ReminderViewHolder(reminderView, mMultiSelector);
  }

  @Override
  public void onBindViewHolder(ReminderViewHolder viewHolder, int position) {
    ReminderItem item = mReminderItems.get(position);
    if (item == null) {
      return;
    }
    if (item.getType().equals(ReminderType.ALERT)) {
      viewHolder.setTimeLabel(item.getFormattedTime());
      viewHolder.setIcon(R.drawable.ic_bell_ring_grey_18dp);
    } else {
      viewHolder.setTimeLabel(null);
      viewHolder.setIcon(0);
    }
    viewHolder.setTitle(item.getTitle());
    viewHolder.setContent(item.getContent());
    viewHolder.setOnClickListener(mOnClickListener);
    viewHolder.setOnLongClickListener(mOnLongClickListener);
    viewHolder.setSelected(mMultiSelector.isSelected(position, 0));
  }


  public int getItemCount() {
    return mReminderItems.size();
  }

  public void setMultiSelector(MultiSelector l) {
    mMultiSelector = l;
  }

  @Override
  public long getItemId(int position){
    return (long) mReminderItems.get(position).getId();
  }

  public ReminderItem getItemAtPosition(int position) {
    return mReminderItems.get(position);
  }

}


