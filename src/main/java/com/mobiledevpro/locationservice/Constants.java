package com.mobiledevpro.locationservice;

/**
 * Constants
 * <p>
 * Created by Dmitriy V. Chernysh on 18.01.17.
 * dmitriy.chernysh@gmail.com
 * <p>
 * www.mobile-dev.pro
 */

class Constants {
    static final String LOG_TAG_DEBUG = "location-service-debug";
    static final String LOG_TAG_ERROR = "location-service-error";

    static final int UPDATE_LOCATION_INTERVAL = 10000; //milliseconds
    static final int UPDATE_LOCATION_INTERVAL_FASTEST = UPDATE_LOCATION_INTERVAL / 5;
}
