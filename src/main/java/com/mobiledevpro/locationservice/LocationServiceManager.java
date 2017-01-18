package com.mobiledevpro.locationservice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Manager for location service
 * <p>
 * Created by Dmitriy V. Chernysh on 18.01.17.
 * dmitriy.chernysh@gmail.com
 * <p>
 * www.mobile-dev.pro
 */

public class LocationServiceManager {

    private static LocationServiceManager sManager;

    private ServiceConnection mLocationServiceConnection;
    private ILocationService mLocationService = null;

    private LocationServiceManager() {
    }

    public static LocationServiceManager getInstance() {
        if (sManager == null) {
            sManager = new LocationServiceManager();
        }
        return sManager;
    }

    /**
     * Bind service
     */
    public void bindLocationService(Context context, Callbacks callbacks) {
        //check network connection
        if (!isDeviceOnline(context)) {
            callbacks.isDeviceOffline();
            return;
        }

        //check location permission
        if (!isLocationPermissionGranted(context)) {
            callbacks.isNotLocationPermissionGranted();
            return;
        }

        Intent intent = new Intent(context, LocationService.class);
        //unbind service if it was already bound
        unbindLocationService(context);
        //init location service connection
        initLocationServiceConnection(callbacks);
        //bind service to context
        context.bindService(
                intent,
                mLocationServiceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    /**
     * Unbind service
     */
    public void unbindLocationService(Context context) {
        if (mLocationServiceConnection != null) {
            context.unbindService(mLocationServiceConnection);
            mLocationServiceConnection = null;
        }
    }


    /**
     * Create service connection
     */
    private void initLocationServiceConnection(final ILocationServiceCallbacks callbacks) {
        mLocationServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(Constants.LOG_TAG_DEBUG, "LocationServiceManager.onServiceConnected(): service connected");

                //define location service
                mLocationService = ILocationService.Stub.asInterface(iBinder);
                //register service callbacks
                try {
                    mLocationService.registerCallback(callbacks);
                } catch (RemoteException e) {
                    Log.e(Constants.LOG_TAG_ERROR, "LocationServiceManager.onServiceConnected: EXCEPTION - " + e.getLocalizedMessage(), e);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(Constants.LOG_TAG_DEBUG, "LocationServiceManager.onServiceDisconnected(): service disconnected");
            }
        };
    }

    /**
     * Check network connection
     *
     * @param context - application context
     * @return true - device online
     */
    private boolean isDeviceOnline(Context context) {
        ConnectivityManager connMngr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMngr.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnected());
    }

    /**
     * Check Location permission
     *
     * @param context Context
     * @return True - permission granted
     */
    private boolean isLocationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED));
        } else {
            return true;
        }
    }

    /**
     * Callbacks for client
     */
    public static abstract class Callbacks extends ILocationServiceCallbacks.Stub {

        @Override
        public abstract void isDeviceOffline();

        @Override
        public abstract void isNotLocationPermissionGranted();

        @Override
        public abstract void onGoogleApiConnectionFailed(int errCode, String errMessage, boolean hasResolution);

        @Override
        public abstract void onLocationUpdated(
                double latitude,
                double longitude,
                double altitude,
                float accuracy);

        @Override
        public abstract void onGetLocationSettingsState(boolean isNetworkLocationOn, boolean isGpsLocationOn);

        @Override
        public void onGetLastLocation(double latitude, double longitude, double altitude, float accuracy) throws RemoteException {

        }
    }

}
