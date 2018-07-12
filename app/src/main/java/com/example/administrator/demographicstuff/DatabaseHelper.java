package com.example.administrator.demographicstuff;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String db_name = "app_db";

    //FOR TICKETS
    public static final String ticket_table = "ticket_table";
    public static final String COL_1 = "ID";
    public static final String COL_5 = "ANDROID_ID";
    public static final String COL_2 = "CATEGORY";
    public static final String COL_3 = "SUBCATEGORY";
    public static final String COL_6 = "FREQUENCY";
    public static final String COL_4 = "QUESTION";

    //FOR DEMOGRAPHIC
    public static final String demographic_table = "demographic_table";
    public static final String DOL_1 = "ANDROID_ID";
    public static final String DOL_2 = "GENDER";
    public static final String DOL_3 = "AGE";
    public static final String DOL_4 = "OCCUPATION";
    public static final String DOL_5 = "BROADBAND";
    public static final String DOL_6 = "POSTAL";


    public DatabaseHelper(Context context) {
        super(context, db_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table " + ticket_table + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, CATEGORY TEXT, SUBCATEGORY TEXT, QUESTION TEXT, ANDROID_ID TEXT)");
        db.execSQL("Create table " + demographic_table + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,ANDROID_ID TEXT, GENDER TEXT, AGE TEXT, OCCUPATION TEXT, BROADBAND TEXT, POSTAL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String sql = "ALTER TABLE " + ticket_table + " ADD COLUMN FREQUENCY TEXT";
        db.execSQL(sql);
    }

    //unosenje podataka za tickete
    public boolean insertTicketData(String category, String subcategory,String question, String android_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, category);
        contentValues.put(COL_3, subcategory);
        contentValues.put(COL_4, question);
        contentValues.put(COL_5, android_id);

        long result = db.insert(ticket_table, null, contentValues);
        if(result == -1) {
            return false;
        }else{
            return true;
        }
    }

    //unosenje demografskih podataka
    public boolean insertDemographicData(String id, String gender, String age, String occupation, String broadband, String postal)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DOL_1, id);
        contentValues.put(DOL_2, gender);
        contentValues.put(DOL_3, age);
        contentValues.put(DOL_4, occupation);
        contentValues.put(DOL_5, broadband);
        contentValues.put(DOL_6, postal);

        long result = db.insert(demographic_table, null, contentValues);
        if(result == -1) {
            return false;
        }else{
            return true;
        }
    }

    //dohvacanje ticket podataka
    public Cursor getAllTicketData(String android_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + ticket_table + " WHERE ANDROID_ID = ?", new String[] {android_id});
        return res;
    }

    //dohvacanje demografskih podataka
    public Cursor getAllDemographicData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + demographic_table, null);
        return res;
    }

    public Cursor findByAndroidId(String android_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + demographic_table + " WHERE ANDROID_ID = ?", new String[] {android_id});
        return res;
    }

}