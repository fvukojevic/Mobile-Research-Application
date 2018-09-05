package com.example.administrator.demographicstuff.send_data;

import android.app.Activity;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.administrator.demographicstuff.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendAppUserData extends JobService {
    URL sendDataUrl;
    HttpURLConnection sendDataHttpConnection;
    InputStream inStream;
    OutputStream outStream;
    BufferedReader inStreamReader;
    StringBuilder stringBuilder;
    int responseCode;
    String data;
    JSONObject json;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        sendDataUrl = null;
        data = jobParameters.getExtras().getString("DATA");
        stringBuilder = new StringBuilder();
        new SendAppUserData.sendData().execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private class sendData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                sendDataUrl = new URL("http://cued.azurewebsites.net/api/appuser/postappuser");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                sendDataHttpConnection = (HttpURLConnection) sendDataUrl.openConnection();
                sendDataHttpConnection.setRequestMethod("POST");
                sendDataHttpConnection.setRequestProperty("Content-Type", "application/json");
                sendDataHttpConnection.setDoOutput(true);
                sendDataHttpConnection.setReadTimeout(15 * 1000);
                sendDataHttpConnection.connect();
                outStream = sendDataHttpConnection.getOutputStream();

                try {
                    json = new JSONObject(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //TODO some parameters are '0' (postalcode, homephone, acceptance)
                outStream.write(json.toString().getBytes());

                responseCode = sendDataHttpConnection.getResponseCode();
                if (responseCode == 200) {
                    //post successful
                    Log.i("Response code: ", Integer.toString(responseCode));
                    inStreamReader = new BufferedReader(new InputStreamReader(sendDataHttpConnection.getInputStream()));
                } else {
                    Log.i("Response code: ", Integer.toString(responseCode));
                    inStreamReader = new BufferedReader(new InputStreamReader(sendDataHttpConnection.getErrorStream()));
                }

                String line = inStreamReader.readLine();
                line = line.replace("\"", "");
                Log.i("Response body: ", line);

                sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.APP_USER_PREFERENCES), Context.MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("APP_USER_ID", line);
                Log.i("USER_ID ", line);
                editor.apply();

                inStreamReader.close();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                sendDataHttpConnection.disconnect();
            }
            return null;
        }
    }
}
