
package com.leo.appmaster.quickgestures;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmater.globalbroadcast.NetworkStateListener;

public class IswipeNetworkStateListener extends NetworkStateListener {
    private static final String TAG = "IswipeNetworkStateListener";
    private static final boolean DBG = true;

    @Override
    public void onNetworkStateChange(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) AppMasterApplication.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        Context context = AppMasterApplication.getInstance();
        if (info == null) {
            // Log.e(Constants.RUN_TAG, "无网络");
            ISwipUpdateRequestManager.getInstance(context)
                    .setNetworkStatus(false);
        } else {
            // Log.e(Constants.RUN_TAG, "有网络");
            ISwipUpdateRequestManager.getInstance(context)
                    .setNetworkStatus(true);
            /* 上次因为没有网络未显示，一旦有网络立即显示 */
            ISwipUpdateRequestManager im = ISwipUpdateRequestManager.getInstance(context);
            if (im.getNoNetworkShow()) {
                if (DBG) {
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd H:m:s");
                    String date = sf.format(new Date(System.currentTimeMillis()));
                    Log.e(TAG,
                            "当前网络恢复正常，执行任务  " + AppMasterPreference.getInstance(context)
                                    .getIswipeAlarmNotifiNumber() + "  ,时间：" + date);
                }
                im.showIswipeUpdateNotificationTip();
                int alarmUseNumbers = AppMasterPreference.getInstance(context)
                        .getIswipeAlarmNotifiNumber();
                AppMasterPreference.getInstance(context)
                        .setIswipeAlarmNotifiNumber(alarmUseNumbers + 1);
                im.setNoNetworkShow(false);
            }
        }
    }
}
