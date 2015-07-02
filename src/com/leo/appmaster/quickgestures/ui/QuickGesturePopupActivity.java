
package com.leo.appmaster.quickgestures.ui;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
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
    private int mNowLayout;
    private boolean isCloseWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long startTime = System.currentTimeMillis();
        setContentView(R.layout.pop_quick_gesture_apple_watch);
        LeoEventBus.getDefaultBus().register(this);
        mContainer = (AppleWatchContainer) findViewById(R.id.gesture_container);

        int showOrientation = getIntent().getIntExtra("show_orientation", 0);
        mContainer.setShowOrientation(showOrientation == 0 ? AppleWatchContainer.Orientation.Left
                : AppleWatchContainer.Orientation.Right);
        mNowLayout = mContainer.getNowLayout();

        fillWhichLayoutFitst(mNowLayout);
        overridePendingTransition(0, 0);
        long finishTime = System.currentTimeMillis();
        long duringTime = finishTime - startTime;
        LeoLog.d("testDuring", "Time is : " + duringTime);
    }

    private void fillWhichLayoutFitst(int mNowLayout) {
        if (mNowLayout == 1) {
            fillDynamicLayout(false);
        } else if (mNowLayout == 2) {
            fillMostUsedLayout(false);
        } else {
            fillSwitcherLayout(false);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            LockManager.getInstatnce().filterAllOneTime(1000);
            FloatWindowHelper.mGestureShowing = false;
            mContainer.saveGestureType();
            finish();
        }
        super.onWindowFocusChanged(hasFocus);
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
            fillMostUsedLayout(true);
            fillSwitcherLayout(true);
        } else if (mNowLayout == 2) {
            fillDynamicLayout(true);
            fillSwitcherLayout(true);
        } else {
            fillDynamicLayout(true);
            fillMostUsedLayout(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void fillDynamicLayout(boolean loadExtra) {
        List<BaseInfo> items = QuickGestureManager.getInstance(this).getDynamicList();
        mContainer.fillGestureItem(GType.DymicLayout, items, loadExtra);
    }

    private void fillMostUsedLayout(boolean loadExtra) {
        List<BaseInfo> items = QuickGestureManager.getInstance(this).getMostUsedList();
        mContainer.fillGestureItem(GType.MostUsedLayout, items, loadExtra);
    }

    private void fillSwitcherLayout(boolean loadExtra) {
        List<BaseInfo> items = QuickGestureManager.getInstance(this).getSwitcherList();
        mContainer.fillGestureItem(GType.SwitcherLayout, items, loadExtra);
    }

    @Override
    protected void onDestroy() {
        LeoEventBus.getDefaultBus().unregister(this);
        FloatWindowHelper.mGestureShowing = false;
        if (!isCloseWindow) {
            createFloatView();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mContainer.isEditing()) {
            mContainer.leaveEditMode();
        } else {
            mContainer.showCloseAnimation(new Runnable() {
                @Override
                public void run() {
                    FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
                    createFloatView();
                }
            });
            mContainer.saveGestureType();
        }
    }

    @Override
    public void finish() {
        overridePendingTransition(0, 0);
        super.finish();
    }

    /* 快捷手势消失，立即创建响应热区 */
    private void createFloatView() {
        // 去除热区红点和去除未读，运营icon
        FloatWindowHelper.cancelAllRedTip(getApplicationContext());
        // 创建热区处理
        isCloseWindow = true;
        FloatWindowHelper.mGestureShowing = false;
        FloatWindowHelper.createFloatWindow(getApplicationContext(),
                QuickGestureManager.getInstance(getApplicationContext()).mSlidAreaSize);
        QuickGestureManager.getInstance(getApplicationContext()).startFloatWindow();
        isCloseWindow = false;
        // 多条短信提示后，未读短信红点提示标记为已读
        if (!QuickGestureManager.getInstance(getApplicationContext()).isMessageRead) {
            QuickGestureManager.getInstance(getApplicationContext()).isMessageRead = true;
            AppMasterPreference.getInstance(getApplicationContext()).setMessageIsRedTip(true);
        }
        // 解决通话未读红点提示后，其他一些原因引起通话记录数据库的改变使红点再次显示
        if (!QuickGestureManager.getInstance(getApplicationContext()).isCallLogRead) {
            QuickGestureManager.getInstance(getApplicationContext()).isCallLogRead = true;
            AppMasterPreference.getInstance(getApplicationContext()).setCallLogIsRedTip(true);
        }
    }

}
