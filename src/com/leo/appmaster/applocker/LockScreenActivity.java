
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.TaskChangeHandler;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.AppUnlockEvent;
import com.leo.appmaster.eventbus.event.LockThemeChangeEvent;
import com.leo.appmaster.fragment.GestureLockFragment;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.fragment.PasswdLockFragment;
import com.leo.appmaster.fragment.PretendAppErrorFragment;
import com.leo.appmaster.fragment.PretendAppUnknowCallFragment5;
import com.leo.appmaster.fragment.PretendAppZhiWenFragment;
import com.leo.appmaster.fragment.PretendFragment;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.push.ui.PushUIHelper;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.FastBlur;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.ProcessUtils;

public class LockScreenActivity extends BaseFragmentActivity implements
        OnClickListener, OnDiaogClickListener {

    public static final String TAG = "LockScreenActivity";

    public static final String THEME_CHANGE = "lock_theme_change";
    public static final String EXTRA_LOCK_MODE = "extra_lock_type";
    public static final String EXTRA_UKLOCK_TYPE = "extra_unlock_type";
    public static final String EXTRA_LOCK_TITLE = "extra_lock_title";

    private int mLockMode;
    private String mLockedPackage;
    private CommonTitleBar mTtileBar;
    private LockFragment mLockFragment;
    private Bitmap mAppBaseInfoLayoutbg;
    private LeoPopMenu mLeoPopMenu;
    private LeoDoubleLinesInputDialog mDialog;
    private LEOAlarmDialog mTipDialog;
    private EditText mEtQuestion, mEtAnwser;
    private String mLockTitle;
    private ImageView mThemeView;

    private boolean mNewTheme;
    private RelativeLayout mLockerGuide;
    private RelativeLayout mPretendLayout;
    private PretendFragment mPretendFragment;

    private Animation mAnim;
    private String mCleanRate;
    private TextView mText;
    private View mLockClean;
    private ActivityManager mAm;

    public boolean mRestartForThemeChanged;
    public boolean mQuickLockMode;
    public String mQuickModeName;
    public int mQuiclModeId;

    private RelativeLayout mLockLayout;

    private boolean mMissingDialogShowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_setting);
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
        checkCleanMem();
        LeoEventBus.getDefaultBus().register(this);

        checkOutcount();
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
        if (!mMissingDialogShowing) {
            boolean lockThemeGuid = checkNewTheme();
            if (mLockMode == LockManager.LOCK_MODE_FULL) {
                if (!lockThemeGuid) {
                    mLockerGuide.setVisibility(View.VISIBLE);
                    themeGuide(mLockerGuide, mAnim);
                } else {
                    mLockerGuide.setVisibility(View.GONE);
                }
                /*
                 * tell PushUIHelper than do not show dialog when lockscreen is
                 * shown
                 */
                PushUIHelper.getInstance(getApplicationContext()).setIsLockScreen(true);
            }
            AppMasterPreference.getInstance(this).setUnlocked(false);
        }
        super.onResume();
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

        if (mNewTheme) {
            mThemeView.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.themetip_spiner_press));
        } else {
            mThemeView.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.theme_spiner_press));
        }
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

        String newLockedPkg = intent.getStringExtra(TaskChangeHandler.EXTRA_LOCKED_APP_PKG);
        if (!TextUtils.equals(newLockedPkg, mLockedPackage)) {
            mLockedPackage = newLockedPkg;
            // change background
            if (!ThemeUtils.checkThemeNeed(this)
                    && (mLockMode == LockManager.LOCK_MODE_FULL)) {
                BitmapDrawable bd = (BitmapDrawable) AppUtil.getDrawable(
                        getPackageManager(),
                        mLockedPackage);
                setAppInfoBackground(bd);
            }

            mLockFragment.onLockPackageChanged(mLockedPackage);
            LeoLog.d(TAG, "onNewIntent" + "     mToPackage = " + mLockedPackage);

            if (mPretendFragment != null) {
                mLockLayout.setVisibility(View.GONE);
                mPretendLayout.setVisibility(View.VISIBLE);
                if(mPretendFragment instanceof PretendAppErrorFragment) {
                    PackageManager pm = getPackageManager();
                    String tip = "";
                    try {
                        ApplicationInfo info = pm.getApplicationInfo(mLockedPackage,
                                PackageManager.GET_UNINSTALLED_PACKAGES);
                        String lab = info.loadLabel(pm).toString();
                        tip = getString(R.string.pretend_app_error, lab);
                    } catch (Exception e) {
                        tip = getString(R.string.weizhuang_error_notice);
                    }
                    ((PretendAppErrorFragment)mPretendFragment).setErrorTip(tip);
                }
            }
        }
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
        if (mQuickLockMode) {
            mQuickModeName = intent.getStringExtra("lock_mode_name");
            mQuiclModeId = intent.getIntExtra("lock_mode_id", -1);

            // home mode replace unlock-all mode
            if (mQuiclModeId == 0) {
                mQuiclModeId = 3;
            }
        }
        mLockMode = intent.getIntExtra(EXTRA_LOCK_MODE,
                LockManager.LOCK_MODE_FULL);
        if (mQuickLockMode) {
            mLockedPackage = getPackageName();
        } else {
            mLockedPackage = intent.getStringExtra(TaskChangeHandler.EXTRA_LOCKED_APP_PKG);
        }

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

        LeoLog.d(TAG, "mToPackage = " + mLockedPackage);
    }

    private void setAppInfoBackground(Drawable drawable) {
        int h = drawable.getIntrinsicHeight() * 9 / 10;
        int w = h * 3 / 5;
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

    @Override
    protected void onDestroy() {
        LockManager.getInstatnce().setPauseScreenonLock(false);
        super.onDestroy();
        if (mAppBaseInfoLayoutbg != null) {
            mAppBaseInfoLayoutbg.recycle();
            mAppBaseInfoLayoutbg = null;
        }
        LeoLog.d(TAG, "onDestroy");
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
        mAnim = AnimationUtils.loadAnimation(this, R.anim.locker_guide);
        mLockerGuide = (RelativeLayout) findViewById(R.id.lockerGuide);
        mThemeView = (ImageView) findViewById(R.id.img_layout_right);
        TextView lockGuideTv = (TextView) mLockerGuide.findViewById(R.id.lock_guide_tv);
        lockGuideTv.setText(getString(R.string.help_setting_guide));
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        if (mLockMode == LockManager.LOCK_MODE_FULL) {
            mTtileBar.setHelpSettingImage(R.drawable.selector_help_icon);
            mTtileBar.setHelpSettingVisiblity(View.VISIBLE);
            mTtileBar.setHelpSettingListener(this);

            mTtileBar.setBackArrowVisibility(View.GONE);
            mTtileBar.setTitle(R.string.app_name);
        } else {
            mTtileBar.setBackViewListener(this);
            if (TextUtils.isEmpty(mLockTitle)) {
                mTtileBar.setTitle(R.string.app_lock);
            } else {
                mTtileBar.setTitle(mLockTitle);
            }
            mTtileBar.setHelpSettingVisiblity(View.INVISIBLE);
        }
        if (AppMasterPreference.getInstance(this).hasPswdProtect()) {
            mTtileBar.setOptionImage(R.drawable.setting_selector);
            mTtileBar.setOptionImageVisibility(View.VISIBLE);
            mTtileBar.setOptionListener(this);
        }
        mThemeView = (ImageView) findViewById(R.id.img_layout_right);
        ((View) mThemeView.getParent()).setVisibility(View.VISIBLE);
        mThemeView.setVisibility(View.VISIBLE);
        mThemeView.setOnClickListener(this);

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
        mPretendFragment = getPretendFragment();
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
        int pretendLock = AppMasterPreference.getInstance(this).getPretendLock();
        // pretendLock = 1;
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
        return null;
    }

    public void onUnlockSucceed() {

        if (mQuickLockMode) {
            LockManager lm = LockManager.getInstatnce();
            List<LockMode> modeList = lm.getLockMode();
            LockMode willLaunch = null;
            for (LockMode lockMode : modeList) {
                if (mQuiclModeId == lockMode.modeId) {
                    willLaunch = lockMode;
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
                lm.setCurrentLockMode(willLaunch, true);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "modeschage", "launcher");
                showModeActiveTip();
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
            AppMasterPreference pref = AppMasterPreference.getInstance(this);
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
            } else if (mLockMode == LockManager.LOCK_MODE_PURE) {
            }
            pref.setUnlocked(true);
            pref.setDoubleCheck(null);
        }
        LockManager.getInstatnce().timeFilter(mLockedPackage, 1000);
        mTtileBar.postDelayed(new Runnable() {

            @Override
            public void run() {
                finish();
            }
        }, 100);
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
        // } else {
        //
        // }

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
                    mLeoPopMenu = new LeoPopMenu();
                    mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
                            if (position == 0) {
                                findPasswd();
                            } else if (position == 1) {

                            }
                            mLeoPopMenu.dismissSnapshotList();
                        }
                    });
                }
                mLeoPopMenu.setPopMenuItems(getPopMenuItems());
                mLeoPopMenu.showPopMenu(this,
                        mTtileBar.findViewById(R.id.tv_option_image), null, null);
                break;
            case R.id.layout_title_back:
                onBackPressed();
                finish();
                break;
            case R.id.img_layout_right:
                Intent intent = new Intent(LockScreenActivity.this,
                        LockerTheme.class);
                SDKWrapper.addEvent(LockScreenActivity.this, SDKWrapper.P1,
                        "theme_enter", "unlock");
                AppMasterPreference amp = AppMasterPreference.getInstance(this);
                amp.setUnlocked(true);
                amp.setDoubleCheck(null);
                startActivityForResult(intent, 0);
                amp.setLockerScreenThemeGuide(true);
                break;
            case R.id.setting_help_tip:
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
                break;
            default:
                break;
        }
    }

    private List<String> getPopMenuItems() {
        List<String> listItems = new ArrayList<String>();
        Resources resources = AppMasterApplication.getInstance().getResources();
        if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_GESTURE) {
            listItems.add(resources.getString(R.string.find_gesture));
        } else if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_PASSWD) {
            listItems.add(resources.getString(R.string.find_passwd));
        }
        return listItems;
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

    /**
     * show the tip when mode success activating
     */
    private void showModeActiveTip() {
        View mTipView = LayoutInflater.from(this).inflate(R.layout.lock_mode_active_tip, null);
        TextView mActiveText = (TextView) mTipView.findViewById(R.id.active_text);
        mActiveText.setText(this.getString(R.string.mode_change, mQuickModeName));
        Toast toast = new Toast(this);
        toast.setView(mTipView);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
