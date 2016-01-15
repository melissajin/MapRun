package com.meljin.maprun;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by s_jin01 on 1/3/16.
 */
public class Ghost{

    private LatLng position;
    private Double speed;
    static Handler handler;
    private static final int GOT_PATH = 1;
    private boolean animationDone = false;
    private Marker marker;

    private boolean getAnimationDone(){
        return animationDone;
    }

    public Ghost(LatLng latLng){
        this.position = latLng;
    }

    public void setGhost(GoogleMap map){

        marker = map.addMarker(new MarkerOptions().position(this.position)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.monster3)));
        getGhostPath(this.position, new LatLng(42.4610379, -71.4896693));
        //Log.i("GHOST POSITION", Double.toString(ghost.position.latitude)+ ", "+ Double.toString(ghost.position.longitude));

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == GOT_PATH){
                    ArrayList<LatLng> path = (ArrayList<LatLng>) msg.obj;
                    Log.i("BEFORE RUNNING", Integer.toString(path.size()));
                    //Animator.animatePath(marker, path);
                }
                super.handleMessage(msg);
            }
        };
    }

    public LatLng getGhostLocation(){
        return position;
    }

    /*  static void animate(final Marker marker, final ArrayList<LatLng> positions, final int i){
          if(i < positions.size()){
              final LinearInterpolator interpolator = new LinearInterpolator();
              float[] results = new float[1];
              final LatLng startLatLng = marker.getPosition();
              Log.i("STARTING POSITION", Double.toString(startLatLng.latitude) + ", " + Double.toString(startLatLng.longitude) + " I: " + Integer.toString(i));
              Location.distanceBetween(startLatLng.latitude, startLatLng.longitude,
                      positions.get(i).latitude, positions.get(i).longitude, results);
              final long start = SystemClock.uptimeMillis();
              final long duration = (long) results[0]*30;
              handler.post(new Runnable() {
                  @Override
                  public void run() {
                      long elapsed = SystemClock.uptimeMillis() - start;
                      float t = interpolator.getInterpolation((float) elapsed
                              / duration);
                      double lng = t * positions.get(i).longitude + (1 - t)
                              * startLatLng.longitude;
                      double lat = t * positions.get(i).latitude + (1 - t)
                              * startLatLng.latitude;
                      marker.setPosition(new LatLng(lat, lng));
                      Log.i("UPDATING POSITION", Double.toString(lat) + ", " + Double.toString(lng) + " I: " + Integer.toString(i));
                      Log.i("DURATION", Long.toString(duration) + ", " + Long.toString(start));
                      if (t < 1.0) {
                          // Post again 16ms later.
                          Log.i("DELAYING", Double.toString(t));
                          handler.postDelayed(this, 16);
                      }
                      else{
                          animate(marker, positions, i+1);
                      }
                  }
              });
          }
      }
  */
    private static void getGhostPath(final LatLng start, final LatLng end){
        final ArrayList<LatLng> path = new ArrayList<LatLng>();
        final String TAG = SnapToRoads.class.getSimpleName();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Reader rd = null;
                try {
                    URL url = new URL("http://maps.google.com/maps/api/directions/json?origin="+start.latitude+","
                            +start.longitude+"&destination="+end.latitude+","+end.longitude+"&sensor=true");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setReadTimeout(10000 /* milliseconds */);
                    con.setConnectTimeout(15000 /* milliseconds */);
                    con.connect();
                    if (con.getResponseCode() == 200) {
                        rd = new InputStreamReader(con.getInputStream());
                        StringBuffer sb = new StringBuffer();
                        final char[] buf = new char[1024];
                        int read;
                        while ((read = rd.read(buf)) > 0) {
                            sb.append(buf, 0, read);
                        }
                        String str = sb.toString();
                        JSONObject jsonObject = new JSONObject(str);
                        JSONArray steps = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                                .getJSONObject(0).getJSONArray("steps");
                        for(int i = 0; i < steps.length(); i++){
                            JSONObject pointOnPath = steps.getJSONObject(i).getJSONObject("end_location");
                            LatLng point = new LatLng(pointOnPath.getDouble("lat"), pointOnPath.getDouble("lng"));
                            path.add(point);
                            Log.i("GHOST PATH: ", Double.toString(point.latitude)+ ", "+ Double.toString(point.longitude));
                        }
                    }
                    con.disconnect();

                } catch (Exception e) {
                    Log.e("foo", "bar", e);
                } finally {
                    if (rd != null) {
                        try {
                            rd.close();
                        } catch (IOException e) {
                            Log.e(TAG, "", e);
                        }
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = handler.obtainMessage();
                        msg.obj = path;
                        msg.what = GOT_PATH;
                        handler.sendMessage(msg);
                    }
                });
            }
        }).start();
    }
}
