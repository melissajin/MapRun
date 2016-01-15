package com.meljin.maprun;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by s_jin01 on 1/4/16.
 */

class SnapToRoads extends AsyncTask<ArrayList<LatLng>, Void, ArrayList<LatLng>> {

    private final String TAG = SnapToRoads.class.getSimpleName();

    @Override
    protected ArrayList<LatLng> doInBackground(ArrayList<LatLng>... params) {
        Reader rd = null;
        ArrayList<LatLng> retVal = new ArrayList<LatLng>();
        try {
            ArrayList<LatLng> latLngs = params[0];

            int count = latLngs.size();
            int i = 0;
            int cnt = 0;
            while(i < count) {

                /*
                 * FIX THIS:
                 * 1. Make sure the snapped point is still in visible region.
                 * 2. All latLngs will probably return different snapped coordinates on the road.
                 *    Need a way to limit the number of snapped points accepted.
                 */
                Double lat = latLngs.get(i).latitude;
                Double lng = latLngs.get(i).longitude;
                URL url = new URL("http://maps.google.com/maps/api/directions/json?origin="+lat+","+lng+"&destination="+lat+","+lng+"&sensor=true");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                //con.setReadTimeout(10000 /* milliseconds */);
                //con.setConnectTimeout(15000 /* milliseconds */);
                con.connect();
                if (con.getResponseCode() == 200) {

                    rd = new InputStreamReader(con.getInputStream());
                    StringBuffer sb = new StringBuffer();
                    final char[] buf = new char[1024];
                    int read;
                    while ((read = rd.read(buf)) > 0) {
                        sb.append(buf, 0, read);
                    }

                    //JSON parsing to get LatLng of roads where markers are to be placed
                    String str = sb.toString();
                    JSONObject json = new JSONObject(str);
                    JSONArray routesArray = json.getJSONArray("routes");
                    JSONObject route = routesArray.getJSONObject(0);
                    JSONObject bounds = route.getJSONObject("bounds");
                    JSONObject northeast = bounds.getJSONObject("northeast");
                    Double newLat = northeast.getDouble("lat");
                    Double newLng = northeast.getDouble("lng");

                    LatLng newLatLng = new LatLng(newLat, newLng);
                    retVal.add(newLatLng);
                }
                con.disconnect();
                i++;
/*
                cnt++;

                if(cnt == 8) {
                    Log.i("SLEEPING", "SKDflKSdjf");
                    Thread.sleep(1000);
                    cnt = 0;
                }
*/
                //Needed so does not exceed Directions API usage limits...makes it soooo slow....
                Thread.sleep(500);
            }
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
        return retVal;
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> latLngs) {
        MapsActivity.addPointsAndGhosts(latLngs);
    }

}