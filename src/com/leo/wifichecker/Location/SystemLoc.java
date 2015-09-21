package com.leo.wifichecker.Location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.leo.wifichecker.utils.LogEx;

/**
 * Created by luqingyuan on 15/9/8.
 */
public class SystemLoc implements  ILocationProvider{
    private static final String TAG = "SystemLoc";

    private LocationManager locationManager;
    /**
     * 最近一次更新的地理位置
     */
    private Location mLastLocation;
    /**
     * 定位方式LocationManager.GPS_PROVIDER，NETWORK_PROVIDER...
     */
    private String mProvider;
    private Context mContext;


    public SystemLoc(Context context) {
        mContext = context.getApplicationContext();
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public boolean isAvalible() {
        return mProvider != null;
    }

    @Override
    public boolean start() {
        LogEx.enter();
        mProvider = getProvider();
        if(mProvider == null) {
            return false;
        }
        locationManager.requestLocationUpdates(mProvider, LocationMgr.MIN_UPDATE_TIME,
                LocationMgr.MIN_UPDATE_DISTANCE, locationListener);
        LogEx.leave();
        return true;
    }

    @Override
    public void stop() {
        LogEx.enter();
        locationManager.removeUpdates(locationListener);
        LogEx.leave();
    }

    @Override
    public Location getLastLocation() {
        Location location = null;
        if(locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if(location == null && locationManager
                .isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        if(location == null && locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        mLastLocation = location;
        return location;
    }

    @Override
    public void restart() {
        LogEx.enter();
        stop();
        start();
        LogEx.leave();
    }

    private String getProvider() {
        //检查gps是否可用，可用即用gps
        boolean isGpsEnale = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        String provider = null;
        if(isGpsEnale) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            //创建一个Criteria对象
            Criteria criteria = new Criteria();
            //设置粗略精确度
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            //设置是否需要返回海拔信息
            criteria.setAltitudeRequired(false);
            //设置是否需要返回方位信息
            criteria.setBearingRequired(false);
            //设置是否允许付费服务
            criteria.setCostAllowed(true);
            //设置电量消耗等级
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            //设置是否需要返回速度信息
            criteria.setSpeedRequired(false);

            //根据设置的Criteria对象，获取最符合此标准的provider对象
            provider = locationManager.getBestProvider(criteria, true);
        }
        LogEx.d(TAG, "currentProvider: " + provider);
        return provider;
    }


    //创建位置监听器
    private LocationListener locationListener = new LocationListener(){
        //位置发生改变时调用
        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
        }

        //provider失效时调用
        @Override
        public void onProviderDisabled(String provider) {
            LogEx.d(TAG, "onProviderDisabled");
        }

        //provider启用时调用
        @Override
        public void onProviderEnabled(String provider) {
            LogEx.d(TAG, "onProviderEnabled");
        }

        //状态改变时调用
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
}
