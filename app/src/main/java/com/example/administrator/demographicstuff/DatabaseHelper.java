package com.example.administrator.demographicstuff;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String db_name = "app_db";

    //FOR TICKETS
    public static final String ticket_table = "ticket_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "CATEGORY";
    public static final String COL_3 = "SUBCATEGORY";
    public static final String COL_4 = "QUESTION";

    //FOR DEMOGRAPHIC
    public static final String demographic_table = "demographic_table";
    public static final String DOL_1 = "ID";
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
        db.execSQL("Create table " + ticket_table + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, CATEGORY TEXT, SUBCATEGORY TEXT, QUESTION TEXT)");
        db.execSQL("Create table " + demographic_table + " (ID TEXT PRIMARY KEY, GENDER TEXT, AGE INT, OCCUPATION TEXT, BROADBAND TEXT, POSTAL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("Drop table if exists " + ticket_table);
        db.execSQL("Drop table if exists " + demographic_table);
    }

    //unosenje podataka za tickete
    public boolean insertTicketData(String category, String subcategory, String question)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, category);
        contentValues.put(COL_3, subcategory);
        contentValues.put(COL_4, question);

        long result = db.insert(ticket_table, null, contentValues);
        if(result == -1) {
            return false;
        }else{
            return true;
        }
    }

    //unosenje demografskih podataka
    public boolean insertDemographicData(String id, String gender, int age, String occupation, String broadband, String postal)
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
    public Cursor getAllTicketData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + ticket_table, null);
        return res;
    }

    //dohvacanje demografskih podataka
    public Cursor getAllDemographicData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + demographic_table, null);
        return res;
    }

}
