
package com.leo.appmaster.quickgestures.ui;

import java.util.AbstractList;
import java.util.List;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;

import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer.GType;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class QuickGesturePopupActivity extends Activity {

    private static int switchNum = 9;
    private QuickGestureContainer mContainer;
    private AbstractList<AppItemInfo> list;
    private List<QuickSwitcherInfo> mSwitchList;
    private AppMasterPreference mSpSwitch;
    private String mSwitchListFromSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture_left);
        mContainer = (QuickGestureContainer) findViewById(R.id.gesture_container);
        mSpSwitch = AppMasterPreference.getInstance(this);
        mSwitchListFromSp = mSpSwitch.getSwitchList();

        list = AppLoadEngine.getInstance(this).getAllPkgInfo();

//        Window window = getWindow();
//        WindowManager.LayoutParams params = window.getAttributes();
//        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        window.setAttributes(params);

        if (mSwitchList == null) {
            if (mSwitchListFromSp.isEmpty()) {
                mSwitchList = QuickSwitchManager.getInstance(this).getSwitchList(switchNum);
            } else {

            }

        }

        fillQg1();
        fillQg2();
        fillQg3();

        mContainer.showOpenAnimation();
    }

    @Override
    protected void onStop() {
        FloatWindowHelper.mGestureShowing = false;
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
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mContainer.showCloseAnimation();
        // super.onBackPressed();
    }

}
