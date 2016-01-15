package com.meljin.maprun;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.util.Log;
import android.util.Property;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by s_jin01 on 1/3/16.
 */
public class Animator {

    static void animateLeg(Marker marker, LatLng finalPosition){
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        TypeEvaluator<LatLng>  typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                double lat = (endValue.latitude - startValue.latitude) * fraction + startValue.latitude;
                double lng = (endValue.longitude - startValue.longitude) * fraction + startValue.longitude;
                return new LatLng(lat, lng);
            }
        };
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(5000);
        animator.start();

    }
    static void animatePath(Marker marker, ArrayList<LatLng> finalPosition) {
        for (int i = 0; i < finalPosition.size(); i++){
            Double lat = marker.getPosition().latitude;
            Double lng = marker.getPosition().longitude;
            Log.i("START POSITION", Double.toString(lat) + ", " + Double.toString(lng));
            animateLeg(marker, finalPosition.get(i));
        }
    }
}