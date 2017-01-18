// ILocationServiceCallback.aidl
package com.mobiledevpro.locationservice;

// Declare any non-default types here with import statements

interface ILocationServiceCallbacks {

    void isDeviceOffline();

    void isNotLocationPermissionGranted();

    void onGoogleApiConnectionFailed(
        int errCode,
        String errMessage
    );

    void onGetLocationSettingsState(
        boolean isNetworkLocationOn,
        boolean isGpsLocationOn
    );

    void onLocationUpdated(
           double latitude,
           double longitude,
           double altitude,
           float accuracy
    );
}
