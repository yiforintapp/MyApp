
package com.leo.appmaster.quickgestures.ui;

import java.util.List;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Button;

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
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.utils.LeoLog;

public class QuickGesturePopupActivity extends BaseActivity {

    private AppleWatchContainer mContainer;
    public boolean isItemClick = false;
    private View mGestureTipTitle;
    private TextView mGestureTipContent;
    private boolean mFromWhiteDot;
    private int mNowLayout;
    private boolean isCloseWindow, ifCreateWhiteFloat;
    private View mSuccessTipView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture_apple_watch);
        LeoEventBus.getDefaultBus().register(this);
        handleIntent();
        initIU();
        checkFirstWhiteClick();
        fillWhichLayoutFitst(mNowLayout);
        overridePendingTransition(0, 0);
    }

    private void handleIntent() {
        mFromWhiteDot = getIntent().getBooleanExtra("from_white_dot", false);
    }

    private void initIU() {
        mContainer = (AppleWatchContainer) findViewById(R.id.gesture_container);
        int showOrientation = getIntent().getIntExtra("show_orientation", 0);
        mContainer.setShowOrientation(showOrientation == 0 ? AppleWatchContainer.Orientation.Left
                : AppleWatchContainer.Orientation.Right);
        mNowLayout = mContainer.getNowLayout();

        mSuccessTipView = findViewById(R.id.gesture_success_tip);
        mGestureTipTitle = findViewById(R.id.gesture_success_title);
        mGestureTipContent = (TextView) findViewById(R.id.gesture_success_content);
    }

    private void checkFirstWhiteClick() {
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        if (mFromWhiteDot && !amp.hasEverCloseWhiteDot()) {
            int clickCount = amp.getUseStrengthenModeTimes();
            if (clickCount == 1) {
                mSuccessTipView.setVisibility(View.VISIBLE);
                mGestureTipTitle.setVisibility(View.GONE);
                mGestureTipContent.setText(R.string.white_dot_click_tip);
                Button mKnowbButton = (Button) mSuccessTipView.findViewById(R.id.know_button);
                final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mSuccessTipView,
                        "alpha", 1.0f, 0f).setDuration(200);
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
                                QuickGestureActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        }
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

    public void setItemClick(boolean set) {
        LeoLog.d("testiconclick", "setItemClick is true");
        isItemClick = set;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            if (!isItemClick) {
                LockManager.getInstatnce().filterAllOneTime(1000);
                LeoLog.d("testiconclick", "go filterAllOneTime");
            }
            isItemClick = false;
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
        Log.i("null", "onResume");
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
        FloatWindowHelper.hideWhiteFloatView(getApplicationContext());
        showSuccessTip();
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
        Log.i("null", "onDestroy");
        LeoEventBus.getDefaultBus().unregister(this);
        FloatWindowHelper.mGestureShowing = false;
        if (!isCloseWindow) {
            createFloatView();
            Log.i("null", "onDestroy  createFloatView");
        }
        if (!ifCreateWhiteFloat) {
            showWhiteFloatView();
            Log.i("null", "onDestroy  showWhiteFloatView");
            ifCreateWhiteFloat = true;
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
                    if (!ifCreateWhiteFloat) {
                        showWhiteFloatView();
                        ifCreateWhiteFloat = true;
                    }
                }
            });
            mContainer.saveGestureType();
            Log.i("null", "onBackPressed");
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
        FloatWindowHelper.mGestureShowing = false;
        FloatWindowHelper.createFloatWindow(getApplicationContext(),
                QuickGestureManager.getInstance(getApplicationContext()).mSlidAreaSize);
        QuickGestureManager.getInstance(getApplicationContext()).startFloatWindow();
        isCloseWindow = false;
        // 多条短信提示后，未读短信红点提示标记为已读,只有当有红点提示，在关闭的时候才会执行
        if (!QuickGestureManager.getInstance(getApplicationContext()).isMessageReadRedTip
                && (QuickGestureManager.getInstance(getApplicationContext()).mMessages != null
                && QuickGestureManager.getInstance(getApplicationContext()).mMessages.size() > 0)) {
            QuickGestureManager.getInstance(getApplicationContext()).isMessageReadRedTip = true;
            AppMasterPreference.getInstance(getApplicationContext()).setMessageIsRedTip(true);
        }
        // 解决通话未读红点提示后，其他一些原因引起通话记录数据库的改变使红点再次显示
        if (!QuickGestureManager.getInstance(getApplicationContext()).isCallLogRead
                && (QuickGestureManager.getInstance(getApplicationContext()).mCallLogs != null
                && QuickGestureManager.getInstance(getApplicationContext()).mCallLogs.size() > 0)) {
            QuickGestureManager.getInstance(getApplicationContext()).isCallLogRead = true;
            AppMasterPreference.getInstance(getApplicationContext()).setCallLogIsRedTip(true);
        }
        // 去除热区红点和去除未读，运营icon
        FloatWindowHelper.cancelAllRedTip(getApplicationContext());
        FloatWindowHelper.removeShowReadTipWindow(getApplicationContext());
    }

    private void showWhiteFloatView() {
        Log.i("null", "FloatWindowHelper.mGestureShowing = " + FloatWindowHelper.mGestureShowing);
        if (AppMasterPreference.getInstance(this).getSwitchOpenStrengthenMode()) {
            mContainer.post(new Runnable() {
                @Override
                public void run() {
                    // FloatWindowHelper.showWhiteFloatView(QuickGesturePopupActivity.this);
                    FloatWindowHelper.removeWhiteFloatView(getApplicationContext());
                    FloatWindowHelper.createWhiteFloatView(getApplicationContext());
                    Log.i("null", "showWhiteFloatView'");
                }
            });
        }
    }

    private void showSuccessTip() {
        AppMasterPreference pref = AppMasterPreference.getInstance(getApplicationContext());
        if (pref.getQuickGestureSuccSlideTiped())
            return;

        mSuccessTipView.setVisibility(View.VISIBLE);
        final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mSuccessTipView, "alpha",
                1.0f, 0f).setDuration(200);
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
