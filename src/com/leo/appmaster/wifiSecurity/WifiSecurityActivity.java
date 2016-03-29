package com.leo.appmaster.wifiSecurity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.WifiSecurityEvent;
import com.leo.appmaster.home.DeskProxyActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.WifiSecurityManager;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.OneButtonDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.PropertyInfoUtil;
import com.leo.appmaster.utils.QuickHelperUtils;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;


/**
 * Created by qili on 15-10-16.
 */
public class WifiSecurityActivity extends BaseFragmentActivity implements View.OnClickListener {
    private static final int SHOW_START_ANIMATION = 34;
    public final static int WIFI_CHANGE = 1;
    public final static int BACK_PRESS = 2;
    public final static int SCAN_DONE = 3;
    public final static int GO_TO_SETTING = 4;
    private View contentView, resultContentView, mSmallBossView;
    private ImageView mLineView, mLineView2, mIconView;
    private ImageView mSmallViewFlash;
    private TextView mWifiName;
    private ImageView resultIconView;
    private TextView resultBigTv, resultSmallTv;
    private CommonToolbar mTitleBar;
    private OneButtonDialog selectWifiDialog;
    private boolean isWifiOpen, isSelectWifi;
    private boolean isScanIng = false;
    private boolean isScanDone = false;
    private WifiTabFragment wifiFragment;
    private WifiResultFrangment wifiResultFrangment;
    private long lastTimeIn;
    private boolean firstCome = true;
    private boolean isSafe;
    private int mScanAnimCount = 0;
    private boolean mIsJudgeAsLowMemory = false;
    private LeoPreference mPt;
    private LEOAlarmDialog mDialogAskCreateShotcut;
    private static final int ACCUMULATIVE_TOTAL_TO_ASK_CREATE_SHOTCUT = 3;
    
