
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.fragment.GestureLockFragment;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.fragment.PasswdLockFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.push.PushUIHelper;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.FastBlur;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.ProcessUtils;
import com.leo.appmaster.videohide.VideoHideMainActivity;
import com.leoers.leoanalytics.LeoStat;

public class LockScreenActivity extends BaseFragmentActivity implements
        OnClickListener, OnDiaogClickListener {

    public static final String THEME_CHANGE = "lock_theme_change";
    public static String EXTRA_UNLOCK_FROM = "extra_unlock_from";
    public static String EXTRA_UKLOCK_TYPE = "extra_unlock_type";
    public static String EXTRA_TO_ACTIVITY = "extra_to_activity";
    public static String EXTRA_LOCK_TITLE = "extra_lock_title";

    int mFromType;
    private String mToPackage;
    private String mToActivity;
    private CommonTitleBar mTtileBar;
    private LockFragment mFragment;
    private Bitmap mAppBaseInfoLayoutbg;
    private LeoPopMenu mLeoPopMenu;
    private LeoDoubleLinesInputDialog mDialog;
    private EditText mEtQuestion, mEtAnwser;
    private String mLockTitle;
    private ImageView spiner;
    // private String number;

    private boolean shouldLock;
    private boolean mNewTheme;
    private RelativeLayout mLockerGuide;
    private Animation mAnim;
    private String mCleanRate;
    private TextView mText;
    private View mLockClean;
    // private ImageView mImage;
    private ActivityManager mAm;

    private ThemeReceiver mThemeChangeReceiver;
    public boolean mRestartForThemeChanged;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_setting);
        handleIntent();
        initUI();
        /**
         * optimize cleanMem
         */
        cleanMem();

        if (mFromType == LockFragment.FROM_OTHER || mFromType == LockFragment.FROM_SCREEN_ON) {
            mThemeChangeReceiver = new ThemeReceiver();
            IntentFilter filter = new IntentFilter(THEME_CHANGE);
            registerReceiver(mThemeChangeReceiver, filter);
        }
    }

    @Override
    protected void onResume() {
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
            spiner.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.themetip_spiner_press));
        } else {
            spiner.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.theme_spiner_press));
        }
        if (mFromType == LockFragment.FROM_OTHER
                || mFromType == LockFragment.FROM_SCREEN_ON) {
            if (!lockThemeGuid) {
                mLockerGuide.setVisibility(View.VISIBLE);
                themeGuide(mLockerGuide, mAnim);
            } else {
                mLockerGuide.setVisibility(View.GONE);
            }
        }

        if (mFromType == LockFragment.FROM_OTHER
                || mFromType == LockFragment.FROM_SCREEN_ON) {
            /*
             * tell PushUIHelper than do not show dialog when lockscreen is
             * shown
             */
            PushUIHelper.getInstance(getApplicationContext()).setIsLockScreen(true);
            AppMasterPreference.getInstance(this).setUnlocked(false);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        PushUIHelper.getInstance(getApplicationContext())
                .setIsLockScreen(false);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void handleIntent() {
        Intent intent = getIntent();
//        int type = intent.getIntExtra(EXTRA_UKLOCK_TYPE,
//                LockFragment.LOCK_TYPE_PASSWD);
        mFromType = intent.getIntExtra(EXTRA_UNLOCK_FROM,
                LockFragment.FROM_SELF);
        int type = AppMasterPreference.getInstance(this).getLockType();
        
        if (type == LockFragment.LOCK_TYPE_PASSWD) {
            mFragment = new PasswdLockFragment();
        } else {
            mFragment = new GestureLockFragment();
        }
        if (!ThemeUtils.checkThemeNeed(this)
                && (mFromType == LockFragment.FROM_OTHER || mFromType == LockFragment.FROM_SCREEN_ON)) {
            BitmapDrawable bd = (BitmapDrawable) AppUtil.getDrawable(
                    getPackageManager(),
                    intent.getStringExtra(LockHandler.EXTRA_LOCKED_APP_PKG));
            // createChoiceDialog();
            setAppInfoBackground(bd);
        }
        mLockTitle = intent.getStringExtra(EXTRA_LOCK_TITLE);
        mFragment.setFrom(mFromType);
        mToPackage = intent.getStringExtra(LockHandler.EXTRA_LOCKED_APP_PKG);
        mToActivity = intent.getStringExtra(EXTRA_TO_ACTIVITY);
        mFragment.setPackage(mToPackage);
        mFragment.setActivity(mToActivity);

        /* SDK: mark user what to unlock which app */
        if (mFromType == LockFragment.FROM_OTHER
                || mFromType == LockFragment.FROM_SCREEN_ON) {
            SDKWrapper.addEvent(this, LeoStat.P1, "access_locked_app",
                    mToPackage);
        }

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

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_lock_layout);

        layout.setBackgroundDrawable(new BitmapDrawable(mAppBaseInfoLayoutbg));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAppBaseInfoLayoutbg != null) {
            mAppBaseInfoLayoutbg.recycle();
            mAppBaseInfoLayoutbg = null;
        }

        if (mThemeChangeReceiver != null) {
            unregisterReceiver(mThemeChangeReceiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFromType == LockFragment.FROM_OTHER) {
            if (!AppMasterPreference.getInstance(this).isAutoLock() || shouldLock) {
                shouldLock = false;
                return;
            }
            finish();
        }
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
        if (mRestartForThemeChanged) {
            Intent intent = getIntent();
            finish();
            mRestartForThemeChanged = false;
            startActivity(intent);
        }
    }

    private void initUI() {
        /**
         * lockerTheme Guide
         */
        mAnim = AnimationUtils.loadAnimation(this, R.anim.locker_guide);
        mLockerGuide = (RelativeLayout) findViewById(R.id.lockerGuide);
        spiner = (ImageView) findViewById(R.id.image1);
        LeoLog.d("LockScreenActivity", "spiner = " + spiner);
        // // AM-463, add protect
        TextView lockGuideTv = (TextView) mLockerGuide.findViewById(R.id.lock_guide_tv);
        lockGuideTv.setText(getString(R.string.help_setting_guide));
        // number = AppMasterApplication.number;
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        /*
         * AM-667
         */
        if (mFromType == LockFragment.FROM_OTHER
                || mFromType == LockFragment.FROM_SCREEN_ON) {
            mTtileBar.setHelpSettingImage(R.drawable.selector_help_icon);
            mTtileBar.setHelpSettingVisiblity(View.VISIBLE);
            mTtileBar.setHelpSettingListener(this);
//            mTtileBar.setHelpSettingParentListener(this);
        }
        if (AppMasterPreference.getInstance(this).hasPswdProtect()) {
            mTtileBar.setOptionImage(R.drawable.setting_selector);
            mTtileBar.setOptionImageVisibility(View.VISIBLE);
            mTtileBar.setOptionListener(this);
        }
        spiner = (ImageView) findViewById(R.id.image1);
        LeoLog.d("LockScreenActivity", "spiner = " + spiner);
        // if (spiner != null) {
        // if ("0".equals(number)) {
        // spiner.setImageDrawable(this.getResources().getDrawable(
        // R.drawable.themetip_spiner_press));
        // } else {
        // spiner.setImageDrawable(this.getResources().getDrawable(
        // R.drawable.theme_spiner_press));
        // }
        // }

        if (ImageHideMainActivity.class.getName().equals(mToActivity)
                || VideoHideMainActivity.class.getName().equals(mToActivity)) { // AM-423
            mTtileBar.setSpinerVibility(View.INVISIBLE);
            LeoLog.d("LockScreenActivity", "ImageHideMainActivity");
        } else {
            mTtileBar.setSpinerVibility(View.VISIBLE);
            LeoLog.d("LockScreenActivity", "spiner.setOnClickListener");
            spiner.setOnClickListener(this);
        }

        if (mFromType == LockFragment.FROM_SELF_HOME
                || mFromType == LockFragment.FROM_SELF) {
            mTtileBar.setBackViewListener(this);
            if (TextUtils.isEmpty(mLockTitle)) {
                mTtileBar.setTitle(R.string.app_lock);
            } else {
                mTtileBar.setTitle(mLockTitle);
            }
        } else {
            mTtileBar.setBackArrowVisibility(View.GONE);
            mTtileBar.setTitle(R.string.app_name);
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tans = fm.beginTransaction();
        tans.replace(R.id.fragment_contain, mFragment);
        tans.commit();

    }

    public void onUnlockSucceed() {
        if (mFromType == LockFragment.FROM_SELF) {
            Intent intent = null;
            intent = new Intent(this, LockService.class);
            this.startService(intent);
            setResult(11);
        } else if (mFromType == LockFragment.FROM_OTHER
                || mFromType == LockFragment.FROM_SCREEN_ON) {
            // input right gesture, just finish self
            Intent intent = new Intent(LockHandler.ACTION_APP_UNLOCKED);
            intent.putExtra(LockHandler.EXTRA_LOCKED_APP_PKG, mToPackage);
            sendBroadcast(intent);

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

            AppMasterPreference pref = AppMasterPreference.getInstance(this);
            pref.setUnlockCount(pref.getUnlockCount() + 1);
            pref.setLaunchOtherApp(false);
            pref.setUnlocked(true);
            spiner.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 500);

            return;
        } else if (mFromType == LockFragment.FROM_SELF_HOME) {
            Intent intent = null;
            // try start lock service
            intent = new Intent(this, LockService.class);
            this.startService(intent);
            intent = new Intent();
            intent.setClassName(this, mToActivity);
            if ((LockSettingActivity.class.getName()).equals(mToActivity)) {
                intent.putExtra(LockSettingActivity.RESET_PASSWD_FLAG, true);
            }
            startActivity(intent);
        }
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        pref.setUnlockCount(pref.getUnlockCount() + 1);
        pref.setLaunchOtherApp(false);
        finish();
    }

    public void onUolockOutcount() {
        shouldLock = true;
        Intent intent = new Intent(this, WaitActivity.class);
        intent.putExtra(LockHandler.EXTRA_LOCKED_APP_PKG, mToPackage);
        intent.putExtra(WaitActivity.KEY_JUST_FINISH, true);
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
        if (mFromType == LockFragment.FROM_SELF_HOME) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent();
            if (mFromType == LockFragment.FROM_OTHER
                    || mFromType == LockFragment.FROM_SCREEN_ON) {

                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
            } else {

                intent.setClassName(getApplicationContext(),
                        HomeActivity.class.getName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            startActivity(intent);
            finish();
        }
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
                onBack();
                break;
            case R.id.image1:
                Intent intent = new Intent(LockScreenActivity.this,
                        LockerTheme.class);
                if (Build.VERSION.SDK_INT > 19
                        && (mFromType == LockFragment.FROM_OTHER || mFromType == LockFragment.FROM_SCREEN_ON)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                }
                SDKWrapper.addEvent(LockScreenActivity.this, LeoStat.P1,
                        "theme_enter", "unlock");
                shouldLock = true;
                startActivityForResult(intent, 0);
                AppMasterPreference.getInstance(this).setLockerScreenThemeGuide(
                        true);
                break;
            case R.id.setting_help_tip:
                AppMasterPreference.getInstance(this).setLockerScreenThemeGuide(true);
                Intent helpSettingIntent = new Intent(LockScreenActivity.this,
                        LockHelpSettingTip.class);
                shouldLock = true;
                helpSettingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    LockScreenActivity.this.startActivity(helpSettingIntent);
                } catch (Exception e) {
                }
                /* SDK Event Mark */
                SDKWrapper.addEvent(LockScreenActivity.this, LeoStat.P1, "help", "help");
                break;
            default:
                break;
        }

    }

    private void onBack() {
        if (mFromType == LockFragment.FROM_SELF_HOME) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent();
            if (mFromType == LockFragment.FROM_OTHER
                    || mFromType == LockFragment.FROM_SCREEN_ON) {

                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
            } else {

                intent.setClassName(getApplicationContext(),
                        HomeActivity.class.getName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            startActivity(intent);
            finish();
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
        return mFromType;
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
     * cleanMem
     */
    private void cleanMem() {
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
        if (mToPackage != null) {
            pkgs.add(mToPackage);
        }
        return pkgs;
    }

    class ThemeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (THEME_CHANGE.equals(action)) {
                mRestartForThemeChanged = true;
            }
        }

    }

}
