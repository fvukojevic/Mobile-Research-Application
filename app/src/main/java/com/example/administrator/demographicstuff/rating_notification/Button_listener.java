package com.example.administrator.demographicstuff.rating_notification;

import android.app.NotificationManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by ferdooo
 */
public class Button_listener extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(intent.getExtras().getInt("id"));


        String clicked = intent.toString();
        String subclicked = clicked.substring(92,95);

        switch (subclicked){
            case "342" :
                Toast.makeText(context, "Sorry to hear that", Toast.LENGTH_SHORT).show();
                break;
            case "474": Toast.makeText(context, "We will try to improve", Toast.LENGTH_SHORT).show();
                break;
            case "606" : Toast.makeText(context, "We can definitely do better!" , Toast.LENGTH_SHORT).show();
                break;
            case "738": Toast.makeText(context , "Almost perfect! " , Toast.LENGTH_SHORT).show();
                break;
            case "837" : Toast.makeText(context, "Perfect! Thank you", Toast.LENGTH_SHORT).show();
                break;
        }

        Toast.makeText(context, "Thanks for your feedback", Toast.LENGTH_SHORT).show();
    }
}