    //开始首页动画
    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_START_ANIMATION:
                    isCheckWifiAlready(true);
                    long totalMemory = PropertyInfoUtil.getTotalMemory(WifiSecurityActivity.this);
                    if (!mIsJudgeAsLowMemory) {
                        showIconLight();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_main);
        LeoEventBus.getDefaultBus().register(this);
        mIsJudgeAsLowMemory = PropertyInfoUtil.getTotalMemory(WifiSecurityActivity.this) <= Constants.TOTAL_MEMORY_JUDGE_AS_LOW_MEMORY;
        SDKWrapper.addEvent(this, SDKWrapper.P1, "wifi_scan", "wifi_scan_cnts");
        mPt = LeoPreference.getInstance();
        handlerIntent();
        initUi();
        isCheckWifiAlready(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    private void handlerIntent() {
        Intent intent = getIntent();
        String from = intent.getStringExtra("from");
        int wifiState = intent.getIntExtra("wifistate", 0);
        if (from != null && from.equals("toast")) {
            if (wifiState == 2) {
                SDKWrapper.
                        addEvent(this,
                                SDKWrapper.P1, "wifi_scan", "wifi_clk_toast_unsafe");
            } else {
                SDKWrapper.
                        addEvent(this,
                                SDKWrapper.P1, "wifi_scan", "wifi_clk_toast_safe");
            }

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        LeoLog.i("testwifi", "focus changed!");
        if (firstCome) {
            mHandler.sendEmptyMessageDelayed(SHOW_START_ANIMATION, 500);
            firstCome = false;
        }
    }

    private void initUi() {
        mTitleBar = (CommonToolbar) findViewById(R.id.wifi_title_bar);
        mTitleBar.setToolbarTitle(R.string.wifi_titlebar_name);
        mTitleBar.setToolbarColorResource(R.color.transparent);
        mTitleBar.setOptionClickListener(this);
        mTitleBar.setNavigationClickListener(this);
        mTitleBar.setOptionImageResource(R.drawable.setup_icon);
        mTitleBar.setOptionMenuVisible(true);

        mSmallBossView = findViewById(R.id.small_boss);

        contentView = findViewById(R.id.wifi_scan_content);
        resultContentView = findViewById(R.id.top_content);
        resultBigTv = (TextView) resultContentView.findViewById(R.id.wifi_big_text);
        resultSmallTv = (TextView) resultContentView.findViewById(R.id.wifi_sma_text);
        resultIconView = (ImageView) resultContentView.findViewById(R.id.wifi_result_icon);

        mSmallViewFlash = (ImageView) findViewById(R.id.wifi_flash2);
        wifiFragment = (WifiTabFragment) getSupportFragmentManager().
                findFragmentById(R.id.wifi_scan_tab);
        wifiResultFrangment = (WifiResultFrangment) getSupportFragmentManager().
                findFragmentById(R.id.wifi_scan_result);
        mWifiName = (TextView) findViewById(R.id.wifi_name);
        mLineView = (ImageView) findViewById(R.id.scan_line);
        mLineView2 = (ImageView) findViewById(R.id.scan_line2);

        mIconView = (ImageView) findViewById(R.id.wifi_big_icon);
        mIconView.setOnClickListener(this);

        //放大光波
//        mExpandView = (ImageView) findViewById(R.id.wifi_expand);
//        mExpandView2 = (ImageView) findViewById(R.id.wifi_expand2);
        //开始首页动画
//        mHandler.sendEmptyMessageDelayed(SHOWANIMATION, 150);
    }

    public WifiSecurityManager getManger() {
        return mWifiManager;
    }

    public LockManager getLockManager() {
        return mLockManager;
    }

    @Override
    public void onBackPressed() {
        if (!mPt.getBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_WIFI_SECURITY, false) && mPt.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_WIFI_SECURITY, 0) >= ACCUMULATIVE_TOTAL_TO_ASK_CREATE_SHOTCUT) {
            mPt.putBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_WIFI_SECURITY, true);
            if (mDialogAskCreateShotcut == null) {
                mDialogAskCreateShotcut = new LEOAlarmDialog(this);
            }
            mDialogAskCreateShotcut.setTitleVisiable(false);
            mDialogAskCreateShotcut.setDialogIcon(R.drawable.qh_wifi_icon);
            mDialogAskCreateShotcut.setContent(getString(R.string.ask_create_shortcut_content_wifi_security));
            mDialogAskCreateShotcut.setLeftBtnStr(getString(R.string.cancel));
            mDialogAskCreateShotcut.setRightBtnStr(getString(R.string.ask_create_shortcut_button_right));
            mDialogAskCreateShotcut.setRightBtnListener(new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SDKWrapper.addEvent(WifiSecurityActivity.this, SDKWrapper.P1, "assistant", "shortcut_wifi");
                    Intent intent = new Intent();
                    intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_WIFI);
                    intent.putExtra("from_quickhelper", true);
                    QuickHelperUtils.createQuickHelper(getString(R.string.quick_helper_wifi_safety), R.drawable.qh_wifi_icon, intent, WifiSecurityActivity.this);
                    Toast.makeText(WifiSecurityActivity.this, getString(R.string.quick_help_add_toast), Toast.LENGTH_SHORT).show();
                    mDialogAskCreateShotcut.dismiss();
                }
            });
            mDialogAskCreateShotcut.setLeftBtnListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SDKWrapper.addEvent(WifiSecurityActivity.this, SDKWrapper.P1, "assistant", "shortcut_wifi_no");
                    mDialogAskCreateShotcut.dismiss();
                }
            });
            mDialogAskCreateShotcut.show();
        } else {
            if (isScanIng) {
                wifiFragment.dismissTab(BACK_PRESS);
                return;
            }
            super.onBackPressed();
        }
    
        
    }

    public void setScanState(boolean scanState) {
        isScanIng = scanState;
//        animationShowNow = 0;
        //开始首页动画
//        startIconAnimation();
    }

    //外圈放大
