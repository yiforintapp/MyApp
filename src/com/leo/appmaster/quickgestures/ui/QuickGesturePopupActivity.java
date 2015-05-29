
package com.leo.appmaster.quickgestures.ui;

import java.util.List;

import android.os.Bundle;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.ClickQuickItemEvent;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.utils.LeoLog;

public class QuickGesturePopupActivity extends BaseActivity {

    private AppleWatchContainer mContainer;
    private AppMasterPreference mPref;
    private int mNowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture_apple_watch);
        LeoEventBus.getDefaultBus().register(this);
        mContainer = (AppleWatchContainer) findViewById(R.id.gesture_container);

        int showOrientation = getIntent().getIntExtra("show_orientation", 0);
        mContainer.setShowOrientation(showOrientation == 0 ? AppleWatchContainer.Orientation.Left
                : AppleWatchContainer.Orientation.Right);
        mContainer.showGestureLayout(mContainer.getCurrentGestureType());
        mPref = AppMasterPreference.getInstance(this);
        mNowLayout = mContainer.getNowLayout();
        LeoLog.d("AppleWatchContainer", "刚来Activity！show 出的是：" + mNowLayout);

        fillWhichLayoutFitst(mNowLayout);
        
        overridePendingTransition(-1, -1);
    }

    private void fillWhichLayoutFitst(int mNowLayout) {
        if (mNowLayout == 1) {
            fillDynamicLayout();
        } else if (mNowLayout == 2) {
            fillMostUsedLayout();
        } else {
            fillSwitcherLayout();
        }
    }

    public void onEventMainThread(ClickQuickItemEvent event) {
        mContainer.checkStatus(event.info);
    }

    @Override
    protected void onResume() {
        FloatWindowHelper.mGestureShowing = true;
        mContainer.post(new Runnable() {
            @Override
            public void run() {
                mContainer.showOpenAnimation(new Runnable() {
                    @Override
                    public void run() {
                        fillTwoLayout(mNowLayout);
                    }

                });
            }
        });
        super.onResume();
    }

    private void fillTwoLayout(int mNowLayout) {
        if (mNowLayout == 1) {
            fillMostUsedLayout();
            fillSwitcherLayout();
        } else if (mNowLayout == 2) {
            fillDynamicLayout();
            fillSwitcherLayout();
        } else {
            fillDynamicLayout();
            fillMostUsedLayout();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        FloatWindowHelper.mGestureShowing = false;
        finish();
        super.onStop();
    }

    private void fillDynamicLayout() {
        List<BaseInfo> items = QuickGestureManager.getInstance(this).getDynamicList();
        mContainer.fillGestureItem(GType.DymicLayout, items);
    }

    private void fillMostUsedLayout() {
        List<BaseInfo> items = QuickGestureManager.getInstance(this).getMostUsedList();
        mContainer.fillGestureItem(GType.MostUsedLayout, items);
    }

    private void fillSwitcherLayout() {
        List<BaseInfo> items = QuickGestureManager.getInstance(this).getSwitcherList();
        mContainer.fillGestureItem(GType.SwitcherLayout, items);
    }

    @Override
    protected void onDestroy() {
        FloatWindowHelper.mGestureShowing = false;
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
    }

}
