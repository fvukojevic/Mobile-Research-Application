package com.example.administrator.demographicstuff.app_map;

import android.content.Context;


import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.example.administrator.demographicstuff.rating_notification.CustomInfoWindowAdapter;
import com.example.administrator.demographicstuff.R;
import com.example.administrator.demographicstuff.app_tickets.TicketNewDatabase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//imports for cluster manager
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import com.example.administrator.demographicstuff.models.PlaceInfo;

import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

class OwnIconRendered extends DefaultClusterRenderer<MapActivity.MyItem> {

    public OwnIconRendered(Context context, GoogleMap map,
                           ClusterManager<MapActivity.MyItem> clusterManager) {
        super(context, map, clusterManager);
    }

    /*
        Funkcija naprvaljena za nadjačavanje Clustererove
        Ono što radi je mijenja defaultni marker
        sa onim koji mi sami odaberemo,
        snipper i title dobiva iz klade MyItem koja se nalazi u MapActivity-u
     */
    @Override
    protected void onBeforeClusterItemRendered(MapActivity.MyItem item, MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ticket_icon));
        markerOptions.snippet(item.getSnippet());
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}