//    AnimatorSet turnBigSet;
//    ObjectAnimator anim001;
//    ObjectAnimator anim002;
//    private void startIconAnimation() {
//        if (!isScanIng) {
//            anim001 = ObjectAnimator.ofFloat(mIconView,
//                    "scaleX", 1f, 1.1f);
//            anim002 = ObjectAnimator.ofFloat(mIconView,
//                    "scaleY", 1f, 1.1f);
//
//
//            turnBigSet = new AnimatorSet();
//            turnBigSet.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                    beSmallAnimation(animationShowNow);
//                }
//            });
//            turnBigSet.setDuration(300);
//            turnBigSet.play(anim001).with(anim002);
//            turnBigSet.start();
//        }
//    }

    //外圈缩小
//    AnimatorSet turnSmallSet;
//    ObjectAnimator anim003;
//    ObjectAnimator anim004;
//    public void beSmallAnimation(final int isSmallNow) {
//        if (!isScanIng) {
//            anim003 = ObjectAnimator.ofFloat(mIconView,
//                    "scaleX", 1.1f, 1f);
//            anim004 = ObjectAnimator.ofFloat(mIconView,
//                    "scaleY", 1.1f, 1f);
//
//            turnSmallSet = new AnimatorSet();
//            turnSmallSet.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                    if (isSmallNow > 1) {
//                        showExpand2Animation();
//                        mHandler.sendEmptyMessageDelayed(SHOWANIMATION, 1500);
//                        animationShowNow = 0;
//                    } else {
//                        showExpand1Animation();
//                        mHandler.sendEmptyMessage(SHOWANIMATION);
//                    }
//
//                }
//            });
//            turnSmallSet.setDuration(300);
//            turnSmallSet.play(anim003).with(anim004);
//            turnSmallSet.start();
//        }
//    }

    //放大光波
//    ObjectAnimator animExpandX;
//    ObjectAnimator animExpandY;
//    ObjectAnimator animExpandAlpha;
//    AnimatorSet turnExpandSet;
//    private void showExpand1Animation() {
//        if (!isScanIng) {
//            animExpandX = ObjectAnimator.ofFloat(mExpandView,
//                    "scaleX", 1f, 1.8f);
//            animExpandY = ObjectAnimator.ofFloat(mExpandView,
//                    "scaleY", 1f, 1.8f);
//            animExpandAlpha = ObjectAnimator.ofFloat(mExpandView,
//                    "alpha", 1f, 0f);
//
//            turnExpandSet = new AnimatorSet();
//            turnExpandSet.setDuration(1500);
//            turnExpandSet.play(animExpandX).with(animExpandY);
//            turnExpandSet.play(animExpandY).with(animExpandAlpha);
//            turnExpandSet.start();
//        }
//    }

    //放大光波
//    ObjectAnimator animExpandX2;
//    ObjectAnimator animExpandY2;
//    ObjectAnimator animExpandAlpha2;
//    AnimatorSet turnExpandSet2;
//    private void showExpand2Animation() {
//        if (!isScanIng) {
//            animExpandX2 = ObjectAnimator.ofFloat(mExpandView2,
//                    "scaleX", 1f, 1.8f);
//            animExpandY2 = ObjectAnimator.ofFloat(mExpandView2,
//                    "scaleY", 1f, 1.8f);
//            animExpandAlpha2 = ObjectAnimator.ofFloat(mExpandView2,
//                    "alpha", 1f, 0f);
//
//            turnExpandSet2 = new AnimatorSet();
//            turnExpandSet2.setDuration(1500);
//            turnExpandSet2.play(animExpandX2).with(animExpandY2);
//            turnExpandSet2.play(animExpandY2).with(animExpandAlpha2);
//            turnExpandSet2.start();
//        }
//    }

