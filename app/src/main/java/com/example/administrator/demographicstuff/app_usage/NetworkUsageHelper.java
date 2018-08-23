package com.example.administrator.demographicstuff.app_usage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NetworkUsageHelper {

    public static final int DATA_GB = 1;
    public static final int DATA_MB = 2;
    public static final int DATA_KB = 3;
    public static final int DATA_B = 4;

    private Context context;
    private NetworkStatsManager networkStatsManager;
    private PackageManager packageManager;

    public NetworkUsageHelper(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        }
        this.packageManager = context.getPackageManager();
        this.context = context;
    }

    public List<AppUsage> getWifiUsageList() {
        return getUsageList(ConnectivityManager.TYPE_WIFI);
    }

    public List<AppUsage> getMobileUsageList() {
        return getUsageList(ConnectivityManager.TYPE_MOBILE);
    }

    private List<AppUsage> getUsageList(final Integer networkType) {
        List<AppUsage> appUsages = getPackagesData();
        List<AppUsage> temp = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            long uploadedData = -1;
            long downloadedData = -1;
            long endTime = System.currentTimeMillis();
            String subscriberId = getSubscriberId(context, ConnectivityManager.TYPE_MOBILE);
            try {
                NetworkStats.Bucket bucket = networkStatsManager.querySummaryForDevice(networkType,
                        subscriberId,
                        0,
                        endTime);
                uploadedData = bucket.getTxBytes();
                downloadedData = bucket.getRxBytes();
            } catch (RemoteException e) {
                return new ArrayList<>();
            }
            for (AppUsage appUsage : appUsages) {
                try {
                    NetworkStats stats = networkStatsManager.queryDetailsForUid(networkType,
                            subscriberId,
                            0,
                            endTime,
                            appUsage.uid);
                    long txBytes = 0L;
                    long rxBytes = 0L;
                    NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                    while (stats.hasNextBucket()) {
                        stats.getNextBucket(bucket);
                        txBytes += bucket.getTxBytes();
                        rxBytes += bucket.getRxBytes();
                    }
                    stats.close();
                    if(txBytes < 5242880 && rxBytes < 5242880){
                        temp.add(appUsage);
                        continue;
                    }
                    appUsage.setUploadedBytes(txBytes);
                    appUsage.setUploadPercentage((float) txBytes / uploadedData);
                    appUsage.setDownloadedBytes(rxBytes);
                    appUsage.setDownloadPercentage((float) rxBytes / downloadedData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            appUsages.removeAll(temp);
            appUsages.sort(new Comparator<AppUsage>() {
                @Override
                public int compare(AppUsage appUsage, AppUsage t1) {
                    if(appUsage.getDownloaded(DATA_B) > t1.getDownloaded(DATA_B)){
                        return -1;
                    } else if (appUsage.getDownloaded(DATA_B) < t1.getDownloaded(DATA_B)){
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        } else {
            long uploadedData = -1;
            long downloadedData = -1;
            if (networkType == ConnectivityManager.TYPE_MOBILE) {
                uploadedData = TrafficStats.getMobileTxBytes();
                downloadedData = TrafficStats.getMobileRxBytes();
            } else {
                uploadedData = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
                downloadedData = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
            }
            for (AppUsage appUsage : appUsages) {
                long txBytes = TrafficStats.getUidTxBytes(appUsage.uid);
                appUsage.setUploadedBytes(txBytes);
                appUsage.setUploadPercentage((float) txBytes / uploadedData);
                long rxBytes = TrafficStats.getUidRxBytes(appUsage.uid);
                appUsage.setDownloadedBytes(rxBytes);
                appUsage.setDownloadPercentage((float) rxBytes / downloadedData);
            }
        }
        return appUsages;
    }

    private List<AppUsage> getPackagesData() {
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        Collections.sort(packageInfoList, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo o1, PackageInfo o2) {
                return (int) ((o2.lastUpdateTime - o1.lastUpdateTime) / 10);
            }
        });
        List<AppUsage> result = new ArrayList<>(packageInfoList.size());
        for (PackageInfo packageInfo : packageInfoList) {
            ApplicationInfo ain = null;
            try {
                ain = packageManager.getApplicationInfo(packageInfo.packageName, 0);

                PackageInfo targetPkgInfo = packageManager.getPackageInfo(
                        packageInfo.packageName, PackageManager.GET_SIGNATURES);
                PackageInfo sys = packageManager.getPackageInfo(
                        "android", PackageManager.GET_SIGNATURES);
                if (targetPkgInfo != null && targetPkgInfo.signatures != null && sys.signatures[0]
                        .equals(targetPkgInfo.signatures[0])) {
                    continue;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (packageManager.checkPermission(Manifest.permission.INTERNET,
                    packageInfo.packageName) == PackageManager.PERMISSION_DENIED) {
                continue;
            }
            AppUsage appUsage = new AppUsage();
            appUsage.setUid(packageInfo.applicationInfo.uid);
            appUsage.setPackageName(packageInfo.packageName);
            try {
                appUsage.setAppIcon(packageManager.getApplicationIcon(packageInfo.packageName));
            } catch (PackageManager.NameNotFoundException e) {
                appUsage.setAppIcon(null);
                e.printStackTrace();
            }
            ApplicationInfo ai = null;
            try {
                ai = packageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (ai == null) {
                appUsage.setAppName("");
            } else {
                CharSequence appName = packageManager.getApplicationLabel(ai);
                if (appName != null) {
                    appUsage.setAppName(appName.toString());
                } else {
                    appUsage.setAppName("");
                }
            }
            result.add(appUsage);
        }
        return result;
    }

    @SuppressLint("MissingPermission")
    private String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        }
        return "";
    }

    class AppUsage {
        private int uid;
        private String packageName;
        private String appName;
        private Drawable appIcon;
        private Float downloadPercentage;
        private Float uploadPercentage;
        private Long uploadedBytes;
        private Long downloadedBytes;

        public int getUid() {
            return uid;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public Drawable getAppIcon() {
            return appIcon;
        }

        public void setAppIcon(Drawable appIcon) {
            this.appIcon = appIcon;
        }

        public Float getDownloadPercentage() {
            return downloadPercentage;
        }

        public void setDownloadPercentage(Float downloadPercentage) {
            this.downloadPercentage = downloadPercentage;
        }

        public Float getUploadPercentage() {
            return uploadPercentage;
        }

        public void setUploadPercentage(Float uploadPercentage) {
            this.uploadPercentage = uploadPercentage;
        }

        public Float getUploaded(Integer unitType) {
            if (unitType.equals(DATA_B)) {
                return Float.valueOf(uploadedBytes);
            } else {
                if (uploadedBytes > 0) {
                    switch (unitType) {
                        case DATA_GB: // GB
                            return (uploadedBytes / 1073741824f);
                        case DATA_MB: // MB
                            return (uploadedBytes / 1048576f);
                        case DATA_KB: // KB
                            return (uploadedBytes / 1024f);
                    }
                } else {
                    return Float.valueOf(uploadedBytes);
                }
            }
            return Float.valueOf(uploadedBytes);
        }

        public void setUploadedBytes(Long uploadedBytes) {
            this.uploadedBytes = uploadedBytes;
        }

        public Float getDownloaded(Integer unitType) {
            if (unitType.equals(DATA_B)) {
                return Float.valueOf(downloadedBytes);
            } else {
                if (downloadedBytes > 0) {
                    switch (unitType) {
                        case DATA_GB: // GB
                            return (downloadedBytes / 1073741824f);
                        case DATA_MB: // MB
                            return (downloadedBytes / 1048576f);
                        case DATA_KB: // KB
                            return (downloadedBytes / 1024f);
                    }
                } else {
                    return Float.valueOf(downloadedBytes);
                }
            }
            return Float.valueOf(downloadedBytes);
        }

        public void setDownloadedBytes(Long downloadedBytes) {
            this.downloadedBytes = downloadedBytes;
        }
    }
}
