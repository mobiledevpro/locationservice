package com.mobiledevpro.locationservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * Location service
 * <p>
 * Created by Dmitriy V. Chernysh on 17.01.17.
 * dmitriy.chernysh@gmail.com
 * <p>
 * www.mobile-dev.pro
 */

public class LocationService extends Service {

    private static final int HANDLE_MSG_LOCATION_UPDATED = 1;

    private final RemoteCallbackList<ILocationServiceCallbacks> mCallbacks = new RemoteCallbackList<>();
    private final ILocationService.Stub mBinder = new ILocationService.Stub() {
        /**
         * Register LocationService callbacks
         *
         * @param callbacks Callbacks for interact from client (Activity, fragment, etc)
         * @throws RemoteException
         */
        @Override
        public void registerCallback(ILocationServiceCallbacks callbacks) throws RemoteException {
            if (callbacks != null) mCallbacks.register(callbacks);
        }

        @Override
        public void unregisterCallback(ILocationServiceCallbacks callbacks) throws RemoteException {
            if (callbacks != null) mCallbacks.unregister(callbacks);
        }
    };

    /**
     * Handler for send callbacks to client
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_MSG_LOCATION_UPDATED:
                    if (!(msg.obj instanceof Location)) return;

                    Location location = (Location) msg.obj;
                    Log.d(Constants.LOG_TAG_DEBUG, "LocationService.handleMessage(): HANDLE_MSG_LOCATION_UPDATED");
                    //send callbacks
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            mCallbacks.getBroadcastItem(i).onLocationUpdated(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                        } catch (RemoteException e) {
                            Log.e(Constants.LOG_TAG_ERROR, "LocationService.onCreate: EXCEPTION - " + e.getLocalizedMessage(), e);
                        }
                    }
                    mCallbacks.finishBroadcast();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    //PRIORITY_BALANCED_POWER_ACCURACY  - Wifi and Network
    //PRIORITY_HIGH_ACCURACY - WiFi , GPS and Network
    private final LocationRequest mLocationRequest = new LocationRequest()
            .setInterval(Constants.UPDATE_LOCATION_INTERVAL)
            .setFastestInterval(Constants.UPDATE_LOCATION_INTERVAL_FASTEST)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private GoogleApiClient mGoogleApiClient;

    /**
     * Listen location changes
     */
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location == null) return;
            Log.d(Constants.LOG_TAG_DEBUG, "LocationListener.onLocationChanged(): lat - " + location.getLatitude() + ", lon - " + location.getLongitude());
            Message msg = new Message();
            msg.obj = location;
            msg.what = HANDLE_MSG_LOCATION_UPDATED;
            mHandler.sendMessage(msg);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Constants.LOG_TAG_DEBUG, "LocationService.onBind(): ");
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(Constants.LOG_TAG_DEBUG, "LocationService.onCreate(): ");
        //connect to google api and start listen location changes
        connectToGoogleApi(getBaseContext());
    }

    @Override
    public void onDestroy() {
        Log.d(Constants.LOG_TAG_DEBUG, "LocationService.onDestroy(): ");
        //disconnect from google api and stop listen location changes
        disconnectFromGoogleApi();
        //unregister all callbacks
        mCallbacks.kill();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Constants.LOG_TAG_DEBUG, "LocationService.onStartCommand(): Received start id - " + startId + ": " + intent);
        return START_NOT_STICKY;
    }


    /**
     * Connect to Google Api Client
     *
     * @param context Context
     */
    private void connectToGoogleApi(Context context) {
        if (context == null) return;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        startLocationUpdate();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(Constants.LOG_TAG_DEBUG, "GoogleApiClient.onConnectionSuspended(): ");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        onGapiFailedConnection(connectionResult);
                    }
                })
                .addApi(LocationServices.API)
                .build();

        Log.d(Constants.LOG_TAG_DEBUG, "LocationService.connectToGoogleApi(): api client - " + (mGoogleApiClient != null));
        mGoogleApiClient.connect();
    }

    /**
     * Disconnect from Google API client
     */
    private void disconnectFromGoogleApi() {
        stopLocationUpdate();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    private void onGapiFailedConnection(ConnectionResult connectionResult) {
        // TODO: 18.01.17 send err code to client
    }

    /**
     * Start listen location changes
     */
    private void startLocationUpdate() {
        Log.d(Constants.LOG_TAG_DEBUG, "LocationService.startLocationUpdate(): ");
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest,
                    mLocationListener
            );
        } catch (SecurityException e) {
            Log.e(Constants.LOG_TAG_ERROR, "LocationService.startLocationUpdate: EXCEPTION - " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Stop listen location changes
     */
    private void stopLocationUpdate() {
        if (mGoogleApiClient == null) return;
        Log.d(Constants.LOG_TAG_DEBUG, "LocationService.stopLocationUpdate(): ");
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient,
                    mLocationListener
            );
        } catch (SecurityException e) {
            Log.e(Constants.LOG_TAG_ERROR, "LocationService.stopLocationUpdate: EXCEPTION - " + e.getLocalizedMessage(), e);
        }
    }

}