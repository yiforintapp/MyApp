package com.leo.appmaster.intruderprotection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.receiver.DeviceReceiverNewOne;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.UserPresentEvent;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.LeoLog;

/**
 * Created by chenfs on 16-3-15.
 */
public class SystLockStatusReceiver extends BroadcastReceiver {
//    private PrivacyDataManager mPDManager;
//    private IntrudeSecurityManager mISManager;
private LockManager mLockManager;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_USER_PRESENT.equals(action)) {

            LeoEventBus.getDefaultBus().post(new UserPresentEvent(EventId.EVENT_USER_PRESENT_ID, "user_present_or_boost"));

            if (mLockManager == null) {
                mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            }

//            mLockManager.filterPackage(context.getPackageName(),1000);
            if (IntrudeSecurityManager.sHasTakenAtLock && IntrudeSecurityManager.sHasPicSaved) {
                IntrudeSecurityManager.sHasShowedWhenUnlock = true;
                final Intent intent2 = new Intent(AppMasterApplication.getInstance(), IntruderCatchedActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra("pkgname", "from_systemlock");
                long delayTime = DeviceReceiverNewOne.NORMAL_TIME;
                if (IntrudeSecurityManager.sEnterBrowser) {
                    delayTime = DeviceReceiverNewOne.LONG_TIME;
                }
                ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                    @Override
                    public void run() {
                        mLockManager.filterPackage(context.getPackageName(), 1000);
                        context.startActivity(intent2);
                        IntrudeSecurityManager.sEnterBrowser = false;
                        IntrudeSecurityManager.sHasTakenAtLock = false;
                        LeoLog.d("poha_admin", "set sHasTakenAtLock false 2");
                    }
                }, delayTime);
                LeoLog.i("delayTime", " 11 " + delayTime);
            } else if(IntrudeSecurityManager.sHasTakenAtLock && !IntrudeSecurityManager.sHasPicSaved){
                IntrudeSecurityManager.sHasShowedWhenUnlock = false;
            }
            IntrudeSecurityManager.sFailTimesAtSystLock = 0;
        }

    }

}
