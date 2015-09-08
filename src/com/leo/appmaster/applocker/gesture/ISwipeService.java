package com.leo.appmaster.applocker.gesture;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.ISwipeInterface;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.utils.LeoLog;

/**
 * ISwipe相关Service
 */
public class ISwipeService extends Service {
    private static final String TAG = "ISwipeService";

    public ISwipeService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LeoLog.i(TAG, "onCreate.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        LeoLog.i(TAG, "onBind.");
        return mBinder;
    }

    private final ISwipeInterface.Stub mBinder = new ISwipeInterface.Stub() {

        @Override
        public void onISwipeDismiss() throws RemoteException {
            // 1秒以内不出现锁屏
            LockManager.getInstatnce().filterAllOneTime(1000);
            LeoLog.i(TAG, "onISwipeDismiss");
        }

        @Override
        public List<LockMode> getLockModeList() throws RemoteException {
            LeoLog.i(TAG, "getLockModeList");
            return LockManager.getInstatnce().getLockMode();
        }
    };
}
