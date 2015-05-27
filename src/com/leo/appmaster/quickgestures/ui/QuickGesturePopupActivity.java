
package com.leo.appmaster.quickgestures.ui;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.ClickQuickItemEvent;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.quickgestures.view.GestureItemView;
import com.leo.appmaster.utils.LeoLog;

public class QuickGesturePopupActivity extends Activity {

    private AppleWatchContainer mContainer;
    private ImageView iv_roket, iv_pingtai, iv_yun;
    private List<BaseInfo> mCommonApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture_apple_watch);
//        QuickSwitchManager.getInstance(this).setActivity(this);
        mCommonApps = new ArrayList<BaseInfo>();
        LeoEventBus.getDefaultBus().register(this);
        mContainer = (AppleWatchContainer) findViewById(R.id.gesture_container);
        iv_roket = (ImageView) findViewById(R.id.iv_rocket);
        iv_pingtai = (ImageView) findViewById(R.id.iv_pingtai);
        iv_yun = (ImageView) findViewById(R.id.iv_yun);
        mContainer.setRockey(iv_roket,iv_pingtai,iv_yun);
        
        int showOrientation = getIntent().getIntExtra("show_orientation", 0);
        mContainer.setShowOrientation(showOrientation == 0 ? AppleWatchContainer.Orientation.Left
                : AppleWatchContainer.Orientation.Right);
//        mContainer.setRocket(this);
        fillDynamicLayout();
        overridePendingTransition(-1, -1);
    }

    public void onEventMainThread(ClickQuickItemEvent event) {
        mContainer.checkStatus(event.info);
    }

    @Override
    protected void onResume() {
        mContainer.post(new Runnable() {
            @Override
            public void run() {
                mContainer.showOpenAnimation(new Runnable() {
                    @Override
                    public void run() {
                        fillMostUsedLayout();
                        fillSwitcherLayout();
                    }
                });
            }
        });
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();
        FloatWindowHelper.mGestureShowing = false;
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
