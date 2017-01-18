## locationservice
Module for listen user's location changes (runs in background as Service)

### How to use:
* Import module using Android Studio.
* Create result callbacks listener:
```java
 LocationServiceManager.Callbacks mCallbacks =
            new LocationServiceManager.Callbacks() {

                @Override
                public void isDeviceOffline() {
                    //show message, go to settings, etc
                    //NOTE: location will not be updated while device is offline
                }

                @Override
                public void isNotLocationPermissionGranted() {
                    //Need to ask user a runtime permision for location
                    //NOTE: location will not be updated while permission isn't granted
                }

                @Override
                public void onGetLocationSettingsState(boolean isNetworkLocationOn, boolean isGpsLocationOn) {
                    //Use settings state if needed.
                    //NOTE: location will not be updated while isNetworkLocationOn == false and isGpsLocationOn == false.
                    //      You can show location settings.
                }

                @Override
                public void onGoogleApiConnectionFailed(int errCode, String errMessage) {
                    //NOTE: location will not be updated while error will not be resolved
                    //Options for resolve error:
                    //1. You can get and handle pending intent (example, for customized dialog/screen)
                   PendingIntent pendingIntent = LocationServiceManager.getGoogleApiErrorResolutionPendingIntent(
                                              mView.getActivity(),
                                              errCode,
                                              /*someRequestCode for sending result to activity*/
                                      );

                   if (pendingIntent != null) {
                                          try {
                                              pendingIntent.send();
                                          } catch (PendingIntent.CanceledException e) {
                                              //do nothing
                                          }
                                      }                   

                    //2. Or you can show default predefined dialog:
                    
                    Dialog dialog = LocationServiceManager.getGoogleApiErrorDialog(
                            activity,
                            errCode,
                            10001
                    );
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            activity.finish();
                        }
                    });
                    dialog.show();
                   
                }

                @Override
                public void onLocationUpdated(double latitude, double longitude, double altitude, float accuracy) {
                    //use new location data
                }
            };
```

* Bind service in onStart() or onResume(): 
```java 
LocationServiceManager.getInstance().bindLocationService(
                /*context for service binding*/
                context,
                /*service callbacks*/
                mCallbacks
            };
        );
```
* Unbind service in onStop() or onPause():
```java
LocationServiceManager.getInstance().unbindLocationService(
     /*context to which service was bound*/
     context
);
```


