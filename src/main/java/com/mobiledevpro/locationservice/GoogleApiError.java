package com.mobiledevpro.locationservice;

/**
 * Model for Google Api connection error
 * <p>
 * Created by Dmitriy V. Chernysh on 18.01.17.
 * dmitriy.chernysh@gmail.com
 * <p>
 * www.mobile-dev.pro
 */

class GoogleApiError {
    private int code;
    private String message;

    GoogleApiError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    int getCode() {
        return code;
    }

    String getMessage() {
        return message != null ? message : "Unable to connect to Google Play Services";
    }
}
