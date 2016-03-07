package com.leo.appmaster.battery;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.AppUnlockEvent;
import com.leo.appmaster.eventbus.event.BatteryViewEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.fragment.GuideFragment;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.ObjectAnimator;


public class BatteryShowViewActivity extends BaseFragmentActivity implements BatteryManager.BatteryStateListener, ViewPager.OnPageChangeListener {
    private final String TAG = "testBatteryView";
    public final static int ADD_LOCK = 1;
    public final static int CANCEL_POP = 2;
    public final static int POP_SHOW_TIME = 5000;
    private BatteryManager.BatteryState newState;
    private String mChangeType = BatteryManager.SHOW_TYPE_IN;
    private int mRemainTime;
    private boolean showWhenScreenOff;
    private int[] mRemainTimeArr;
    private ViewPager mViewPager;
    private BatteryFragmentHoler[] mFragmentHolders = new BatteryFragmentHoler[1];

    private BatteryViewFragment batteryFragment;

    public static Boolean isActivityAlive = false;
//    private RelativeLayout mBatterViewBg; // 背景
    private HomeWatcherReceiver mReceiver;

    private GuideFragment mGuideFragment;
    private boolean isShowGuide = false;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ADD_LOCK:
                    final TaskDetectService tds = TaskDetectService.getService();
                    if (tds != null) {
                        LeoLog.d(TAG, "onStop, call pretend app launch.");
                        tds.callPretendAppLaunch();
                        tds.ignoreBatteryPage(false);
                    }
                    break;
                case CANCEL_POP:
                    if (isShowGuide) {
                        cancelGuide();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeoLog.d(TAG, "onCreate");
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        handleIntent();

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        boolean secureKeyguard = AppUtil.hasSecureKeyguard(this, false);
        LeoLog.d(TAG, "isScreenLocked = " + secureKeyguard);
        // AM-3824 FLAG_DISMISS_KEYGUARD 对带上的系统keyguard没用
        if (!secureKeyguard) {
            win.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        if (!showWhenScreenOff) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//            win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        setContentView(R.layout.activity_batter_show_view);

        initAll();
        isActivityAlive = true;

        registerHomeKeyReceiver();

        SDKWrapper.addEvent(this, SDKWrapper.P1, "batterypage", "screen");

//        AM-3884移动到启动BatteryShowViewActivity的地方调用
//        TaskDetectService tds = TaskDetectService.getService();
//        if (tds != null) {
//            tds.ignoreBatteryPage(true);
//        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        LeoLog.d(TAG, "onResume -> " + this);
    }

    private void registerHomeKeyReceiver() {
        LeoLog.d("lisHome", "registerHomeKeyReceiver");
        mReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        registerReceiver(mReceiver, homeFilter);
    }

    private void unregisterHomeKeyReceiver() {
        LeoLog.d("lisHome", "unregisterHomeKeyReceiver");
        if (null != mReceiver) {
            unregisterReceiver(mReceiver);
        }
    }

    public class HomeWatcherReceiver extends BroadcastReceiver {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LeoLog.d("lisHome", "onReceive: action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                LeoLog.d("lisHome", "reason: " + reason);

                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    LeoLog.d("lisHome", "homekey");
                    finishActiviytAnim();
                    // AM-4033: 提前到这里
                    isActivityAlive = false;

                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 长按Home键 或者 activity切换键
                    LeoLog.d("lisHome", "long press home key or activity switch");

                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    // 锁屏
                    LeoLog.d("lisHome", "lock");
                } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    // samsung 长按Home键
                    LeoLog.d("lisHome", "assist");
                }


            }
        }

    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//            finishActiviytAnim();
