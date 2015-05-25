
package com.leo.appmaster.quickgestures.ui;

import java.util.AbstractList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.eventbus.event.ClickQuickItemEvent;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.Orientation;
import com.leo.appmaster.utils.LeoLog;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class QuickGesturePopupActivity extends Activity implements
        OnSystemUiVisibilityChangeListener {

    private static int switchNum;
    private AppleWatchContainer mContainer;
    private AbstractList<AppItemInfo> list;
    private List<QuickSwitcherInfo> mSwitchList;
    private AppMasterPreference mSpSwitch;
    private String mSwitchListFromSp;
    private ImageView iv_roket, iv_pingtai;
    private WindowManager wm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent();
        setContentView(R.layout.pop_quick_gesture_apple_watch);

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
        mContainer.setRocket(this);

        mSpSwitch = AppMasterPreference.getInstance(this);
        list = AppLoadEngine.getInstance(this).getAllPkgInfo();

        if (mSwitchList == null) {
            mSwitchListFromSp = mSpSwitch.getSwitchList();
            switchNum = mSpSwitch.getSwitchListSize();
            LeoLog.d("testFirstInGet", "mSwitchListFromSp : " + mSwitchListFromSp);
            if (mSwitchListFromSp.isEmpty()) {
                mSwitchList = QuickSwitchManager.getInstance(this).getSwitchList(switchNum);
                String saveToSp = QuickSwitchManager.getInstance(this).ListToString(mSwitchList,
                        switchNum);
                mSpSwitch.setSwitchList(saveToSp);
                LeoLog.d("testFirstInGet", "saveToSp:" + saveToSp);
            } else {
                LeoLog.d("testFirstInGet", "get list from sp");
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
                LeoLog.e("xxxx", "onFocusChange");
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
        LeoLog.e("xxxx", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LeoLog.e("xxxx", "onStop");
        FloatWindowHelper.mGestureShowing = false;
        finish();
        super.onStop();
    }

    private void fillQg1() {
        mContainer.fillGestureItem(GType.DymicLayout, list.subList(0, 13));
    }

    private void fillQg2() {
        mContainer.fillGestureItem(GType.MostUsedLayout, list.subList(11, 24));
    }

    private void fillQg3() {
        mContainer.fillGestureItem(GType.SwitcherLayout, mSwitchList);
    }

    @Override
    protected void onDestroy() {
        FloatWindowHelper.mGestureShowing = false;
        // 反注册
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
        LeoLog.e("xxxx", "visibility = " + visibility);

    }

    public void RockeyAnimation(int mLayoutTop, int mRocketX, int mRocketY) {

        // iv_roket.setX(mRocketX - iv_roket.getWidth() / 2);
        // iv_roket.setY(mRocketY - iv_roket.getHeight() / 2);
        // iv_roket.layout(mRocketX, mRocketY, mRocketX + iv_roket.getWidth() ,
        // mRocketY + iv_roket.getHeight());

        int smallRockeyX = mRocketX - iv_roket.getWidth() / 2;
        int smallRockeyY = mRocketY - iv_roket.getHeight() / 2;
        MarginLayoutParams margin = new MarginLayoutParams(iv_roket.getLayoutParams());
        margin.setMargins(smallRockeyX, smallRockeyY, smallRockeyX + iv_roket.getWidth(),
                smallRockeyY + iv_roket.getHeight());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
        iv_roket.setLayoutParams(layoutParams);
        iv_roket.setVisibility(View.VISIBLE);

        ObjectAnimator turnBig = ObjectAnimator.ofFloat(iv_roket, "scaleX", 1f, 1.5f);
        ObjectAnimator turnBig2 = ObjectAnimator.ofFloat(iv_roket, "scaleY", 1f, 1.5f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(turnBig).with(turnBig2);
        animSet.setDuration(1000);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                iv_pingtai.setVisibility(View.VISIBLE);
                int mScreenHeight = wm.getDefaultDisplay().getHeight();
                int mScreenWidth = wm.getDefaultDisplay().getWidth();
                int transY = (int) iv_pingtai.getTranslationY();

                ObjectAnimator moveToX = ObjectAnimator.ofFloat(iv_roket, "translationX",
                        iv_roket.getTranslationX(), 100f);
                ObjectAnimator moveToY = ObjectAnimator.ofFloat(iv_roket, "translationY",
                        iv_roket.getTranslationY(), 100f);
                ObjectAnimator pingtaiMoveToY = ObjectAnimator
                        .ofFloat(iv_pingtai, "translationY", mScreenHeight, transY);
                AnimatorSet animMoveSet = new AnimatorSet();
                animMoveSet.play(moveToX).with(moveToY).with(pingtaiMoveToY);
                animMoveSet.setDuration(1000);
                animMoveSet.start();

            }
        });
        animSet.start();

        // ScaleAnimation showScale = new ScaleAnimation(1.0f, 1.5f, 1.0f,
        // 1.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
        // ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        // showScale.setDuration(500);
        // showScale.setFillAfter(true);
        // showScale.setAnimationListener(new AnimationListener() {
        // @Override
        // public void onAnimationStart(Animation animation) {
        //
        // }
        //
        // @Override
        // public void onAnimationRepeat(Animation animation) {
        //
        // }
        //
        // @Override
        // public void onAnimationEnd(Animation animation) {
        // // 平台show
        // showPingTai();
        // }
        // });
        // iv_roket.setAnimation(showScale);
    }

    protected void showPingTai() {
        iv_pingtai.setVisibility(View.VISIBLE);
        int mScreenHeight = wm.getDefaultDisplay().getHeight();
        int mScreenWidth = wm.getDefaultDisplay().getWidth();
        int transY = (int) iv_pingtai.getTranslationY();

        ObjectAnimator
                .ofFloat(iv_pingtai, "translationY", mScreenHeight, transY)
                .setDuration(800)
                .start();

        Animation translateAnimation = new
                TranslateAnimation(iv_roket.getX(), mScreenWidth / 2 - iv_roket.getWidth() / 2,
                        iv_roket.getTranslationY(), iv_roket.getTranslationY());
        translateAnimation.setDuration(1000);
        translateAnimation.setFillAfter(true);
        iv_roket.setAnimation(translateAnimation);

        LeoLog.d("testFirstInGet",
                "iv_roket.getX(): " + iv_roket.getX() + " ; mScreenWidth/2 : " + mScreenWidth / 2);
    }

}
