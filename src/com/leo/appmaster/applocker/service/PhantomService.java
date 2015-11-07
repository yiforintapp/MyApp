package com.leo.appmaster.applocker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PhantomService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, TaskDetectService.getNotification(getApplicationContext()));
        stopSelf();
        return Service.START_NOT_STICKY;
    }

}
