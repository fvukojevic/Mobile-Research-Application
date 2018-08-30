package com.example.administrator.demographicstuff.app_live_conditions;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class LocationDataCollectionJobSchedule extends JobService{
    private static final String NETWORK_TYPE_2G = "2G";
    private static final String NETWORK_TYPE_3G = "3G";
    private static final String NETWORK_TYPE_4G = "4G";
    private static final String NETWORK_TYPE_WIFI = "Wi-Fi";

    JobParameters params;
    ConnectivityManager connectivityManager;
    NetworkInfo activeNetworkInfo;
    NetworkCapabilities netcap;
    TelephonyManager telephonyManager;
    WifiManager wifiManager;
    HashMap<String, String> map;
    String networkType;
    LocationManager locationManager;
    Location lastLocation;
    Location location;
    JSONObject json;
    boolean isWorking = false;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        params = jobParameters;
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        netcap = connectivityManager != null ? connectivityManager.getNetworkCapabilities(connectivityManager != null ? connectivityManager.getActiveNetwork() : null) : null;
        telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        map = new HashMap<>();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        lastLocation = null;
        location = null;

        isWorking = true;
        new Thread(new Runnable() {
            public void run() {
                dataCollect(jobParameters);
            }
        }).start();
        return isWorking;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        jobFinished(jobParameters, isWorking);
        return false;
    }

    public void dataCollect(JobParameters params) {
        getData();
        getLocation(params.getExtras().get("PROVIDER").toString());
        map.put("timestamp", Calendar.getInstance().getTime().toString());
        json = new JSONObject(map);
        try {
            writeToFile(json);
        } catch (IOException e) {
            Log.d("Write to file", "Failed");
            e.printStackTrace();
        }
        map.clear();
        isWorking = false;
        jobFinished(params, isWorking);

    }


    public void writeToFile(JSONObject json) throws IOException {
        FileOutputStream stream;
        String file1 = Environment.getExternalStorageDirectory().getAbsolutePath();
        String filename = "data.txt";

        File f = new File(file1 + File.separator + filename);

        FileOutputStream fstream = new FileOutputStream(f, true);
        fstream.write(json.toString().getBytes());
        fstream.write("\r\n ".getBytes());
        fstream.close();
    }

    private String readFromFile(Context context) {
        String ret = "";
        try {
            InputStream inputStream = context.openFileInput("data.txt");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
    }

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
                    getWiFiParameters();
                }
            }
        } else if (telephonyManager.isDataEnabled()) {
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                if (netcap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    switch (activeNetworkInfo != null ? activeNetworkInfo.getSubtype() : -1) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                        case TelephonyManager.NETWORK_TYPE_GSM:
                            networkType = NETWORK_TYPE_2G;
                            map.put("technology", "2G");
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
                            networkType = NETWORK_TYPE_3G;
                            map.put("technology", NETWORK_TYPE_3G);
                            getMobileParameters();
                            return;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            networkType = NETWORK_TYPE_4G;
                            map.put("technology", NETWORK_TYPE_4G);
                            getMobileParameters();
                    }
                }
            }
        }
    }

    public void getWiFiParameters() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        map.put("technology", NETWORK_TYPE_WIFI);
        map.put("WiFiSSID", String.valueOf(wifiInfo.getSSID()));
        map.put("WiFiRSSI", String.valueOf(wifiInfo.getRssi()));
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
                map.put("cellid", String.valueOf(gsm.getCellIdentity().getCid()));
                map.put("mcc", String.valueOf(gsm.getCellIdentity().getMcc()));
                map.put("mnc", String.valueOf(gsm.getCellIdentity().getMnc()));
                map.put("rssi", String.valueOf(gsm.getCellSignalStrength().getDbm()));
                map.put("xarfcn", String.valueOf(gsm.getCellIdentity().getArfcn()));
                map.put("snr", "");
                map.put("rsrp", "");
                map.put("rsrq", "");
                map.put("tac", String.valueOf(gsm.getCellIdentity().getLac()));
                map.put("cqi", "");
                map.put("ta", String.valueOf(gsm.getCellSignalStrength().getTimingAdvance()));
                return;
            case NETWORK_TYPE_3G:
                try {
                    CellInfoWcdma wcdma = (CellInfoWcdma) cellinfo.get(0);
                    map.put("cellid", String.valueOf(wcdma.getCellIdentity().getCid()));
                    map.put("mcc", String.valueOf(wcdma.getCellIdentity().getMcc()));
                    map.put("mnc", String.valueOf(wcdma.getCellIdentity().getMnc()));
                    map.put("rssi", String.valueOf(wcdma.getCellSignalStrength().getDbm()));
                    map.put("xarfcn", String.valueOf(wcdma.getCellIdentity().getUarfcn()));
                    map.put("snr", "");
                    map.put("rsrp", "");
                    map.put("rsrq", "");
                    map.put("tac", String.valueOf(wcdma.getCellIdentity().getLac()));
                    map.put("cqi", "");
                    map.put("ta", "");
                    map.put("enodeb", String.valueOf(wcdma.getCellIdentity().getCid() >> 8));
                    map.put("pci", String.valueOf(wcdma.getCellIdentity().getPsc()));
                } catch (ClassCastException e) {
                    CellInfoCdma cdma = (CellInfoCdma) cellinfo.get(0);
                    map.put("cellid", String.valueOf(cdma.getCellIdentity().getBasestationId()));
                    map.put("rssi", String.valueOf(cdma.getCellSignalStrength().getCdmaDbm()));
                }
                return;
            case NETWORK_TYPE_4G:
                CellInfoLte lte = (CellInfoLte) cellinfo.get(0);
                map.put("cellid", String.valueOf(lte.getCellIdentity().getCi()));
                map.put("mcc", String.valueOf(lte.getCellIdentity().getMcc()));
                map.put("mnc", String.valueOf(lte.getCellIdentity().getMnc()));
                map.put("rssi", String.valueOf(lte.getCellSignalStrength().getDbm()));
                map.put("xarfcn", String.valueOf(lte.getCellIdentity().getEarfcn()));
                map.put("snr", String.valueOf(lte.getCellSignalStrength().getRssnr()));
                map.put("rsrp", String.valueOf(lte.getCellSignalStrength().getRsrp()));
                map.put("rsrq", String.valueOf(lte.getCellSignalStrength().getRsrq()));
                map.put("tac", String.valueOf(lte.getCellIdentity().getTac()));
                map.put("cqi", String.valueOf(lte.getCellSignalStrength().getCqi()));
                if (lte.getCellSignalStrength().getCqi() > 30) {
                    map.put("cqi", "-1");
                }
                map.put("ta", String.valueOf(lte.getCellSignalStrength().getTimingAdvance()));
                map.put("enodeb", String.valueOf(lte.getCellIdentity().getCi() >> 8));
                map.put("pci", String.valueOf(lte.getCellIdentity().getPci()));
        }
    }

    public void getLocation(String provider) {
        Log.i("Provider id LocationDataCollection", provider);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            location = locationManager.getLastKnownLocation(provider);
            if (lastLocation == null) lastLocation = location;
        } catch (SecurityException | NullPointerException e) {
            e.printStackTrace();
        }

        if (location != null) {
            map.put("latitude", String.valueOf(location.getLatitude()));
            map.put("longitude", String.valueOf(location.getLongitude()));
            map.put("altitude", String.valueOf(location.getAltitude()));
            map.put("acuracy", String.valueOf(location.getAccuracy()));
            map.put("speed", String.valueOf(location.getSpeed()));

        }
    }
}

