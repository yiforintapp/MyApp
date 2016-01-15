package com.leo.appmaster.battery;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BatteryViewEvent;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.WifiSecurityManager;
import com.leo.appmaster.mgr.impl.BatteryManagerImpl;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.utils.LeoLog;


public class BatteryShowViewActivity extends BaseActivity implements BatteryManager.BatteryStateListener {
    private final String TAG = "AskAddToBlacklistActivity";
    private BatteryManagerImpl.BatteryState newState;
    private String mChangeType = BatteryManagerImpl.SHOW_TYPE_IN;
    private int mRemainTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_batter_show_view);

        initAll();
        handleIntent();
    }

    private void process() {
        BatteryProtectView mProtectView = BatteryProtectView.makeText(this);

        LeoLog.d("testBatteryEvent", "mChangeType : " + mChangeType);

        if (mChangeType.equals(BatteryManagerImpl.SHOW_TYPE_IN)) {
            mProtectView.setBatteryStatus(true);
            mProtectView.setBatteryLevel(newState.level);
            mProtectView.notifyViewUi();
            mProtectView.show();
        } else if (mChangeType.equals(BatteryManagerImpl.SHOW_TYPE_OUT)) {
            mProtectView.setBatteryLevel(newState.level);
            mProtectView.setBatteryStatus(false);
            mProtectView.notifyViewUi();
        } else if (mChangeType.equals(BatteryManagerImpl.UPDATE_UP)) {
            mProtectView.setBatteryLevel(newState.level);
            mProtectView.setBatteryStatus(true);
            mProtectView.notifyViewUi();
        } else {
            mProtectView.setBatteryLevel(newState.level);
            mProtectView.setBatteryStatus(true);
            mProtectView.notifyViewUi();
        }

    }

    private void initAll() {
        LeoEventBus.getDefaultBus().register(this);
        mBatteryManager.setBatteryStateListener(this);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        newState = (BatteryManagerImpl.BatteryState)
                intent.getExtras().get(BatteryManagerImpl.SEND_BUNDLE);
        mChangeType = intent.getStringExtra(BatteryManagerImpl.PROTECT_VIEW_TYPE);
        mRemainTime = intent.getIntExtra(BatteryManagerImpl.REMAIN_TIME, 0);
        process();
    }

    public void onEventMainThread(BatteryViewEvent event) {
        LeoLog.d("testBatteryEvent", "getEvent : " + event.eventMsg);
        if (event.eventMsg.equals("finish_activity")) {
            finish();
        }
    }


    @Override
    public void finish() {
        super.finish();
        BatteryProtectView.handleHide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onStateChange(BatteryManager.EventType type, BatteryManager.BatteryState state, int remainTime) {
        if (type.equals(BatteryManager.EventType.BAT_EVENT_CHARGING)) {
            //3
            mChangeType = BatteryManagerImpl.UPDATE_UP;
        } else if (type.equals(BatteryManager.EventType.BAT_EVENT_CONSUMING)) {
            //4
            mChangeType = BatteryManagerImpl.UPDATE_DONW;
        } else if (type.equals(BatteryManager.EventType.SHOW_TYPE_OUT)) {
            //2
            mChangeType = BatteryManagerImpl.SHOW_TYPE_OUT;
        } else {
            //1
            mChangeType = BatteryManagerImpl.SHOW_TYPE_IN;
        }
        newState = state;
        mRemainTime = remainTime;
        process();
    }
}
