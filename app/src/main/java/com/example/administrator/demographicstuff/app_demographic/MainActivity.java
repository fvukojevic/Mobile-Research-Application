package com.example.administrator.demographicstuff.app_demographic;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.administrator.demographicstuff.R;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    public static RadioGroup rg1, rg2, rg3, rg4;
    public static RadioButton rb1, rb2, rb3, rb4;
    public static EditText postal;
    public static Button confirm;
    public static DemograficDatabase db;
    public static String android_id;
    public static Button terms;
    public static Button privacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        db = new DemograficDatabase(this);
        Cursor res = db.findByAndroidId(android_id);

        if (res.getCount() > 0) {
            Intent intent = new Intent(".FirstPageActivity");
            new Task().execute(intent);
            finish();
        } else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_main);
            inputChecker();
        }
    }

    private class Task extends AsyncTask<Intent, Void, Void>{
        @Override
        protected Void doInBackground(Intent... intents) {
            startActivity(intents[0]);
            return null;
        }
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }


    public void inputChecker() {
        rg2 = findViewById(R.id.radioGroup);
        rg1 = findViewById(R.id.radioGroup2);
        rg3 = findViewById(R.id.radioGroup3);
        rg4 = findViewById(R.id.radioGroup4);
        postal = findViewById(R.id.postal_code);
        confirm = findViewById(R.id.confirm);
        terms = findViewById(R.id.termsOfUse);
        privacy = findViewById(R.id.privacyBtn);


        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMessage("Terms", "Here come the terms!");
                return;
            }
        });

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMessage("Privacy:", "Privacy info goes here!");
                return;
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rg1.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(MainActivity.this, "Choose a gender first", Toast.LENGTH_SHORT).show();
                } else if (rg2.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(MainActivity.this, "Choose an age first", Toast.LENGTH_SHORT).show();
                } else if (rg3.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(MainActivity.this, "Choose an occupation first", Toast.LENGTH_SHORT).show();
                } else if (rg4.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(MainActivity.this, "Answer last question", Toast.LENGTH_SHORT).show();
                } else if (postal.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Enter postal code information", Toast.LENGTH_SHORT).show();
                } else {
                    int genderID = rg1.getCheckedRadioButtonId();
                    int ageID = rg2.getCheckedRadioButtonId();
                    int ocuID = rg3.getCheckedRadioButtonId();
                    int broadbandID = rg4.getCheckedRadioButtonId();

                    // find the radiobutton by returned id
                    rb1 = findViewById(genderID);
                    rb2 = findViewById(ageID);
                    rb3 = findViewById(ocuID);
                    rb4 = findViewById(broadbandID);

                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("android_id", android_id);
                        postData.put("gender", rb1.getText().toString());
                        postData.put("age", rb2.getText().toString());
                        postData.put("occupation", rb3.getText().toString());
                        postData.put("nesto", rb4.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("MyApp", postData.toString());

                    boolean check = db.insertDemographicData(android_id, rb1.getText().toString(), rb2.getText().toString(),
                            rb3.getText().toString(), rb4.getText().toString(), postal.getText().toString());
                    if (check == false) {
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Demographic submited!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(".FirstPageActivity");
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

    }
}
