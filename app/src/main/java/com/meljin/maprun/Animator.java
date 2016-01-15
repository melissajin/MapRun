package com.meljin.maprun;

/**
 * Created by s_jin01 on 1/3/16.
 */
public class Animator {

    /*private static AnimatorSet anim;

    static void animateLeg(Marker marker, ArrayList<LatLng> finalPosition){
        List<Animator> animators = new ArrayList<Animator>();
        anim = new AnimatorSet();
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        TypeEvaluator<LatLng>  typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                double lat = (endValue.latitude - startValue.latitude) * fraction + startValue.latitude;
                double lng = (endValue.longitude - startValue.longitude) * fraction + startValue.longitude;
                return new LatLng(lat, lng);
            }
        };
        for (int i = 0; i < finalPosition.size(); i++) {
            Double lat = marker.getPosition().latitude;
            Double lng = marker.getPosition().longitude;
            Log.i("START POSITION", Double.toString(lat) + ", " + Double.toString(lng));
            ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition.get(i));
            animator.setDuration(5000);
            animators.add(animator);
        }
        anim.playSequentially(animators);
    }
    static void animatePath(Marker marker, ArrayList<LatLng> finalPosition) {
        anim = new AnimatorSet();
        for (int i = 0; i < finalPosition.size(); i++){
            Double lat = marker.getPosition().latitude;
            Double lng = marker.getPosition().longitude;
            Log.i("START POSITION", Double.toString(lat) + ", " + Double.toString(lng));
            anim.playSequentially();animateLeg(marker, finalPosition.get(i));
        }
    }*/
}