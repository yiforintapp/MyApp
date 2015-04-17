
package com.leo.appmaster.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockModeActivity;
import com.leo.appmaster.applocker.LockModeView;
import com.leo.appmaster.applocker.LockOptionActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.WeiZhuangActivity;
import com.leo.appmaster.applocker.WeiZhuangFirstIn;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.eventbus.event.NewThemeEvent;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.sdk.SDKWrapper;

public class HomeLockFragment extends BaseFragment implements OnClickListener, Selectable {

    private LockModeView mLockModeCircle;
    private TextView mAppLockBtn;
    private TipTextView mLockThemeBtn;
    private TextView mLockModeBtn;
    private TextView mLockSettingBtn;
    private AppMasterPreference sp_weizhuang;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_home_lock;
    }

    @Override
    protected void onInitUI() {
        sp_weizhuang = sp_weizhuang.getInstance(mActivity);
        mLockModeCircle = (LockModeView) findViewById(R.id.lock_mode_circle);
//        mLockModeCircle.setOnClickListener(this);
        mAppLockBtn = (TextView) findViewById(R.id.app_lock);
        mAppLockBtn.setOnClickListener(this);
        mLockThemeBtn = (TipTextView) findViewById(R.id.lock_theme);
        mLockThemeBtn.setOnClickListener(this);
        mLockModeBtn = (TextView) findViewById(R.id.lock_mode);
        mLockModeBtn.setOnClickListener(this);
        mLockSettingBtn = (TextView) findViewById(R.id.lock_setting);
        mLockSettingBtn.setOnClickListener(this);

        mLockModeCircle.startAnimation();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LeoEventBus.getDefaultBus().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        updateModeUI();
        checkNewTheme();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if(mLockModeCircle != null) {
            mLockModeCircle.stopAnimation();
        }
    }

    @Override
    public void onDestroy() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
    }

    private void checkNewTheme() {
        String locSerial = AppMasterPreference.getInstance(mActivity)
                .getLocalThemeSerialNumber();
        String onlineSerial = AppMasterPreference.getInstance(mActivity)
                .getOnlineThemeSerialNumber();
        if (!locSerial.equals(onlineSerial)) {
            mLockThemeBtn.showTip(true);
        } else {
            mLockThemeBtn.showTip(false);
        }
    }

    public void onEventMainThread(NewThemeEvent event) {
        mLockThemeBtn.showTip(event.newTheme);
    }

    public void onEventMainThread(LockModeEvent event) {
        mLockModeCircle.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateModeUI();
            }
        }, 500);
    }

    private void updateModeUI() {
        LockManager lm = LockManager.getInstatnce();
        mLockModeCircle.updateMode(lm.getCurLockName(), lm.getLockedAppCount() + "");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.app_lock:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "lock");
                LockManager lm = LockManager.getInstatnce();
                LockMode curMode = lm.getCurLockMode();
                if (curMode != null && curMode.defaultFlag == 1 && !curMode.haveEverOpened) {
                    startRcommendLock(0);
                    curMode.haveEverOpened = true;
                    lm.updateMode(curMode);
                } else {
                    enterLockList();
                }
                break;
            case R.id.lock_theme:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "theme");
                if (mLockThemeBtn != null && mLockThemeBtn.isShowingTip()) {
                    mLockThemeBtn.showTip(false);
                }
                enterLockTheme();
                break;
            case R.id.lock_mode:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "modes");
                enterLockMode();
                break;
            case R.id.lock_setting:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "locksetting");
//                enterLockSetting();
                enterAppWeiZhuang();
                break;
            case R.id.lock_mode_circle:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "changemode");
                // int[] locations = view.getLocationOnScreen();
                //
                // int width = view.getWidth();
                // int height = view.getHeight();
                //
                // int[] center = new int[2];
                // center[0] = locations[0] + width / 2;
                // center[1] = locations[1] + height / 2;

                ((HomeActivity) mActivity).showModePages(true/* , center */);
                break;

            default:
                break;
        }

    }

    private void enterAppWeiZhuang() {
        boolean isFirstIn = sp_weizhuang.getWeiZhuang();
        Intent intent ;
        if(isFirstIn){
            intent = new Intent(mActivity, WeiZhuangFirstIn.class);
            sp_weizhuang.setWeiZhuang(false);
        }else {
            intent = new Intent(mActivity, WeiZhuangActivity.class);
        }
        mActivity.startActivity(intent);
    }

    private void enterLockSetting() {
        Intent intent = new Intent(mActivity, LockOptionActivity.class);
        intent.putExtra(LockOptionActivity.TAG_COME_FROM, LockOptionActivity.FROM_HOME);
        mActivity.startActivity(intent);
    }

    private void enterLockMode() {
        Intent intent = new Intent(mActivity, LockModeActivity.class);
        mActivity.startActivity(intent);
    }

    private void enterLockTheme() {
        Intent intent = new Intent(mActivity, LockerTheme.class);
        mActivity.startActivity(intent);
    }

    private void enterLockList() {
        Intent intent = null;
        intent = new Intent(mActivity, AppLockListActivity.class);
        startActivity(intent);

    }

    private void startRcommendLock(int target) {
        Intent intent = new Intent(mActivity, RecommentAppLockListActivity.class);
        intent.putExtra("target", target);
        startActivity(intent);
    }

    @Override
    public void onSelected() {
        if (mLockModeCircle != null) {
            mLockModeCircle.startAnimation();
        }
    }

    @Override
    public void onScrolling() {
        if (mLockModeCircle != null) {
            mLockModeCircle.stopAnimation();
        }

    }

}