//    public void beNormalAnimation() {
//        ObjectAnimator anim111 = ObjectAnimator.ofFloat(mIconView,
//                "scaleX", 1.1f, 1f);
//        ObjectAnimator anim112 = ObjectAnimator.ofFloat(mIconView,
//                "scaleY", 1.1f, 1f);
//
//        AnimatorSet turnSmallSet = new AnimatorSet();
//        turnSmallSet.setDuration(300);
//        turnSmallSet.play(anim111).with(anim112);
//        turnSmallSet.start();
//    }

    private void isCheckWifiAlready(boolean isGotoScan) {
        //wifi open?
        isWifiOpen = mWifiManager.isWifiOpen();
        //connect wifi?
        isSelectWifi = mWifiManager.getIsWifi();
        if (!isWifiOpen || !isSelectWifi) {
            if (!isWifiOpen && isGotoScan) {
                String text = this.getString(R.string.wifi_is_close);
                showSelectWifiDialog(text);
            }
            if (!isSelectWifi && isGotoScan) {
                String text = this.getString(R.string.no_wifi_now);
                showSelectWifiDialog(text);
            }
            setWifiName(false);
        } else if (isGotoScan) {
            isScanIng = true;
            wifiFragment.showTab();
            showMoveUp();
            setWifiName(true);
        } else {
            setWifiName(true);
        }
    }

    private AnimatorSet iconFlashSet;
    private ObjectAnimator animFlash;
    private ObjectAnimator animDark;

    private void showIconLight() {
        iconFlashSet = new AnimatorSet();
        animFlash = ObjectAnimator.ofFloat(mSmallViewFlash,
                "alpha", 1f, 0f);
        animFlash.setDuration(500);

        animDark = ObjectAnimator.ofFloat(mSmallViewFlash,
                "alpha", 0f, 1f);
        animDark.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                showIconLight();
            }
        });
        animDark.setDuration(500);
        iconFlashSet.play(animFlash).before(animDark);
        iconFlashSet.start();
    }


