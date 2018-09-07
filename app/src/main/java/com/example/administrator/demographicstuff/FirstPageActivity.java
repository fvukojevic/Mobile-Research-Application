package com.example.administrator.demographicstuff;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.demographicstuff.app_demographic.DemograficDatabase;
import com.example.administrator.demographicstuff.app_demographic.MainActivity;
import com.example.administrator.demographicstuff.app_live_conditions.DataCollectionJobSchedule;
import com.example.administrator.demographicstuff.app_live_conditions.LocationService;
import com.example.administrator.demographicstuff.app_tickets.TicketNewDatabase;
import com.example.administrator.demographicstuff.app_usage.AppUsageDatabase;
import com.example.administrator.demographicstuff.rating_notification.AlarmReceiver;
import com.example.administrator.demographicstuff.send_data.SendNetworkData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FirstPageActivity extends AppCompatActivity {
    private static TicketNewDatabase tb;
    private static AppUsageDatabase ab;
    private static DemograficDatabase db;
    public String android_id;
    @BindView(R.id.create_ticket2)
    TextView showLiveConditions;
    @BindView(R.id.apps)
    TextView appUsage;
    @BindView(R.id.create_ticket)
    Button create_ticket;
    @BindView(R.id.show_tickets)
    Button show_tickets;
    @BindView(R.id.ticketsList)
    TextView tickets;
    @BindView(R.id.show_wifi)
    Button show_wifi;
    @BindView(R.id.show_map)
    Button show_map;
    @BindView(R.id.rssi_rsrp)
    TextView rssi_rsrp;
    @BindView(R.id.networkInfo)
    TextView networkInfo;
    @BindView(R.id.typeNetwork)
    TextView typeNetwork;
    @BindView(R.id.latInfo)
    TextView latInfo;
    @BindView(R.id.longInfo)
    TextView longInfo;
    @BindView(R.id.altInfo)
    TextView altInfo;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;


    //For custom notification
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    public static int notification_id;
    public static RemoteViews remoteViews;
    private Context context;
    public static PendingIntent pendingIntent;
    public StringBuffer buffer, app_buffer;
    //<--              -->//


    //For google maps
    private static final int ERROR_DIALOG_REQUEST = 9001;

    ConnectivityManager connectivityManager;

    private static final String NETWORK_TYPE_2G = "2G";
    private static final String NETWORK_TYPE_3G = "3G";
    private static final String NETWORK_TYPE_4G = "4G";
    private static final String TYPE_WIFI = "Network - WiFi:\n";
    private static final String TYPE_MOBILE = "Network - Mobile:\n";
    private String networkType = "";
    private String typeNet = "";
    private static final int DATA_COLLECTION_SCHEDULE_SERVICE = 7;
    private static final int SEND_DATA_TO_SERVER = 8;
    private static final int REQUEST_INTERNET_PERMISSION = 16;
    private static final int MULTIPLE_PERMISSIONS = 17;
    private static final int TIME_UPDATE = 1200000;
    private static final int SEND_DATA_INTERVAL = 3600000;
    private Location lastLocation = null;

    public static int user_id;

    NetworkInfo activeNetworkInfo;
    NetworkCapabilities netcap;
    TelephonyManager telephonyManager;
    WifiManager wifiManager;
    List<TextView> listOfMobileParameters;
    LocationManager locationManager;
    Location location;
    Handler handler;
    JobScheduler jobScheduler;
    JobInfo dataCollectionJobInfo, sendDataJobInfo;
    Criteria crit;
    ComponentName dataCollectionComponent, sendDataComponent;
    SharedPreferences sharedPreferences;
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE};


    public static String real_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);
        tb = new TicketNewDatabase(FirstPageActivity.this);
        ab = new AppUsageDatabase(FirstPageActivity.this);
        db = new DemograficDatabase(FirstPageActivity.this);
        ButterKnife.bind(this);

        //getting id-s for local and server database
        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Cursor res = db.getMyId();
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            user_id = Integer.parseInt(res.getString(0));
        }
        Log.d("MY ID", "onCreate: MY ID is " + user_id);
        //<--                      -->//

        //<-- GETTING CUSTOM NOTIFICATION LAYOUT -->//
        //TODO get permission as done in MainActivity (jako bitno)
        context = FirstPageActivity.this;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(context);

        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        remoteViews.setImageViewResource(R.id.notif_icon, R.mipmap.ic_launcher);
        remoteViews.setTextViewText(R.id.notif_title, "Rate us!");

        notification_id = (int) System.currentTimeMillis();

        Intent button_intent = new Intent("button_click");
        button_intent.putExtra("id", notification_id);
        PendingIntent button_pending_event = PendingIntent.getBroadcast(context, 123,
                button_intent, 0);

        remoteViews.setOnClickPendingIntent(R.id.rtg1, button_pending_event);
        remoteViews.setOnClickPendingIntent(R.id.rtg2, button_pending_event);
        remoteViews.setOnClickPendingIntent(R.id.rtg3, button_pending_event);
        remoteViews.setOnClickPendingIntent(R.id.rtg4, button_pending_event);
        remoteViews.setOnClickPendingIntent(R.id.rtg5, button_pending_event);

        Intent notification_intent = new Intent(context, FirstPageActivity.class);
        pendingIntent = PendingIntent.getActivity(context, 0, notification_intent, 0);
        //<--     end of custom notification    -->//

        //<-- alarm menager -->//
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 500);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
        //<-- End of alarm menager -->//

        if (checkPermissions()) {
            start();
        }


    }

    public boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for(int result: grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            return;
                        }
                    }
                    start();
                }
            }
        }
    }

    public void start() {
        new SetClickListeners().execute();
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        boolean isConnected = activeNetworkInfo != null &&
                activeNetworkInfo.isConnectedOrConnecting();
        netcap = connectivityManager != null ? connectivityManager.getNetworkCapabilities(connectivityManager != null ? connectivityManager.getActiveNetwork() : null) : null;
        telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        listOfMobileParameters = new LinkedList<>();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        handler = new Handler();

        startForegroundService(new Intent(this, LocationService.class));

        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        dataCollectionComponent = new ComponentName(this, DataCollectionJobSchedule.class);
        dataCollectionJobInfo = new JobInfo.Builder(DATA_COLLECTION_SCHEDULE_SERVICE, dataCollectionComponent)
                .setPeriodic(TIME_UPDATE, JobInfo.getMinFlexMillis())
                .setPersisted(true)
                .setRequiresBatteryNotLow(true)
                .build();
        jobScheduler.schedule(dataCollectionJobInfo);

        sendDataComponent = new ComponentName(this, SendNetworkData.class);
        sendDataJobInfo = new JobInfo.Builder(SEND_DATA_TO_SERVER, sendDataComponent)
                .setPeriodic(SEND_DATA_INTERVAL)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresBatteryNotLow(true)
                .build();

        String file1 = Environment.getExternalStorageDirectory().getAbsolutePath();
        String filename = "data.txt";
        File f = new File(file1 + File.separator + filename);
        if (!f.exists()) {
            FileOutputStream fstream;
            try {
                fstream = new FileOutputStream(f, true);
                fstream.write("".getBytes());
                fstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FirstPageActivity.this, new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET_PERMISSION);
            return;
        } else {
            jobScheduler.schedule(sendDataJobInfo);
        }

        getData();
        getLocation();
        handler.postDelayed(dataCollect, 1000);


    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(dataCollect);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handler.postDelayed(dataCollect, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        scrollView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

    }

    private Runnable dataCollect = new Runnable() {
        @Override
        public void run() {
            getData();
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    updateLocation(location);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };

            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            handler.postDelayed(dataCollect, 1000);
        }
    };


    public boolean isServicesOK() {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(FirstPageActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(FirstPageActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FirstPageActivity.this);

        builder.setMessage("Are you sure you want to Exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirstPageActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private class SetClickListeners extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //dohvacanje najkoristenijih aplikacija
            String data = "No Apps found";
            Cursor res = ab.getMostUsedApps(getApplicationContext().getSharedPreferences(getString(R.string.APP_USER_PREFERENCES), Context.MODE_PRIVATE).getString("APP_USER_ID", ""));
            real_id = getApplicationContext().getSharedPreferences(getString(R.string.APP_USER_PREFERENCES), Context.MODE_PRIVATE).getString("APP_USER_ID", "");
            app_buffer = new StringBuffer();
            while (res.moveToNext()) {
                app_buffer.append(res.getString(2) + "\n");
                app_buffer.append(res.getString(3) + " MB\n");
                app_buffer.append("\n");
            }
            //show all data
            //TODO this button crashes "Only the original thread that created a view hierarchy can touch its views." (jako bitno)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appUsage.setText(app_buffer.toString());
                }
            });


            //Otvaranje prozora za pravljenje novog ticketa
            create_ticket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(".CreateTicketActivity");
                    startActivity(intent);
                }
            });

            //prelazak na mapu
            if (isServicesOK()) {
                show_map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(".MapActivity");
                        startActivity(intent);
                    }
                });
            }

            //dohvacanje wifi usage-a
            show_wifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressBar.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                    Intent intent = new Intent(".AppCombinedUsage");
                    startActivity(intent);
                }
            });

            showLiveConditions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(".LiveConditions");
                    startActivity(intent);
                }
            });

            //dohvacanje svih Ticketa
            show_tickets.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Cursor res = tb.getMyTickets(getApplicationContext().getSharedPreferences(getString(R.string.APP_USER_PREFERENCES), Context.MODE_PRIVATE).getString("APP_USER_ID", ""));
                    if (res.getCount() == 0) {
                        //Show message
                        showMessage("Empty", "No tickets found");
                        return;
                    } else {
                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            buffer.append("ID: " + res.getString(0) + "\n");
                            buffer.append("Android_id: " + res.getString(1) + "\n");
                            buffer.append("Category: " + res.getString(2) + "\n");
                            buffer.append("Subcategory: " + res.getString(3) + "\n");
                            buffer.append("Frequency: " + res.getString(4) + "\n");
                            buffer.append("Question: " + res.getString(5) + "\n");
                            buffer.append("Date: " + res.getString(6) + "\n");
                            buffer.append("Time: " + res.getString(7) + "\n");
                            buffer.append("Long: " + res.getString(8) + "\n");
                            buffer.append("Lat: " + res.getString(9) + "\n");
                            buffer.append("\n");
                        }

                        //show all data
                        showMessage("Data", buffer.toString());
                    }
                }
            });

            //dohvacanje prva tri ticketa
            Cursor res3 = tb.getMyThreeTickets(getApplicationContext().getSharedPreferences(getString(R.string.APP_USER_PREFERENCES), Context.MODE_PRIVATE).getString("APP_USER_ID", ""));
            if (res3.getCount() == 0) {
                //Show message
                //TODO fix line below :"Only the original thread that created a view hierarchy can touch its views." (jako bitno)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tickets.setText("No tickets found");
                    }
                });
            } else {
                buffer = new StringBuffer();
                while (res3.moveToNext()) {
                    buffer.append("ID: " + res3.getString(0) + " - " + res3.getString(2) + "\n");
                    buffer.append("Date: " + res3.getString(6) + "\n");
                    buffer.append("\n");
                }

                //show all data
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tickets.setText(buffer.toString());
                    }
                });

            }
            return null;
        }

    }

    //prikaz svih tiketaa u Alert Dialogu
    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }


    //prikaz informacija o mrezi
    public void getData() {
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        netcap = connectivityManager != null ? connectivityManager.getNetworkCapabilities(connectivityManager != null ? connectivityManager.getActiveNetwork() : null) : null;
        telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        getNetworkType();
    }

    public void getNetworkType() {
        netcap = connectivityManager != null ? connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork()) : null;
        activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        if (wifiManager.isWifiEnabled()) {
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                if (netcap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    for (TextView text : listOfMobileParameters) {
                        text.setText(R.string.noData);
                    }
                    getWiFiParameters();
                }
            }
        } else if (telephonyManager.isDataEnabled()) {
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                if (netcap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    networkInfo.setText(R.string.noData);
                    rssi_rsrp.setText(R.string.noData);
                    switch (activeNetworkInfo != null ? activeNetworkInfo.getSubtype() : -1) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                        case TelephonyManager.NETWORK_TYPE_GSM:
                            typeNet = TYPE_MOBILE;
                            networkType = NETWORK_TYPE_2G;
                            networkInfo.setText(R.string.networkType2G);
                            typeNetwork.setText(R.string.mobile);
                            getMobileParameters();
                            return;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                        case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                            typeNet = TYPE_MOBILE;
                            networkType = NETWORK_TYPE_3G;
                            networkInfo.setText(R.string.networkType3G);
                            typeNetwork.setText(R.string.mobile);
                            getMobileParameters();
                            return;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            typeNet = TYPE_MOBILE;
                            networkType = NETWORK_TYPE_4G;
                            networkInfo.setText(R.string.networkType4G);
                            typeNetwork.setText(R.string.mobile);
                            getMobileParameters();
                    }
                }
            }
        } else {
            return;
        }
    }

    public void getWiFiParameters() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        typeNet = TYPE_WIFI;
        typeNetwork.setText(R.string.WiFi);
        networkInfo.setText(wifiInfo.getSSID() + "\n");
        rssi_rsrp.setText("RSSI: " + String.valueOf(wifiInfo.getRssi()));
    }

    public void getMobileParameters() {
        List<CellInfo> cellinfo = null;
        try {
            cellinfo = telephonyManager != null ? telephonyManager.getAllCellInfo() : null;
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (cellinfo == null) return;
        switch (networkType) {
            case NETWORK_TYPE_2G:
                CellInfoGsm gsm = (CellInfoGsm) cellinfo.get(0);
                rssi_rsrp.setText("\nRSRP: " + String.format("%s dBm", String.valueOf(gsm.getCellSignalStrength().getDbm())));
                return;
            case NETWORK_TYPE_3G:
                try {
                    CellInfoWcdma wcdma = (CellInfoWcdma) cellinfo.get(0);
                    rssi_rsrp.setText("\nRSRP: " + String.format("%s dBm", String.valueOf(wcdma.getCellSignalStrength().getDbm())));
                } catch (ClassCastException e) {
                    CellInfoCdma cdma = (CellInfoCdma) cellinfo.get(0);
                    rssi_rsrp.setText(String.valueOf(cdma.getCellSignalStrength().getCdmaDbm()));
                }
                return;
            case NETWORK_TYPE_4G:
                CellInfoLte lte = (CellInfoLte) cellinfo.get(0);
                rssi_rsrp.setText("\nRSRP: " + String.format("%s dBm", String.valueOf(lte.getCellSignalStrength().getDbm())));
        }
    }

    public void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        location = null;
        crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        String provider;
        try {
            provider = locationManager != null ? locationManager.getBestProvider(crit, true) : null;
            if (provider == null) {
                provider = LocationManager.NETWORK_PROVIDER;
            }
            location = locationManager != null ? locationManager.getLastKnownLocation(provider) : null;
            if (lastLocation == null) lastLocation = location;
        } catch (SecurityException | NullPointerException e) {
            e.printStackTrace();
        }
        if (location != null) {
            latInfo.setText(String.valueOf(location.getLatitude()));
            longInfo.setText(String.valueOf(location.getLongitude()));
            altInfo.setText(String.valueOf(location.getAltitude()));
        }
    }


    public void updateLocation(Location location) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        location = null;
        crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        String provider;
        try {
            provider = locationManager.getBestProvider(crit, true);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastLocation == null) lastLocation = location;
        } catch (SecurityException | NullPointerException e) {
            e.printStackTrace();
        }

        if (location != null) {
            latInfo.setText(String.valueOf(location.getLatitude()));
            longInfo.setText(String.valueOf(location.getLongitude()));
            altInfo.setText(String.valueOf(location.getAltitude()));
        }
    }

}

