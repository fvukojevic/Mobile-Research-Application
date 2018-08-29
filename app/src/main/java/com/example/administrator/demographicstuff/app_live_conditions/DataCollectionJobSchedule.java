package com.example.administrator.demographicstuff.app_live_conditions;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
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
import java.security.Provider;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class DataCollectionJobSchedule extends JobService {
    private static final String NETWORK_TYPE_2G = "2G";
    private static final String NETWORK_TYPE_3G = "3G";
    private static final String NETWORK_TYPE_4G = "4G";

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
        getLocation();
        map.put("aaupdated", "byTime");
        map.put("aaTimestamp", Calendar.getInstance().getTime().toString());
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
                            map.put("mobileTechnology", "2G");
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
                            map.put("mobileTechnology", NETWORK_TYPE_3G);
                            getMobileParameters();
                            return;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            networkType = NETWORK_TYPE_4G;
                            map.put("mobileTechnology", NETWORK_TYPE_4G);
                            getMobileParameters();
                    }
                }
            }
        }
    }

    public void getWiFiParameters() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
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
                map.put("cellId", String.valueOf(gsm.getCellIdentity().getCid()));
                map.put("MCC", String.valueOf(gsm.getCellIdentity().getMcc()));
                map.put("MNC", String.valueOf(gsm.getCellIdentity().getMnc()));
                map.put("mobileRSSI", String.valueOf(gsm.getCellSignalStrength().getDbm()));
                map.put("rfcn", String.valueOf(gsm.getCellIdentity().getArfcn()));
                map.put("SNR", "-");
                map.put("RSRP", "-");
                map.put("RSRQ", "-");
                map.put("TAC", String.valueOf(gsm.getCellIdentity().getLac()));
                map.put("CQI", "-");
                map.put("TA", String.valueOf(gsm.getCellSignalStrength().getTimingAdvance()));
                return;
            case NETWORK_TYPE_3G:
                try {
                    CellInfoWcdma wcdma = (CellInfoWcdma) cellinfo.get(0);
                    map.put("cellId", String.valueOf(wcdma.getCellIdentity().getCid()));
                    map.put("MCC", String.valueOf(wcdma.getCellIdentity().getMcc()));
                    map.put("MNC", String.valueOf(wcdma.getCellIdentity().getMnc()));
                    map.put("mobileRSSI", String.valueOf(wcdma.getCellSignalStrength().getDbm()));
                    map.put("rfcn", String.valueOf(wcdma.getCellIdentity().getUarfcn()));
                    map.put("SNR", "-");
                    map.put("RSRP", "-");
                    map.put("RSRQ", "-");
                    map.put("TAC", String.valueOf(wcdma.getCellIdentity().getLac()));
                    map.put("CQI", "-");
                    map.put("TA", "-");
                    map.put("ENodeB", String.valueOf(wcdma.getCellIdentity().getCid() >> 8));
                    map.put("PCI", String.valueOf(wcdma.getCellIdentity().getPsc()));
                } catch (ClassCastException e) {
                    CellInfoCdma cdma = (CellInfoCdma) cellinfo.get(0);
                    map.put("cellId", String.valueOf(cdma.getCellIdentity().getBasestationId()));
                    map.put("mobileRSSI", String.valueOf(cdma.getCellSignalStrength().getCdmaDbm()));
                }
                return;
            case NETWORK_TYPE_4G:
                CellInfoLte lte = (CellInfoLte) cellinfo.get(0);
                map.put("cellId", String.valueOf(lte.getCellIdentity().getCi()));
                map.put("MCC", String.valueOf(lte.getCellIdentity().getMcc()));
                map.put("MNC", String.valueOf(lte.getCellIdentity().getMnc()));
                map.put("mobileRSSI", String.valueOf(lte.getCellSignalStrength().getDbm()));
                map.put("rfcn", String.valueOf(lte.getCellIdentity().getEarfcn()));
                map.put("SNR", String.valueOf(lte.getCellSignalStrength().getRssnr()));
                map.put("RSRP", String.valueOf(lte.getCellSignalStrength().getRsrp()));
                map.put("RSRQ", String.valueOf(lte.getCellSignalStrength().getRsrq()));
                map.put("TAC", String.valueOf(lte.getCellIdentity().getTac()));
                map.put("CQI", String.valueOf(lte.getCellSignalStrength().getCqi()));
                if (lte.getCellSignalStrength().getCqi() > 30) {
                    map.put("CQI", "-1");
                }
                map.put("TA", String.valueOf(lte.getCellSignalStrength().getTimingAdvance()));
                map.put("ENodeB", String.valueOf(lte.getCellIdentity().getCi() >> 8));
                map.put("PCI", String.valueOf(lte.getCellIdentity().getPci()));
        }
    }

    public void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = null;
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        String provider;
        try {
            provider = locationManager.getBestProvider(crit, true);
            location = locationManager.getLastKnownLocation(provider);
            if (lastLocation == null) lastLocation = location;
        } catch (SecurityException | NullPointerException e) {
            e.printStackTrace();
        }
        if (location != null) {
            map.put("lat", String.valueOf(location.getLatitude()));
            map.put("long", String.valueOf(location.getLongitude()));
            map.put("alt", String.valueOf(location.getAltitude()));
            map.put("acc", String.valueOf(location.getAccuracy()));
            map.put("speed", String.valueOf(location.getSpeed()));
        }
    }
}

