package com.example.administrator.demographicstuff.app_demographic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DemograficDatabase extends SQLiteOpenHelper {

    public static final String db_name = "demografic_db";

    //<-- Lokalne varijable za demografske podatke -->//
    public static final String demographic_table = "new_demografic_table";
    public static final String DOL_1 = "ANDROID_ID";
    public static final String DOL_2 = "GENDER";
    public static final String DOL_3 = "AGE";
    public static final String DOL_4 = "OCCUPATION";
    public static final String DOL_5 = "BROADBAND";
    public static final String DOL_6 = "POSTAL";


    public DemograficDatabase(Context context) {
        super(context, db_name, null, 1);
    }

    //<-- Kreiranje baze podataka -->//
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table " + demographic_table + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,ANDROID_ID TEXT, GENDER TEXT, AGE TEXT, OCCUPATION TEXT, BROADBAND TEXT, POSTAL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    }

    //unosenje demografskih podataka
    public boolean insertDemographicData(String id, String gender, String age, String occupation, String broadband, String postal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DOL_1, id);
        contentValues.put(DOL_2, gender);
        contentValues.put(DOL_3, age);
        contentValues.put(DOL_4, occupation);
        contentValues.put(DOL_5, broadband);
        contentValues.put(DOL_6, postal);

        long result = db.insert(demographic_table, null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    //dohvacanje demografskih podataka
    public Cursor getAllDemographicData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + demographic_table, null);
        return res;
    }

    //dohvacanje korisnikovog id-a
    public Cursor getMyId(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select id from " + demographic_table, null);
        return res;
    }

    //dohvacanje podataka pomoću android_id-a uređaja
    public Cursor findByAndroidId(String android_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + demographic_table + " WHERE ANDROID_ID = ?", new String[]{android_id});
        return res;
    }
}