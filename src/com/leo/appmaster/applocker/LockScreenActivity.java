
package com.leo.appmaster.applocker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.AndroidAuthenticator;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.SDKWrapper;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.fragment.GestureLockFragment;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.fragment.PasswdLockFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.FastBlur;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.ProcessUtils;
import com.leo.appmaster.utils.TextFormater;
import com.leoers.leoanalytics.LeoStat;

public class LockScreenActivity extends FragmentActivity implements
        OnClickListener, OnDiaogClickListener {

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
    private String number;

    private boolean toTheme;
    private RelativeLayout mLockerGuide;
    private Animation mAnim;
    private String mCleanRate;
    private TextView mText;
    private View mLockClean;
    // private ImageView mImage;
    private ActivityManager mAm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_setting);
        /**
         * lockerTheme Guide
         */
        mAnim = AnimationUtils.loadAnimation(this, R.anim.locker_guide);
        mLockerGuide = (RelativeLayout) findViewById(R.id.lockerGuide);

        number = AppMasterApplication.number;
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        spiner = (ImageView) findViewById(R.id.image1);
        if (number.equals("0")) {
            spiner.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.themetip_spiner_press));
        } else {
            spiner.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.theme_spiner_press));
        }
        handleIntent();
        initUI();
        /**
         * cleanMemToast
         */
        cleanMem();
    }

    @Override
    protected void onResume() {
        if (number.equals("0")) {
            spiner.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.themetip_spiner_press));
        } else {
            spiner.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.theme_spiner_press));
        }
        if (mFromType == LockFragment.FROM_OTHER
                || mFromType == LockFragment.FROM_SCREEN_ON) {
        if (number.equals("0")) {
            mLockerGuide.setVisibility(View.VISIBLE);
            themeGuide(mLockerGuide, mAnim);
        } else {
            mLockerGuide.setVisibility(View.GONE);
        }
        }
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        int type = intent.getIntExtra(EXTRA_UKLOCK_TYPE,
                LockFragment.LOCK_TYPE_PASSWD);
        mFromType = intent.getIntExtra(EXTRA_UNLOCK_FROM,
                LockFragment.FROM_SELF);

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
    }

    // private void createChoiceDialog() {
    // final String[] valueString = getResources().getStringArray(
    // R.array.det_lock_time_items);
    //
    // AlertDialog scaleIconListDlg = new AlertDialog.Builder(this)
    // .setTitle(R.string.change_lock_time)
    // .setSingleChoiceItems(R.array.lock_time_entrys, -1,
    // new DialogInterface.OnClickListener() {
    // public void onClick(DialogInterface dialog,
    // int whichButton) {
    // AppMasterPreference.getInstance(
    // LockScreenActivity.this)
    // .setRelockTimeout(
    // valueString[whichButton]);
    // SDKWrapper.addEvent(LockScreenActivity.this,
    // LeoStat.P1, "lock_setting",
    // valueString[whichButton]);
    // dialog.dismiss();
    // }
    // })
    // .setNegativeButton(R.string.cancel,
    // new DialogInterface.OnClickListener() {
    // public void onClick(DialogInterface dialog,
    // int whichButton) {
    // /* User clicked No so do some stuff */
    // }
    // }).create();
    // TextView title = new TextView(this);
    // title.setText(getString(R.string.change_lock_time));
    // title.setTextColor(Color.WHITE);
    // title.setTextSize(20);
    // title.setPadding(DipPixelUtil.dip2px(this, 20),
    // DipPixelUtil.dip2px(this, 10), 0, DipPixelUtil.dip2px(this, 10));
    // title.setBackgroundColor(getResources().getColor(
    // R.color.dialog_title_area_bg));
    // scaleIconListDlg.setCustomTitle(title);
    // scaleIconListDlg.show();
    // }

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
        LeoLog.d("LockScreenActivity", "onDestroy");
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mAppBaseInfoLayoutbg != null) {
            mAppBaseInfoLayoutbg.recycle();
            mAppBaseInfoLayoutbg = null;
        }
    }

    @Override
    protected void onStop() {
        LeoLog.d("LockScreenActivity", "onStop" + "      mFromType = "
                + mFromType);
        super.onStop();
        if (mFromType == LockFragment.FROM_OTHER) {
            if (!AppMasterPreference.getInstance(this).isAutoLock() || toTheme) {
                toTheme = false;
                return;
            }
            finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void initUI() {
        if (AppMasterPreference.getInstance(this).hasPswdProtect()) {
            mTtileBar.setOptionImage(R.drawable.setting_selector);
            mTtileBar.setOptionImageVisibility(View.VISIBLE);
            mTtileBar.setOptionListener(this);
        }
        mTtileBar.setSpinerVibility(View.VISIBLE);
        mTtileBar.setSpinerListener(this);

        spiner.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(LockScreenActivity.this,
                        LockerTheme.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                SDKWrapper.addEvent(LockScreenActivity.this, LeoStat.P1, "theme_enter", "unlock");

                toTheme = true;

                startActivityForResult(intent, 0);
                AppMasterApplication.setSharedPreferencesNumber("1");
                number = "1";
            }
        });

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
            /*
             * mImage.setVisibility(View.VISIBLE); mAnim = new
             * TranslateAnimation(0, 0, 0, -1500);
             * mAnim.setInterpolator(LockScreenActivity.this,
             * android.R.anim.accelerate_interpolator); mAnim.setDuration(500);
             * mAnim.setAnimationListener(new AnimationListener() {
             * @Override public void onAnimationStart(Animation arg0) { }
             * @Override public void onAnimationRepeat(Animation arg0) { }
             * @Override public void onAnimationEnd(Animation arg0) {
             * mImage.setVisibility(View.GONE); finish(); } });
             * mImage.startAnimation(mAnim);
             */
            Toast mToast = new Toast(this);
            LayoutInflater mLayoutInflater = LayoutInflater.from(LockScreenActivity.this);
            mLockClean = mLayoutInflater.inflate(R.layout.activity_lockclean_toast, null);
            mText = (TextView) mLockClean.findViewById(R.id.textToast);
            String textResource = getResources().getString(R.string.locker_clean);
            String cleanRate = String.format(textResource, mCleanRate);
            mText.setText(cleanRate);
            mToast.setGravity(Gravity.BOTTOM, 0, 66);
            mToast.setDuration(1000);
            mToast.setView(mLockClean);
            mToast.show();

            AppMasterPreference pref = AppMasterPreference.getInstance(this);
            pref.setUnlockCount(pref.getUnlockCount() + 1);

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
            startActivity(intent);
        }

        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        pref.setUnlockCount(pref.getUnlockCount() + 1);
        finish();
    }

    public void onUolockOutcount() {
        Intent intent = new Intent(this, WaitActivity.class);
        intent.putExtra(LockHandler.EXTRA_LOCKED_APP_PKG, mToPackage);
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
            default:
                break;
        }

    }

    private void onBack() {
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
        double number = ((double) cleanMem / lastUsedMem) * 2;
        if (number <= 1) {
            int random=(int)(Math.random() * 10+1);
            mCleanRate =random+ "%";
        } else {
            int cleanNumber = (int) (number * 100);
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
        return pkgs;
    }
}
