// ILocationServiceCallback.aidl
package com.mobiledevpro.locationservice;

// Declare any non-default types here with import statements

interface ILocationServiceCallbacks {
    void onLocationUpdated(double lat, double lon);
}
