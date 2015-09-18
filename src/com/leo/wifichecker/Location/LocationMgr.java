package com.leo.wifichecker.Location;

import android.content.Context;
import android.location.Location;

import com.leo.wifichecker.utils.LogEx;

/**
 * Created by luqingyuan on 15/9/8.
 */
public class LocationMgr implements  ILocationProvider{
    private static final String TAG = "LocationMgr";
    /**
     * 最小更新距离
     */
    public static final long MIN_UPDATE_DISTANCE = 100; // 20 meters
    /**
     * 最短更新时间间隔
     */
    public static long MIN_UPDATE_TIME = 1000 * 60 * 1; //1 minute
    private ILocationProvider mCurrentLoc;
    private Context mContext;

    public LocationMgr(Context context) {
        mContext = context.getApplicationContext();
        // mCurrentLoc = new GoogleLoc(mContext);
        // if(!mCurrentLoc.isAvalible()) {
        mCurrentLoc = new SystemLoc(mContext);
        // LogEx.d(TAG,"use system loc ");
        // } else {
        // LogEx.d(TAG,"use google loc ");
        // }
    }

    /**
     * 定位服务是否可用
     * @return 是否可用
     */
    @Override
    public boolean isAvalible() {
        if(mCurrentLoc == null) {
            return false;
        }
        return mCurrentLoc.isAvalible();
    }

    /**
     * 启动定位服务
     * @return 是否成功
     */
    @Override
    public boolean start() {
        LogEx.enter();
        if(mCurrentLoc == null) {
            return false;
        }
        LogEx.leave();
        return mCurrentLoc.start();
    }

    /**
     * 停止定位服务
     */
    @Override
    public void stop() {
        LogEx.enter();
        if(mCurrentLoc == null) {
            return;
        }
        mCurrentLoc.stop();
        mCurrentLoc = null;
        LogEx.leave();
    }

    @Override
    public void restart() {
        LogEx.enter();
        stop();
        start();
        LogEx.leave();
    }
    /**
     * 获取最近一次定位的位置信息
     * @return
     */
    @Override
    public Location getLastLocation() {
        if(mCurrentLoc == null) {
            return null;
        }
        Location lc = mCurrentLoc.getLastLocation();
        //当GoogleLoc未连接上时，获取地理位置为null, 此时尝试从系统获取
        if(lc == null && !(mCurrentLoc instanceof SystemLoc)) {
            SystemLoc location = new SystemLoc(mContext);
            lc = location.getLastLocation();
        }
        String msg = "getLastLocation null";
        if(lc != null) {
            msg = "getLastLocation lat="+ lc.toString();
        }
        LogEx.d(TAG,msg);
        return lc;
    }
}
