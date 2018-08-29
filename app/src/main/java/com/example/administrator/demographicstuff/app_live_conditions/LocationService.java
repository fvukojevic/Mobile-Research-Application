package com.example.administrator.demographicstuff.app_live_conditions;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.administrator.demographicstuff.R;

public class LocationService extends Service {
    private static final int LOCATION_DATA_COLLECTION_SCHEDULE_SERVICE = 8;
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 500;
    private static final int LOCATION_DISTANCE = 10;
    private Location lastLocation;


    //TODO Use Google Location API
    private class LocationListener implements android.location.LocationListener {
        String provider;
        public LocationListener(String provider) {
            this.provider = provider;
            lastLocation = null;
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i("Location Changed", location.toString());
            collectData(location, provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    public void collectData(Location location, String provider) {
        if (lastLocation == null) {
            lastLocation = location;
            Log.i("lastLocationWasNull", location.toString());
            startService(provider);
        }
        if (lastLocation.distanceTo(location) >= 20) {
            lastLocation = location;
            Log.i("lastLocationGT20", lastLocation.toString());
            startService(provider);
        }
    }


    public void startService(String provider){
        JobScheduler jobScheduler;
        JobInfo jobInfo;
        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(this, LocationDataCollectionJobSchedule.class);
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("PROVIDER", provider);
        jobInfo = new JobInfo.Builder(LOCATION_DATA_COLLECTION_SCHEDULE_SERVICE, componentName)
                .setExtras(bundle)
                .setRequiresStorageNotLow(true)
                .setMinimumLatency(20)
                .setOverrideDeadline(200)
                .setRequiredNetworkType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        if (jobScheduler != null) {
            jobScheduler.schedule(jobInfo);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        NotificationChannel channel = new NotificationChannel("CuedLocationForeground",
                "Cued location service",
                NotificationManager.IMPORTANCE_LOW);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "CuedLocationForeground")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Location based data collection").build();
        startForeground(1, notification);
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (SecurityException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(".restartService");
        sendBroadcast(broadcastIntent);
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}