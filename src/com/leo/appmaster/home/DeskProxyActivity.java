
package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.WeiZhuangActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.sdk.SDKWrapper;

public class DeskProxyActivity extends Activity {
    public static final int mAppLockType = 1;
    public static final int mAppWeiZhuang = 2;
    private boolean mDelayFinish = false;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int type = intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                StatusBarEventService.EVENT_EMPTY);
        if (type == StatusBarEventService.EVENT_EMPTY) {
            mDelayFinish = true;
            mHandler = new Handler();
        } else {
            if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
                Intent mIntent = new Intent(this, LockSettingActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
                startActivity(mIntent);
            } else {
                if (type == mAppLockType) {
                    goToAppLock(type);
                } else if (type == mAppWeiZhuang) {
                    goToAppWeiZhuang(type);
                }
            }
            finish();
        }
    }

    private void goToAppWeiZhuang(int type) {
        Intent intent = new Intent(this, WeiZhuangActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToAppLock(int type) {
        LockManager lm = LockManager.getInstatnce();
        LockMode curMode = lm.getCurLockMode();
        Intent intent;
        if (curMode != null && curMode.defaultFlag == 1 && !curMode.haveEverOpened) {
            intent = new Intent(this, RecommentAppLockListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("target", 0);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
            startActivity(intent);
            curMode.haveEverOpened = true;
            lm.updateMode(curMode);
        } else {
            intent = new Intent(this, AppLockListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.onResume(this);
        if (mDelayFinish && mHandler != null) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "fdau", "view");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SDKWrapper.onPause(this);
        mDelayFinish = false;
        mHandler = null;
    }

}
