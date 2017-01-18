package com.mobiledevpro.locationservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
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
     * Callbacks for client
     */
    public static abstract class Callbacks extends ILocationServiceCallbacks.Stub {
        @Override
        public abstract void onLocationUpdated(double lat, double lon);
    }

}
