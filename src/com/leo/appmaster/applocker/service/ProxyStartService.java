package com.leo.appmaster.applocker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.leo.appmaster.utils.LeoLog;

/**
 * Created by runlee on 16-1-16.
 */
public class ProxyStartService extends Service {
    private static final String TAG = "ProxyStartService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LeoLog.d(TAG, "PG_ProxyStartService---start service...");
        this.stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}
