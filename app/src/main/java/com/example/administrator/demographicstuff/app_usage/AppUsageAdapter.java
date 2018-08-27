package com.example.administrator.demographicstuff.app_usage;

import android.content.Context;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.List;

public class AppUsageAdapter extends RecyclerView.Adapter<AppUsageAdapter.ViewHolder> {

    public AppUsageDatabase db;
    public List<NetworkUsageHelper.AppUsage> usageList = new ArrayList<>();
    private String android_id;
    private Context context;

    public AppUsageAdapter(Context context, String android_id) {
        this.context = context;
        this.android_id = android_id;
    }

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NetworkUsageHelper.AppUsage usage = usageList.get(position);
        holder.icon.setImageDrawable(usage.getAppIcon());
        holder.name.setText(usage.getAppName());
        float uploaded = usage.getUploaded(NetworkUsageHelper.DATA_MB);
        if (uploaded > 15000f) {
            uploaded = usage.getUploaded(NetworkUsageHelper.DATA_GB);
            holder.uploaded.setText("U: " + Math.round(uploaded) + " GB");
        } else {
            holder.uploaded.setText("U: " + Math.round(uploaded) + " MB");
        }
        float downloaded = usage.getDownloaded(NetworkUsageHelper.DATA_MB);
        float downloaded_ALLMB = usage.getDownloaded(NetworkUsageHelper.DATA_MB);
        if (downloaded > 15000f) {
            downloaded = usage.getDownloaded(NetworkUsageHelper.DATA_GB);
            holder.downloaded.setText("D: " + Math.round(downloaded) + " GB");
        } else {
            holder.downloaded.setText("D: " + Math.round(downloaded) + " MB");
        }
        holder.name.setText(usage.getAppName());
        holder.usage.setProgress(Float.valueOf(usage.getDownloadPercentage() * 100f).intValue());

        if(downloaded_ALLMB > 10)
        {
            Cursor res = db.getAppUsage(android_id, usage.getAppName());
            if(res.getCount() == 0)
            {
                //Json for server database
                JSONObject postData = new JSONObject();
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                try {
                    postData.put("appuserid", FirstPageActivity.user_id);
                    postData.put("name", usage.getAppName());
                    postData.put("data", downloaded_ALLMB);
                    postData.put("timestamp", ts);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("AppUsage", postData.toString());

                //Show message
                db.insertAppUsage(android_id, usage.getAppName(), downloaded_ALLMB);
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
                            postData.put("appuserid", FirstPageActivity.user_id);
                            postData.put("name", usage.getAppName());
                            postData.put("data", downloaded_ALLMB);
                            postData.put("timestamp", ts);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("AppUsage", postData.toString());

                        db.updateAppUsage(android_id, usage.getAppName(), downloaded_ALLMB);
                        Toast.makeText(context, usage.getAppName() + " is updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return usageList.size();
    }

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
