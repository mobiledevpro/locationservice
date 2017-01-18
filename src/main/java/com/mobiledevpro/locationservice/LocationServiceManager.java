package com.mobiledevpro.locationservice;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;

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
    public void bindLocationService(@NonNull Context context, @NonNull Callbacks callbacks) {
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
    public void unbindLocationService(@NonNull Context context) {
        if (mLocationServiceConnection != null) {
            context.unbindService(mLocationServiceConnection);
            mLocationServiceConnection = null;
        }
    }

    /**
     * Create Google Play Services error dialog
     *
     * @param activity     Activity
     * @param apiErrorCode Error code form onConnectionFailed() method
     * @return Dialog
     */
    public static Dialog getGoogleApiErrorDialog(@NonNull final Activity activity, int apiErrorCode, int requestCode) {
        return GoogleApiAvailability.getInstance().getErrorDialog(
                activity,
                apiErrorCode,
                requestCode
        );
    }

    /**
     * Get Google Play Service resolution pending intent
     *
     * @param context     Context
     * @param errCode     Error code
     * @param requestCode Request code
     * @return Pending intent
     */
    public static PendingIntent getGoogleApiErrorResolutionPendingIntent(@NonNull Context context, int errCode, int requestCode) {
        return GoogleApiAvailability.getInstance().getErrorResolutionPendingIntent(
                context,
                errCode,
                requestCode
        );
    }

    /**
     * Open Location settings
     *
     * @param activity Context
     */
    public static void openLocationSettings(@NonNull Activity activity) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivity(intent);
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
    private boolean isDeviceOnline(@NonNull Context context) {
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
    private boolean isLocationPermissionGranted(@NonNull Context context) {
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
        public abstract void onGoogleApiConnectionFailed(int errCode, String errMessage);

        @Override
        public abstract void onLocationUpdated(
                double latitude,
                double longitude,
                double altitude,
                float accuracy);

        @Override
        public abstract void onGetLocationSettingsState(boolean isNetworkLocationOn, boolean isGpsLocationOn);
    }

}
