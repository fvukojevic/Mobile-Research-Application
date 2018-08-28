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

        /*
         * Kada kliknemo na ImageButton u našoj rating notifikaciji zatvara tu notifikacjiju
         * DOES NOT WORK ATM
         */
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(intent.getExtras().getInt("id"));


        String clicked = intent.toString();
        String subclicked = clicked.substring(92,95);

        /*
         * Ovisno na koji smo ImageButton kliknuli, na osnovu njegove lokacije saznajemo o kojemu je riječ
         * Trenutno nisam mogao nači lakši način s obzirom da koristim CustomNotification view
         * Što otežava način saznanja na koji je button klknuto
         * Treba dodati funkcionalnost spremanja na bazu ovisno o kliknutom
         */
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

        /*
         * Povratna poruka na kraju svakog ocijenjivanja aplikacije
         */
        Toast.makeText(context, "Thanks for your feedback", Toast.LENGTH_SHORT).show();
    }
}
