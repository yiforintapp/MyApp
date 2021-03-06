package com.zlf.appmaster.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.zlf.appmaster.home.TradeTabFragment;
import com.zlf.appmaster.utils.LeoLog;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.Timer;

/**
 * Created by Administrator on 2016/12/14.
 */
public class TimeService extends Service {
    private boolean isStart = false;
    private static final long CHECKTIME = 10000;
    private String TAG = "TimeService";
    private Timer mTimer = null;
    private SimpleDateFormat mSdf = null;
    private Intent mTimeIntent = null;
    private Bundle mBundle = null;

    private DataHandler mHandle;
    public final static int REFRESH_TIME = 1;

    //用于处理消息的Handler
    private static class DataHandler extends Handler {
        WeakReference<TimeService> mActivityReference;

        public DataHandler(TimeService service) {
            super();
            mActivityReference = new WeakReference<TimeService>(service);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TimeService service = mActivityReference.get();
            if (service == null) {
                return;
            }

            if (REFRESH_TIME == msg.what) {
                service.sendTimeChangedBroadcast();
            }
            service.sendMessage();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LeoLog.d(TAG, "TimeService->onCreate");
    }

    /**
     * 相关变量初始化
     */
    private void init() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mSdf == null) {
            mSdf = new SimpleDateFormat("yyyy年MM月dd日 " + "hh:mm:ss");
        }
        if (mTimeIntent == null) {
            mTimeIntent = new Intent();
        }
        if (mBundle== null) {
            mBundle = new Bundle();
        }
        if (mHandle == null) {
            mHandle = new DataHandler(this);
        }
    }

    /**
     * 发送广播，通知UI层时间已改变
     */
    private void sendTimeChangedBroadcast() {

        mTimeIntent.setAction(TradeTabFragment.TIME_CHANGED_ACTION);
        //发送广播，通知UI层时间改变了
        sendBroadcast(mTimeIntent);
        LeoLog.d(TAG, "send");
    }

    /**
     * 获取最新系统时间
     *
     * @return
     */
    private String getTime() {
        return mSdf.format(System.currentTimeMillis());
    }

    @Override
    public ComponentName startService(Intent service) {
        LeoLog.d(TAG, "TimeService->startService");
        return super.startService(service);
    }


    public class MyBinder extends Binder {
        public TimeService getService() {
            return TimeService.this;
        }
    }

    private MyBinder binder = new MyBinder();
    private final Random generator = new Random();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LeoLog.d(TAG, "TestService -> onStartCommand, startId: " + startId + ", Thread: " + Thread.currentThread().getName());
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LeoLog.d(TAG, "TestService -> onBind, Thread: " + Thread.currentThread().getName());
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LeoLog.d(TAG, "TestService -> onUnbind, from:" + intent.getStringExtra("from"));
        return false;
    }

    @Override
    public void onDestroy() {
        LeoLog.d(TAG, "TestService -> onDestroy, Thread: " + Thread.currentThread().getName());
        stopTimer();
        super.onDestroy();
    }

    public void stopTimer() {
        isStart = false;
        if (mTimer != null) {
            mTimer.purge();
            mTimer.cancel();
            mTimer = null;
        }
        if (mHandle != null) {
            mHandle.removeMessages(REFRESH_TIME);
            mHandle = null;
        }
    }

    //getRandomNumber是Service暴露出去供client调用的公共方法
    public int getRandomNumber(){
        return generator.nextInt();
    }

    public void startTimer() {
        LeoLog.d(TAG, "startTimer init");
        this.init();
        if(!isStart){
            sendMessage();
            isStart = true;
        }
    }

    private void sendMessage() {
        if (mHandle != null) {
            Message message = mHandle.obtainMessage();
            message.what = REFRESH_TIME;
            mHandle.sendMessageDelayed(message, CHECKTIME);
        }
    }
}