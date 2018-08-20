package com.example.administrator.demographicstuff;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FirstPageActivity extends AppCompatActivity {

    private static Button create_ticket, show_tickets, show_wifi, show_map;
    private static TicketNewDatabase tb;
    private static AppUsageDatabase ab;
    public String android_id;
    @BindView(R.id.create_ticket2)
    TextView showLiveConditions;

    //For google maps
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);
        tb = new TicketNewDatabase(FirstPageActivity.this);
        ab = new AppUsageDatabase(FirstPageActivity.this);
        ButterKnife.bind(this);

        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        AppsUsed();
        toCreate();
        showTickets();
        showWifiUsage();
        if(isServicesOK()){
            showMap();
        }
        showLiveConditions();
    }

    public boolean isServicesOK(){

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(FirstPageActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(FirstPageActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FirstPageActivity.this);

        builder.setMessage("Are you sure you want to Exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirstPageActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //dohvacanje najkoristenijih aplikacija
    public void AppsUsed() {
        Cursor res = ab.getMostUsedApps(android_id);
        if (res.getCount() == 0) {
            //Show message
            showMessage("Empty", "No apps found");
            return;
        } else {
            StringBuffer buffer = new StringBuffer();
            while (res.moveToNext()) {
                buffer.append("App name: " + res.getString(2) + "\n");
                buffer.append("Usage: " + res.getString(3) + " MB\n");
                buffer.append("\n");
            }
            //show all data
            showMessage("Most used apps", buffer.toString());
        }
    }

    //Otvaranje prozora za pravljenje novog ticketa
    public void toCreate() {
        create_ticket = findViewById(R.id.create_ticket);
        create_ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(".CreateTicketActivity");
                startActivity(intent);
            }
        });
    }

    //prelazak na mapu
    public void showMap()
    {
        show_map = findViewById(R.id.show_map);
        show_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(".MapActivity");
                startActivity(intent);
            }
        });
    }
    //dohvacanje wifi usage-a
    public void showWifiUsage() {
        show_wifi = findViewById(R.id.show_wifi);
        show_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(".AppWifiUsage");
                startActivity(intent);
            }
        });
    }

    public void showLiveConditions() {
        showLiveConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(".LiveConditions");
                startActivity(intent);
            }
        });
    }


    //dohvacanje svih Ticketa
    public void showTickets() {
        show_tickets = (Button) findViewById(R.id.show_tickets);
        show_tickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor res = tb.getMyTickets(android_id);
                if (res.getCount() == 0) {
                    //Show message
                    showMessage("Empty", "No tickets found");
                    return;
                } else {
                    StringBuffer buffer = new StringBuffer();
                    while (res.moveToNext()) {
                        buffer.append("ID: " + res.getString(0) + "\n");
                        buffer.append("Android_id: " + res.getString(1) + "\n");
                        buffer.append("Category: " + res.getString(2) + "\n");
                        buffer.append("Subcategory: " + res.getString(3) + "\n");
                        buffer.append("Frequency: " + res.getString(4) + "\n");
                        buffer.append("Question: " + res.getString(5) + "\n");
                        buffer.append("Date: " + res.getString(6) + "\n");
                        buffer.append("Time: " + res.getString(7) + "\n");
                        buffer.append("Long: " + res.getString(8) + "\n");
                        buffer.append("Lat: " + res.getString(9) + "\n");
                        buffer.append("\n");
                    }

                    //show all data
                    showMessage("Data", buffer.toString());
                }
            }
        });
    }

    //prikaz svih tiketaa u Alert Dialogu
    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}
