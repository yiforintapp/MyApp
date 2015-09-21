
package com.leo.appmaster.quickgestures.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmater.globalbroadcast.PackageChangedListener;

public class IswipePackageChangedListener extends PackageChangedListener {
    private static final String TAG = "IswipePackageChangedListener";
    private static final boolean DBG = false;

    @Override
    public void onPackageChanged(Intent intent) {
        super.onPackageChanged(intent);
        /* 安装ISwipe监听 */
        installISwipe(intent);
    }

    private void installISwipe(Intent intent) {
        final Context context = AppMasterApplication.getInstance();
        String packageName = intent.getData().getSchemeSpecificPart();
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            if (AppLoadEngine.ISWIPE_PACKAGENAME.equals(packageName)) {
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        /* 让主页iswipe不显示 */
                        ISwipUpdateRequestManager.getInstance(context).cancelShowIswipUpdate();
                        /* 取消定时任务 */
                        AppMasterPreference.getInstance(context).setIswipeUpdateTipTime(-1);
                        /* 恢复定时器已经通知的次数默认值 */
                        AppMasterPreference.getInstance(context).setIswipeAlarmNotifiNumber(1);
                        /* 关闭本地iswipe插件，通知iswipe应用开启处理 */
                        boolean isUseIswipe = ISwipUpdateRequestManager.getInstance(context)
                                .isUseIswipUser();
                        if (isUseIswipe) {
                            ISwipUpdateRequestManager.getInstance(context).iswipeOpenHandler();
                        }
                    }
                });
            }
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            if (AppLoadEngine.ISWIPE_PACKAGENAME.equals(packageName)) {
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        /* 让主页iswipe不显示 */
                        ISwipUpdateRequestManager.getInstance(context).cancelShowIswipUpdate();
                        /* 取消定时任务 */
                        AppMasterPreference.getInstance(context).setIswipeUpdateTipTime(-1);
                        /* 恢复定时器已经通知的次数默认值 */
                        AppMasterPreference.getInstance(context).setIswipeAlarmNotifiNumber(1);
                        /* 恢复快捷手势为未使用状态 */
                        AppMasterPreference.getInstance(context).setFristSlidingTip(false);
                    }
                });
            }
        }

    }
}
