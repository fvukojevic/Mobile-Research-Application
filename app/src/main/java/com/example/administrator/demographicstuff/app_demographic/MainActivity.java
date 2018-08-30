package com.example.administrator.demographicstuff.app_demographic;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //local variables
    public static RadioGroup rg1, rg2, rg3, rg4;
    public static RadioButton rb1, rb2, rb3, rb4;
    public static EditText postal;
    public static Button confirm;
    public static DemograficDatabase db;
    public static String android_id, imei, imsi, model_number,manufacturer,release,device_name,android_version;
    public static int sdkVersion;
    public static Button terms;
    public static Button privacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //dohvacanje android _id-a i spajanje na bazu. Pokušava se pronaći korisnik sa tim
        //android id-om, ukoliko se pronađe MainActivity se neće prikazati i prelazimo u
        //FirstPageActivity
        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
       imei = telephonyManager.getDeviceId();
       imsi = telephonyManager.getSubscriberId();
        manufacturer = Build.MANUFACTURER;
        model_number = manufacturer + " " + Build.MODEL;
        device_name = Build.DEVICE;

        release = Build.VERSION.RELEASE;
        sdkVersion = Build.VERSION.SDK_INT;
        android_version = sdkVersion + " " + release;

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

    //<-- Funkcija za prikaz podataka u AlertDialog-u
    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }


    //<-- Funcija za on click listenere -->//
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

        //<-- Potvrđujemo i provjeravamo je li svako polje popunjeno -->//
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

                    //<-- Pohranjivanje demografskih podataka u JSON object
                    //<-- Spremno za slanje na server
                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("android_id", android_id);
                        postData.put("gender", rb1.getText().toString());
                        postData.put("age", rb2.getText().toString());
                        postData.put("occupation", rb3.getText().toString());
                        postData.put("nesto", rb4.getText().toString());
                        postData.put("imei", imei);
                        postData.put("imsi", imsi);
                        postData.put("Model number", model_number);
                        postData.put("Devica name", device_name);
                        postData.put("Android version", android_version);
                        writeToFile(postData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
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

    public void writeToFile(JSONObject json) throws IOException {
        FileOutputStream stream;
        String file1 = Environment.getExternalStorageDirectory().getAbsolutePath();
        String filename = "demographic.txt";

        File f = new File(file1 + File.separator + filename);

        FileOutputStream fstream = new FileOutputStream(f, true);
        fstream.write(json.toString().getBytes());
        fstream.write("\r\n ".getBytes());
        fstream.close();
    }
}
