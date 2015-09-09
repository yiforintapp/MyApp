
package com.leo.appmaster.applocker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.animation.ColorEvaluator;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.TaskChangeHandler;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.AppUnlockEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.eventbus.event.LockThemeChangeEvent;
import com.leo.appmaster.fragment.GestureLockFragment;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.fragment.PasswdLockFragment;
import com.leo.appmaster.fragment.PretendAppBeautyFragment;
import com.leo.appmaster.fragment.PretendAppErrorFragment;
import com.leo.appmaster.fragment.PretendAppUnknowCallFragment5;
import com.leo.appmaster.fragment.PretendAppZhiWenFragment;
import com.leo.appmaster.fragment.PretendFragment;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.push.ui.PushUIHelper;
import com.leo.appmaster.sdk.update.UIHelper;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoCircleView;
import com.leo.appmaster.ui.LeoHomePopMenu;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.FastBlur;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.ProcessUtils;
import com.leo.appmaster.utils.Utilities;
import com.mobvista.sdk.m.core.MobvistaAdWall;
import com.mobvista.sdk.m.core.WallIconCallback;

public class LockScreenActivity extends BaseFragmentActivity implements
        OnClickListener, OnDiaogClickListener {

    public static final String TAG = "LockScreenActivity";

    private static final String mPrivateLockPck = "com.leo.appmaster";
    public static final String THEME_CHANGE = "lock_theme_change";
    public static final String EXTRA_LOCK_MODE = "extra_lock_type";
    public static final String EXTRA_UKLOCK_TYPE = "extra_unlock_type";
    public static final String EXTRA_LOCK_TITLE = "extra_lock_title";
    public static final String SHOW_NOW = "mode changed_show_now";
    public static final long CLICK_OVER_DAY = 24 * 1000 * 60 * 60;
    public static final int SHOW_RED_MAN = 1;

    public static final int AD_TYPE_SHAKE = 1;
    public static final int AD_TYPE_JUMP = 2;
    public static final int AD_TYPE_STAY = 3;
    public int SHOW_AD_TYPE = 0;
    private int mLockMode;
    private String mLockedPackage;
    private CommonTitleBar mTtileBar;
    private LockFragment mLockFragment;
    private Bitmap mAppBaseInfoLayoutbg;
    private LeoHomePopMenu mLeoPopMenu;
    private LeoDoubleLinesInputDialog mDialog;
    private LEOAlarmDialog mTipDialog;
    private EditText mEtQuestion, mEtAnwser;
    private String mLockTitle;
    // private ImageView mThemeView;
    private ImageView mAdIcon, mAdIconRedTip;
    private View switch_bottom_content;
    private ImageView mADAnimalEntry;

    private boolean mNewTheme;
    private RelativeLayout mPretendLayout;
    private PretendFragment mPretendFragment;

    private Animation mAnim;
    private String mCleanRate;
    private TextView mText;
    private View mLockClean;
    private ActivityManager mAm;

    public boolean mRestartForThemeChanged;
    public boolean mQuickLockMode;
    public boolean mFromHome;
    public boolean mFromQuickGesture;
    public String mQuickModeName;
    public int mQuiclModeId;
    private RelativeLayout mLockLayout;
    private boolean mMissingDialogShowing;
    private MobvistaAdWall wallAd;
    public static boolean sLockFilterFlag = false;
    private static AnimationDrawable adAnimation;
    private Handler handler;
    public static boolean interupAinimation = false;
    private boolean clickShakeIcon = false;
    private boolean isClickAny = false;

    private MobvistaEngine mAdEngine;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_RED_MAN:
                    mADAnimalEntry.setBackgroundResource(R.drawable.adanimation3);
                    AnimationDrawable redmanAnimation = (AnimationDrawable)
                            mADAnimalEntry.getBackground();
                    redmanAnimation.start();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_setting);
        LeoLog.e("LockScreenActivity", "onCreate");

        mLockLayout = (RelativeLayout) findViewById(R.id.activity_lock_layout);
        handleIntent();
        LockManager lm = LockManager.getInstatnce();
        lm.setPauseScreenonLock(true);
        // for fix lock mode shortcut bug
        if (mQuickLockMode) {
            List<LockMode> modeList = lm.getLockMode();
            LockMode mode = null;
            for (LockMode lockMode : modeList) {
                if (lockMode.modeId == mQuiclModeId) {
                    mode = lockMode;
                    break;
                }
            }
            if (mode != null) {
                if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
                    if (mode.defaultFlag != -1) {
                        Intent intent = new Intent(this, LockSettingActivity.class);
                        intent.putExtra("from_quick_mode", true);
                        intent.putExtra("just_finish", true);
                        intent.putExtra("mode_id", mQuiclModeId);
                        this.startActivity(intent);
                    }
                    finish();
                    return;
                }
            } else {
                showModeMissedTip();
                return;
            }
        }

        initUI();
        mobvistaCheck();
        checkCleanMem();
        LeoEventBus.getDefaultBus().register(this);
        checkOutcount();
        handler = new Handler();
    }

    private void mobvistaCheck() {
        // init方法统一放到MobvistaEngine里，by lishuai
        // mobvista ad
        // MobvistaAd.init(this, Constants.MOBVISTA_APPID,
        // Constants.MOBVISTA_APPKEY);
        // -----------------Mobvista Sdk--------------------

        // init wall controller
        // newAdWallController(Context context,String unitid, String fbid)
        wallAd = MobvistaEngine.getInstance().createAdWallController(this);
        if (wallAd != null) {
            // preload the wall data
            wallAd.preloadWall();
        }

    }

    @SuppressWarnings("deprecation")
    private void setMobvistaIcon() {
        // In some case, we have no AD
        if (wallAd != null) {
            wallAd.loadWallIcon(new WallIconCallback() {

                @Override
                public void loaded(Drawable drawable) {
                    mAdIcon.setImageDrawable(drawable);
                }

                @Override
                public void failed() {

                }
            });

            if (SHOW_AD_TYPE == AD_TYPE_SHAKE && !isClickAny) {
                mAdIconRedTip.setVisibility(View.VISIBLE);
                mAdIcon.setBackgroundResource(R.drawable.adanimation2);
                adAnimation = (AnimationDrawable)
                        mAdIcon.getBackground();
                adAnimation.start();

            } else { // jump
                mAdIconRedTip.setVisibility(View.GONE);
                if (SHOW_AD_TYPE == AD_TYPE_JUMP && !isClickAny) {
                    mAdIcon.setBackgroundResource(R.drawable.adanimation);
                    adAnimation = (AnimationDrawable)
                            mAdIcon.getBackground();
                    adAnimation.start();
                } else {
                    mAdIcon.setBackgroundDrawable((this.getResources()
                            .getDrawable(R.drawable.jump_1)));
                }
            }
        }
    }

    private void showModeMissedTip() {
        mMissingDialogShowing = true;
        mTipDialog = new LEOAlarmDialog(this);
        mTipDialog.setTitle(R.string.tips_title);
        mTipDialog.setContent(getString(R.string.mode_missing));
        mTipDialog.setRightBtnStr(getString(R.string.lock_mode_guide_button_text));
        mTipDialog.setLeftBtnVisibility(false);
        mTipDialog.setRightBtnListener(new LEOAlarmDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                LockScreenActivity.this.finish();
            }
        });
        mTipDialog.show();
    }

    @Override
    protected void onResume() {
        whichTypeShow();

        LeoLog.e("poha", AppMasterPreference.getInstance(this).getADShowType()
                + ":current ad show type");

        if (AppMasterPreference.getInstance(this).getADShowType() == 3
                && NetWorkUtil.isNetworkAvailable(getApplicationContext())) {
            mADAnimalEntry.setVisibility(View.VISIBLE);
            if (SHOW_AD_TYPE != AD_TYPE_JUMP && SHOW_AD_TYPE != AD_TYPE_SHAKE && !isClickAny) {
                startShakeRotateAnimation(true);
            }
        }
        setMobvistaIcon();
        // 每次返回界面时，隐藏下方虚拟键盘，解决华为部分手机上每次返回界面如果之前有虚拟键盘会上下振动的bug
        // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // handlePretendLock(); 貌似oncreate里的init方法已经执行了，容易曹成内存泄露
        if (!mMissingDialogShowing) {

            // boolean lockThemeGuid = checkNewTheme();

            if (mLockMode == LockManager.LOCK_MODE_FULL) {
                /*
                 * tell PushUIHelper than do not show dialog when lockscreen is
                 * shown
                 */
                PushUIHelper.getInstance(getApplicationContext()).setIsLockScreen(true);
            }
            AppMasterPreference.getInstance(this).setUnlocked(false);
        }
        super.onResume();

        SDKWrapper.addEvent(this, SDKWrapper.P1, "tdau", "app");
    }

    private void whichTypeShow() {
        mHaveNewThings = AppMasterPreference.getInstance(this)
                .getIsADAppwallNeedUpdate();
        mLastTime = AppMasterPreference.getInstance(LockScreenActivity.this)
                .getAdClickTime();
        isClickJump = AppMasterPreference.getInstance(LockScreenActivity.this)
                .getJumpIcon();
        long mNowTime = System.currentTimeMillis();
        if ((mLastTime != 0 && mNowTime - mLastTime > CLICK_OVER_DAY) || mHaveNewThings) {// shake
            SHOW_AD_TYPE = AD_TYPE_SHAKE;
        } else { // jump
            if (!isClickJump && !clickShakeIcon) {
                SHOW_AD_TYPE = AD_TYPE_JUMP;
            } else {
                SHOW_AD_TYPE = AD_TYPE_STAY;
            }
        }
    }

    @SuppressLint("NewApi")
    private void startShakeRotateAnimation(boolean isFirstIn) {
        mADAnimalEntry.setBackgroundResource(R.drawable.alien_bg1);

        RotateAnimation reverse = new RotateAnimation(0f, 16f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
        reverse.setDuration(500);
        reverse.setFillAfter(true);
        if (isFirstIn) {
            reverse.setStartOffset(500);
        }
        mADAnimalEntry.setAnimation(reverse);
        reverse.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                RotateAnimation reverse1 = new RotateAnimation(16f, -14f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
                reverse1.setFillAfter(true);
                reverse1.setDuration(600);
                mADAnimalEntry.setAnimation(reverse1);
                reverse1.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        RotateAnimation reverse2 = new RotateAnimation(-14f, 12f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
                        reverse2.setFillAfter(true);
                        reverse2.setDuration(700);
                        mADAnimalEntry.setAnimation(reverse2);
                        reverse2.setAnimationListener(new AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                RotateAnimation tozero = new RotateAnimation(12f, 0f,
                                        Animation.RELATIVE_TO_SELF,
                                        0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
                                tozero.setFillAfter(true);
                                tozero.setDuration(750);
                                mADAnimalEntry.setAnimation(tozero);
                                tozero.setAnimationListener(new AnimationListener() {

                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        interupAinimation = false;
                                        mADAnimalEntry.clearAnimation();
                                        Message msg = new Message();
                                        msg.what = SHOW_RED_MAN;
                                        mHandler.sendEmptyMessageAtTime(msg.what,
                                                500);
                                    }
                                });
                                if (interupAinimation) {
                                    mADAnimalEntry.clearAnimation();
                                    interupAinimation = false;
                                } else {
                                    tozero.start();
                                }
                            }
                        });
                        if (interupAinimation) {
                            mADAnimalEntry.clearAnimation();
                            interupAinimation = false;
                        } else {
                            reverse2.start();
                        }
                    }
                });
                if (interupAinimation) {
                    mADAnimalEntry.clearAnimation();
                    interupAinimation = false;
                } else {
                    reverse1.start();
                }
            }
        });
        reverse.start();
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    private boolean checkNewTheme() {
        String locSerial = AppMasterPreference.getInstance(this)
                .getLocalThemeSerialNumber();
        String onlineSerial = AppMasterPreference.getInstance(this)
                .getOnlineThemeSerialNumber();
        boolean lockThemeGuid = AppMasterPreference.getInstance(this)
                .getLockerScreenThemeGuid();
        if (!locSerial.equals(onlineSerial)) {
            mNewTheme = true;
        } else {
            mNewTheme = false;
        }

        // if (mNewTheme) {
        // mThemeView.setImageDrawable(this.getResources().getDrawable(
        // R.drawable.theme_active_icon));
        // } else {
        // mThemeView.setImageDrawable(this.getResources().getDrawable(
        // R.drawable.theme_icon));
        // }

        return lockThemeGuid;
    }

    @Override
    protected void onPause() {
        PushUIHelper.getInstance(getApplicationContext())
                .setIsLockScreen(false);
        super.onPause();
    }

    /**
     * <b>Note</b> if lock mode is changed from LockManager.LOCK_MODE_PURE to
     * LockManager.LOCK_MODE_FULL, just restart screen pager
     */
    @Override
    protected void onNewIntent(Intent intent) {

        if (mLockMode == LockManager.LOCK_MODE_PURE && intent.getIntExtra(EXTRA_LOCK_MODE,
                LockManager.LOCK_MODE_FULL) == LockManager.LOCK_MODE_FULL) {
            finish();
            startActivity(intent);

            return;
        }

        mQuickLockMode = intent.getBooleanExtra("quick_lock_mode", false);
        mFromQuickGesture = intent.getBooleanExtra("from_quick_gesture", false);
        if (!mFromQuickGesture) {
            mFromHome = intent.getBooleanExtra("from_home", false);
        }

        if (mQuickLockMode) {
            mQuickModeName = intent.getStringExtra("lock_mode_name");
            mQuiclModeId = intent.getIntExtra("lock_mode_id", -1);
            // home mode replace unlock-all mode
            if (mQuiclModeId == 0) {
                mQuiclModeId = 3;
            }
        }
        if (mQuickLockMode) {
            mLockedPackage = getPackageName();
            mTtileBar.setTitle(R.string.change_lock_mode);
        } else {
            mTtileBar.setTitle(R.string.app_name);
        }

        String newLockedPkg = intent.getStringExtra(TaskChangeHandler.EXTRA_LOCKED_APP_PKG);
        if (!TextUtils.equals(newLockedPkg, mLockedPackage)) {
            mLockedPackage = newLockedPkg;

            if (mPretendFragment != null) {
                mPretendLayout.setVisibility(View.GONE);
                mLockLayout.setVisibility(View.VISIBLE);
            }

            // change background
            if (!ThemeUtils.checkThemeNeed(this)
                    && (mLockMode == LockManager.LOCK_MODE_FULL)) {
                BitmapDrawable bd = (BitmapDrawable) AppUtil.getDrawable(
                        getPackageManager(),
                        mLockedPackage);
                if (bd == null) {
                    bd = (BitmapDrawable) AppUtil.getDrawable(
                            getPackageManager(),
                            getPackageName());
                }
                setAppInfoBackground(bd);
            }

            mLockFragment.onLockPackageChanged(mLockedPackage);
            LeoLog.d(TAG, "onNewIntent" + "     mToPackage = " + mLockedPackage);
            if (mPretendFragment == null) {
                // 解决Fragment内存泄露
                mPretendFragment = getPretendFragment();
            }
            boolean showPretend = !mPrivateLockPck.equals(mLockedPackage);
            if (mPretendFragment != null && showPretend) { // ph
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction tans;
                mPretendLayout = (RelativeLayout) findViewById(R.id.pretend_layout);
                mLockLayout.setVisibility(View.GONE);
                mPretendLayout.setVisibility(View.VISIBLE);
                tans = fm.beginTransaction();
                tans.replace(R.id.pretend_layout, mPretendFragment);
                tans.commitAllowingStateLoss();
            }
            if (mPretendFragment != null && showPretend) {
                mLockLayout.setVisibility(View.GONE);
                mPretendLayout.setVisibility(View.VISIBLE);
                if (mPretendFragment instanceof PretendAppErrorFragment) {
                    String tip = "";
                    PackageManager pm = this.getPackageManager();
                    try {
                        ApplicationInfo info = pm.getApplicationInfo(mLockedPackage,
                                PackageManager.GET_UNINSTALLED_PACKAGES);
                        String lab = info.loadLabel(pm).toString();
                        tip = getString(R.string.pretend_app_error, lab);

                    } catch (NameNotFoundException e) {
                        tip = getString(R.string.weizhuang_error_notice);
                        e.printStackTrace();
                    }

                    ((PretendAppErrorFragment) mPretendFragment).setErrorTip(tip);
                }
            }
        }
        mLockFragment.setPackage(mLockedPackage);
        mLockFragment.onNewIntent();
        checkOutcount();
        super.onNewIntent(intent);
    }

    private void checkOutcount() {
        LockManager lm = LockManager.getInstatnce();
        int outcountTime = lm.getOutcountTime(mLockedPackage);
        if (outcountTime > 0) {
            LockManager.getInstatnce().timeFilter(getPackageName(), 200);
            Intent intent = new Intent(this, WaitActivity.class);
            intent.putExtra("outcount_time", 10 - outcountTime / 1000);
            startActivity(intent);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mRestartForThemeChanged = intent.getBooleanExtra("from_theme_change", false);
        mQuickLockMode = intent.getBooleanExtra("quick_lock_mode", false);
        mFromHome = intent.getBooleanExtra("from_home", false);
        mFromQuickGesture = intent.getBooleanExtra("from_quick_gesture", false);
        if (mQuickLockMode) {
            mQuickModeName = intent.getStringExtra("lock_mode_name");
            mQuiclModeId = intent.getIntExtra("lock_mode_id", -1);

            // home mode replace unlock-all mode
            if (mQuiclModeId == 0) {
                mQuiclModeId = 3;
            }
        }
        if (mQuickLockMode) {
            mLockedPackage = getPackageName();
        } else {
            mLockedPackage = intent.getStringExtra(TaskChangeHandler.EXTRA_LOCKED_APP_PKG);
        }

        mLockMode = intent.getIntExtra(EXTRA_LOCK_MODE,
                LockManager.LOCK_MODE_FULL);

        int type = AppMasterPreference.getInstance(this).getLockType();
        if (type == LockFragment.LOCK_TYPE_PASSWD) {
            mLockFragment = new PasswdLockFragment();
        } else {
            mLockFragment = new GestureLockFragment();
        }
        if (!ThemeUtils.checkThemeNeed(this)
                && (mLockMode == LockManager.LOCK_MODE_FULL)) {
            BitmapDrawable bd = (BitmapDrawable) AppUtil.getDrawable(
                    getPackageManager(), mLockedPackage);
            setAppInfoBackground(bd);
        }
        mLockTitle = intent.getStringExtra(EXTRA_LOCK_TITLE);
        mLockFragment.setLockMode(mLockMode);
        mLockFragment.setPackage(mLockedPackage);

        /* SDK: mark user what to unlock which app */
        if (mLockMode == LockManager.LOCK_MODE_FULL) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "access_locked_app",
                    mLockedPackage);
        }

        LeoLog.d("LockScreenActivity", "mToPackage = " + mLockedPackage);
    }

    private void setAppInfoBackground(Drawable drawable) {
        if (drawable != null) {
            int h = drawable.getIntrinsicHeight() * 9 / 10;
            int w = h * 3 / 5;
            if (h > 0 && w > 0) {
                mAppBaseInfoLayoutbg = Bitmap.createBitmap(w, h,
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(mAppBaseInfoLayoutbg);
                canvas.drawColor(Color.WHITE);
                drawable.setBounds(-(drawable.getIntrinsicWidth() - w) / 2,
                        -(drawable.getIntrinsicHeight() - h) / 2,
                        (drawable.getIntrinsicWidth() - w) / 2 + w,
                        (drawable.getIntrinsicHeight() - h) / 2 + h);
                drawable.draw(canvas);
                canvas.drawColor(Color.argb(70, 0, 0, 0));
                mAppBaseInfoLayoutbg = FastBlur.doBlur(mAppBaseInfoLayoutbg, 25, true);
                mLockLayout.setBackgroundDrawable(new BitmapDrawable(mAppBaseInfoLayoutbg));
            }
        }
    }

    @Override
    protected void onDestroy() {
        LockManager.getInstatnce().setPauseScreenonLock(false);
        super.onDestroy();
        if (mAppBaseInfoLayoutbg != null) {
            mAppBaseInfoLayoutbg.recycle();
            mAppBaseInfoLayoutbg = null;
        }
        // 这部分注释掉，担心影响到MobvistaEngine里的逻辑，因为init调用只有一次, by lishuai
        // try {
        // MobvistaAd.release();
        // } catch (Exception e) {
        // }
        if (wallAd != null) {
            wallAd.release();
            wallAd = null;
        }
        LeoLog.d("LockScreenActivity", "onDestroy");
        LeoEventBus.getDefaultBus().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void finish() {
        if (!isFinishing()) {
            super.finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /**
         * dont change it, for lock theme
         */
        if (mRestartForThemeChanged) {
            Intent intent = getIntent();
            finish();
            mRestartForThemeChanged = false;
            intent.putExtra("from_theme_change", true);
            startActivity(intent);
        }
    }

    private void initUI() {

        mADAnimalEntry = (ImageView) findViewById(R.id.iv_AD_entry);
        if (!NetWorkUtil.isNetworkAvailable(getApplicationContext())) {
            mADAnimalEntry.setVisibility(View.GONE);
        }

        mADAnimalEntry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isClickAny = true;
                SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "ad_cli", "draw");
                interupAinimation = true;
                // Toast.makeText(LockScreenActivity.this, showType + "",
                // 0).show();
                mADAnimalEntry.setVisibility(View.GONE);
                Intent intent = new Intent(LockScreenActivity.this,
                        UFOActivity.class);
                startActivity(intent);
                SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "ad_act", "ad_draw");
                overridePendingTransition(DEFAULT_KEYS_DISABLE, DEFAULT_KEYS_DISABLE);
            }
        });

        mAnim = AnimationUtils.loadAnimation(this, R.anim.locker_guide);
        // mThemeView = (ImageView) findViewById(R.id.img_layout_right);
        switch_bottom_content = findViewById(R.id.switch_bottom_content);
        switch_bottom_content.setVisibility(View.INVISIBLE);
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        if (mLockMode == LockManager.LOCK_MODE_FULL) {
            mTtileBar.setBackArrowVisibility(View.GONE);

            if (mQuickLockMode) {
                LockManager lm = LockManager.getInstatnce();
                List<LockMode> modes = lm.getLockMode();
                LockMode targetMode = null;
                for (LockMode lockMode : modes) {
                    if (lockMode.modeId == mQuiclModeId) {
                        targetMode = lockMode;
                        break;
                    }
                }
                if (targetMode != null) {
                    mTtileBar.setTitle(R.string.change_lock_mode);
                } else {
                    mTtileBar.setTitle(R.string.app_name);
                }
            } else {
                mTtileBar.setTitle(R.string.app_name);
            }
        } else {
            mTtileBar.setBackViewListener(this);
            if (TextUtils.isEmpty(mLockTitle)) {
                mTtileBar.setTitle(R.string.app_lock);
            } else {
                mTtileBar.setTitle(mLockTitle);
            }
            mTtileBar.setHelpSettingVisiblity(View.INVISIBLE);
        }

        // if (AppMasterPreference.getInstance(this).getLockScreenMenuClicked())
        // {
        mTtileBar.setOptionImage(R.drawable.menu_item_btn);
        // } else {
        // mTtileBar.setOptionImage(R.drawable.menu_item_red_tip_btn);
        // }

        mTtileBar.setOptionImageVisibility(View.VISIBLE);
        mTtileBar.setOptionImagePadding(DipPixelUtil.dip2px(this, 5));
        mTtileBar.setOptionListener(this);

        mAdIconRedTip = (ImageView) findViewById(R.id.gift_red_tip);
        mAdIcon = (ImageView) findViewById(R.id.icon_ad_layout);
        ((View) mAdIcon.getParent()).setVisibility(View.VISIBLE);
        mAdIcon.setVisibility(View.VISIBLE);
        mAdIcon.setOnClickListener(this);

        // mThemeView = (ImageView) findViewById(R.id.img_layout_right);
        // ((View) mThemeView.getParent()).setVisibility(View.VISIBLE);
        // mThemeView.setVisibility(View.VISIBLE);
        // mThemeView.setOnClickListener(this);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tans = fm.beginTransaction();
        tans.replace(R.id.fragment_contain, mLockFragment);
        tans.commit();

        handlePretendLock();

    }

    // handle pretend lock
    private void handlePretendLock() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tans;
        mPretendLayout = (RelativeLayout) findViewById(R.id.pretend_layout);
        if (mPretendFragment == null) {
            // 解决Fragment内存泄露
            mPretendFragment = getPretendFragment();
        }

        if (mPretendFragment != null && !mRestartForThemeChanged) {
            mLockLayout.setVisibility(View.GONE);
            mPretendLayout.setVisibility(View.VISIBLE);
            tans = fm.beginTransaction();
            tans.add(R.id.pretend_layout, mPretendFragment);
            tans.commit();
        } else {
            mLockLayout.setVisibility(View.VISIBLE);
            mPretendLayout.setVisibility(View.GONE);
        }
    }

    private PretendFragment getPretendFragment() {
        if (!mPrivateLockPck.equals(mLockedPackage) && !mQuickLockMode) {
            int pretendLock = AppMasterPreference.getInstance(this).getPretendLock();
            // pretendLock = 2;
            if (pretendLock == 1) { /* app error */
                PretendAppErrorFragment paf = new PretendAppErrorFragment();

                String tip = "";
                PackageManager pm = this.getPackageManager();
                try {
                    ApplicationInfo info = pm.getApplicationInfo(mLockedPackage,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
                    String lab = info.loadLabel(pm).toString();
                    tip = getString(R.string.pretend_app_error, lab);
                } catch (Exception e) {
                    tip = getString(R.string.weizhuang_error_notice);
                    e.printStackTrace();
                }
                paf.setErrorTip(tip);
                return paf;
            } else if (pretendLock == 2) {/* unknow call */
                PretendAppUnknowCallFragment5 unknowcall = new PretendAppUnknowCallFragment5();
                return unknowcall;
            } else if (pretendLock == 3) {/* fingerprint */
                PretendAppZhiWenFragment weizhuang = new PretendAppZhiWenFragment();
                return weizhuang;
            }
            else if (pretendLock == 4)
            {
                PretendAppBeautyFragment weizhuang = new PretendAppBeautyFragment();
                return weizhuang;
            }
        }
        return null;
    }

    public void onUnlockSucceed() {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        if (mQuickLockMode) {
            LockManager lm = LockManager.getInstatnce();
            List<LockMode> modeList = lm.getLockMode();
            LockMode willLaunch = null;
            for (LockMode lockMode : modeList) {
                if (mQuiclModeId == lockMode.modeId) {
                    willLaunch = lockMode;
                    Log.i("tag", "falg ==" + lockMode.defaultFlag);
                    break;
                }
            }

            if (willLaunch == null) {
                LockMode homeMode = null;
                for (LockMode lockMode : modeList) {
                    if (lockMode.defaultFlag == 3) {
                        homeMode = lockMode;
                        break;
                    }
                }
                willLaunch = homeMode;
            }
            if (willLaunch != null) {
                LockMode lockMode = LockManager.getInstatnce().getCurLockMode();
                lm.setCurrentLockMode(willLaunch, true);
                checkLockTip();
                SDKWrapper.addEvent(this, SDKWrapper.P1, "modeschage", "launcher");
                /** mode change tip **/
                if (null != lockMode) {
                    int currentModeFlag = lockMode.defaultFlag;
                    showModeActiveTip(willLaunch.defaultFlag, currentModeFlag);
                    SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "modeschage",
                            "shortcuts");
                } else {
                    showModeActiveTip(willLaunch);
                }
                LeoEventBus.getDefaultBus().post(
                        new LockModeEvent(EventId.EVENT_MODE_CHANGE, "mode changed_show_now"));
            } else {
                // Toast.makeText(this, mQuickModeName + "模式不存在, 请重试",
                // 0).show();
            }

        } else {
            /**
             * notify LockManager
             */
            LeoEventBus.getDefaultBus().post(
                    new AppUnlockEvent(mLockedPackage, AppUnlockEvent.RESULT_UNLOCK_SUCCESSFULLY));
            if (mLockMode == LockManager.LOCK_MODE_FULL) {
                if (AppMasterPreference.getInstance(LockScreenActivity.this)
                        .isLockerClean()) {
                    Toast mToast = new Toast(this);
                    LayoutInflater mLayoutInflater = LayoutInflater
                            .from(LockScreenActivity.this);
                    mLockClean = mLayoutInflater.inflate(
                            R.layout.activity_lockclean_toast, null);
                    mText = (TextView) mLockClean.findViewById(R.id.textToast);
                    String textResource = getResources().getString(
                            R.string.locker_clean);
                    String cleanRate = String.format(textResource, mCleanRate);
                    mText.setText(cleanRate);
                    mToast.setGravity(Gravity.BOTTOM, 0, 66);
                    mToast.setDuration(1000);
                    mToast.setView(mLockClean);
                    mToast.show();
                }

                pref.setUnlockCount(pref.getUnlockCount() + 1);
                // quick gesture unlock count
                pref.setNewUserUnlockCount(pref.getNewUserUnlockCount() + 1);
            } else if (mLockMode == LockManager.LOCK_MODE_PURE) {
            }

            SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "unlock", "done");

        }
        /* 解锁成功弹出升级提示 */
        boolean isUnLockUpdateTip = pref.getUnlockUpdateTip();
        if (isUnLockUpdateTip) {
            UIHelper.getInstance(this).unlockSuccessUpdateTip();
        }
        LockManager.getInstatnce().timeFilter(mLockedPackage, 1000);
        mTtileBar.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mFromHome) { // for fix bug: AM-1904
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                }
                // finish self
                finish();
            }
        }, 100);

        AppMasterPreference amp = AppMasterPreference.getInstance(LockScreenActivity.this);
        amp.setLockerScreenThemeGuide(true);
        amp.setUnlocked(true);
        amp.setDoubleCheck(null);
    }

    private void checkLockTip() {
        int switchCount = AppMasterPreference.getInstance(this).getSwitchModeCount();
        switchCount++;
        AppMasterPreference.getInstance(this).setSwitchModeCount(switchCount);
        LockManager lm = LockManager.getInstatnce();
        List<TimeLock> timeLockList = lm.getTimeLock();
        List<LocationLock> locationLockList = lm.getLocationLock();
        if (switchCount == 6) {

            int timeLockCount = timeLockList.size();
            int locationLockCount = locationLockList.size();

            if (timeLockCount == 0 && locationLockCount == 0) {
                // show three btn dialog
                LEOThreeButtonDialog dialog = new LEOThreeButtonDialog(
                        AppMasterApplication.getInstance());
                dialog.setTitle(R.string.time_location_lock_tip_title);
                String tip = this.getString(R.string.time_location_lock_tip_content);
                dialog.setContent(tip);
                dialog.setLeftBtnStr(this.getString(R.string.cancel));
                dialog.setMiddleBtnStr(this.getString(R.string.lock_mode_time));
                dialog.setRightBtnStr(this.getString(R.string.lock_mode_location));
                dialog.setRightBtnBackground(R.drawable.manager_mode_lock_third_button_selecter);
                dialog.setOnClickListener(new LEOThreeButtonDialog.OnDiaogClickListener() {
                    @Override
                    public void onClick(int which) {
                        Intent intent = null;
                        if (which == 0) {
                            // cancel
                        } else if (which == 1) {
                            // new time lock
                            intent = new Intent(AppMasterApplication.getInstance(),
                                    TimeLockEditActivity.class);
                            intent.putExtra("new_time_lock", true);
                            intent.putExtra("from_dialog", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            AppMasterApplication.getInstance().startActivity(intent);
                        } else if (which == 2) {
                            // new location lock
                            intent = new Intent(AppMasterApplication.getInstance(),
                                    LocationLockEditActivity.class);
                            intent.putExtra("new_location_lock", true);
                            intent.putExtra("from_dialog", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            AppMasterApplication.getInstance().startActivity(intent);
                        }
                    }
                });
                dialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
            } else {
                if (timeLockCount == 0 && locationLockCount != 0) {
                    // show time lock btn dialog
                    LEOAlarmDialog dialog = new LEOAlarmDialog(AppMasterApplication.getInstance());
                    dialog.setTitle(R.string.time_location_lock_tip_title);
                    String tip = this.getString(R.string.time_location_lock_tip_content);
                    dialog.setContent(tip);
                    dialog.setRightBtnStr(this.getString(R.string.lock_mode_time));
                    dialog.setRightBtnBackground(R.drawable.manager_right_contact_button_selecter);
                    dialog.setLeftBtnStr(this.getString(R.string.cancel));
                    dialog.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            Intent intent = null;
                            if (which == 0) {
                                // cancel
                            } else if (which == 1) {
                                // new time lock
                                intent = new Intent(AppMasterApplication.getInstance(),
                                        TimeLockEditActivity.class);
                                intent.putExtra("new_time_lock", true);
                                intent.putExtra("from_dialog", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                AppMasterApplication.getInstance().startActivity(intent);
                            }

                        }
                    });
                    dialog.getWindow().setType(
                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();

                } else if (timeLockCount != 0 && locationLockCount == 0) {
                    // show lcaotion btn dialog
                    LEOAlarmDialog dialog = new LEOAlarmDialog(AppMasterApplication.getInstance());
                    dialog.setTitle(R.string.time_location_lock_tip_title);
                    String tip = this.getString(R.string.time_location_lock_tip_content);
                    dialog.setContent(tip);
                    dialog.setRightBtnStr(this.getString(R.string.lock_mode_location));
                    dialog.setRightBtnBackground(R.drawable.manager_right_contact_button_selecter);
                    dialog.setLeftBtnStr(this.getString(R.string.cancel));

                    dialog.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            if (which == 0) {
                                // cancel
                            } else if (which == 1) {
                                // new time lock
                                Intent intent = new Intent(AppMasterApplication.getInstance(),
                                        LocationLockEditActivity.class);
                                intent.putExtra("new_location_lock", true);
                                intent.putExtra("from_dialog", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                AppMasterApplication.getInstance().startActivity(intent);
                            }

                        }
                    });
                    dialog.getWindow().setType(
                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();
                }
            }
        }
    }

    public void onUnlockOutcount() {
        /**
         * notify LockManager
         */
        LeoEventBus.getDefaultBus().post(
                new AppUnlockEvent(mLockedPackage, AppUnlockEvent.RESULT_UNLOCK_OUTCOUNT));
        AppMasterPreference.getInstance(this).setDoubleCheck(null);
        LockManager.getInstatnce().recordOutcountTask(mLockedPackage);
        Intent intent = new Intent(this, WaitActivity.class);
        intent.putExtra(TaskChangeHandler.EXTRA_LOCKED_APP_PKG, mLockedPackage);
        startActivity(intent);
        SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "unlock", "fail");
    }

    private void findPasswd() {
        mDialog = new LeoDoubleLinesInputDialog(this);
        mDialog.setTitle(R.string.pleas_input_anwser);
        mDialog.setFirstHead(R.string.passwd_question);
        mDialog.setSecondHead(R.string.passwd_anwser);
        mDialog.setOnClickListener(this);
        mEtQuestion = mDialog.getFirstEditText();
        mEtAnwser = mDialog.getSecondEditText();
        mEtQuestion.setFocusable(false);
        mEtQuestion.setText(AppMasterPreference.getInstance(this)
                .getPpQuestion());
        mDialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        // if (mLockMode == LockManager.LOCK_MODE_FULL) {
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        /**
         * notify LockManager
         */
        LeoEventBus.getDefaultBus().post(
                new AppUnlockEvent(mLockedPackage, AppUnlockEvent.RESULT_UNLOCK_CANCELED));
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_option_image:
                if (mLeoPopMenu == null) {
                    mLeoPopMenu = new LeoHomePopMenu();
                    mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
                            setPopWindowItemClick(position);
                            mLeoPopMenu.dismissSnapshotList();
                        }
                    });
                }
                mLeoPopMenu.setPopMenuItems(this, getPopMenuItems(), getMenuIcons());
                mLeoPopMenu.showPopMenu(this,
                        mTtileBar.findViewById(R.id.tv_option_image), null, null);
                mLeoPopMenu.setListViewDivider(null);
                AppMasterPreference.getInstance(LockScreenActivity.this).setLockScreenMenuClicked(
                        true);
                mTtileBar.setOptionImage(R.drawable.menu_item_btn);
                break;
            case R.id.layout_title_back:
                onBackPressed();
                finish();
                break;
            // case R.id.img_layout_right:
            // sLockFilterFlag = true;
            // Intent intent = new Intent(LockScreenActivity.this,
            // LockerTheme.class);
            // SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1,
            // "theme_enter", "unlock");
            // AppMasterPreference amp =
            // AppMasterPreference.getInstance(this);
            // amp.setUnlocked(true);
            // amp.setDoubleCheck(null);
            // startActivityForResult(intent, 0);
            // amp.setLockerScreenThemeGuide(true);
            // break;
            case R.id.icon_ad_layout:
                isClickAny = true;
                sLockFilterFlag = true;
                AppMasterPreference mAmp = AppMasterPreference.getInstance(this);
                mAmp.setUnlocked(true);
                mAmp.setDoubleCheck(null);
                // wallAd.clickWall();
                mAmp.setIsADAppwallNeedUpdate(false);
                Intent mWallIntent = wallAd.getWallIntent();
                startActivity(mWallIntent);
                if (!mHaveNewThings && !clickShakeIcon) {
                    AppMasterPreference.getInstance(LockScreenActivity.this).setJumpIcon(true);
                } else {
                    clickShakeIcon = true;
                }
                AppMasterPreference.getInstance(LockScreenActivity.this).setAdClickTime(
                        System.currentTimeMillis());
                // Send ad event for network available only
                if (NetWorkUtil.isNetworkAvailable(getApplicationContext())) {
                    SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1,
                            "ad_cli", "unlocktop");
                }
                break;
            default:
                break;
        }
    }

    /**
     * setting the menu item,if has password protect then add find password item
     * 
     * @return
     */
    private List<String> getPopMenuItems() {
        List<String> listItems = new ArrayList<String>();
        Resources resources = AppMasterApplication.getInstance().getResources();
        if (AppMasterPreference.getInstance(this).hasPswdProtect()) {
            if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_GESTURE) {
                listItems.add(resources.getString(R.string.find_gesture));
            } else if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_PASSWD) {
                listItems.add(resources.getString(R.string.find_passwd));
            }
        }
        listItems.add(resources.getString(R.string.unlock_theme));
        listItems.add(resources.getString(R.string.setting_hide_lockline));
        listItems.add(resources.getString(R.string.help_setting_tip_title));

        return listItems;
    }

    private List<Integer> getMenuIcons() {
        List<Integer> icons = new ArrayList<Integer>();
        if (AppMasterPreference.getInstance(this).hasPswdProtect()) {
            icons.add(R.drawable.forget_password_icon);
        }
        icons.add(R.drawable.theme_icon_black);
        if (AppMasterPreference.getInstance(this).getIsHideLine()) {
            icons.add(R.drawable.show_locus_icon);
        } else {
            icons.add(R.drawable.hide_locus_icon);
        }
        icons.add(R.drawable.help_tip_icon);
        return icons;
    }

    private void setPopWindowItemClick(int position) {
        if (AppMasterPreference.getInstance(this).hasPswdProtect()) {
            if (position == 0) {
                findPasswd();
            } else if (position == 1) {
                onMoveToTheme();
            } else if (position == 2) {
                onHideLockLineClicked(position);
            } else {
                onHelpItemClicked();
            }
        } else {
            if (position == 0) {
                onMoveToTheme();
            } else if (position == 1) {
                onHideLockLineClicked(position);
            } else if (position == 2) {
                onHelpItemClicked();
            }
        }
    }

    private void onMoveToTheme() {
        sLockFilterFlag = true;
        Intent intent = new Intent(LockScreenActivity.this,
                LockerTheme.class);
        SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1,
                "theme_enter", "unlock");
        AppMasterPreference amp =
                AppMasterPreference.getInstance(this);
        amp.setUnlocked(true);
        amp.setDoubleCheck(null);
        startActivityForResult(intent, 0);
        amp.setLockerScreenThemeGuide(true);
    }

    private void onHideLockLineClicked(int position) {
        String tip;
        if (AppMasterPreference.getInstance(this).getIsHideLine()) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "trackhide", "unlock_off");
            mLeoPopMenu.updateItemIcon(position, R.drawable.hide_locus_icon);
            AppMasterPreference.getInstance(this).setHideLine(false);
            tip = getString(R.string.lock_line_visiable);
        } else {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "trackhide", "unlock_on");
            mLeoPopMenu.updateItemIcon(position, R.drawable.show_locus_icon);
            AppMasterPreference.getInstance(this).setHideLine(true);
            tip = getString(R.string.lock_line_hide);
        }
        if (mLockFragment instanceof GestureLockFragment) {
            ((GestureLockFragment) mLockFragment).reInvalideGestureView();
        }
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
    }

    private void onHelpItemClicked() {
        sLockFilterFlag = true;
        AppMasterPreference ampp = AppMasterPreference.getInstance(this);
        ampp.setLockerScreenThemeGuide(true);
        ampp.setUnlocked(true);
        ampp.setDoubleCheck(null);
        Intent helpSettingIntent = new Intent(LockScreenActivity.this,
                LockHelpSettingTip.class);
        helpSettingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try {
            LockScreenActivity.this.startActivity(helpSettingIntent);
        } catch (Exception e) {
        }
        /* SDK Event Mark */
        SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1, "help", "help_tip");
    }

    @Override
    public void onClick(int which) {
        if (which == 1) {// make sure
            String anwser = AppMasterPreference.getInstance(this).getPpAnwser();
            if (anwser.equals(mEtAnwser.getText().toString())) {
                AppMasterPreference ampp = AppMasterPreference.getInstance(this);
                ampp.setUnlocked(true);
                ampp.setDoubleCheck(null);
                // goto reset passwd
                Intent intent = new Intent(this, LockSettingActivity.class);
                intent.putExtra(LockSettingActivity.RESET_PASSWD_FLAG, true);
                this.startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, R.string.reinput_anwser, 0).show();
                mEtAnwser.setText("");
            }
        } else if (which == 0) { // cancel
            mDialog.dismiss();
        }
    }

    public int getFromType() {
        return mLockMode;
    }

    /**
     * themeGuide
     * 
     * @param view
     * @param anim
     */
    private void themeGuide(View view, Animation anim) {
        view.startAnimation(anim);
    }

    /**
     * check clean memory
     */
    private void checkCleanMem() {
        if (AppMasterPreference.getInstance(LockScreenActivity.this)
                .isLockerClean()) {
            long totalMem = ProcessUtils.getTotalMem();
            long lastUsedMem = totalMem - ProcessUtils.getAvailableMem(this);
            cleanAllProcess();
            long curUsedMem = totalMem - ProcessUtils.getAvailableMem(this);
            long cleanMem = Math.abs(lastUsedMem - curUsedMem);
            double number = (double) cleanMem / lastUsedMem;
            int numberRate = (int) (number * 100);
            if (numberRate <= 0) {
                int random = (int) (Math.random() * 10 + 1);
                mCleanRate = random + "%";
            } else {
                int cleanNumber = numberRate;
                mCleanRate = cleanNumber + "%";
            }
        }
    }

    private void cleanAllProcess() {
        mAm = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> list = mAm.getRunningAppProcesses();
        List<String> launchers = getLauncherPkgs(this);
        for (RunningAppProcessInfo runningAppProcessInfo : list) {
            if (runningAppProcessInfo.importance > RunningAppProcessInfo.IMPORTANCE_CANT_SAVE_STATE) {
                if (!launchers.contains(runningAppProcessInfo.processName)) {
                    mAm.killBackgroundProcesses(runningAppProcessInfo.processName);
                }
            }
        }
    }

    private List<String> getLauncherPkgs(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        List<String> pkgs = new ArrayList<String>();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo resolveInfo : apps) {
            ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
            String packageName = applicationInfo.packageName;
            pkgs.add(packageName);
        }
        if (mLockedPackage != null) {
            pkgs.add(mLockedPackage);
        }
        return pkgs;
    }

    /**
     * dont change this method
     * 
     * @param event
     */
    public void onEventMainThread(LockThemeChangeEvent event) {
        mRestartForThemeChanged = true;
    }

    public void removePretendFrame() {
        mPretendLayout.setVisibility(View.GONE);
        mLockLayout.setVisibility(View.VISIBLE);
    }

    private float top, width, height;
    private LeoCircleView bgView;
    private ImageView modeIconOut;
    private ImageView modeIconIn;
    private ImageView modeIconDown;
    private TextView mActiveText;
    private Map<String, Object> currModeIconMap;
    private Map<String, Object> willModeIconMap;

    private boolean mHaveNewThings = false;

    private long mLastTime;
    private boolean isClickJump;

    /**
     * show the tip when mode success activating
     */
    private void showModeActiveTip(int willModeFlag, int currentModeFlag) {
        currModeIconMap = modeIconSwitch(currentModeFlag);
        willModeIconMap = modeIconSwitch(willModeFlag);

        View mTipView = LayoutInflater.from(this).inflate(R.layout.lock_mode_active_tip, null);
        mActiveText = (TextView) mTipView.findViewById(R.id.active_text);
        mActiveText.setText(this.getString(R.string.mode_change, mQuickModeName));
        mActiveText.setMaxLines(2);
        bgView = (LeoCircleView) mTipView.findViewById(R.id.mode_active_bg);
        modeIconIn = (ImageView) mTipView.findViewById(R.id.mode_active_in);
        modeIconOut = (ImageView) mTipView.findViewById(R.id.mode_active_out);
        modeIconDown = (ImageView) mTipView.findViewById(R.id.mode_active_down);
        modeIconIn.setImageResource((Integer) willModeIconMap.get("modeIcon"));
        modeIconOut.setImageResource((Integer) currModeIconMap.get("modeIcon"));
        modeIconDown.setImageResource((Integer) willModeIconMap.get("modeDown"));
        bgView.setColor(Color.parseColor(currModeIconMap.get("bgColor").toString()));

        ViewTreeObserver vto = modeIconOut.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                modeIconOut.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                width = modeIconOut.getWidth();
                top = modeIconOut.getTop();
                height = modeIconOut.getHeight();
                activeAnimation();
            }
        });

        Toast toast = new Toast(this);
        toast.setView(mTipView);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(1000);
        toast.show();
    }

    private void activeAnimation() {
        int bg_anim_time = 300;
        int in_anim_time = 400;
        int out_anim_time = 300;

        ValueAnimator colorAnim = ValueAnimator.ofObject(new ColorEvaluator(),
                currModeIconMap.get("bgColor"), willModeIconMap.get("bgColor"));
        colorAnim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (Integer) animation.getAnimatedValue();
                bgView.setColor(color);
            }
        });
        colorAnim.setDuration(bg_anim_time);

        final float outLength = top + height;
        ValueAnimator outAnimator = ValueAnimator.ofFloat(0, outLength).setDuration(out_anim_time);
        outAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curr = (Float) animation.getAnimatedValue();
                float percent = 1.0f - curr / outLength;
                modeIconOut.setTranslationY(curr);
                if (percent >= 0.6f) {
                    modeIconOut.setAlpha(percent);
                    modeIconOut.setScaleX(percent);
                    modeIconOut.setScaleY(percent);
                }
            }
        });

        final float maxLength = 2 * top;
        final float upLength = top + height / 2;
        ValueAnimator inAnimator = ValueAnimator.ofFloat(-(upLength), top, 0f).setDuration(
                in_anim_time);
        inAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                modeIconIn.setVisibility(View.VISIBLE);
            }
        });
        inAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curr = (Float) animation.getAnimatedValue();
                modeIconIn.setTranslationY(curr);
                curr = curr + upLength;
                float percent = curr / maxLength;
                if (percent > 1.0f) {
                    percent = 1.0f;
                }
                modeIconIn.setAlpha(0.6f * percent + 0.4f);
                modeIconIn.setScaleX(0.3f * percent + 0.7f);
                modeIconIn.setScaleY(0.3f * percent + 0.7f);
            }
        });

        ValueAnimator downAnimator = ValueAnimator.ofFloat(0, 1.2f, 0.9f, 1.0f).setDuration(300);
        downAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                modeIconDown.setVisibility(View.VISIBLE);
            }
        });
        downAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curr = (Float) animation.getAnimatedValue();
                modeIconDown.setScaleX(curr);
                modeIconDown.setScaleY(curr);
            }
        });
        downAnimator.setStartDelay(in_anim_time);

        AnimatorSet set = new AnimatorSet();
        set.setStartDelay(400);
        set.playTogether(colorAnim, outAnimator, inAnimator, downAnimator);
        set.start();
    }

    private Map<String, Object> modeIconSwitch(int modeFlag) {
        Map<String, Object> iconMap = new HashMap<String, Object>();
        switch (modeFlag) {
            case 1:
                iconMap.put("bgColor", "#5653b4");
                iconMap.put("modeIcon", R.drawable.visitor_mode);
                iconMap.put("modeDown", R.drawable.visitor_mode_done);
                break;
            case 3:
                iconMap.put("bgColor", "#ffa71c");
                iconMap.put("modeIcon", R.drawable.family_mode);
                iconMap.put("modeDown", R.drawable.family_mode_done);
                break;
            default:
                iconMap.put("bgColor", "#44c4f5");
                iconMap.put("modeIcon", R.drawable.default_mode);
                iconMap.put("modeDown", R.drawable.default_mode_done);
                break;
        }
        return iconMap;
    }

    /**
     * show the defalut toast tip when mode success activating
     */
    private void showModeActiveTip(LockMode mode) {
        Map<String, Object> willLaunchMap = modeIconSwitch(mode.defaultFlag);
        View mTipView = LayoutInflater.from(this).inflate(R.layout.lock_mode_active_tip, null);
        mActiveText = (TextView) mTipView.findViewById(R.id.active_text);
        bgView = (LeoCircleView) mTipView.findViewById(R.id.mode_active_bg);
        modeIconOut = (ImageView) mTipView.findViewById(R.id.mode_active_out);
        modeIconDown = (ImageView) mTipView.findViewById(R.id.mode_active_down);

        modeIconOut.setImageResource((Integer) willLaunchMap.get("modeIcon"));
        modeIconDown.setImageResource((Integer) willLaunchMap.get("modeDown"));
        modeIconDown.setVisibility(View.VISIBLE);
        bgView.setColor(Color.parseColor(willLaunchMap.get("bgColor").toString()));
        mActiveText.setText(this.getString(R.string.mode_change, mode.modeName));
        mActiveText.setMaxLines(2);

        Toast toast = new Toast(this);
        toast.setView(mTipView);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
