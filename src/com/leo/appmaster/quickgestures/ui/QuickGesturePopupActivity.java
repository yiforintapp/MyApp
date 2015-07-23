
package com.leo.appmaster.quickgestures.ui;

import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.ClickQuickItemEvent;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.quickgestures.view.QgLockModeSelectView;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;

public class QuickGesturePopupActivity extends BaseActivity {

    private AppleWatchContainer mContainer;
    public boolean isItemClick = false;
    private TextView mGestureTipContent;
    private boolean mFromWhiteDot, mFromSelfApp;
    private int mNowLayout;
    private boolean isCloseWindow, ifCreateWhiteFloat;
    private View mSuccessTipView;

    private QgLockModeSelectView mModeSelectView;
    /**
     * 弹窗点击确定后刷新界面，但不走动画，在onResume内不走
     */
    private boolean isCanNotDoAnimation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture_apple_watch);
        LeoEventBus.getDefaultBus().register(this);
        handleIntent();
        initIU();
        checkFirstWhiteClick();
        fillWhichLayoutFitst(mNowLayout, false);
        overridePendingTransition(0, 0);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mFromWhiteDot = intent.getBooleanExtra("from_white_dot", false);
        mFromSelfApp = intent.getBooleanExtra("from_self_app", false);
    }

    public boolean isFromSelfApp() {
        return mFromSelfApp;
    }

    private void initIU() {
        mContainer = (AppleWatchContainer) findViewById(R.id.gesture_container);
        int showOrientation = getIntent().getIntExtra("show_orientation", 0);
        mContainer.setShowOrientation(showOrientation == 0 ? AppleWatchContainer.Orientation.Left
                : AppleWatchContainer.Orientation.Right);
        mNowLayout = mContainer.getNowLayout();

        mSuccessTipView = findViewById(R.id.gesture_success_tip);
        mGestureTipContent = (TextView) findViewById(R.id.gesture_success_content);

        mModeSelectView = (QgLockModeSelectView) findViewById(R.id.mode_select_view);
    }

    private void checkFirstWhiteClick() {
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        int clickCount = amp.getUseStrengthenModeTimes();
        if (mFromWhiteDot && !amp.hasEverCloseWhiteDot() && !BuildProperties.isGTS5282()) {
            amp.setEverCloseWhiteDot(true);
            if (clickCount == 1) {
                mSuccessTipView.setVisibility(View.VISIBLE);
                mGestureTipContent.setText(R.string.white_dot_click_tip);
                Button mKnowbButton = (Button) mSuccessTipView.findViewById(R.id.know_button);
                final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mSuccessTipView,
                        "alpha", 1.0f, 0f).setDuration(200);
                alphaAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSuccessTipView.setVisibility(View.GONE);
                    }
                });
                mKnowbButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alphaAnimator.start();
                    }
                });

                Button setButton = (Button) mSuccessTipView.findViewById(R.id.set_button);
                setButton.setVisibility(View.VISIBLE);
                setButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(QuickGesturePopupActivity.this,
                                QuickGestureSettingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }
    }

    private void fillWhichLayoutFitst(int mNowLayout, boolean mFalg) {
        if (mNowLayout == 1) {
            fillDynamicLayout(mFalg);
        } else if (mNowLayout == 2) {
            fillMostUsedLayout(mFalg);
        } else {
            fillSwitcherLayout(mFalg);
        }
    }

    public void setItemClick(boolean set) {
        LeoLog.d("testiconclick", "setItemClick is true");
        isItemClick = set;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.i("null", "onWindowFocusChanged");

        if (!hasFocus && !QuickGestureManager.isFromDialog) {
            if (!isItemClick) {
                LockManager.getInstatnce().filterAllOneTime(1000);
                LeoLog.d("testiconclick", "go filterAllOneTime");
            }
            isItemClick = false;
            FloatWindowHelper.mGestureShowing = false;
            mContainer.saveGestureType();
            finish();
        }

        QuickGestureManager.isFromDialog = false;
        super.onWindowFocusChanged(hasFocus);
    }

    public void onEventMainThread(ClickQuickItemEvent event) {
        mContainer.checkStatus(event.info);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        int nowLayout = mContainer.getNowLayout();
        isCanNotDoAnimation = true;
        if (QuickGestureManager.isClickSure) {
            LeoLog.d("testActivity", "onNewIntent && nowLayout is : " + mNowLayout);
            fillWhichLayoutFitst(nowLayout, true);
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        Log.i("null", "QuickGesturePopupActivity onResume hideWhiteFloatView");
        // LeoLog.d("testActivity", "onResume");
        FloatWindowHelper.mGestureShowing = true;
        isCloseWindow = false; // 动画结束是否执行去除红点标识
        if (!isCanNotDoAnimation) {
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
        }
        isCanNotDoAnimation = false;
        FloatWindowHelper.hideWhiteFloatView(getApplicationContext());
        showSuccessTip();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "tdau", "qt");
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
        if (!ifCreateWhiteFloat && !FloatWindowHelper.mGestureShowing) {
            mContainer.post(new Runnable() {
                @Override
                public void run() {
                    showWhiteFloatView();
                    ifCreateWhiteFloat = true;
                }
            });
            Log.i("null", "QuickGesturePopupActivity onPause  showWhiteFloatView");
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i("null", "QuickGesturePopupActivity onStop()");
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
        Log.i("null", "QuickGesturePopupActivity onDestroy()");
        LeoEventBus.getDefaultBus().unregister(this);
        if (!isCloseWindow) {
            FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
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
                    if (!ifCreateWhiteFloat) {
                        showWhiteFloatView();
                        ifCreateWhiteFloat = true;
                        Log.i("null", "onBackPressed  showWhiteFloatView");
                    }
                    mContainer.post(new Runnable() {
                        @Override
                        public void run() {
                            FloatWindowHelper.removeAllFloatWindow(getApplicationContext());
                            createFloatView();
                        }
                    });
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
        // 创建热区处理
        isCloseWindow = true;
        // FloatWindowHelper.mGestureShowing = false;
        // 多条短信提示后，未读短信红点提示标记为已读,只有当有红点提示，在关闭的时候才会执行
        if (!QuickGestureManager.getInstance(getApplicationContext()).isMessageReadRedTip
                && (QuickGestureManager.getInstance(getApplicationContext())
                        .getQuiQuickNoReadMessage() != null
                && QuickGestureManager.getInstance(getApplicationContext())
                        .getQuiQuickNoReadMessage().size() > 0)) {
            QuickGestureManager.getInstance(getApplicationContext()).isMessageReadRedTip = true;
            AppMasterPreference.getInstance(getApplicationContext()).setMessageIsRedTip(true);
        }
        // 解决通话未读红点提示后，其他一些原因引起通话记录数据库的改变使红点再次显示
        if (!QuickGestureManager.getInstance(getApplicationContext()).isCallLogRead
                && (QuickGestureManager.getInstance(getApplicationContext()).getQuickNoReadCall() != null
                && QuickGestureManager.getInstance(getApplicationContext()).getQuickNoReadCall()
                        .size() > 0)) {
            QuickGestureManager.getInstance(getApplicationContext()).isCallLogRead = true;
            AppMasterPreference.getInstance(getApplicationContext()).setCallLogIsRedTip(true);
        }

        // 去除所有类型产生的红点
        cancelAllRedPointTip();
        FloatWindowHelper.createFloatWindow(getApplicationContext(),
                QuickGestureManager.getInstance(getApplicationContext()).mSlidAreaSize);
        QuickGestureManager.getInstance(getApplicationContext()).startFloatWindow();
    }

    private void cancelAllRedPointTip() {
        // 去除热区红点和去除未读，运营icon
        FloatWindowHelper.cancelAllRedTip(getApplicationContext());
        // Log.e(FloatWindowHelper.RUN_TAG, "是否显示红点："
        // + QuickGestureManager.getInstance(this).isShowSysNoReadMessage);
        FloatWindowHelper.removeShowReadTipWindow(getApplicationContext());
    }

    /**
     * 显示小白点
     */
    private void showWhiteFloatView() {
        if (AppMasterPreference.getInstance(this).getSwitchOpenStrengthenMode()
                && !QuickGestureManager
                        .getInstance(this).isDialogShowing) {
            // FloatWindowHelper.showWhiteFloatView(QuickGesturePopupActivity.this);
            FloatWindowHelper.removeWhiteFloatView(QuickGesturePopupActivity.this);
            FloatWindowHelper.createWhiteFloatView(QuickGesturePopupActivity.this);
            Log.i("null", "showWhiteFloatView");
        }
    }

    private void showSuccessTip() {
        if (!BuildProperties.isGTS5282()) {
            AppMasterPreference pref = AppMasterPreference.getInstance(getApplicationContext());
            if (pref.getQuickGestureSuccSlideTiped())
                return;

            mSuccessTipView.setVisibility(View.VISIBLE);
            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mSuccessTipView, "alpha",
                    1.0f, 0f).setDuration(200);
            alphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSuccessTipView.setVisibility(View.GONE);
                }
            });
            Button mKnowbButton = (Button) mSuccessTipView.findViewById(R.id.know_button);
            mKnowbButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    alphaAnimator.start();
                }
            });
            pref.setQuickGestureSuccSlideTiped(true);
        }
    }

    public void showLockMode() {
        if (mSuccessTipView.getVisibility() == View.VISIBLE) {
            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mSuccessTipView, "alpha",
                    1.0f, 0f).setDuration(200);
            alphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSuccessTipView.setVisibility(View.GONE);
                }
            });
            alphaAnimator.start();
        }
        mModeSelectView.show();
        mContainer.enterModeSelect();
    }

    public void onModeSelectViewClosed() {
        mContainer.leaveModeSelect();
    }

}
