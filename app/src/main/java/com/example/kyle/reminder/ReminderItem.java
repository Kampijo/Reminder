package com.example.kyle.reminder;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by nygellopez on 2017-12-29.
 */

public class ReminderItem implements Parcelable {

  private int mId;
  private ReminderType mType;
  private String mTitle;
  private String mContent;
  private long mTimeInMillis;
  private int mFrequency;

  private static final SimpleDateFormat TIME_FORMAT =
      new SimpleDateFormat("HH:mm, MMM d ''yy",Locale.CANADA);

  public ReminderItem() {
    mId = -1;
  }

  public ReminderItem(Cursor cursor) {
    mId = cursor.getInt(cursor.getColumnIndex(ReminderParams.ID));
    mType = ReminderType.fromString(cursor.getString(cursor.getColumnIndex(ReminderParams.TYPE)));
    mContent = cursor.getString(cursor.getColumnIndex(ReminderParams.CONTENT));
    mTitle = cursor.getString(cursor.getColumnIndex(ReminderParams.TITLE));
    mTimeInMillis = cursor.getLong(cursor.getColumnIndex(ReminderParams.TIME));
    mFrequency = cursor.getInt(cursor.getColumnIndex(ReminderParams.FREQUENCY));
  }

  public int getId() {
    return mId;
  }

  public void setId(int id) {
    mId = id;
  }

  public ReminderType getType() {
    return mType;
  }

  public void setType(ReminderType type) {
    mType = type;
  }

  public String getTitle() {
    return mTitle;
  }

  public void setTitle(String title) {
    mTitle = title;
  }

  public String getContent() {
    return mContent;
  }

  public void setContent(String content) {
    mContent = content;
  }

  public long getTimeInMillis() {
    return mTimeInMillis;
  }

  public void setTimeInMillis(long timeInMillis) {
    mTimeInMillis = timeInMillis;
  }

  public String getFormattedTime() {
    return TIME_FORMAT.format(getTimeInMillis());
  }

  public int getFrequency() {
    return mFrequency;
  }

  public void setFrequency(int frequency) {
    mFrequency = frequency;
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.mId);
    dest.writeString(this.mType.getName());
    dest.writeString(this.mTitle);
    dest.writeString(this.mContent);
    dest.writeLong(this.mTimeInMillis);
    dest.writeInt(this.mFrequency);
  }

  protected ReminderItem(Parcel in) {
    this.mId = in.readInt();
    this.mType =  ReminderType.fromString(in.readString());
    this.mTitle = in.readString();
    this.mContent = in.readString();
    this.mTimeInMillis = in.readLong();
    this.mFrequency = in.readInt();
  }

  public static final Creator<ReminderItem> CREATOR = new Creator<ReminderItem>() {
    @Override
    public ReminderItem createFromParcel(Parcel source) {
      return new ReminderItem(source);
    }

    @Override
    public ReminderItem[] newArray(int size) {
      return new ReminderItem[size];
    }
  };
}
