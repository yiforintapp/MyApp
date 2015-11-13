package com.leo.appmaster.privacycontact;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by runlee on 15-11-12.
 */
public class LocatinUpdateListener implements LocationListener {
    private LocationManager locationManager;
    private Context mContext;
    private LocatHandlerListener mHandlerListener;

    public LocatinUpdateListener(Context context, LocationManager locationManager, LocatHandlerListener handlerListener) {
        mContext = context.getApplicationContext();
        this.locationManager = locationManager;
        mHandlerListener = handlerListener;
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(mContext, "onLocationChanged", Toast.LENGTH_SHORT).show();
        mHandlerListener.locatHandlerListener();
    }


    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(mContext, "onProviderDisabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(mContext, "onProviderEnabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(mContext, "onStatusChanged", Toast.LENGTH_SHORT).show();
    }

    /*获取位置时监听位置变化处理*/
    public interface LocatHandlerListener {
        public void locatHandlerListener();
    }
}
