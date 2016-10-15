package com.app.jlee.gmap.ui.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.jlee.gmap.R;
import com.app.jlee.gmap.Utility;
import com.app.jlee.gmap.position.Position;
import com.app.jlee.gmap.position.PositionPresenter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import rx.Observable;
import rx.schedulers.Schedulers;

public class MapsFragment extends SupportMapFragment {
    private final OnMapFragmentMapReadyCallback callback = new OnMapFragmentMapReadyCallback();
    private final int MAP_POSITION_ANIMATION_DURATION = 250;

    private PositionPresenter presenter;
    private Observable<Location> locationObservable;

    private GoogleMap map;

    private Marker marker = null;
    private Location location;
    private boolean isStart;
    private float zoom = 20;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        presenter = new Position(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        getMapAsync(callback);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (presenter != null && presenter.isActive()) {
            locationObservable.unsubscribeOn(Schedulers.io());
            presenter.stop();
        }
    }

    private class OnMapFragmentMapReadyCallback implements OnMapReadyCallback {
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;

            // TODO: Fix listening while zoom movements
            map.setOnCameraMoveListener(() -> {
                if (isStart) {
                    return;
                }

                zoom = map.getCameraPosition().zoom;
            });

            if (marker == null) {
                marker = map.addMarker(new MarkerOptions()
                        .icon(Utility.getMarkerIconFromDrawable(getActivity(), R.drawable.ic_navigation_black_24dp))
                        .position(new LatLng(0, 0)));
            }

            if (presenter != null && !presenter.isActive()) {
                presenter.start();
                isStart = true;

                locationObservable = presenter.onPositionChanged();
                locationObservable.subscribeOn(Schedulers.newThread())
                        .subscribe(location -> {
                            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

                            marker.setPosition(position);

                            // TODO: Calculate zoom and tilt per speed
                            // see reference at
                            // driving 17, bike 20
                            // https://developers.google.com/android/reference/com/google/android/gms/maps/model/CameraPosition.Builder.html#tilt(float)
                            final CameraUpdate update = CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.builder()
                                            .target(position)
                                            .zoom(zoom)
                                            .tilt(60)
                                            .bearing(location.getBearing())
                                            .build());

                            map.animateCamera(update,
                                    MAP_POSITION_ANIMATION_DURATION,
                                    new GoogleMap.CancelableCallback() {
                                        @Override
                                        public void onFinish() {
                                            clearStartFlag();
                                        }

                                        @Override
                                        public void onCancel() {
                                            clearStartFlag();
                                        }

                                        private void clearStartFlag() {
                                            if (isStart) {
                                                isStart = !isStart;
                                            }
                                        }
                                    });
                        });
            }
        }
    }
}
