package com.example.administrator.demographicstuff;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;

public class CreateTicketActivity extends AppCompatActivity {

    public static Spinner category_spinner, subcategory_spinner, frequency_spinner;
    public static EditText question_field;
    public static TextView category_text,subcategory_text,frequency_text;
    public static Button submit;
    public static String android_id;
    public static DatabaseHelper db;
    public static BottomNavigationView bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ticket);

        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        db = new DatabaseHelper(CreateTicketActivity.this);

        bottomGo();
        onSelect();
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

    public void onSelect()
    {
        category_spinner = findViewById(R.id.spinner);
        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String category = category_spinner.getSelectedItem().toString();
                if(category.equals(""))
                {
                    subcategory_text = findViewById(R.id.subcategory_text);
                    subcategory_text.setVisibility(View.INVISIBLE);

                    submit = findViewById(R.id.btn_ticket);
                    submit.setVisibility(View.INVISIBLE);

                    subcategory_spinner = findViewById(R.id.spinner2);
                    subcategory_spinner.setVisibility(View.INVISIBLE);

                    question_field = findViewById(R.id.question_field);
                    question_field.setVisibility(View.INVISIBLE);

                    frequency_spinner = findViewById(R.id.spinner3);
                    frequency_spinner.setVisibility(View.INVISIBLE);

                    frequency_text = findViewById(R.id.frequency_text);
                    frequency_text.setVisibility(View.INVISIBLE);
                }
                else{
                    subcategory_spinner = findViewById(R.id.spinner2);
                    subcategory_spinner.setVisibility(View.VISIBLE);

                    subcategory_text = findViewById(R.id.subcategory_text);
                    subcategory_text.setVisibility(View.VISIBLE);

                    ArrayList<String> subcategory_strings = new ArrayList<String>();

                    subcategory_strings.add("");
                    if(category.equals("Voice"))
                    {
                        subcategory_text.setText("What type of voice isshue");
                        subcategory_strings.add("Voice calls can not connect");
                        subcategory_strings.add("Voice calls drop unexpectedly");
                        subcategory_strings.add("Bad quality of voice calls");
                    }
                    else if(category.equals("Coverage"))
                    {
                        subcategory_text.setText("What type of Coverage isshue");
                        subcategory_strings.add("I lost connection the the network");
                        subcategory_strings.add("I have a weak network signal");
                        subcategory_strings.add("I can never connect to 4G");
                    }
                    else if(category.equals("Mobile Data")){
                        subcategory_text.setText("What type of Mobile Data isshue");
                        subcategory_strings.add("No access to internet");
                        subcategory_strings.add("Slow speed on mobile data");
                        subcategory_strings.add("Web pages always time out");
                        subcategory_strings.add("No 4G connection available");
                    }else
                    {
                        subcategory_text.setText("What type of Device isshue");
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
                            if(!subcategory.equals(""))
                            {
                                frequency_spinner = findViewById(R.id.spinner3);
                                frequency_spinner.setVisibility(View.VISIBLE);

                                frequency_text = findViewById(R.id.frequency_text);
                                frequency_text.setVisibility(View.VISIBLE);

                                frequency_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                        final String frequency_string = frequency_spinner.getSelectedItem().toString();
                                        if(!frequency_string.equals(""))
                                        {
                                            question_field = findViewById(R.id.question_field);
                                            question_field.setVisibility(View.VISIBLE);

                                            submit = findViewById(R.id.btn_ticket);
                                            submit.setVisibility(View.VISIBLE);

                                            submit.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    boolean check = db.insertTicketData(category_spinner.getSelectedItem().toString(),subcategory_spinner.getSelectedItem().toString(), question_field.getText().toString(),android_id);
                                                    if(check == false)
                                                    {
                                                        Toast.makeText(CreateTicketActivity.this, "Ticket not submitted", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(CreateTicketActivity.this, "Ticket submited!", Toast.LENGTH_SHORT).show();
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
}

