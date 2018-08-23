package com.example.administrator.demographicstuff.app_usage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppUsageDatabase extends SQLiteOpenHelper {

    public static final String db_name = "app_usage_db";

    //FOR APP USAGE
    public static final String app_usage_table = "usage_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "ANDROID_ID";
    public static final String COL_3 = "APP_NAME";
    public static final String COL_4 = "USAGE";

    public AppUsageDatabase(Context context) {
        super(context, db_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table " + app_usage_table + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, ANDROID_ID TEXT, APP_NAME TEXT, USAGE REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists " + app_usage_table);
    }

    //unosenje podataka za APP USAGE
    public boolean insertAppUsage(String android_id, String app_name, double usage)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, android_id);
        contentValues.put(COL_3, app_name);
        contentValues.put(COL_4, usage);

        long result = db.insert(app_usage_table, null, contentValues);
        if(result == -1) {
            return false;
        }else{
            return true;
        }
    }

    //update usage-a za aplikaciju
    public boolean updateAppUsage(String android_id, String app_name, double usage)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        newValues.put(COL_4, usage);

        String[] args = new String[]{android_id, app_name};
        db.update(app_usage_table, newValues, "ANDROID_ID=? AND APP_NAME=?", args);return true;
    }

    //dohvacanje app_usage podataka
    public Cursor getAppUsage(String android_id, String app_name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + app_usage_table + " WHERE ANDROID_ID = ? AND APP_NAME = ? ", new String[] {android_id, app_name});
        return res;
    }

    //dohvacanje 3 najkoristenije aplikacije
    public Cursor getMostUsedApps(String android_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + app_usage_table + " WHERE ANDROID_ID = ? ORDER BY USAGE DESC LIMIT 3", new String[] {android_id});
        return res;
    }


}