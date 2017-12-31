package com.example.kyle.reminder;

/**
 * Created by nygellopez on 2017-12-29.
 */

public enum ReminderType {
  ALL("all"), ALERT("alert"), NOTE("note");

  private final String mName;
  ReminderType(String name){
      mName = name;
    }

  public String getName(){
      return mName;
    }

  public static ReminderType fromString(String name){
   ReminderType[] actionTypes = values();
   for (ReminderType type : actionTypes){
     if (type.mName.equals(name)) {
       return type;
     }
   }
    return null;
  }

}
