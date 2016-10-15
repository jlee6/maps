package com.app.jlee.gmap.position;

/**
 * Created by jlee on 10/10/16.
 */

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

@SuppressWarnings("MissingPermission")
public class Position implements PositionPresenter {
    private Context context;

    private boolean active;
    private int permission;

    private Location location;
    private LocationManager locationManager;

    private PositionUpdateListener listener;

    public Position(Context context) {
        this.context = context;

        permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void start() {
        initializeLocationService();
    }

    @Override
    public void stop() {
        dispose();
    }

    @Override
    public Observable<Location> onPositionChanged() {
        return Observable.create(subscriber -> listener = subscriber::onNext);
    }

    private void dispose() {
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.removeUpdates(locationListener);
    }

    private void initializeLocationService() {
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        String locationProvider = locationManager.getBestProvider(createFineCriteria(), true);

        if (!TextUtils.isEmpty(locationProvider) && !locationProvider.equals("passive")) {
            locationManager.requestLocationUpdates(locationProvider, 1000, 0f, locationListener);
            return;
        }

        AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle("Enable Location Provider")
                .setMessage("Unable to find location provider\r\nWould like to enable location settings now?")
                .setPositiveButton("Enable", (dialog, which) -> {
                    Intent settings = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(settings);
                })
                .setNegativeButton("Leave", null)
                .create();

        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }

    private LocationListener locationListener = new LocationListener() {
        double distance;
        Location last;

        @Override
        public void onLocationChanged(Location location) {
            if (last == null) {
                distance = 0;
                last = location;
            }

            if (listener == null) {
                return;
            }

            Log.d(getClass().getSimpleName(), "Updating Location: " + location.toString());
            listener.onPositionChanged(location);
            last = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            active = true;
            reset();
            String locationProvider = locationManager.getBestProvider(createFineCriteria(), true);
            locationManager.requestLocationUpdates(locationProvider, 0, 0f, locationListener);
        }

        @Override
        public void onProviderDisabled(String provider) {
            active = false;
            reset();
            locationManager.removeUpdates(locationListener);
        }

        private void reset() {
            last = null;
            distance = 0;
        }
    };

    private interface PositionUpdateListener {
        void onPositionChanged(Location location);
    }

    private static Criteria createFineCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        return criteria;
    }

    private static double calculateDistance(Location from, Location to) {
        return (from != null) ? to.distanceTo(from) : 0;
    }
}
