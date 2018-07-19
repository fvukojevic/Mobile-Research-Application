package com.example.administrator.demographicstuff;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LiveConditions extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE = 3;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 4;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 5;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 6;
    private static final String NETWORK_TYPE_2G = "2G";
    private static final String NETWORK_TYPE_3G = "3G";
    private static final String NETWORK_TYPE_4G = "4G";
    private String networkType = "";
    private Location lastLocation = null;
    @BindView(R.id.tvMobileTechnology)
    TextView tvMobileTechnology;
    @BindView(R.id.tvMCC)
    TextView tvMCC;
    @BindView(R.id.tvMNC)
    TextView tvMNC;
    @BindView(R.id.tvMobileRSSI)
    TextView tvMobileRSSI;
    @BindView(R.id.tvCellId)
    TextView tvCellId;
    @BindView(R.id.tvWiFiSSID)
    TextView tvWiFiSSID;
    @BindView(R.id.tvWiFiRSSI)
    TextView tvWiFiRSSI;
    @BindView(R.id.tvMobileStatus)
    TextView tvMobileStatus;
    @BindView(R.id.tvWiFiStatus)
    TextView tvWiFiStatus;
    @BindView(R.id.tvRSRP)
    TextView tvRSRP;
    @BindView(R.id.tvRSRQ)
    TextView tvRSRQ;
    @BindView(R.id.tvPCI)
    TextView tvPCI;
    @BindView(R.id.tvENodeB)
    TextView tvENodeB;
    @BindView(R.id.tvTimingAdvance)
    TextView tvTimingAdvance;
    @BindView(R.id.tvSNR)
    TextView tvSNR;
    @BindView(R.id.tvEarfcn)
    TextView tvRfcn;
    @BindView(R.id.tvTAC)
    TextView tvTAC;
    @BindView(R.id.tvTacName)
    TextView tvTacName;
    @BindView(R.id.tvRfcnName)
    TextView tvRfcnName;
    @BindView(R.id.tvPCIName)
    TextView tvPciName;
    @BindView(R.id.tvNodeName)
    TextView tvNodeName;
    @BindView(R.id.tvCqi)
    TextView tvCqi;
    @BindView(R.id.tvLocationLat)
    TextView tvLocationLat;
    @BindView(R.id.tvLocationLong)
    TextView tvLocationLong;
    @BindView(R.id.tvLocationAlt)
    TextView tvLocationAlt;
    @BindView(R.id.tvLocationAcc)
    TextView tvLocationAcc;
    @BindView(R.id.tvSpeed)
    TextView tvSpeed;


    ConnectivityManager connectivityManager;
    NetworkInfo activeNetworkInfo;
    NetworkCapabilities netcap;
    TelephonyManager telephonyManager;
    WifiManager wifiManager;
    Handler handler;
    DataCollectionService dataCollectionService;
    LocationManager locationManager;
    List<TextView> listOfMobileParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_conditions);
        ButterKnife.bind(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE);
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        netcap = connectivityManager != null ? connectivityManager.getNetworkCapabilities(connectivityManager != null ? connectivityManager.getActiveNetwork() : null) : null;
        telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        handler = new Handler();
        dataCollectionService = new DataCollectionService();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listOfMobileParameters = new LinkedList<>();
        listOfMobileParameters.addAll(Arrays.asList(tvCellId,tvCqi,tvENodeB,tvMCC,tvMNC,tvMobileRSSI,tvMobileTechnology,tvPCI,tvRfcn,tvRSRP,tvRSRQ,tvSNR,tvTAC,tvTimingAdvance));

        if (activeNetworkInfo == null) {
            tvMobileStatus.setText(R.string.nijeSpojeno);
            tvWiFiStatus.setText(R.string.nijeSpojeno);
        } else {
            startService(new Intent(this, DataCollectionService.class));
            getData();
            getLocation();
            handler.postDelayed(dataCollect, 1000);
        }
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            handler.postDelayed(dataCollect, 1000);
        }
    };

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
            if(activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                if (netcap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    tvMobileStatus.setText(R.string.nijeSpojeno);
                    for (TextView text : listOfMobileParameters) {
                        text.setText(R.string.noData);
                    }
                    getWiFiParameters();
                }
            }
        } else if (telephonyManager.isDataEnabled()){
            if(activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                if (netcap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    tvWiFiStatus.setText(R.string.nijeSpojeno);
                    tvWiFiSSID.setText(R.string.noData);
                    tvWiFiRSSI.setText(R.string.noData);
                    switch (activeNetworkInfo != null ? activeNetworkInfo.getSubtype() : -1) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                        case TelephonyManager.NETWORK_TYPE_GSM:
                            networkType = NETWORK_TYPE_2G;
                            tvMobileTechnology.setText(R.string.networkType2G);
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
                            tvMobileTechnology.setText(R.string.networkType3G);
                            getMobileParameters();
                            return;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            networkType = NETWORK_TYPE_4G;
                            tvMobileTechnology.setText(R.string.networkType4G);
                            getMobileParameters();
                    }
                }
            }
        } else {
            tvMobileStatus.setText(R.string.nijeSpojeno);
            tvWiFiStatus.setText(R.string.nijeSpojeno);
        }
    }


    public void getWiFiParameters() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        tvWiFiSSID.setText(wifiInfo.getSSID());
        tvWiFiRSSI.setText(String.valueOf(wifiInfo.getRssi()));
        tvWiFiStatus.setText(R.string.spojeno);
    }

    public void getMobileParameters() {
        List<CellInfo> cellinfo = null;
        try {
            cellinfo = telephonyManager != null ? telephonyManager.getAllCellInfo() : null;
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (cellinfo == null) return;
        tvMobileStatus.setText(R.string.spojeno);
        switch (networkType) {
            case NETWORK_TYPE_2G:
                CellInfoGsm gsm = (CellInfoGsm) cellinfo.get(0);
                tvCellId.setText(String.valueOf(gsm.getCellIdentity().getCid()));
                tvMCC.setText(String.valueOf(gsm.getCellIdentity().getMcc()));
                tvMNC.setText(String.valueOf(gsm.getCellIdentity().getMnc()));
                tvMobileRSSI.setText(String.format("%s dBm", String.valueOf(gsm.getCellSignalStrength().getDbm())));
                tvRfcn.setText(String.valueOf(gsm.getCellIdentity().getArfcn()));
                tvRfcnName.setText(R.string.arfcn);
                tvSNR.setText(R.string.noData);
                tvCqi.setText(R.string.noData);
                tvTimingAdvance.setText(String.valueOf(gsm.getCellSignalStrength().getTimingAdvance()));
                tvRSRQ.setText(R.string.noData);
                tvRSRP.setText(R.string.noData);
                tvTAC.setText(String.valueOf(gsm.getCellIdentity().getLac()));
                tvTacName.setText(R.string.lac);
                return;
            case NETWORK_TYPE_3G:
                try {
                    CellInfoWcdma wcdma = (CellInfoWcdma) cellinfo.get(0);
                    tvCellId.setText(String.valueOf(wcdma.getCellIdentity().getCid()));
                    tvMCC.setText(String.valueOf(wcdma.getCellIdentity().getMcc()));
                    tvMNC.setText(String.valueOf(wcdma.getCellIdentity().getMnc()));
                    tvMobileRSSI.setText(String.format("%s dBm", String.valueOf(wcdma.getCellSignalStrength().getDbm())));
                    tvRfcn.setText(String.valueOf(wcdma.getCellIdentity().getUarfcn()));
                    tvRfcnName.setText(R.string.uarfcn);
                    tvPCI.setText(String.valueOf(wcdma.getCellIdentity().getPsc()));
                    tvPciName.setText(R.string.psc);
                    tvNodeName.setText(R.string.nodeb);
                    tvENodeB.setText(String.valueOf(wcdma.getCellIdentity().getCid() >> 8));
                    tvSNR.setText(R.string.noData);
                    tvCqi.setText(R.string.noData);
                    tvRSRQ.setText(R.string.noData);
                    tvRSRP.setText(R.string.noData);
                    tvTimingAdvance.setText(R.string.noData);
                    tvTAC.setText(String.valueOf(wcdma.getCellIdentity().getLac()));
                    tvTacName.setText(R.string.lac);
                } catch (ClassCastException e) {
                    CellInfoCdma cdma = (CellInfoCdma) cellinfo.get(0);
                    tvCellId.setText(String.valueOf(cdma.getCellIdentity().getBasestationId()));
                    tvMCC.setText(R.string.noData);
                    tvMNC.setText(R.string.noData);
                    tvMobileRSSI.setText(String.valueOf(cdma.getCellSignalStrength().getCdmaDbm()));
                    tvRfcn.setText(R.string.noData);
                    tvSNR.setText(R.string.noData);
                    tvCqi.setText(R.string.noData);
                    tvRSRQ.setText(R.string.noData);
                    tvRSRP.setText(R.string.noData);
                }
                return;
            case NETWORK_TYPE_4G:
                CellInfoLte lte = (CellInfoLte) cellinfo.get(0);
                tvCellId.setText(String.valueOf(lte.getCellIdentity().getCi()));
                tvMCC.setText(String.valueOf(lte.getCellIdentity().getMcc()));
                tvMNC.setText(String.valueOf(lte.getCellIdentity().getMnc()));
                tvMobileRSSI.setText(String.format("%s dBm", String.valueOf(lte.getCellSignalStrength().getDbm())));
                tvRSRP.setText(String.format("%s dBm", String.valueOf(lte.getCellSignalStrength().getRsrp())));
                tvRSRQ.setText(String.format("%s dBm", String.valueOf(lte.getCellSignalStrength().getRsrq())));
                tvPCI.setText(String.valueOf(lte.getCellIdentity().getPci()));
                tvPciName.setText(R.string.pci);
                tvSNR.setText(String.format("%s dB", String.valueOf(lte.getCellSignalStrength().getRssnr())));
                tvRfcn.setText(String.valueOf(lte.getCellIdentity().getEarfcn()));
                tvRfcnName.setText(R.string.earfcn);
                tvTimingAdvance.setText(String.valueOf(lte.getCellSignalStrength().getTimingAdvance()));
                tvTAC.setText(String.valueOf(lte.getCellIdentity().getTac()));
                tvTacName.setText(R.string.tac);
                tvNodeName.setText(R.string.enodeb);
                tvENodeB.setText(String.valueOf(lte.getCellIdentity().getCi() >> 8));
                if (lte.getCellSignalStrength().getCqi() != Integer.MAX_VALUE) {
                    tvCqi.setText(String.valueOf(lte.getCellSignalStrength().getCqi()));
                }
        }
    }

    public void getLocation() {
        Location location = null;
        try {
            location = locationManager != null ? locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) : null;
            if (lastLocation == null) {
                lastLocation = location;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (location != null) {
            if (lastLocation.distanceTo(location) >= 20) {
                lastLocation = location;
            }
            tvLocationLat.setText(String.valueOf(location.getLatitude()));
            tvLocationLong.setText(String.valueOf(location.getLongitude()));
            tvLocationAlt.setText(String.valueOf(location.getAltitude()));
        }
    }


    public void updateLocation(Location location) {
        if (lastLocation == null) {
            lastLocation = location;
        }
        if (location != null) {
            if (lastLocation.distanceTo(location) >= 20) {
                lastLocation = location;
            }
            tvLocationLat.setText(String.valueOf(location.getLatitude()));
            tvLocationLong.setText(String.valueOf(location.getLongitude()));
            tvLocationAlt.setText(String.valueOf(location.getAltitude()));
            tvLocationAcc.setText(String.valueOf(location.getAccuracy()));
            tvSpeed.setText(String.valueOf(location.getSpeed()));
        }
    }
}