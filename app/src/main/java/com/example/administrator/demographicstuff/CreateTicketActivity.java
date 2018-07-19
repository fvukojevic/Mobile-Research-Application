package com.example.administrator.demographicstuff;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CreateTicketActivity extends AppCompatActivity{

    static final int REQUEST_LOCATION = 1;

    public static Spinner category_spinner, subcategory_spinner, frequency_spinner;
    public static EditText question_field;
    public static TextView category_text, subcategory_text, frequency_text, email_text;
    public static Button submit;
    public static String android_id;
    public static Double alti;
    public static TicketNewDatabase tb;
    public static BottomNavigationView bottom;
    public static TextView date_text, time_text, long_text, lat_text;
    public static LocationManager locationManager;
    public static Button permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ticket);

        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long_text = findViewById(R.id.long1);
        lat_text = findViewById(R.id.lat);

        tb = new TicketNewDatabase(CreateTicketActivity.this);

        getTimeAndDate();
        bottomGo();
        onSelect();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocation();
    }

    public void getLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(CreateTicketActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null){
            double latti = location.getLatitude();
            double longi = location.getLongitude();
            alti = location.getAltitude();

            long_text.setText(String.valueOf(longi));
            lat_text.setText(String.valueOf(latti));
        } else {
            long_text.setText("Can't find");
            lat_text.setText("Can't find");
        }
    }
    }
    public void getTimeAndDate() {
        date_text = findViewById(R.id.date);
        time_text = findViewById(R.id.time);

        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance().format(calendar.getTime());

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String time = format.format(calendar.getTime());

        date_text.setText(currentDate);
        time_text.setText(time);
    }

    @SuppressLint("RestrictedApi")
    public void bottomGo() {
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
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        Intent home_intent = new Intent(".FirstPageActivity");
                        startActivity(home_intent);
                        return true;
                    case R.id.nav_ticket:
                        Intent ticket_intent = new Intent(".CreateTicketActivity");
                        startActivity(ticket_intent);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    public void onSelect() {
        category_spinner = findViewById(R.id.spinner);
        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String category = category_spinner.getSelectedItem().toString();
                if (category.equals("")) {
                    permission = findViewById(R.id.permission);
                    permission.setVisibility(View.VISIBLE);

                    email_text = findViewById(R.id.email_text);
                    email_text.setVisibility(View.VISIBLE);

                    permission.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            email_text.setVisibility(View.VISIBLE);
                        }
                    });

                    subcategory_text = findViewById(R.id.subcategory_text);
                    subcategory_text.setVisibility(View.VISIBLE);

                    submit = findViewById(R.id.btn_ticket);
                    submit.setVisibility(View.VISIBLE);

                    subcategory_spinner = findViewById(R.id.spinner2);
                    subcategory_spinner.setVisibility(View.VISIBLE);

                    question_field = findViewById(R.id.question_field);
                    question_field.setVisibility(View.VISIBLE);

                    frequency_spinner = findViewById(R.id.spinner3);
                    frequency_spinner.setVisibility(View.VISIBLE);

                    frequency_text = findViewById(R.id.frequency_text);
                    frequency_text.setVisibility(View.VISIBLE);
                } else {
                    subcategory_spinner = findViewById(R.id.spinner2);
                    subcategory_spinner.setVisibility(View.VISIBLE);

                    subcategory_text = findViewById(R.id.subcategory_text);
                    subcategory_text.setVisibility(View.VISIBLE);

                    ArrayList<String> subcategory_strings = new ArrayList<String>();

                    subcategory_strings.add("");
                    if (category.equals("Voice")) {
                        subcategory_text.setText("What type of voice issue");
                        subcategory_strings.add("Voice calls can not connect");
                        subcategory_strings.add("Voice calls drop unexpectedly");
                        subcategory_strings.add("Bad quality of voice calls");
                    } else if (category.equals("Coverage")) {
                        subcategory_text.setText("What type of Coverage issue");
                        subcategory_strings.add("I lost connection the the network");
                        subcategory_strings.add("I have a weak network signal");
                        subcategory_strings.add("I can never connect to 4G");
                    } else if (category.equals("Mobile Data")) {
                        subcategory_text.setText("What type of Mobile Data issue");
                        subcategory_strings.add("No access to internet");
                        subcategory_strings.add("Slow speed on mobile data");
                        subcategory_strings.add("Web pages always time out");
                        subcategory_strings.add("No 4G connection available");
                    } else {
                        subcategory_text.setText("What type of Device issue");
                        subcategory_strings.add("My battery is draining quickly");
                        subcategory_strings.add("My device runs warm");
                        subcategory_strings.add("My device is slow, lagging");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateTicketActivity.this, android.R.layout.simple_spinner_item, subcategory_strings);

                    subcategory_spinner.setAdapter(adapter);

                    subcategory_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            final String subcategory = subcategory_spinner.getSelectedItem().toString();
                            if (!subcategory.equals("")) {
                                frequency_spinner = findViewById(R.id.spinner3);
                                frequency_spinner.setVisibility(View.VISIBLE);

                                frequency_text = findViewById(R.id.frequency_text);
                                frequency_text.setVisibility(View.VISIBLE);

                                frequency_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                        final String frequency_string = frequency_spinner.getSelectedItem().toString();
                                        if (!frequency_string.equals("")) {
                                            question_field = findViewById(R.id.question_field);
                                            question_field.setVisibility(View.VISIBLE);

                                            permission = findViewById(R.id.permission);
                                            permission.setVisibility(View.VISIBLE);

                                            submit = findViewById(R.id.btn_ticket);
                                            submit.setVisibility(View.VISIBLE);

                                            submit.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    boolean check = tb.insertTicket(android_id, category_spinner.getSelectedItem().toString() ,
                                                            subcategory_spinner.getSelectedItem().toString()
                                                    , frequency_spinner.getSelectedItem().toString(), question_field.getText().toString(), date_text.getText().toString(),
                                                            time_text.getText().toString(), Double.parseDouble(long_text.getText().toString()),
                                                            Double.parseDouble(lat_text.getText().toString()),alti, email_text.getText().toString());
                                                    if (check == false) {
                                                        Toast.makeText(CreateTicketActivity.this, "Ticket not submitted", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(CreateTicketActivity.this, "Ticket submited!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(".FirstPageActivity");
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapterView) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            question_field = findViewById(R.id.question_field);
                            question_field.setVisibility(View.INVISIBLE);

                            submit = findViewById(R.id.btn_ticket);
                            submit.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION:
                getLocation();
                break;
        }
    }
}

