
package com.leo.appmater.globalbroadcast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.manager.ADShowTypeRequestManager;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.utils.AppUtil;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ScreenOnOffListener extends BroadcastListener {

    public static final String TAG = "SCREEN ON OFF";

    public final void onEvent(String action) {
        if (Intent.ACTION_SCREEN_OFF.equals(action)
                || Intent.ACTION_SCREEN_ON.equals(action)
                || Intent.ACTION_USER_PRESENT.equals(action)) {
            onScreenChanged(mIntent);
        }
    }

    @Override
    protected final IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        return filter;
    }

    /**
     * added, changed, removed
     */
    public void onScreenChanged(Intent intent) {
        /* 解锁手机加载iSwipe更新数据 */
        loadISwipeUpdateForOnScreen(intent);
//        loadADShowTypeUpdateOnScreen(intent);

    }

//    private void loadADShowTypeUpdateOnScreen(Intent intent)
//    {
//        Context mContext = AppMasterApplication.getInstance();
//
//        if (!AppUtil.isScreenLocked(mContext)
//                && Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
//            ADShowTypeRequestManager.getInstance(mContext).loadADCheckShowType();
//        } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
//            ADShowTypeRequestManager.getInstance(mContext).loadADCheckShowType();
//        }
//
//    }

    private void loadISwipeUpdateForOnScreen(Intent intent) {
        Context mContext = AppMasterApplication.getInstance();
        boolean isLoadData = ISwipUpdateRequestManager.getInstance(mContext).isLoadIswipeData();
        if (!AppUtil.isScreenLocked(mContext)
                && Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            // Log.d(Constants.RUN_TAG, "开屏");
            if (isLoadData) {
                if (ISwipUpdateRequestManager.getInstance(mContext).isUseIswipUser()) {
                    ISwipUpdateRequestManager.getInstance(mContext)
                            .showIswipeAlarmNotificationHandler();
                }
                ISwipUpdateRequestManager.getInstance(mContext).loadIswipCheckNew();
            }
        } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            // Log.d(Constants.RUN_TAG, "解锁");
            if (isLoadData) {
                boolean isUseIswip = ISwipUpdateRequestManager.getInstance(mContext)
                        .isUseIswipUser();
                int iSwipeUpdateFlag = AppMasterPreference.getInstance(mContext)
                        .getIswipUpdateFlag();
                boolean isIswipUpdate = (iSwipeUpdateFlag == 1);
                if (isUseIswip && isIswipUpdate) {
                    ISwipUpdateRequestManager.getInstance(mContext)
                            .showIswipeAlarmNotificationHandler();
                }
                ISwipUpdateRequestManager.getInstance(mContext).loadIswipCheckNew();
            }
        }
    }
}
