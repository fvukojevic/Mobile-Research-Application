package com.example.administrator.demographicstuff.app_live_conditions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationDataCollectionServiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, LocationService.class));
    }
}


