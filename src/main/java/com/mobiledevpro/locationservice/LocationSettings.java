package com.mobiledevpro.locationservice;

/**
 * Model from location settings
 * <p>
 * Created by Dmitriy V. Chernysh on 19.01.17.
 * dmitriy.chernysh@gmail.com
 * <p>
 * www.mobile-dev.pro
 */

class LocationSettings {
    private boolean isGpsOn;
    private boolean isNetworkLocationOn;

    LocationSettings(boolean isGpsOn, boolean isNetworkLocationOn) {
        this.isGpsOn = isGpsOn;
        this.isNetworkLocationOn = isNetworkLocationOn;
    }

    boolean isGpsOn() {
        return isGpsOn;
    }

    boolean isNetworkLocationOn() {
        return isNetworkLocationOn;
    }
}
