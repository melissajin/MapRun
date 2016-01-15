package com.meljin.maprun;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Iterator;

/**
 * Created by s_jin01 on 1/4/16.
 */
public class Board {

    private static final double TOLERANCE = .001; //haha...idk. minimum distance between current location and point/ghost for something to happen.

    public static void tryRemovePoint(LatLng currLoc){
        /* Checking all points if close enough to current location.
         * Can definitely be optimized if sorted lat lng points in k-d tree
         * and used to find nearest neighbor. But i'll save that for later.
         */

        Iterator<LatLng> iter = MapsActivity.getKeySet().iterator();
        while(iter.hasNext()){
            LatLng point = iter.next();
            Log.i("(Lat, Lng)", "(" + Double.toString(point.latitude) + ", " + Double.toString(point.longitude) + ")");
            Log.i("Distance: ", Double.toString(getDistance(point, currLoc)));
            if(getDistance(point, currLoc) < TOLERANCE){
                Player.increaseScore();
                MapsActivity.removePoint(point);
                iter.remove();
            }
        }
        //if player collects all points, game is won
        if(MapsActivity.getKeySet().size() == 0){
            MapsActivity.endGameCond = 0;
            //END GAME
        }
    }

    public static void isEndGame(LatLng currLoc){
        for(LatLng ghostPos : MapsActivity.getGhostLocations()){
            if(getDistance(ghostPos, currLoc) < TOLERANCE){
                //END GAME
                MapsActivity.endGameCond = 1;
            }
        }
    }

    public static Double getDistance(LatLng a, LatLng b){
        return Math.sqrt(Math.pow(a.latitude-b.latitude,2) + Math.pow(a.longitude-b.longitude, 2));
    }
}
