package com.example.administrator.demographicstuff.send_data;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;

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

public class SendTicketData extends JobService {
    URL sendDataUrl;
    HttpURLConnection sendDataHttpConnection;
    OutputStream outStream;
    BufferedReader inStreamReader;
    StringBuilder stringBuilder;
    int responseCode;
    boolean running;
    String data;
    JSONObject json;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        sendDataUrl = null;
        running  = true;
        data = jobParameters.getExtras().getString("DATA");
        stringBuilder = new StringBuilder();
        new SendTicketData.sendData().execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return running;
    }

    private class sendData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                sendDataUrl = new URL("http://cued.azurewebsites.net/api/ticket/posttickets");
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


                stringBuilder.append("[");
                //TODO get data from db (bitno)(budem ja ako se ne snadete)
                try {
                    json = new JSONObject(data);
                    stringBuilder.append(json);
                    Log.i("DATA", json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                stringBuilder.append("]");
                outStream.write(stringBuilder.toString().getBytes());

                responseCode = sendDataHttpConnection.getResponseCode();
                if (responseCode == 200) {
                    //post successful
                    Log.i("Response code: ", Integer.toString(responseCode));
                    inStreamReader = new BufferedReader(new InputStreamReader(sendDataHttpConnection.getInputStream()));
                    Log.i("Response code: ", inStreamReader.toString());
                } else {
                    Log.i("Response code: ", Integer.toString(responseCode));
                    inStreamReader = new BufferedReader(new InputStreamReader(sendDataHttpConnection.getErrorStream()));
                }

                String line;
                while ((line = inStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
                Log.i("Response body: ", stringBuilder.toString());

                inStreamReader.close();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                sendDataHttpConnection.disconnect();
            }
            running = false;
            return null;
        }
    }
}
