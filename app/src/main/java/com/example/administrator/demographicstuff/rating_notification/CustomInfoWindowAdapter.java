package com.example.administrator.demographicstuff.rating_notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.demographicstuff.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/*
 * Created by FVukojević.
 */

/*
 * Ova klasa osigurava i nadjačava InfoWindow u Google Mapi
 * Pronašao sam je na android developers stranici u sample-ima
 * Manje promjene su napravljene da izgleda kako izgleda (nije ista kao verzija na int.)
 */
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContext;

    public CustomInfoWindowAdapter(Context mContext) {
        this.mContext = mContext;
        this.mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window,null);
    }

    private void renderWindowText(Marker marker, View view)
    {
        String title = marker.getTitle();
        TextView mTitle = view.findViewById(R.id.title);

        String snippet = marker.getSnippet();
        TextView mSnippet = view.findViewById(R.id.snippet);

        if(!title.equals("")){
            mTitle.setText(title);
        }
        if(!snippet.equals("")){
            mSnippet.setText(snippet);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }
}