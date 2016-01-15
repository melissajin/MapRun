package com.meljin.maprun;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private static GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double left, right, top, bottom; //bounds of the visible region of map.
    private ArrayList<LatLng> latLngs = new ArrayList<LatLng>(); //LatLngs at set intervals on map (points to be snapped).
    private static HashMap<LatLng, Circle> points = new HashMap<LatLng, Circle>(); //LatLngs are snapped points.
    private static ArrayList<Ghost> ghosts = new ArrayList<Ghost>();
    private static final int GHOST_INCR = 4; //number of points per ghost.
    private Button pauseResumeButton;
    private Button endGameButton;

    //Timer variables
    public static TextView timerVal;
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    public static int endGameCond = 2; // 0 = win, 1 = loose, 2 = nothing

    AppLocationService appLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        timerVal = (TextView)findViewById(R.id.timer);

        pauseResumeButton = (Button) findViewById(R.id.pause_resume_button);
        pauseResumeButton.setTag(0);
        pauseResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = (Integer) v.getTag();
                if(status == 0){
                    onPause();
                    //pause timer
                    pauseResumeButton.setText("Resume");
                    pauseResumeButton.setTag(1);
                    timeSwapBuff += timeInMilliseconds;
                    customHandler.removeCallbacks(updateTimerThread);
                }
                else{
                    //resume timer
                    onResume();
                    pauseResumeButton.setText("Pause");
                    pauseResumeButton.setTag(0);
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);

                }
            }
        });

        endGameButton = (Button) findViewById(R.id.end_game_button);
        endGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endGameCond = 2;
                mMap = null;
                Intent intent = new Intent(getApplication(), EndGame.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timerVal.setText("" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            customHandler.postDelayed(this, 0);

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        appLocationService = new AppLocationService(MapsActivity.this);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = appLocationService.getLocation(bestProvider);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //DISABLE SCREEN ROTATION TO LANDSCAPE MODE
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                VisibleRegion vr = mMap.getProjection().getVisibleRegion();
                left = vr.latLngBounds.southwest.longitude;
                right = vr.latLngBounds.northeast.longitude;
                top = vr.latLngBounds.northeast.latitude;
                bottom = vr.latLngBounds.southwest.latitude;

                //Create 2D Array of LatLngs
                double incrX = Math.abs(right - left)/5;
                double incrY = Math.abs(top - bottom)/5;
                for(double x = left; x < right; x += incrX){
                    for(double y = top; y > bottom; y -= incrY){
                        LatLng latLng = new LatLng(y, x);
                        latLngs.add(latLng);
                    }
                }
                //Place all LatLngs on closest road.
                new SnapToRoads().execute(latLngs);
            }
        });
    }

    public static void addPointsAndGhosts(ArrayList<LatLng> latLngs){
        int i = 0;
        for(LatLng coord : latLngs){
            if(i % GHOST_INCR == 0){
                Ghost g = new Ghost(latLngs.get(2));
                ghosts.add(g);
                g.setGhost(mMap);
            }
            else {
                Circle c = mMap.addCircle(new CircleOptions().center(coord).radius(8).fillColor(Color.GREEN).strokeColor(Color.GREEN));
                points.put(coord, c);
            }
            i++;

        }
    }

    public static void removePoint(LatLng latLng){
        Log.i("POINT REMOVED", "");
        points.get(latLng).remove(); //remove point from google map
    }

    public static Set<LatLng> getKeySet(){
        return points.keySet();
    }

    public static ArrayList<LatLng> getGhostLocations(){
        ArrayList<LatLng> ghostLocs = new ArrayList<LatLng>();
        for(Ghost g : ghosts){
            ghostLocs.add(g.getGhostLocation());
        }
        return ghostLocs;
    }

    public static void startTimer(){

    }
}
