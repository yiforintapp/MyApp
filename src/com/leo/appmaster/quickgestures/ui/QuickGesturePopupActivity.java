
package com.leo.appmaster.quickgestures.ui;

import java.util.AbstractList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer.GType;
import com.leo.appmaster.utils.LeoLog;

public class QuickGesturePopupActivity extends Activity {

    private static int switchNum;
    private QuickGestureContainer mContainer;
    private AbstractList<AppItemInfo> list;
    private List<QuickSwitcherInfo> mSwitchList;
    private AppMasterPreference mSpSwitch;
    private String mSwitchListFromSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture_left);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        mContainer = (QuickGestureContainer) findViewById(R.id.gesture_container);
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
    }

    @Override
    protected void onStop() {
        FloatWindowHelper.mGestureShowing = false;
        // 去除系统短信未读提示
        if (FloatWindowHelper.isShowSysNoReadMessage) {
            FloatWindowHelper.isShowSysNoReadMessage = false;
        }
        finish();
        super.onStop();
    }

    private void fillQg1() {
        mContainer.fillGestureItem(GType.DymicLayout, list.subList(0, 7));
    }

    private void fillQg2() {
        mContainer.fillGestureItem(GType.MostUsedLayout, list.subList(7, 17));
    }

    private void fillQg3() {
        mContainer.fillGestureItem(GType.SwitcherLayout, mSwitchList);
    }

    @Override
    protected void onDestroy() {
        FloatWindowHelper.mGestureShowing = false;
        Log.e("############", "" + FloatWindowHelper.isShowSysNoReadMessage);
        // 去除系统短信未读提示
        if (FloatWindowHelper.isShowSysNoReadMessage) {
            FloatWindowHelper.isShowSysNoReadMessage = false;
        }
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

}
