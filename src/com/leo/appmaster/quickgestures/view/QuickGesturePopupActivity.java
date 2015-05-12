
package com.leo.appmaster.quickgestures.view;

import java.util.AbstractList;
import java.util.List;

import com.leo.appmaster.R;

import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer.GType;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class QuickGesturePopupActivity extends Activity {

    private static int switchNum;
    private QuickGestureContainer mContainer;
    private AbstractList<AppItemInfo> list;
    private List<QuickSwitcherInfo> mSwitchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture_left);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        window.setAttributes(params);

        mContainer = (QuickGestureContainer) findViewById(R.id.gesture_container);
        list = AppLoadEngine.getInstance(this).getAllPkgInfo();
        switchNum = 9;
        if (mSwitchList == null) {
            mSwitchList = QuickSwitchManager.getInstance(this).getSwitchList(switchNum);
        }

        fillQg1();
        fillQg2();
        fillQg3();

        mContainer.showOpenAnimation();
    }

    @Override
    protected void onStop() {
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
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
//        mContainer.showCloseAnimation();
         super.onBackPressed();
    }
}