//            LeoLog.d("testActivityBack","按下了back键 onKeyDown()");
//            return false;
//        } else {
//            return super.onKeyDown(keyCode, event);
//        }
//
//    }

    private void initAll() {
        LeoEventBus.getDefaultBus().register(this);
        mBatteryManager.setBatteryStateListener(this);

//        LayoutInflater inflater = this.getLayoutInflater();
//        View view = inflater.inflate(R.layout.activity_batter_show_view,null);
//        mBatterViewBg = (RelativeLayout) findViewById(R.id.batter_view_bg);

        mViewPager = (ViewPager) findViewById(R.id.battery_viewpager);
        initFragment();
        mViewPager.setAdapter(new BatteryPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(this);

        if (batteryFragment != null) {
            batteryFragment.initCreate(mChangeType, newState, mRemainTime, mRemainTimeArr);
        }


        if (mBatteryManager.shouldShowBubble()) {
            mGuideFragment = (GuideFragment) getSupportFragmentManager().findFragmentById(R.id.battery_guide);
            mGuideFragment.setEnable(true, GuideFragment.GUIDE_TYPE.BATTERY_GUIDE);
            isShowGuide = true;
            mBatteryManager.markShowBubble();
            mHandler.sendEmptyMessageDelayed(CANCEL_POP, POP_SHOW_TIME);
        }
    }

    private void initFragment() {
//        BatteryFragmentHoler holder = new BatteryFragmentHoler();
//        holder.title = "type_0";
//        emptyFragment = new EmptyFragment();
//        holder.fragment = emptyFragment;
//        mFragmentHolders[0] = holder;

        BatteryFragmentHoler holder = new BatteryFragmentHoler();
        holder.title = "type_1";
        batteryFragment = new BatteryViewFragment();
        holder.fragment = batteryFragment;
        mFragmentHolders[0] = holder;

        // AM-614, remove cached fragments
        FragmentManager fm = getSupportFragmentManager();
        try {
            FragmentTransaction ft = fm.beginTransaction();
            List<Fragment> list = fm.getFragments();
            if (list != null) {
                for (Fragment f : fm.getFragments()) {
                    ft.remove(f);
                }
            }
            ft.commit();
        } catch (Exception e) {

        }
    }

    class BatteryFragmentHoler {
        String title;
        BaseFragment fragment;
    }

    class BatteryPagerAdapter extends FragmentPagerAdapter {
        public BatteryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentHolders[position].fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentHolders[position].title;
        }

        @Override
        public int getCount() {
            return mFragmentHolders.length;
        }
    }


    private void handleIntent() {
        Intent intent = getIntent();
        newState = (BatteryManager.BatteryState)
                intent.getExtras().get(BatteryManager.SEND_BUNDLE);
        mChangeType = intent.getStringExtra(BatteryManager.PROTECT_VIEW_TYPE);
        mRemainTime = intent.getIntExtra(BatteryManager.REMAIN_TIME, 0);
        showWhenScreenOff = intent.getBooleanExtra(BatteryManager.SHOW_WHEN_SCREEN_OFF_FLAG, false);
        mRemainTimeArr = intent.getIntArrayExtra(BatteryManager.ARR_REMAIN_TIME);
    }

    public void onEventMainThread(BatteryViewEvent event) {
        LeoLog.d("testBatteryEvent", "getEvent : " + event.eventMsg);
        if (("finish_activity").equals(event.eventMsg)) {
            BatteryShowViewActivity.isActivityAlive = false;
            finishActiviytAnim();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LeoLog.d(TAG, "onStop");
    }

    @Override
    public void onBackPressed() {
        if (isShowGuide) {
            cancelGuide();
        }
        finishActiviytAnim();
//        finish();
    }

    private void cancelGuide() {
        if (mGuideFragment != null) {
            mGuideFragment.setEnable(false, GuideFragment.GUIDE_TYPE.PIC_GUIDE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        LeoLog.d(TAG, "finish");
        LeoEventBus.getDefaultBus().unregister(this);
        isActivityAlive = false;

        if (AppMasterApplication.getInstance().isHomeOnTopAndBackground()) {
            LeoLog.d("isOnHome", "yes");
            mHandler.sendEmptyMessage(ADD_LOCK);
        } else {
            LeoLog.d("isOnHome", "no");
            mHandler.sendEmptyMessageDelayed(ADD_LOCK, 1000);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoLog.d(TAG, "onDestroy -> " + this);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "batterypage", "screen_back&home");
        isActivityAlive = false;
        if (mReceiver != null) {
            unregisterHomeKeyReceiver();
        }
        if (isShowGuide) {
            cancelGuide();
        }
    }

    @Override
    public void onStateChange(BatteryManager.EventType type, BatteryManager.BatteryState state, int remainTime, int[] timeArr) {
        if (type.equals(BatteryManager.EventType.BAT_EVENT_CHARGING)) {
            //3
            mChangeType = BatteryManager.UPDATE_UP;
        } else if (type.equals(BatteryManager.EventType.BAT_EVENT_CONSUMING)) {
            //4
            mChangeType = BatteryManager.UPDATE_DONW;
        } else if (type.equals(BatteryManager.EventType.SHOW_TYPE_OUT)) {
            //2
            mChangeType = BatteryManager.SHOW_TYPE_OUT;
        } else {
            //1
            mChangeType = BatteryManager.SHOW_TYPE_IN;
        }
        newState = state;
        mRemainTime = remainTime;
        mRemainTimeArr = timeArr;
        process();
    }

    private void process() {
        LeoLog.d(TAG, "process mChangeType : " + mChangeType);
        if (batteryFragment != null) {
            batteryFragment.process(mChangeType, newState, mRemainTime, mRemainTimeArr);
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
//        if (i == 0) {
//            mViewPager.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    SDKWrapper.addEvent(BatteryShowViewActivity.this, SDKWrapper.P1, "batterypage", "screen_unlock");
//                    finish();
//                }
//            }, 250);
//        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    public void onFinishActivity(boolean showNoti, int level) {

        if (showNoti && mBatteryManager.getScreenViewStatus()) {
            mBatteryManager.showSaverNotification(level);
        }

        finishActiviytAnim();
    }


    public void finishActiviytAnim() {
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mViewPager,
                        "y", mViewPager.getTop(), mViewPager.getTop() - mViewPager.getHeight());
                animMoveY.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        LeoLog.d("lisHome", "call finish() here when animation ends");
                        isActivityAlive = false;
                        finish();
                    }
                });
                animMoveY.setDuration(200);
                animMoveY.start();
            }
        });
    }

    public void onEvent(AppUnlockEvent event) {
        LeoLog.d(TAG, "onEvent, result: " + event.mUnlockResult);
        if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_CANCELED) {
            this.finish();
        }
    }
}