//    private void showIconDark() {
//        animDark = ObjectAnimator.ofFloat(mSmallViewFlash,
//                "alpha", 0f, 1f);
//        animDark.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                showIconLight();
//            }
//        });
//        animDark.setDuration(500);
//        animDark.start();
//    }

    private void cancelIconLightAnimation() {
        if (iconFlashSet != null) {
            iconFlashSet.cancel();
            iconFlashSet = null;
        }

        if (animFlash != null) {
            animFlash.cancel();
            animFlash = null;
        }

        if (animDark != null) {
            animDark.cancel();
            animDark = null;
        }
    }

    public void showSelectWifiDialog(final String text) {
        SDKWrapper.addEvent(this, SDKWrapper.P1, "wifi_scan", "wifi_none_dlg");
        if (selectWifiDialog == null) {
            selectWifiDialog = new OneButtonDialog(this);
        }

        final String makePointString = getString(R.string.can_not_connect_wifi);
        if (!selectWifiDialog.isShowing()) {
            selectWifiDialog.setOnClickListener(new OneButtonDialog.OnWifiDiaogClickListener() {
                @Override
                public void onClick() {
                    if (makePointString.equals(text)) {
                        SDKWrapper.
                                addEvent(WifiSecurityActivity.this,
                                        SDKWrapper.P1, "wifi_rst", "wifi_rst_netbreak_other");
                    }
                    SDKWrapper.
                            addEvent(WifiSecurityActivity.this,
                                    SDKWrapper.P1, "wifi_scan", "wifi_none_other");

                    mLockManager.filterSelfOneMinites();
                    mLockManager.filterPackage(Constants.PKG_SETTINGS, 1000);
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                }
            });
            if (text.equals(this.getString(R.string.wifi_is_close))) {
                selectWifiDialog.setBtnText(this.getString(R.string.wifi_open_it));
            }
            selectWifiDialog.setText(text);
            if (!isFinishing()) {
                selectWifiDialog.show();
            }
        }
    }


    public void onEventMainThread(WifiSecurityEvent event) {

        long nowIn = System.currentTimeMillis();

        if (nowIn - lastTimeIn > 2000) {

            if (isScanDone) {
                WifiSecurityActivity.this.finish();
                return;
            }

            LeoLog.d("testWifiPart", "onEventMainThread , event is:" + event.eventMsg);
            if (event.eventMsg.equals(WifiSecurityManager.WIFITAG)) {
                if (wifiFragment.isTabShowing()) {
                    wifiFragment.dismissTab(WIFI_CHANGE);
                }
                isCheckWifiAlready(false);

                if (!mWifiManager.isWifiOpen()) {
                    showSelectWifiDialog(this.getString(R.string.wifi_is_close));
                } else if (!mWifiManager.getIsWifi()) {
                    showSelectWifiDialog(this.getString(R.string.no_wifi_now));
                } else {
                    if (selectWifiDialog != null && selectWifiDialog.isShowing()) {
                        selectWifiDialog.dismiss();
                    }
                }
            }
            lastTimeIn = nowIn;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
        if (mDialogAskCreateShotcut != null && mDialogAskCreateShotcut.isShowing()) {
            mDialogAskCreateShotcut.dismiss();
            mDialogAskCreateShotcut = null;
        }

        if (selectWifiDialog != null && selectWifiDialog.isShowing()) {
            selectWifiDialog.dismiss();
            selectWifiDialog = null;
        }
    }

    private void setWifiName(boolean connectWifi) {
        if (connectWifi) {
            String wifiName = mWifiManager.getWifiName();
            if (wifiName != null && wifiName.startsWith("\"")) {
                //去除双引号
                wifiName = wifiName.substring(wifiName.indexOf("\"") + 1, wifiName.lastIndexOf("\""));
            }
            if (wifiName != null) {
                mWifiName.setText(wifiName);
            }
        } else {
            mWifiName.setText(this.getString(R.string.no_wifi_now));
        }

    }

    public void loadFinish() {
        isScanIng = false;
        wifiFragment.dismissTab(SCAN_DONE);
    }

    public void showMoveUp() {
        //1
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(contentView,
                "y", contentView.getTop(), -(contentView.getHeight() / 6));
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                showLineAnimation();//TODO
            }
        });
        anim1.setDuration(400);
        anim1.start();
    }

    private AnimatorSet animationSet;
    private ObjectAnimator anim1;
    private ObjectAnimator anim2;
    private int lineStartPlace;
    private int lineEndPlace;

    private void showLineAnimation() {
        animationSet = new AnimatorSet();
        lineStartPlace = mIconView.getTop() - mLineView.getHeight();
        lineEndPlace = mIconView.getBottom() + mLineView.getHeight();

        anim1 = ObjectAnimator.ofFloat(mLineView,
                "y", lineStartPlace, lineEndPlace);
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mLineView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mLineView.setVisibility(View.GONE);
            }
        });
        if (mIsJudgeAsLowMemory) {
            anim1.setDuration(1500);
        } else {
            anim1.setDuration(1000);
        }

        anim2 = ObjectAnimator.ofFloat(mLineView2,
                "y", lineEndPlace, lineStartPlace);
        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mLineView2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mLineView2.setVisibility(View.GONE);
                mScanAnimCount++;

                if ((mScanAnimCount < 1 && mIsJudgeAsLowMemory) || !mIsJudgeAsLowMemory) {
                    showLineAnimation();
                }
            }
        });
        if (mIsJudgeAsLowMemory) {
            anim2.setDuration(1500);
        } else {
            anim2.setDuration(1000);
        }
        animationSet.play(anim1).before(anim2);
        animationSet.start();
    }

    public void showMoveDown() {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(contentView,
                "y", -(contentView.getHeight() / 6), contentView.getTop());
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        anim1.setDuration(500);
        anim1.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wifi_big_icon:
                if (isScanIng || wifiFragment.isTabShowing()) return;
                isCheckWifiAlready(true);
                break;
            case R.id.ct_option_1_rl:
                if (isScanIng) {
                    wifiFragment.dismissTab(GO_TO_SETTING);
                } else {
                    SDKWrapper.addEvent(this,
                            SDKWrapper.P1, "wifi_scan", "wifi_scan_setting");
                    Intent intent = new Intent(WifiSecurityActivity.this, WifiSettingActivity.class);
                    WifiSecurityActivity.this.startActivity(intent);
                }
                break;
            case R.id.ct_back_rl:
                onBackPressed();
//                if (isScanIng) {
//                    wifiFragment.dismissTab(BACK_PRESS);
//                } else {
//                    WifiSecurityActivity.this.finish();
//                }
                break;
        }
    }

    //取消首页动画效果
