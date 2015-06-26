
package com.leo.appmaster.quickgestures.ui;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.ClickQuickItemEvent;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.model.QuickGestureContactTipInfo;
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
        // Log.e("#########", "快捷手势进入onCreate时间："+System.currentTimeMillis());
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
        // Log.e("#########", "快捷手势进入结束onCreate时间："+System.currentTimeMillis());
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
            FloatWindowHelper.mGestureShowing = false;
            mContainer.saveGestureType();
            finish();
        } else {
            /*
             * 快捷手势界面创建后停止创建热区的任务，解决关闭界面时立即创建热区与任务创建热区之间的时间差，引起的创建问题
             */
            FloatWindowHelper.stopFloatWindowCreate(getApplicationContext());
        }

        super.onWindowFocusChanged(hasFocus);
    }

    public void onEventMainThread(ClickQuickItemEvent event) {
        mContainer.checkStatus(event.info);
    }

    @Override
    protected void onResume() {
        // Log.e("#########", "快捷手势进入onResume时间："+System.currentTimeMillis());
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
        // Log.e("#########", "快捷手势结束onResume时间："+System.currentTimeMillis());
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
        LockManager.getInstatnce().filterAllOneTime(500);
        if (mContainer.isEditing()) {
            mContainer.leaveEditMode();
        } else {
            mContainer.showCloseAnimation();
            mContainer.saveGestureType();
            FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
            createFloatView();
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
        cancelAllRedTip(getApplicationContext());
        // 创建热区处理
        isCloseWindow = true;
        FloatWindowHelper.mGestureShowing = false;
        FloatWindowHelper.createFloatWindow(getApplicationContext(),
                QuickGestureManager.getInstance(getApplicationContext()).mSlidAreaSize);
        mContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                /* 再次为mGestureShowing赋值，解决偶尔出现创建热区时mGestureShowing值没有即使更新问题 */
                FloatWindowHelper.mGestureShowing = false;
                QuickGestureManager.getInstance(getApplicationContext()).startFloatWindow();
                isCloseWindow = false;
            }
        }, 500);
        // 多条短信提示后，未读短信红点提示标记为已读
        if (!QuickGestureManager.getInstance(getApplicationContext()).isMessageRead) {
            QuickGestureManager.getInstance(getApplicationContext()).isMessageRead = true;
            AppMasterPreference.getInstance(getApplicationContext()).setMessageIsRedTip(true);
        }
    }

    // 去除热区红点，未读，运营icon和红点
    private void cancelAllRedTip(Context context) {
        // 隐私通话
        if (QuickGestureManager.getInstance(context).isShowPrivacyCallLog) {
            QuickGestureManager.getInstance(context).isShowSysNoReadMessage = false;
            QuickGestureManager.getInstance(context).isShowPrivacyCallLog = false;
            AppMasterPreference.getInstance(context).setQuickGestureCallLogTip(
                    false);
        }
        // 隐私短信
        if (QuickGestureManager.getInstance(context).isShowPrivacyMsm) {
            QuickGestureManager.getInstance(context).isShowSysNoReadMessage = false;
            QuickGestureManager.getInstance(context).isShowPrivacyMsm = false;
            AppMasterPreference.getInstance(context).setQuickGestureMsmTip(false);
        }
        // 短信，通话记录
        if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage) {
            QuickGestureManager.getInstance(context).isShowSysNoReadMessage = false;
            if (QuickGestureManager.getInstance(context).mCallLogs != null) {
                QuickGestureManager.getInstance(context).mCallLogs.clear();
            }
            if (QuickGestureManager.getInstance(context).mMessages != null) {
                QuickGestureManager.getInstance(context).mMessages.clear();
            }
        }
        // 运营
        if (!AppMasterPreference.getInstance(context).getLastBusinessRedTipShow()) {
            AppMasterPreference.getInstance(context).setLastBusinessRedTipShow(true);
        }
    }
}
