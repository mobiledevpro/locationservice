## locationservice
Module for listen user's location changes (runs in background as Service)

### How to use:
* Import module using Android Studio.
* Bind service in onStart() or onResume(): 
```java 
LocationServiceManager.getInstance().bindLocationService(
                /*context for service binding*/
                context,
                /*service callbacks*/
                new LocationServiceManager.Callbacks() {
                   @Override
                   public void onLocationUpdated(double lat, double lon) {
                       .....
                   }
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


