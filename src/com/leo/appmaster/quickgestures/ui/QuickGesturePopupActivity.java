
package com.leo.appmaster.quickgestures.ui;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.ClickQuickItemEvent;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.quickgestures.view.GestureItemView;
import com.leo.appmaster.utils.LeoLog;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class QuickGesturePopupActivity extends Activity implements
        OnSystemUiVisibilityChangeListener {

    private static int switchNum;
    private AppleWatchContainer mContainer;
    private List<BaseInfo> list;
    private List<BaseInfo> mSwitchList;
    private AppMasterPreference mSpSwitch;
    private String mSwitchListFromSp;
    private ImageView iv_roket, iv_pingtai, iv_yun;
    private WindowManager wm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent();
        setContentView(R.layout.pop_quick_gesture_apple_watch);
        QuickSwitchManager.getInstance(this).setActivity(this);
        
        // 注册eventBus
        LeoEventBus.getDefaultBus().register(this);
        wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        // Window window = getWindow();
        // WindowManager.LayoutParams params = window.getAttributes();
        // params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        // window.setAttributes(params);
        
        mContainer = (AppleWatchContainer) findViewById(R.id.gesture_container);
        iv_roket = (ImageView) findViewById(R.id.iv_rocket);
        iv_pingtai = (ImageView) findViewById(R.id.iv_pingtai);
        iv_yun = (ImageView) findViewById(R.id.iv_yun);
        mContainer.setRocket(this);

        mSpSwitch = AppMasterPreference.getInstance(this);
        list = new ArrayList<BaseInfo>();
        list.addAll(AppLoadEngine.getInstance(this).getAllPkgInfo());

        if (mSwitchList == null) {
            mSwitchListFromSp = mSpSwitch.getSwitchList();
            switchNum = mSpSwitch.getSwitchListSize();
            if (mSwitchListFromSp.isEmpty()) {
                mSwitchList = new ArrayList<BaseInfo>();
                mSwitchList = QuickSwitchManager.getInstance(this).getSwitchList(switchNum);
                String saveToSp = QuickSwitchManager.getInstance(this).listToString(mSwitchList,
                        switchNum);
                mSpSwitch.setSwitchList(saveToSp);
            } else {
                mSwitchList = new ArrayList<BaseInfo>();
                mSwitchList = QuickSwitchManager.getInstance(this).StringToList(mSwitchListFromSp);
            }

            fillQg1();
            fillQg2();
            fillQg3();
            mContainer.showOpenAnimation();
        }
        overridePendingTransition(-1, -1);
        mContainer.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });
    }

    private void handleIntent() {

    }

    public void onEventMainThread(ClickQuickItemEvent event) {
        mContainer.checkStatus(event.info);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void fillQg1() {
        ArrayList<BaseInfo> items = new ArrayList<BaseInfo>(list.subList(0, 12));
        mContainer.fillGestureItem(GType.DymicLayout, items);
    }

    private void fillQg2() {
        ArrayList<BaseInfo> items = new ArrayList<BaseInfo>(list.subList(13, 20));
        mContainer.fillGestureItem(GType.MostUsedLayout, items);
    }

    private void fillQg3() {
        mContainer.fillGestureItem(GType.SwitcherLayout, mSwitchList);
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
        // super.onBackPressed();
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
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
                final int mScreenHeight = wm.getDefaultDisplay().getHeight();
                int mScreenWidth = wm.getDefaultDisplay().getWidth();
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
