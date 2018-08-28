package com.example.administrator.demographicstuff.rating_notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.administrator.demographicstuff.FirstPageActivity;
import com.example.administrator.demographicstuff.R;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;

public class AlarmReceiver extends BroadcastReceiver{
    private static final String CHANNEL_ID = "123";

    /*
     * On Recive se poziva nakon određenog vremena postavljenog u FirstPageActivity
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, FirstPageActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(FirstPageActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        Notification.Builder builder = new Notification.Builder(context);

        /*
         * postavljanje notifikacije tako što joj dajemo remoteView koji je postavljen u FirstPageActivity-u
         */
        Notification notification = builder.setCustomBigContentView(FirstPageActivity.remoteViews)
                .setSmallIcon(R.mipmap.ericsson_logo)
                .setAutoCancel(true)
                .setContentIntent(FirstPageActivity.pendingIntent).build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "NotificationDemo",
                    IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(FirstPageActivity.notification_id, notification);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(intent.getExtras().getInt("id"));
    }
}