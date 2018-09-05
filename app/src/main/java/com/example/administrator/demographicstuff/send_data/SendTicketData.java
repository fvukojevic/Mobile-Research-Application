package com.example.administrator.demographicstuff.send_data;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;

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

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        sendDataUrl = null;
        running  = true;
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
                sendDataUrl = new URL("http://cued.azurewebsites.net/api/ticket/postticket");
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


                //TODO get data from db (bitno)(budem ja ako se ne snadete)
                String str = ("{\"appuserid\":\"\"," +
                        "\"category\":\"\"," +
                        "\"subcategory\":\"\"," +
                        "\"frequency\":\"\"," +
                        "\"comment\":\"\"," +
                        "\"longitude\":\"\"," +
                        "\"latitude\":\"\"," +
                        "\"altitude\":\"\"," +
                        "\"timestamp\":\"\"," +
                        "\"email\":\"\"," +
                        "\"acceptance\":\"\"" +
                        "}");

                outStream.write(str.getBytes());

                responseCode = sendDataHttpConnection.getResponseCode();
                if (responseCode == 200) {
                    //post successful
                    Log.i("Response code: ", Integer.toString(responseCode));
                    inStreamReader = new BufferedReader(new InputStreamReader(sendDataHttpConnection.getInputStream()));
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
