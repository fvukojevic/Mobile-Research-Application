package com.example.administrator.demographicstuff;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class FirstPageActivity extends AppCompatActivity {

    private static Button create_ticket, show_tickets;
    private static DatabaseHelper db;
    public String android_id;
    private BottomNavigationView bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);
        db = new DatabaseHelper(FirstPageActivity.this);

        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        bottomGo();
        toCreate();
        showTickets();
    }

    //Otvaranje prozora za pravljenje novog ticketa
    public void toCreate(){
        create_ticket = findViewById(R.id.create_ticket);
        create_ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(".CreateTicketActivity");
                startActivity(intent);
            }
        });
    }

    //dohvacanje svih Ticketa
    public void showTickets()
    {
        show_tickets = (Button)findViewById(R.id.show_tickets);
        show_tickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor res = db.getAllTicketData(android_id);
                if(res.getCount() == 0)
                {
                    //Show message
                    showMessage("Error", "No tickets found");
                    return;
                }
                else{
                    StringBuffer buffer = new StringBuffer();
                    while(res.moveToNext())
                    {
                        buffer.append("ID: " + res.getString(0) + "\n");
                        buffer.append("Category: " + res.getString(1)+ "\n");
                        buffer.append("Subcategory: " + res.getString(2)+ "\n");
                        buffer.append("Text: " + res.getString(3)+ "\n");
                        buffer.append("Android_id" + res.getString(4));
                        buffer.append("\n");
                    }

                    //show all data
                    showMessage("Data", buffer.toString());
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    public void bottomGo()
    {
        bottom = findViewById(R.id.bottom);
        BottomNavigationView bottom = (BottomNavigationView) findViewById(R.id.bottom);
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottom.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
            itemView.setShifting(false);
            itemView.setChecked(false);
        }
        bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_home:
                        Intent home_intent = new Intent(".FirstPageActivity");
                        startActivity(home_intent);
                        return true;
                    case R.id.nav_ticket:
                        Intent ticket_intent = new Intent(".CreateTicketActivity");
                        startActivity(ticket_intent);
                        return true;
                    default:return false;
                }
            }
        });
    }
    //prikaz svih tiketaa u Alert Dialogu
    public void showMessage(String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}