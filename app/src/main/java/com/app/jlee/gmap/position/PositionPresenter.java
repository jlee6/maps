package com.app.jlee.gmap.position;

import android.location.Location;

import rx.Observable;

public interface PositionPresenter {
    boolean isActive();
    void start();
    void stop();

    Observable<Location> onPositionChanged();
}