//    public void cancelTurnBigAnimation() {
//        beNormalAnimation();
//        mHandler.removeCallbacksAndMessages(null);
//        anim001.cancel();
//        anim002.cancel();
//        turnBigSet.cancel();
//        anim003.cancel();
//        anim004.cancel();
//        turnSmallSet.cancel();
//    }

    public void cancelLineAnimation() {
        if (anim1 != null && anim2 != null && animationSet != null) {
            anim1.cancel();
            anim1 = null;
            anim2.cancel();
            anim1 = null;
            animationSet.cancel();
            animationSet = null;
        }
        mLineView.setVisibility(View.GONE);
        mLineView2.setVisibility(View.GONE);
    }

    public void resultViewAnimation() {
        isScanDone = true;
        resultContentView.setVisibility(View.VISIBLE);
        if (!isSafe) {
            SDKWrapper.addEvent(this,
                    SDKWrapper.P1, "wifi_rst", "wifi_rst_risk");
            resultBigTv.setText(getString(R.string.wifi_scan_result_no_safe));
            resultSmallTv.setText(getString(R.string.wifi_scan_result_no_safe_advice));
            resultSmallTv.setVisibility(View.VISIBLE);
            resultIconView.setBackgroundResource(R.drawable.wifiunsafety);
//            resultIconView.setImageResource(R.drawable.wifiunsafety);
        } else {
            SDKWrapper.addEvent(this,
                    SDKWrapper.P1, "wifi_rst", "wifi_rst_safe");
            resultBigTv.setText(getString(R.string.wifi_scan_result_safe));
            resultIconView.setBackgroundResource(R.drawable.wifisafety);
//            resultIconView.setImageResource(R.drawable.wifisafety);
        }

        ObjectAnimator anim20 = ObjectAnimator.ofFloat(resultContentView,
                "scaleX", 0f, 1.1f);
        ObjectAnimator anim21 = ObjectAnimator.ofFloat(resultContentView,
                "scaleY", 0f, 1.1f);
        ObjectAnimator anim22 = ObjectAnimator.ofFloat(resultContentView,
                "alpha", 0f, 1f);
        //bg turn red
        ObjectAnimator anim23 = ObjectAnimator.ofFloat(mSmallBossView,
                "alpha", 1f, 0f);

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fixResultAnimation();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                contentView.setVisibility(View.GONE);
            }
        });
        set.setDuration(800);
        set.play(anim20).with(anim21);
        set.play(anim21).with(anim22);
        if (!isSafe) {
            set.play(anim22).with(anim23);
        }
        set.start();
    }

    private void fixResultAnimation() {
        ObjectAnimator anim30 = ObjectAnimator.ofFloat(resultContentView,
                "scaleX", 1.1f, 1f);
        ObjectAnimator anim31 = ObjectAnimator.ofFloat(resultContentView,
                "scaleY", 1.1f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        set.setDuration(250);
        set.play(anim30).with(anim31);
        set.start();
    }

    public void scanDone(boolean wifiSafe, boolean isConnect, boolean mPingOk,
                         boolean mOneState, boolean mTwoState,
                         boolean mThreeState, boolean mFourState) {
        isSafe = wifiSafe;
        //1,showResultFragment
        if (wifiSafe) {
            wifiResultFrangment.showTab(wifiSafe, isConnect);
        } else {
            wifiResultFrangment.showTab(wifiSafe, isConnect, mPingOk,
                    mOneState, mTwoState, mThreeState, mFourState);
        }
        //3,hide the scanline && change the iconBG
//        cancelLineAnimation();
        cancelIconLightAnimation();
        mIconView.setImageResource(R.drawable.wifiscan_bg_2);
    }

    public void setCanNotClick() {
        mIconView.setClickable(false);
    }
}
