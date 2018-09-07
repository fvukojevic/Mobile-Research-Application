package com.example.administrator.demographicstuff.app_usage;

import android.app.job.JobParameters;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.demographicstuff.FirstPageActivity;
import com.example.administrator.demographicstuff.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AppUsageAdapter extends RecyclerView.Adapter<AppUsageAdapter.ViewHolder> {

    public AppUsageDatabase db;
    public List<NetworkUsageHelper.AppUsage> usageList = new ArrayList<>();
    private String android_id;
    private Context context;
    /*
     * Konstruktor klase, prima context i android_id korišten za spremanje u lokalnu bazu
     */
    public AppUsageAdapter(Context context, String android_id) {
        this.context = context;
        this.android_id = android_id;
    }

    /*
     * Stvarane View Holdera i njegovo povezivanje sa item_layout-om
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        db = new AppUsageDatabase(context);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.icon = view.findViewById(R.id.icon);
        holder.name = view.findViewById(R.id.name);
        holder.uploaded = view.findViewById(R.id.uploaded);
        holder.downloaded = view.findViewById(R.id.downloaded);
        holder.usage = view.findViewById(R.id.progress);
        holder.usage.setMax(100);
        return holder;
    }

    /*
     * Postavljanje svih parametara holdera sa dobivenim podatcima AppUsage klase
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NetworkUsageHelper.AppUsage usage = usageList.get(position);
        holder.icon.setImageDrawable(usage.getAppIcon());
        holder.name.setText(usage.getAppName());
        float uploaded = usage.getUploaded(NetworkUsageHelper.DATA_MB);
        if (uploaded > 1024f) {
            uploaded = usage.getUploaded(NetworkUsageHelper.DATA_GB);
            holder.uploaded.setText("U: " + String.format("%.2f", uploaded) + " GB");
        } else {
            holder.uploaded.setText("U: " + Math.round(uploaded) + " MB");
        }
        float downloaded = usage.getDownloaded(NetworkUsageHelper.DATA_MB);
        float downloaded_ALLMB = usage.getDownloaded(NetworkUsageHelper.DATA_MB);
        if (downloaded > 1024f) {
            downloaded = usage.getDownloaded(NetworkUsageHelper.DATA_GB);
            holder.downloaded.setText("D: " + String.format("%.2f", downloaded) + " GB");
        } else {
            holder.downloaded.setText("D: " + Math.round(downloaded) + " MB");
        }
        holder.name.setText(usage.getAppName());
        holder.usage.setProgress(Float.valueOf(usage.getDownloadPercentage() * 100f).intValue());


        /*
         * Uvijeti spremanja u bazu
         * Ukoliko je promet manji od 10MB ne sprema se u bazu
         * Ukoliko je spremljen, zahtijeva minimalno još 10MB novog prometa
         * Tek tada dolazi do update-a podataka u bazi
         * Razlog ovoga je što postoje aplikacije koje jednom potroše promet i više nikad
         * Takve Aplikacije nema smisla čuvati u App usage bazi
         */
        if(downloaded_ALLMB > 10)
        {
            Cursor res = db.getAppUsage(FirstPageActivity.real_id, usage.getAppName());
            if(res.getCount() == 0)
            {
                //Json for server database
                JSONObject postData = new JSONObject();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateandTime = sdf.format(new Date());
                try {
                    postData.put("appuserid", FirstPageActivity.real_id);
                    postData.put("name", usage.getAppName());
                    postData.put("data", downloaded_ALLMB);
                    postData.put("timestamp", currentDateandTime);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("AppUsage", postData.toString());
                try {
                    writeToFile(postData);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Show message
                db.insertAppUsage(FirstPageActivity.real_id, usage.getAppName(), downloaded_ALLMB);
                Toast.makeText(context, usage.getAppName() + " is now inserted", Toast.LENGTH_SHORT).show();
            }
            else{
                while(res.moveToNext())
                {
                    double old_usage = res.getDouble(3);
                    if(downloaded_ALLMB - old_usage > 10)
                    {
                        //Json for server database
                        JSONObject postData = new JSONObject();
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        try {
                            postData.put("appuserid", FirstPageActivity.real_id);
                            postData.put("name", usage.getAppName());
                            postData.put("data", downloaded_ALLMB);
                            postData.put("timestamp", ts);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("AppUsage", postData.toString());
                        try {
                            writeToFile(postData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        db.updateAppUsage(FirstPageActivity.real_id, usage.getAppName(), downloaded_ALLMB);
                        Toast.makeText(context, usage.getAppName() + " is updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public void writeToFile(JSONObject json) throws IOException {
        String file1 = Environment.getExternalStorageDirectory().getAbsolutePath();
        String filename = "app.txt";

        File f = new File(file1 + File.separator + filename);

        FileOutputStream fstream = new FileOutputStream(f, true);
        fstream.write(json.toString().getBytes());
        fstream.write("\r\n ".getBytes());
        fstream.close();
    }

    /*
     * Dohvaćanje veličine liste, tj broja item-a u listi
     */
    @Override
    public int getItemCount() {
        return usageList.size();
    }

    /*
     * Od čega se naš ViewHolder sastoji
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView downloaded;
        TextView uploaded;
        ProgressBar usage;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
