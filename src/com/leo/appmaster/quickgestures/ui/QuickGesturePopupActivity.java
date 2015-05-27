
package com.leo.appmaster.quickgestures.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.ClickQuickItemEvent;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.QuickGestureManager.AppLauncherRecorder;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickGsturebAppInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.quickgestures.view.GestureItemView;
import com.leo.appmaster.utils.LeoLog;

public class QuickGesturePopupActivity extends Activity {

    private static int switchNum;
    private AppleWatchContainer mContainer;
    private List<BaseInfo> mSwitchList;
    private AppMasterPreference mSpSwitch;
    private String mSwitchListFromSp;
    private ImageView iv_roket, iv_pingtai, iv_yun;
    private List<BaseInfo> mCommonApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture_apple_watch);
        QuickSwitchManager.getInstance(this).setActivity(this);
        mCommonApps = new ArrayList<BaseInfo>();
        mSpSwitch = AppMasterPreference.getInstance(this);
        LeoEventBus.getDefaultBus().register(this);
        mContainer = (AppleWatchContainer) findViewById(R.id.gesture_container);
        iv_roket = (ImageView) findViewById(R.id.iv_rocket);
        iv_pingtai = (ImageView) findViewById(R.id.iv_pingtai);
        iv_yun = (ImageView) findViewById(R.id.iv_yun);

        int showOrientation = getIntent().getIntExtra("show_orientation", 0);
        mContainer.setShowOrientation(showOrientation == 0 ? AppleWatchContainer.Orientation.Left
                : AppleWatchContainer.Orientation.Right);
        mContainer.setRocket(this);
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
        FloatWindowHelper.mGestureShowing = false;
        finish();
        FloatWindowHelper.mGestureShowing = false;
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

    public void rockeyAnimation(GestureItemView tv, final int mLayoutBottom, int mRocketX,
            int mRocketY, final QuickSwitcherInfo info) {
        int smallRockeyX = mRocketX - iv_roket.getWidth() / 2;
        int smallRockeyY = mRocketY - iv_roket.getHeight() / 2;
        LeoLog.d("AppleWatchContainer", " smallRockeyX : " + smallRockeyX
                + " ;  smallRockeyY : " + smallRockeyY);

        float iv_roketScaleX = iv_roket.getScaleX();
        float iv_width = iv_roket.getWidth() * iv_roketScaleX;
        float iv_height = iv_roket.getHeight() * iv_roketScaleX;
        LeoLog.d("AppleWatchContainer", " iv_roket.getScaleX: " + iv_roketScaleX
                + " ;  iv_roket.getWidth : " + iv_width);

        MarginLayoutParams margin = new MarginLayoutParams(iv_roket.getLayoutParams());
        margin.width = (int) iv_width;
        margin.height = (int) iv_height;
        margin.setMargins(smallRockeyX, smallRockeyY, smallRockeyX + iv_roket.getWidth(),
                smallRockeyY + iv_roket.getHeight());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
        iv_roket.setLayoutParams(layoutParams);
        iv_roket.setVisibility(View.VISIBLE);

        ObjectAnimator turnBig = ObjectAnimator.ofFloat(iv_roket, "scaleX", 1f, 1.3f);
        ObjectAnimator turnBig2 = ObjectAnimator.ofFloat(iv_roket, "scaleY", 1f, 1.3f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(turnBig).with(turnBig2);
        animSet.setDuration(400);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                iv_pingtai.setVisibility(View.VISIBLE);
                final int mScreenHeight = mContainer.getHeight();
                int mScreenWidth = mContainer.getWidth();
                int transY = (int) iv_pingtai.getTranslationY();
                int mRockeyMoveX = mScreenWidth / 2
                        - (iv_roket.getLeft() + iv_roket.getWidth() / 2);
                int mRockeyMoveY = mLayoutBottom - iv_roket.getHeight() - iv_roket.getTop();
                ObjectAnimator moveToX = ObjectAnimator.ofFloat(iv_roket, "translationX",
                        iv_roket.getTranslationX(), mRockeyMoveX);
                ObjectAnimator moveToY = ObjectAnimator.ofFloat(iv_roket, "translationY",
                        iv_roket.getTranslationY(), mRockeyMoveY);
                ObjectAnimator pingtaiMoveToY = ObjectAnimator
                        .ofFloat(iv_pingtai, "translationY", mScreenHeight, transY);
                AnimatorSet animMoveSet = new AnimatorSet();
                animMoveSet.play(moveToX).with(moveToY).with(pingtaiMoveToY);
                animMoveSet.setDuration(800);
                animMoveSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        iv_yun.setVisibility(View.VISIBLE);
                        ObjectAnimator mRocketmoveToY = ObjectAnimator.ofFloat(iv_roket,
                                "translationY",
                                iv_roket.getTranslationY(), -mScreenHeight);
                        ObjectAnimator pingtaiMoveDownToY = ObjectAnimator
                                .ofFloat(iv_pingtai, "translationY", iv_pingtai.getTranslationY(),
                                        mScreenHeight);
                        ObjectAnimator yunComeOut = ObjectAnimator.ofFloat(iv_yun, "alpha", 0.0f,
                                1f);
                        ObjectAnimator yunLeave = ObjectAnimator.ofFloat(iv_yun, "alpha", 1.0f,
                                0.0f);
                        AnimatorSet animMoveGoSet = new AnimatorSet();
                        animMoveGoSet.play(mRocketmoveToY).with(pingtaiMoveDownToY)
                                .with(yunComeOut).before(yunLeave);
                        animMoveGoSet.setDuration(800);
                        animMoveGoSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // all return
                                iv_roket.setVisibility(View.INVISIBLE);
                                iv_pingtai.setVisibility(View.INVISIBLE);
                                ObjectAnimator turnSmall = ObjectAnimator.ofFloat(iv_roket,
                                        "scaleX", 1.3f, 1.0f);
                                ObjectAnimator turnSmall2 = ObjectAnimator.ofFloat(iv_roket,
                                        "scaleY", 1.3f, 1.0f);
                                ObjectAnimator returnX = ObjectAnimator.ofFloat(iv_roket,
                                        "translationX", iv_roket.getTranslationX(), 0);
                                ObjectAnimator returnY = ObjectAnimator.ofFloat(iv_roket,
                                        "translationY", iv_roket.getTranslationY(), 0);
                                ObjectAnimator pingtai_returnY = ObjectAnimator.ofFloat(iv_pingtai,
                                        "translationY", iv_pingtai.getTranslationY(), 0);
                                AnimatorSet returnAnimation = new AnimatorSet();
                                returnAnimation.play(turnSmall).with(turnSmall2).with(returnX)
                                        .with(returnY).with(pingtai_returnY);
                                returnAnimation.setDuration(200);
                                returnAnimation.start();
                                // make normal iCon
                                mContainer.makeNormalIcon(info);
                            }
                        });
                        animMoveGoSet.start();
                    }
                });
                animMoveSet.start();
            }
        });
        animSet.start();
    }
}
