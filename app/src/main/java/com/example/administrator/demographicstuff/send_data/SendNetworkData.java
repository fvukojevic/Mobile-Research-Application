package com.example.administrator.demographicstuff.send_data;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.administrator.demographicstuff.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendNetworkData extends JobService {
    URL sendDataUrl;
    HttpURLConnection sendDataHttpConnection;
    OutputStream outStream;
    BufferedReader inStreamReader;
    StringBuilder stringBuilder;
    int responseCode;
    JSONArray json;
    String data;
    boolean running;
    JobParameters params;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        params = jobParameters;
        sendDataUrl = null;
        stringBuilder = new StringBuilder();
        running = true;
        new Thread(new Runnable() {
            public void run() {
                sendData(params);
            }
        }).start();
        return running;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        jobFinished(jobParameters, running);
        return false;
    }

    public void sendData(JobParameters params) {
        try {
            sendDataUrl = new URL("http://cued.azurewebsites.net/api/network/postnetwork");
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
                FileInputStream inStream;
                FileOutputStream outputStream;
                String file1 = Environment.getExternalStorageDirectory().getAbsolutePath();
                String filename = "data.txt";

                File f = new File(file1 + File.separator + filename);
                inStream = new FileInputStream(f);
                stringBuilder.append("[");
                if (inStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    Log.i("Read line", "Starting...");
                    while ((receiveString = bufferedReader.readLine()) != null) {
                        Log.i("Read line", receiveString);
                        stringBuilder.append(receiveString);
                    }
                    outputStream = new FileOutputStream(f, false);
                    outputStream.write("".getBytes());
                    outputStream.close();
                    inStream.close();

                }
                stringBuilder.append("]");
                data = stringBuilder.toString();
            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            }

            try {
                Log.i("Data in string ", data);
                json = new JSONArray(data);
                Log.i("Data in json ", json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            outStream.write(json.toString().getBytes());

            responseCode = sendDataHttpConnection.getResponseCode();
            if (responseCode == 200) {
                //post successful
                Log.i("Response code ", Integer.toString(responseCode));
                inStreamReader = new BufferedReader(new InputStreamReader(sendDataHttpConnection.getInputStream()));
            } else {
                Log.i("Response code ", Integer.toString(responseCode));
                inStreamReader = new BufferedReader(new InputStreamReader(sendDataHttpConnection.getErrorStream()));
            }

            String line;
            while ((line = inStreamReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            Log.i("Response body ", stringBuilder.toString());

            inStreamReader.close();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sendDataHttpConnection.disconnect();
        }
        running = false;
        jobFinished(params, true);
    }
}
