package com.example.administrator.demographicstuff.app_map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.demographicstuff.FirstPageActivity;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//imports for cluster manager
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import com.example.administrator.demographicstuff.models.PlaceInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by FVukojević.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener{

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        //<-- Provjera permisija lokacije -->//
        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
        //<-- Postavljanje Marker Clusterera na markere -->//
        setUpClusterer();
    }

    private static final String TAG = "MapActivity";

    //<-- Permisije + DEFAULT ZOOM aplikacije
    //<-- Također i lat_lng bounds koji osigurava da karta obuhvaća cijelu zemlju
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));


    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps, mInfo, mTickets, mSignals;


    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private Marker mMarker;
    private static TicketNewDatabase tb;
    public static int ticket_tester = 0;
    public static int signal_tester = 0;
    public InputStream isr;
    public String result;

    // Inicijalizacija cluster managera.
    private ClusterManager<MyItem> mClusterManager;


    //funckija za postavljanje map clusterera
    private void setUpClusterer() {
        mClusterManager = new ClusterManager<>(this, mMap);
        mClusterManager.setAnimation(false);
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                Toast.makeText(MapActivity.this, "Zoom to see markers", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mClusterManager.setRenderer(new OwnIconRendered(MapActivity.this.getApplicationContext(), mMap , mClusterManager));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
    }

    //Dodavanje testnih item-a u map clusterer
    //Koristim ovo radi testiranja funckionalnosti
    //uzeo sam koordinate mijesta u BiH jer ne želim ugrođavat testiranjem na koordinatama RH
    private void addItems() {

        LatLng stariTrzni = new LatLng(43.3453916, 17.8009922);
        mClusterManager.addItem(new MyItem(stariTrzni.latitude, stariTrzni.longitude, "title1", "snippet1"));
        LatLng mepas = new LatLng(43.3476426, 17.8038751);
        mClusterManager.addItem(new MyItem(mepas.latitude, mepas.longitude, "title2", "snippet2"));
        LatLng karting = new LatLng(43.3501085, 17.7979277);
        mClusterManager.addItem(new MyItem(karting.latitude, karting.longitude, "title3", "snippet3"));
        LatLng franjevacka = new LatLng(43.337563,17.8097269);
        mClusterManager.addItem(new MyItem(franjevacka.latitude, franjevacka.longitude));
        LatLng katedrala = new LatLng(43.3389234,17.7976635);
        mClusterManager.addItem(new MyItem(katedrala.latitude, katedrala.longitude));
        LatLng stadion = new LatLng(43.3454522,17.7958132);
        mClusterManager.addItem(new MyItem(stadion.latitude, stadion.longitude));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mInfo = (ImageView) findViewById(R.id.place_info);
        mTickets = (ImageView) findViewById(R.id.get_tickets);
        mSignals = (ImageView) findViewById(R.id.get_signals);

        tb = new TicketNewDatabase(MapActivity.this);

        getLocationPermission();

    }

    //Inicijaliziranje GoogleMap-e i GoogleApi-a
    //koristimo varijable sa vrha classe
    private void init(){
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        mTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapActivity.this, "Ticket button clicked", Toast.LENGTH_SHORT).show();
                showTickets();
            }
        });

        mSignals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapActivity.this, "Signals button clicked", Toast.LENGTH_SHORT).show();
                showSignals();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked place info");
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }else{
                        Log.d(TAG, "onClick: place info: " + mPlace.toString());
                        mMarker.showInfoWindow();
                    }
                }catch (NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException: " + e.getMessage() );
                }
            }
        });

        hideSoftKeyboard();
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    //dohvacanje trenutne lokacije korisnika aplikacije
    //kamera se pomiče na tu lokaciju
    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            if(currentLocation != null) {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM,
                                        "My Location");
                            }
                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    /*
     * Dohvacanje svih korisnikovih tiketa
     * prikaz na mapi
     * spremanje u map cluster
     */
    public void showTickets() {
        if (ticket_tester == 0)
            ticket_tester = 1;
        else
            ticket_tester = 0;

        //Prvi i svaki neparni put kada pristisnemo button za prikaz tiketa
        if (ticket_tester == 1) {
            addItems();
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
            Cursor res = tb.getMyTickets(FirstPageActivity.real_id);
            if (res.getCount() == 0) {
                Toast.makeText(MapActivity.this, "No tickets", Toast.LENGTH_SHORT).show();
                return;
            } else {
                while (res.moveToNext()) {
                    String ticket_id = "ID: " + res.getString(0) + " ";
                    String ticket_aid = res.getString(1);
                    String ticket_ctg = "Category: " + res.getString(2);
                    String ticket_subctg = "Subcategory: " + res.getString(3);
                    String ticket_frq = "Frequency: " + res.getString(4);
                    String ticket_q = "Question: " + res.getString(5);
                    String ticket_date = "Date: " + res.getString(6);
                    String ticket_time = "Time: " + res.getString(7);

                    LatLng latLng = new LatLng(Double.parseDouble(res.getString(9)),
                            Double.parseDouble(res.getString(8)));


                    String snippet = ("Category:  " + ticket_ctg + "\n" +
                            "Subcategory: " + ticket_subctg + "\n" +
                            "Frequency: " + ticket_frq + "\n" +
                            "Question: " + ticket_q + "\n" +
                            "Date:  " + ticket_date + "\n" +
                            "Time: " + ticket_time + "\n" +
                            "Longitude: " + Double.parseDouble(res.getString(8)) + "\n" +
                            "Latitude: " + Double.parseDouble(res.getString(9)) + "\n" +
                            "Altitude: " + Double.parseDouble(res.getString(10)));

                   /* MarkerOptions options = new MarkerOptions()
                            .position(latLng)
                            .title(ticket_id)
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ticket_icon));
                    mMarker = mMap.addMarker(options);*/

                    mClusterManager.addItem(new MyItem(Double.parseDouble(res.getString(9)),
                            Double.parseDouble(res.getString(8)), ticket_id, snippet));
                }
            }
        }//Drugi i svaki parni put kada pritisnemo na prikz tiketa
        else {
            mMap.clear();
            mClusterManager.clearItems();
        }
    }

    public void showSignals(){
        if(signal_tester == 0){
            signal_tester = 1;
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
            new getData().execute("");
        }else{
            signal_tester = 0;
            mMap.clear();
            mClusterManager.clearItems();
        }

    }
    private class getData extends AsyncTask<String, Void, String> {
        String name;

        @Override
        protected String doInBackground(String... params) {
            result = "";
            isr = null;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet("http://cued.azurewebsites.net/api/network/getnetwork/"
                        + String.valueOf(FirstPageActivity.real_id));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                isr = entity.getContent();
                Log.i("Data", isr.toString());
            } catch (Exception e) {
                Log.e("log_tag", "Error in http connection " + e.toString());

            }
            //convert response to string
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(isr, "utf-8"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                String data = sb.toString();

                String data_sub = data.substring(1,data.length()-1);
                Log.i("Data_bad_string", data_sub);

                JSONArray jsonArray = new JSONArray(data_sub.replaceAll("\\\\",""));
                Log.i("Data_array", jsonArray.toString());
                for(int i=0; i<=jsonArray.length();i++){
                    JSONObject signal = jsonArray.getJSONObject(i);
                    String snippet = signal.get("technology") + "\n" + signal.get("rsrp") + "\n"
                            + signal.get("longitude") + "\n" + signal.get("latitude");
                    int rsrp = Integer.parseInt(signal.get("rsrp").toString());
                    if(rsrp > -85 ){
                        mClusterManager.addItem(new MyItem(Double.parseDouble(signal.get("latitude").toString()),
                                Double.parseDouble(signal.get("longitude").toString()), "Good RSRP",
                                snippet));
                    }
                    else if(rsrp <= -85 && rsrp >= -110){
                        mClusterManager.addItem(new MyItem(Double.parseDouble(signal.get("latitude").toString()),
                                Double.parseDouble(signal.get("longitude").toString()), "Decent RSRP",
                                snippet));
                    }else{
                        mClusterManager.addItem(new MyItem(Double.parseDouble(signal.get("latitude").toString()),
                                Double.parseDouble(signal.get("longitude").toString()), "Bad RSRP",
                                snippet));
                    }

                }
            } catch (Exception e) {
                Log.e("log_tag", "Error  converting result " + e.toString());
            }
            return "Executed";
        }
        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    //Pomicanje kamere
    //Dobiva latlng koordinate, zoom da zna koji zoom level koristiti
    //Također i placeInfo klasu koja se nalazi u paketu 'modeli'
    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));

        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);

            }catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage() );
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyboard();
    }

    //Pomicanje kamere
    //Za razliku od gornje ne prima place info već samo title koji postavlja na marker na koji se pomkne
    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    //Inicijalizacija mape
    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    //Dohvacanje lokacijskih permisija
    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    //Nakon unosa skriva keyboard
    //Tj nakon sto pritisnemo enter ili odaberemo nešto u placeinfo helperu
    //tipokvnica bi se trebala ugasiti
    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /*
        --------------------------- google places API autocomplete suggestions -----------------
     */

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace);

            places.release();
        }
    };

    /*
        My Item class-a koja je napravljena i u nju spremamo
        informacije o mjestu koje želimo ubaciti u naš clusterer
     */
    public class MyItem implements ClusterItem {
        private LatLng mPosition;
        private String mTitle = "";
        private String mSnippet = "";

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        public MyItem(double lat, double lng, String title, String snippet) {
            mPosition = new LatLng(lat, lng);
            mTitle = title;
            mSnippet = snippet;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public String getTitle() {
            return mTitle;
        }

        @Override
        public String getSnippet() {
            return mSnippet;
        }
    }
}


