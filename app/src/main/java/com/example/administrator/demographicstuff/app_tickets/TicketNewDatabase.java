package com.example.administrator.demographicstuff.app_tickets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TicketNewDatabase extends SQLiteOpenHelper {

    public static final String db_name = "new_new_ticket_db";

    //FOR APP USAGE
    public static final String new_ticket_table = "new_NEW_ticket_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "ANDROID_ID";
    public static final String COL_3 = "CATEGORY";
    public static final String COL_4 = "SUBCATEGORY";
    public static final String COL_5 = "FREQUENCY";
    public static final String COL_6 = "QUESTION";
    public static final String COL_7 = "DATE"; //string
    public static final String COL_8 = "TIME"; //string
    public static final String COL_9 = "LONG"; //double
    public static final String COL_10 = "LAT"; //double
    public static final String COL_11 = "ALTITUDE";
    public static final String COL_12 = "EMAIL";

    public TicketNewDatabase(Context context) {
        super(context, db_name, null, 1);
    }

    /*
     * Stvaranje lokalne baze za spremanje Tiket-a
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table " + new_ticket_table + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, ANDROID_ID TEXT, " +
                "CATEGORY TEXT, SUBCATEGORY TEXT, FREQUENCY TEXT, QUESTION TEXT, DATE TEXT, TIME TEXT, LONG REAL, LAT REAL," +
                "ALTITUDE REAL, EMAIL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists " + new_ticket_table);
    }

    /*
     * Unošenje podataka u  Tikete
     */
    public boolean insertTicket(String android_id, String category, String subcategory,
                                String frequency, String question, String date, String time,
                                double longitude, double lat, double altitude, String email)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, android_id);
        contentValues.put(COL_3, category);
        contentValues.put(COL_4, subcategory);
        contentValues.put(COL_5, frequency);
        contentValues.put(COL_6, question);
        contentValues.put(COL_7, date);
        contentValues.put(COL_8, time);
        contentValues.put(COL_9, longitude);
        contentValues.put(COL_10, lat);
        contentValues.put(COL_11, altitude);
        contentValues.put(COL_12, email);

        long result = db.insert(new_ticket_table, null, contentValues);
        if(result == -1) {
            return false;
        }else{
            return true;
        }
    }

    /*
     * Dohvacanje svih tiketa
     */
    public Cursor getMyTickets(String android_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + new_ticket_table + " WHERE ANDROID_ID = ?", new String[] {android_id});
        return res;
    }

    /*
     * Dohvat zadnja 3 tiketa
     */
    public Cursor getMyThreeTickets(String android_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + new_ticket_table + " WHERE ANDROID_ID = ? ORDER BY id DESC LIMIT 3", new String[] {android_id});
        return res;
    }
}

