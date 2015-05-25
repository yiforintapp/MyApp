
package com.leo.appmaster.quickgestures.ui;

import java.util.AbstractList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.eventbus.event.ClickQuickItemEvent;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.Orientation;
import com.leo.appmaster.utils.LeoLog;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnSystemUiVisibilityChangeListener;

public class QuickGesturePopupActivity extends Activity implements
        OnSystemUiVisibilityChangeListener {

    private static int switchNum;
    private AppleWatchContainer mContainer;
    private AbstractList<AppItemInfo> list;
    private List<QuickSwitcherInfo> mSwitchList;
    private AppMasterPreference mSpSwitch;
    private String mSwitchListFromSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent();
        setContentView(R.layout.pop_quick_gesture_apple_watch);

        // 注册eventBus
        LeoEventBus.getDefaultBus().register(this);

        // Window window = getWindow();
        // WindowManager.LayoutParams params = window.getAttributes();
        // params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        // window.setAttributes(params);
        mContainer = (AppleWatchContainer) findViewById(R.id.gesture_container);
        // mContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        mSpSwitch = AppMasterPreference.getInstance(this);
        list = AppLoadEngine.getInstance(this).getAllPkgInfo();

        if (mSwitchList == null) {
            mSwitchListFromSp = mSpSwitch.getSwitchList();
            switchNum = mSpSwitch.getSwitchListSize();
            LeoLog.d("testFirstInGet", "mSwitchListFromSp : " + mSwitchListFromSp);
            if (mSwitchListFromSp.isEmpty()) {
                mSwitchList = QuickSwitchManager.getInstance(this).getSwitchList(switchNum);
                String saveToSp = QuickSwitchManager.getInstance(this).ListToString(mSwitchList,
                        switchNum);
                mSpSwitch.setSwitchList(saveToSp);
                LeoLog.d("testFirstInGet", "saveToSp:" + saveToSp);
            } else {
                LeoLog.d("testFirstInGet", "get list from sp");
                mSwitchList = QuickSwitchManager.getInstance(this).StringToList(mSwitchListFromSp);
            }

            fillQg1();
            fillQg2();
            fillQg3();
            mContainer.showOpenAnimation();
        }
        overridePendingTransition(-1, -1);
        mContainer.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                LeoLog.e("xxxx", "onFocusChange");
            }
        });
    }

    private void handleIntent() {

    }

    public void onEventMainThread(ClickQuickItemEvent event) {
        mContainer.checkStatus(event.info);
    }

    @Override
    protected void onPause() {
        LeoLog.e("xxxx", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LeoLog.e("xxxx", "onStop");
        FloatWindowHelper.mGestureShowing = false;
        finish();
        super.onStop();
    }

    private void fillQg1() {
        mContainer.fillGestureItem(GType.DymicLayout, list.subList(0, 13));
    }

    private void fillQg2() {
        mContainer.fillGestureItem(GType.MostUsedLayout, list.subList(11, 24));
    }

    private void fillQg3() {
        mContainer.fillGestureItem(GType.SwitcherLayout, mSwitchList);
    }

    @Override
    protected void onDestroy() {
        FloatWindowHelper.mGestureShowing = false;
        // 反注册
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (mContainer.isEditing()) {
            mContainer.leaveEditMode();
        } else {
            mContainer.showCloseAnimation();
        }
        // super.onBackPressed();
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        LeoLog.e("xxxx", "visibility = " + visibility);

    }

}
