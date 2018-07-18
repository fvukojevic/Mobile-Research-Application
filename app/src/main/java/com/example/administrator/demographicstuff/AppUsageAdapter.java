package com.example.administrator.demographicstuff;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
        Long downloaded = usage.getDownloaded(NetworkUsageHelper.DATA_MB);
        if(downloaded > 1024)
            holder.downloaded.setText(usage.getDownloaded(NetworkUsageHelper.DATA_GB) + " GB");
        else if(downloaded < 5)
            holder.downloaded.setText(usage.getDownloaded(NetworkUsageHelper.DATA_KB) + " KB");
        else
            holder.downloaded.setText(usage.getDownloaded(NetworkUsageHelper.DATA_MB) + " MB");

        holder.name.setText(usage.getAppName());
        holder.usage.setProgress(Float.valueOf(usage.getDownloadPercentage() * 100f).intValue());

        if(downloaded > 10)
        {
            Cursor res = db.getAppUsage(android_id, usage.getAppName());
            if(res.getCount() == 0)
            {
                //Show message
                db.insertAppUsage(android_id, usage.getAppName(), downloaded);
                Toast.makeText(context, usage.getAppName() + " is now inserted", Toast.LENGTH_SHORT).show();
            }
            else{
                while(res.moveToNext())
                {
                    double old_usage = res.getDouble(3);
                    if(downloaded - old_usage > 10)
                    {
                        db.updateAppUsage(android_id, usage.getAppName(), downloaded);
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
