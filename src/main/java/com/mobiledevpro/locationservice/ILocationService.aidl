// iLocationService.aidl
package com.mobiledevpro.locationservice;
import com.mobiledevpro.locationservice.ILocationServiceCallbacks;

interface ILocationService {
    /**
     * Often you want to allow a service to call back to its clients.
     * This shows how to do so, by registering a callback interface with
     * the service.
     */
    void registerCallback(ILocationServiceCallbacks callbacks);

    /**
     * Remove a previously registered callback interface.
     */
    void unregisterCallback(ILocationServiceCallbacks callbacks);
}
