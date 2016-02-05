
package com.leo.appmaster.lockertheme;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.HttpRequestAgent.RequestListener;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.ThemeChanageListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.LockThemeChangeEvent;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.ThemeItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.LeoPagerTab;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.LoadFailUtils;
import com.leo.appmater.globalbroadcast.LeoGlobalBroadcast;
import com.leo.appmater.globalbroadcast.PackageChangedListener;
import com.leo.imageloader.ImageLoader;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LockerTheme extends BaseActivity implements OnClickListener, ThemeChanageListener,
        OnRefreshListener2<ListView> {
    private static final String TAG = "LockerTheme";

    private ViewPager mViewPager;
    private PullToRefreshListView localThemeList;
    public PullToRefreshListView mOnlineThemeList;
    private View mOnlineThemeView;
    private View mOnlineThemeHolder;
    private View mErrorView, mLayoutEmptyTip;
    private TextView mTvRetry;
    private ProgressBar mProgressBar;
    private LEOThreeButtonDialog dialog;
    private LEOAlarmDialog mAlarmDialog;
    private LeoPagerTab mPagerTab;

    private List<ThemeItemInfo> mLocalThemes;
    private List<ThemeItemInfo> mOnlineThemes;
    private LockerThemeAdapter mLocalThemeAdapter;
    private LockerThemeAdapter mOnlineThemeAdapter;
    private List<String> mHideThemes;
    private ThemeItemClickListener itemListener;
    public ThemeItemInfo lastSelectedItem;

    private boolean isFromLock = false;

    private int number = 0;
    private String mFrom;
    private boolean mGuideFlag;
    // private int mCurrentShowPage = 0;
    // private int mNextLoadPage = 0;

    private static final int MSG_LOAD_INIT_FAILED = 0;
    private static final int MSG_LOAD_INIT_SUCCESSED = 1;
    private static final int MSG_LOAD_PAGE_DATA_FAILED = 3;
    private static final int MSG_LOAD_PAGE_DATA_SUCCESS = 4;

    public static final String LOCAL_AD_NAME = "local";
    public static final String ONLINE_AD_NAME = "online";
    public static final int LOCAL_AD_LOCATION = 1;
    public static final int ONLINE_AD_LOCATION = 2;

    private static final int LOAD_INIT = 100;
    private static final int LOAD_MORE = 101;

    private EventHandler mHandler;
    private String mFromTheme;
    private int mHelpSettingCurrent;
    private LEOCircleProgressDialog mProgressDialog;
    private boolean mNeedLoadTheme;

    private LockManager mLockManager;

    private static class EventHandler extends Handler {
        WeakReference<LockerTheme> lockerTheme;

        public EventHandler(LockerTheme lockerTheme) {
            super();
            this.lockerTheme = new WeakReference<LockerTheme>(lockerTheme);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_INIT_FAILED:
                    if (lockerTheme.get() != null) {
                        lockerTheme.get().onLoadInitThemeFinish(false, null);
                    }
                    break;
                case MSG_LOAD_INIT_SUCCESSED:
                    if (lockerTheme.get() != null) {
                        AppMasterPreference pref = AppMasterPreference
                                .getInstance(lockerTheme.get());
                        pref.setLocalThemeSerialNumber(pref.getOnlineThemeSerialNumber());
                        lockerTheme.get().onLoadInitThemeFinish(true, msg.obj);
                    }
                    break;
                case MSG_LOAD_PAGE_DATA_FAILED:
                    if (lockerTheme.get() != null) {
                        lockerTheme.get().mOnlineThemeList.onRefreshComplete();

                        lockerTheme.get().onLoadMoreThemeFinish(false, null);
                    }
                    break;
                case MSG_LOAD_PAGE_DATA_SUCCESS:
                    if (lockerTheme.get() != null) {
                        lockerTheme.get().mOnlineThemeList.onRefreshComplete();
                        AppMasterPreference pref = AppMasterPreference
                                .getInstance(lockerTheme.get());
                        pref.setLocalThemeSerialNumber(pref.getOnlineThemeSerialNumber());
                        List<ThemeItemInfo> loadList = (List<ThemeItemInfo>) msg.obj;
                        lockerTheme.get().onLoadMoreThemeFinish(true, loadList);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_locker_theme);

        mHandler = new EventHandler(this);
        loadData();
        initUI();
        handleIntent();
        LeoLog.i("setHideThemeList_time", "getHideThemeList_time:" + SystemClock.elapsedRealtime());
        if (mNeedLoadTheme && mHideThemes.isEmpty()) {
//            showProgressDialog(getString(R.string.tips),
//                    getString(R.string.pull_to_refresh_refreshing_label) + "...", true, true);
            if (mLocalThemeAdapter != null) {
                loadTheme();
            }
        }

        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        /*
         * IntentFilter intentFilter = new IntentFilter();
         * intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
         * intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
         * intentFilter.addDataScheme("package"); mLockerThemeReceive = new
         * LockerThemeReceive(); registerReceiver(mLockerThemeReceive,
         * intentFilter);
         */

        LeoGlobalBroadcast.registerBroadcastListener(mPackageChangedListener);

        createResultToHelpSetting();
        AppLoadEngine.getInstance(this).setThemeChanageListener(this);
    }

    private void addLocalAd() {
        ThemeItemInfo localInfo = new ThemeItemInfo();
        localInfo.themeName = LOCAL_AD_NAME;
        localInfo.packageName = LOCAL_AD_NAME;
        localInfo.themeType = Constants.THEME_TYPE_LOCAL;
        if (mLocalThemes.size() > 0) {
            mLocalThemes.add(LOCAL_AD_LOCATION, localInfo);
        }
    }

    private void addOnlineAd() {
        ThemeItemInfo onlineInfo = new ThemeItemInfo();
        onlineInfo.themeName = ONLINE_AD_NAME;
        onlineInfo.packageName = ONLINE_AD_NAME;
        onlineInfo.themeType = Constants.THEME_TYPE_ONLINE;
        if (mOnlineThemes.size() > 1) {
            mOnlineThemes.add(ONLINE_AD_LOCATION, onlineInfo);
        } else if (mOnlineThemes.size() == 0) {
            mOnlineThemes.add(0, onlineInfo);
        } else if (mOnlineThemes.size() == 1) {
            mOnlineThemes.add(1, onlineInfo);
        }
    }

    private PackageChangedListener mPackageChangedListener = new PackageChangedListener() {
        @Override
        public void onPackageChanged(Intent intent) {
            onPackageEvent(intent);
            super.onPackageChanged(intent);
        }
    };

    private void handleIntent() {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        Intent intent = this.getIntent();
        mFromTheme = intent.getStringExtra("theme_package");
        mNeedLoadTheme = intent.getBooleanExtra("is_need_loading", false);
        mFrom = intent.getStringExtra("from");
        if (mFromTheme != null && !mFromTheme.equals("")) {
            tryHideThemeApk(mFromTheme);
            for (int i = 0; i < mLocalThemes.size(); i++) {
                if (mLocalThemes.get(i).packageName.equals(mFromTheme)) {
                    number = i;
                    showAlarmDialog(mLocalThemes.get(i).themeName, mLocalThemes.get(i).themeLogo,
                            View.VISIBLE);
                    lastSelectedItem = mLocalThemes.get(i);
                }
            }
        } else {
            number = 0;
        }
        // localThemeList.setSelection(number);

//        String locSerial = AppMasterPreference.getInstance(this)
//                .getLocalThemeSerialNumber();
//        String onlineSerial = AppMasterPreference.getInstance(this)
//                .getOnlineThemeSerialNumber();
//        if (!locSerial.equals(onlineSerial)) {
        boolean isHaveRedDot = intent.getBooleanExtra("isRedDot", false);
        if (isHaveRedDot) {
            mViewPager.setCurrentItem(1);
            pref.setLocalThemeSerialNumber(pref.getOnlineThemeSerialNumber());
        }
//            PreferenceTable.getInstance().putBoolean(Constants.IS_CLICK_LOCK_TAB, false);
//        }

        // form statusbar
        if (mFrom != null && mFrom.equals("new_theme_tip")) {
            /* SDK event mark */
            SDKWrapper.addEvent(this, SDKWrapper.P1, "theme_enter", "statusbar");
            mViewPager.setCurrentItem(1);
        }

        // but if from theme app, select page 0
        if (mFromTheme != null && !mFromTheme.equals("")) {
            mViewPager.setCurrentItem(0);
        }

        localThemeList.getRefreshableView().setSelection(number);
        boolean fromHelpSettingFalg = intent.getBooleanExtra("to_online_theme", false);
        if (fromHelpSettingFalg) {
            mViewPager.setCurrentItem(1);
        }
        int current = intent.getIntExtra("help_setting_current", 10001);
        if (current != 10001) {
            mHelpSettingCurrent = current;
        }

        isFromLock = intent.getBooleanExtra("fromLock", false);
    }

    protected void onPackageEvent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            final String packageName = intent.getData()
                    .getSchemeSpecificPart();
            if (packageName != null
                    && packageName.startsWith("com.leo.theme")) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // if need to load online theme
                        for (ThemeItemInfo info : mLocalThemes) {
                            if (packageName.equals(info.packageName)) {
                                String remove = null;
                                for (String hide : mHideThemes) {
                                    if (hide.equals(info.packageName)) {
                                        remove = hide;
                                        break;
                                    }
                                }
                                // AM-760
                                try {
                                    mHideThemes.remove(remove);
                                } catch (Exception e) {
                                }
                                loadMoreOnlineTheme();

                                break;
                            }
                        }

                        loadLocalTheme();
                        if (mLocalThemeAdapter != null) {
                            mLocalThemeAdapter.notifyDataSetChanged();
                        }
                    }
                }, 1000);
            }
        }
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            final String packageName = intent.getData()
                    .getSchemeSpecificPart();
            if (packageName != null
                    && packageName.startsWith("com.leo.theme")) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadLocalTheme();
                        if (mLocalThemeAdapter != null) {
                            mLocalThemeAdapter.notifyDataSetChanged();
                        }
                        // if need to load online theme
                        ThemeItemInfo remove = null;
                        for (ThemeItemInfo info : mOnlineThemes) {
                            if (info.packageName.equals(packageName)) {
                                remove = info;
                            }
                        }
                        if (remove != null) {
                            // AM-760
                            try {
                                mOnlineThemes.remove(remove);
                            } catch (Exception e) {
                            }
                            if (mOnlineThemeAdapter != null) {
                                mOnlineThemeAdapter.notifyDataSetChanged();
                            }
                            if (mOnlineThemes.isEmpty()) {
                                mLayoutEmptyTip.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }, 1000);
            }
        }

    }

    protected void removeOnlineAd() {
        for (int i = 0; i < mOnlineThemes.size(); i++) {
            ThemeItemInfo onlineInfo = mOnlineThemes.get(i);
            if (onlineInfo.themeName.equals(ONLINE_AD_NAME)) {
                mOnlineThemes.remove(i);
            }
        }
    }

    private void loadData() {
        mLocalThemes = new ArrayList<ThemeItemInfo>();
        mOnlineThemes = new ArrayList<ThemeItemInfo>();
        loadLocalTheme();
        loadInitOnlineTheme();
    }

    private void loadLocalTheme() {
        AppMasterPreference pref = AppMasterPreference
                .getInstance(LockerTheme.this);
        mLocalThemes.clear();
        mHideThemes = pref.getHideThemeList();
        mLocalThemes.add(getDefaultTheme());
        Context themeContext = null;
        PackageManager pm = getPackageManager();
        for (String themePackage : mHideThemes) {
            ThemeItemInfo bean = new ThemeItemInfo();
            bean.themeType = Constants.THEME_TYPE_LOCAL;
            try {
                PackageInfo pi = pm.getPackageInfo(themePackage, 0);
                if (pi != null) {
                    bean.installTime = pi.firstInstallTime;
                }
                themeContext = createPackageContext(themePackage, Context.CONTEXT_IGNORE_SECURITY);
                String str = (String) themeContext.getResources().getText(
                        themeContext.getResources().getIdentifier("app_name", "string", themeContext.getPackageName()));
                bean.themeName = str;
                bean.packageName = themePackage;
                int themePreview = themeContext.getResources().getIdentifier(
                        "lockertheme", "drawable", themeContext.getPackageName());
                if (themePreview > 0) {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    File file = null;
                    boolean loaded = false;
                    if (Constants.THEME_PACKAGE_NIGHT.equals(bean.packageName)) {
                        loaded = false;
                        file = imageLoader.getDiskCache().get(Constants.THEME_MOONNIGHT_URL);
                        if (file != null && file.exists()) {
                            BitmapDrawable bd = new BitmapDrawable(this.getResources(), file.getAbsolutePath());
                            if (bd != null) {
                                bean.themeImage = bd;
                                loaded = true;
                            }
                        }
                        if (!loaded) {
                            bean.themeImage = themeContext.getResources().getDrawable(themePreview);
                        }
                    } else if (Constants.THEME_PACKAGE_CHRITMAS.equals(bean.packageName)) {
                        loaded = false;
                        file = imageLoader.getDiskCache().get(Constants.THEME_CHRISTMAS_URL);
                        if (file != null && file.exists()) {
                            BitmapDrawable bd = new BitmapDrawable(this.getResources(), file.getAbsolutePath());
                            if (bd != null) {
                                bean.themeImage = bd;
                                loaded = true;
                            }
                        }
                        if (!loaded) {
                            bean.themeImage = themeContext.getResources().getDrawable(themePreview);
                        }
                    } else if (Constants.THEME_PACKAGE_FRUIT.equals(bean.packageName)) {
                        loaded = false;
                        file = imageLoader.getDiskCache().get(Constants.THEME_FRUIT_URL);
                        if (file != null && file.exists()) {
                            BitmapDrawable bd = new BitmapDrawable(this.getResources(), file.getAbsolutePath());
                            if (bd != null) {
                                bean.themeImage = bd;
                                loaded = true;
                            }
                        }
                        if (!loaded) {
                            bean.themeImage = themeContext.getResources().getDrawable(themePreview);
                        }
                    } else if (Constants.THEME_PACKAGE_SPATIAL.equals(bean.packageName)) {
                        loaded = false;
                        file = imageLoader.getDiskCache().get(Constants.THEME_SPATIAL_URL);
                        if (file != null && file.exists()) {
                            BitmapDrawable bd = new BitmapDrawable(this.getResources(), file.getAbsolutePath());
                            if (bd != null) {
                                bean.themeImage = bd;
                                loaded = true;
                            }
                        }
                        if (!loaded) {
                            bean.themeImage = themeContext.getResources().getDrawable(themePreview);
                        }
                    } else {
                        bean.themeImage = themeContext.getResources().getDrawable(themePreview);
                    }

                } else {
                    bean.themeImage = this.getResources().getDrawable(R.drawable.default_theme);
                }
                bean.label = (String) this.getResources().getText(R.string.localtheme);
                int themeLogo = themeContext.getResources().getIdentifier("ic_launcher", "drawable", themeContext.getPackageName());
                if (themeLogo > 0) {
                    bean.themeLogo = themeContext.getResources().getDrawable(themeLogo);
                } else {
                    bean.themeLogo = this.getResources().getDrawable(R.drawable.default_theme_logo);
                }

                if (AppMasterApplication.usedThemePackage.equals(themePackage)) {
                    bean.curUsedTheme = true;
                } else {
                    bean.curUsedTheme = false;
                }
                mLocalThemes.add(bean);
            } catch (NameNotFoundException e1) {
                LeoLog.e("Context", "getContext error");
            }
        }
        Collections.sort(mLocalThemes, new Comparator<ThemeItemInfo>() {
            @Override
            public int compare(ThemeItemInfo lhs, ThemeItemInfo rhs) {
                if (lhs.installTime < rhs.installTime) {
                    return 1;
                } else if (lhs.installTime > rhs.installTime) {
                    return -1;
                }
                return 0;
            }
        });
    }

    private void loadInitOnlineTheme() {
        ThemeListener listener = new ThemeListener(this, LOAD_INIT);
        HttpRequestAgent.getInstance(this).loadOnlineTheme(mHideThemes, listener);
    }

    private void initUI() {
        mPagerTab = (LeoPagerTab) findViewById(R.id.tab_indicator);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        LayoutInflater inflater = LayoutInflater.from(this);
        CommonToolbar title = (CommonToolbar) findViewById(R.id.layout_title_bar);
        title.setToolbarTitle(R.string.lockerTheme);
        title.setToolbarColorResource(R.color.cb);
        title.setOptionMenuVisible(false);
        title.setNavigationClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
//        title.setTitle(R.string.lockerTheme);
//        title.setBackViewListener(this);

        // inflate local theme
        localThemeList = (PullToRefreshListView) inflater.inflate(
                R.layout.theme_local_list, null);
        localThemeList.setMode(Mode.DISABLED);
        mLocalThemeAdapter = new LockerThemeAdapter(this, mLocalThemes);
        localThemeList.setAdapter(mLocalThemeAdapter);
        itemListener = new ThemeItemClickListener();
        localThemeList.setOnItemClickListener(itemListener);

        // inflate online theme
        mOnlineThemeView = inflater.inflate(R.layout.theme_online_list, null);
        mProgressBar = (ProgressBar) mOnlineThemeView
                .findViewById(R.id.progressbar_loading);
        mErrorView = mOnlineThemeView.findViewById(R.id.layout_load_error);
        mLayoutEmptyTip = mOnlineThemeView.findViewById(R.id.layout_empty);
        mTvRetry = (TextView) mOnlineThemeView.findViewById(R.id.tv_reload);
        mTvRetry.setOnClickListener(this);
        mOnlineThemeHolder = mOnlineThemeView.findViewById(R.id.content_holder);
        mOnlineThemeList = (PullToRefreshListView) mOnlineThemeView
                .findViewById(R.id.list_online);
        mOnlineThemeList.setMode(Mode.PULL_FROM_END);
        mOnlineThemeAdapter = new LockerThemeAdapter(this, mOnlineThemes);
        mOnlineThemeList.setAdapter(mOnlineThemeAdapter);
        mOnlineThemeList.setOnItemClickListener(itemListener);

        // fill viewpager
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                String title = "";
                if (position == 0) {
                    title = getString(R.string.localtheme);
                } else if (position == 1) {
                    title = getString(R.string.onlinetheme);
                }
                return title;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = null;
                if (position == 0) {
                    view = localThemeList;
                } else {
                    view = mOnlineThemeView;
                }
                container.addView(view, LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                return view;
            }
        });
        mPagerTab.setViewPager(mViewPager);
    }

    private void onLoadMoreThemeFinish(boolean succeed, Object object) {
        if (succeed) {
            List<ThemeItemInfo> list = (List<ThemeItemInfo>) object;
            if (list == null || list.isEmpty()) {
                Toast.makeText(this, R.string.no_more_theme, Toast.LENGTH_SHORT).show();
            } else {
                addMoreOnlineTheme(list);
            }
        } else {
            Toast.makeText(this, R.string.network_error_msg,
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void onLoadInitThemeFinish(boolean succeed, Object object) {
        if (mOnlineThemeAdapter != null) {
            mOnlineThemeHolder.setVisibility(View.VISIBLE);
            if (succeed) {
                mOnlineThemeHolder.setVisibility(View.VISIBLE);
                mErrorView.setVisibility(View.INVISIBLE);
                mOnlineThemes.clear();
                if (object != null) {
                    mOnlineThemes.addAll((List<ThemeItemInfo>) object);
                }

                // filter local theme
                if (!mOnlineThemes.isEmpty()) {
                    List<ThemeItemInfo> removeList = new ArrayList<ThemeItemInfo>();
                    for (ThemeItemInfo themeInfo : mOnlineThemes) {
                        for (ThemeItemInfo localInfo : mLocalThemes) {
                            if (themeInfo.packageName.equals(localInfo.packageName)) {
                                removeList.add(themeInfo);
                                break;
                            }
                        }
                    }
                    mOnlineThemes.removeAll(removeList);
                }

                mOnlineThemeAdapter.notifyDataSetChanged();
                if (mOnlineThemes.isEmpty()) {
                    mLayoutEmptyTip.setVisibility(View.VISIBLE);
                    mOnlineThemeList.setVisibility(View.VISIBLE);
                } else {
                    mOnlineThemeList.setVisibility(View.VISIBLE);
                    mLayoutEmptyTip.setVisibility(View.INVISIBLE);
                }
            } else {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "load_failed", "theme");
                mErrorView.setVisibility(View.VISIBLE);
                mOnlineThemeList.setVisibility(View.INVISIBLE);
                mLayoutEmptyTip.setVisibility(View.INVISIBLE);
            }
            mProgressBar.setVisibility(View.INVISIBLE);
            mOnlineThemeList.setOnRefreshListener(this);
        }
    }

    private void loadMoreOnlineTheme() {
        // mNextLoadPage = mCurrentShowPage + 1;
        List<String> loadedTheme = new ArrayList<String>();
        loadedTheme.addAll(mHideThemes);
        for (ThemeItemInfo info : mOnlineThemes) {
            loadedTheme.add(info.packageName);
        }

        ThemeListener listener = new ThemeListener(this, LOAD_MORE);
        HttpRequestAgent.getInstance(this).loadOnlineTheme(loadedTheme, listener);
    }

    public void addMoreOnlineTheme(List<ThemeItemInfo> loadList) {
        if (mOnlineThemeAdapter != null) {
            boolean add;
            boolean newTheme = false;
            for (ThemeItemInfo appLockerThemeBean : loadList) {
                add = true;
                for (ThemeItemInfo themeInfo : mLocalThemes) {
                    if (themeInfo.packageName
                            .equals(appLockerThemeBean.packageName)) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    for (ThemeItemInfo themeInfo : mOnlineThemes) {
                        if (themeInfo.packageName
                                .equals(appLockerThemeBean.packageName)) {
                            add = false;
                            break;
                        }
                    }
                }
                if (add) {
                    newTheme = true;
                    mOnlineThemes.add(appLockerThemeBean);
                }
            }
            if (newTheme) {
                mLayoutEmptyTip.setVisibility(View.INVISIBLE);
                mOnlineThemeAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, R.string.no_more_theme, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void tryHideThemeApk(String pkg) {
        if (pkg == null)
            return;
        if (pkg.startsWith("com.leo.theme")) {
            String action = "disable_theme_" + pkg;
            Intent intent = new Intent(action);
            this.sendBroadcast(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "tdau", "theme");
        if (mLocalThemeAdapter != null) {
            mLocalThemeAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalThemeAdapter = null;
        mOnlineThemeAdapter = null;

        // unregisterReceiver(mLockerThemeReceive);
        LeoGlobalBroadcast.unregisterBroadcastListener(mPackageChangedListener);
        AppLoadEngine.getInstance(this).setThemeChanageListener(null);
        ImageLoader.getInstance().clearMemoryCache();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        LeoLog.i("dfsdfsdfasdfas", "mNeedLoadTheme = " + mNeedLoadTheme);
        if (mFromTheme != null && !mFromTheme.equals("")) {
            mLockManager.filterPackage(getPackageName(), 1000);
            Intent intent = null;
            if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                intent = new Intent(this, LockSettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            super.onBackPressed();
        } else if (TextUtils.equals(mFrom, "new_theme_tip")) {
            if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                LeoLog.d("Track Lock Screen", "apply lockscreen form SplashActivity");
                mLockManager.applyLock(LockManager.LOCK_MODE_FULL,
                        getPackageName(), true, new LockManager.OnUnlockedListener() {
                            @Override
                            public void onUnlocked() {
                                mLockManager.filterPackage(getPackageName(), 500);
                                Intent intent = new Intent(LockerTheme.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                AppMasterApplication.getInstance().startActivity(intent);
                                LockerTheme.this.finish();
                            }

                            @Override
                            public void onUnlockOutcount() {

                            }

                            @Override
                            public void onUnlockCanceled() {
                            }
                        });
            } else {
                Intent intent = new Intent(this, LockSettingActivity.class);
                startActivity(intent);
                super.onBackPressed();
            }
        } else if (mNeedLoadTheme) {
//            Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
//            mHomeIntent.addCategory(Intent.CATEGORY_HOME);
//            mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//            startActivity(mHomeIntent);
//            finish();
        } else if (isFromLock) {
            mLockManager.applyLock(LockManager.LOCK_MODE_FULL, this.getPackageName(), false, null);
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }


    public void showAlarmDialogWith2Button(String packageName, Drawable themeImage) {
        mAlarmDialog = new LEOAlarmDialog(this);
        mAlarmDialog.setDialogIconDrawable(themeImage);
        mAlarmDialog.setTitle(getResources().getString(R.string.theme_apply_title, packageName));
        mAlarmDialog.setRightBtnStr(getString(R.string.locker_apply));
        mAlarmDialog.setContentVisiable(false);
        final String themeName = packageName;
        mAlarmDialog.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 0) {
                    mAlarmDialog.cancel();

                } else if (which == 1) {
                    for (int i = 0; i < mLocalThemes.size(); i++) {
                        mLocalThemes.get(i).curUsedTheme = false;
                    }
                    List<String> packageNames = new ArrayList<String>();
                    for (ThemeItemInfo themeItemInfo : mLocalThemes) {
                        packageNames.add(themeItemInfo.themeName);
                    }
                    boolean flag = packageNames.contains(themeName);
                    if (flag) {
                        AppMasterApplication
                                .setSharedPreferencesValue(lastSelectedItem.packageName);

                        // notify lock theme change
                        LeoEventBus.getDefaultBus().post(new LockThemeChangeEvent());

                        lastSelectedItem.curUsedTheme = true;
                        lastSelectedItem.label = (String) LockerTheme.this
                                .getResources().getText(R.string.localtheme);
                        if (mLocalThemeAdapter != null) {
                            mLocalThemeAdapter.notifyDataSetChanged();
                        }

                        SDKWrapper.addEvent(LockerTheme.this, SDKWrapper.P1,
                                "theme_apply", lastSelectedItem.packageName);
                        mAlarmDialog.cancel();
                        mGuideFlag = AppMasterPreference.getInstance(LockerTheme.this)
                                .getUseThemeGuide();
                        if (!mGuideFlag) {
                            setLockerGuideShare();
                        }
                    } else {
                        AppMasterApplication
                                .setSharedPreferencesValue(lastSelectedItem.packageName);
                        mLocalThemes.get(0).curUsedTheme = true;
                        lastSelectedItem.label = (String) LockerTheme.this
                                .getResources().getText(R.string.localtheme);
                        if (mLocalThemeAdapter != null) {
                            mLocalThemeAdapter.notifyDataSetChanged();
                        }
                        mAlarmDialog.cancel();
                    }
                }
            }
        });
        mAlarmDialog.show();
    }


    public void showAlarmDialog(String packageName, Drawable themeImage, int rightVisible) {
        if (dialog == null) {
            dialog = new LEOThreeButtonDialog(this);
        }
        dialog.setDialogIconDrawable(themeImage);
        dialog.setTitle(getResources().getString(R.string.theme_apply_title, packageName));
        dialog.setMiddleBtnStr(getString(R.string.locker_apply));
        dialog.setRightBtnStr(getString(R.string.locker_uninstall));
        dialog.setRightBtnVisiable(rightVisible);
        dialog.setContentVisiable(false);
        final String themeName = packageName;
        dialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 0) {
                    dialog.cancel();
                } else if (which == 1) {
                    if (mLocalThemes != null && mLocalThemes.size() > 0) {
                        for (int i = 0; i < mLocalThemes.size(); i++) {
                            mLocalThemes.get(i).curUsedTheme = false;
                        }
                    }
                    List<String> packageNames = new ArrayList<String>();
                    for (ThemeItemInfo themeItemInfo : mLocalThemes) {
                        packageNames.add(themeItemInfo.themeName);
                    }
                    boolean flag = packageNames.contains(themeName);
                    if (flag) {
                        AppMasterApplication
                                .setSharedPreferencesValue(lastSelectedItem.packageName);

                        // notify lock theme change
                        LeoEventBus.getDefaultBus().post(new LockThemeChangeEvent());

                        lastSelectedItem.curUsedTheme = true;
                        lastSelectedItem.label = (String) LockerTheme.this
                                .getResources().getText(R.string.localtheme);
                        int number = 0;
                        if (packageNames != null && packageNames.size() > 0) {
                            for (int i = 0; i < packageNames.size(); i++) {
                                if (mLocalThemes == null) {
                                    break;
                                }
                                if (TextUtils.isEmpty(packageNames.get(i))) {
                                    continue;
                                }
                                if (packageNames.get(i).contains(themeName)) {
                                    mLocalThemes.get(i).curUsedTheme = true;
                                    number = i;
                                    break;
                                }

                            }
                        }

                        if (mLocalThemeAdapter != null) {
                            mLocalThemeAdapter = new LockerThemeAdapter(LockerTheme.this, mLocalThemes);
                            localThemeList.setAdapter(mLocalThemeAdapter);
                            mLocalThemeAdapter.notifyDataSetChanged();
                            localThemeList.getRefreshableView().setSelection(number);
                        }

                        SDKWrapper.addEvent(LockerTheme.this, SDKWrapper.P1,
                                "theme_apply", lastSelectedItem.packageName);
                        dialog.cancel();
                        mGuideFlag = AppMasterPreference.getInstance(LockerTheme.this)
                                .getUseThemeGuide();
                        if (!mGuideFlag) {
                            setLockerGuideShare();
                        }
                    } else {
                        AppMasterApplication
                                .setSharedPreferencesValue(lastSelectedItem.packageName);
                        mLocalThemes.get(0).curUsedTheme = true;
                        lastSelectedItem.label = (String) LockerTheme.this
                                .getResources().getText(R.string.localtheme);
                        if (mLocalThemeAdapter != null) {
                            mLocalThemeAdapter.notifyDataSetChanged();
                        }
                        dialog.cancel();
                    }
                } else if (which == 2) {
                    Uri uri = Uri.fromParts("package",
                            lastSelectedItem.packageName, null);
                    Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                    try {
                        startActivity(intent);
                        mLockManager.filterSelfOneMinites();
                    } catch (Exception e) {
                    }
                    dialog.cancel();
                }
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private ThemeItemInfo getDefaultTheme() {
        ThemeItemInfo defaultTheme = new ThemeItemInfo();
        defaultTheme.themeImage = getResources().getDrawable(
                R.drawable.default_theme);
        defaultTheme.themeLogo = getResources().getDrawable(R.drawable.default_theme_logo);
        defaultTheme.themeName = (String) this.getResources().getText(
                R.string.ParadoxTheme);
        defaultTheme.packageName = "com.leo.theme.default";
        defaultTheme.label = (String) this.getResources().getText(
                R.string.defaultTheme);
        defaultTheme.themeName = (String) this.getResources().getText(
                R.string.defaultTheme);
        defaultTheme.curUsedTheme = true;

        if (AppMasterApplication.usedThemePackage
                .equals(Constants.DEFAULT_THEME)) {
            defaultTheme.curUsedTheme = true;
        } else {
            defaultTheme.curUsedTheme = false;
        }

        return defaultTheme;
    }

    /**
     * setLockerGuideShare
     */
    private void setLockerGuideShare() {
        Intent intent = new Intent(LockerTheme.this,
                LockerThemeGuideActivity.class);
        startActivity(intent);
        AppMasterPreference.getInstance(this).setUseThemeGuide(true);
        // SharedPreferences mLockerGuideShared = getSharedPreferences(
        // "LockerThemeGuide", LockerTheme.this.MODE_WORLD_WRITEABLE);
        // Editor editor = mLockerGuideShared.edit();
        // editor.putString("guideFlag", "1");
        // mGuideFlag = "1";
        // editor.commit();
    }

    private void doReload() {
        mOnlineThemeHolder.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        loadInitOnlineTheme();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv_reload:
                doReload();
                break;
            case R.id.layout_title_back:
                onBackPressed();
                break;
            default:
                break;
        }

    }


    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        loadMoreOnlineTheme();
    }

    private class ThemeItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            lastSelectedItem = (ThemeItemInfo) arg0.getItemAtPosition(arg2);
            /* SDK mark user click theme - begin */
            if (lastSelectedItem.themeType == Constants.THEME_TYPE_ONLINE) {
                SDKWrapper.addEvent(LockerTheme.this, SDKWrapper.P1,
                        "theme_choice_online", lastSelectedItem.packageName);
            } else if (lastSelectedItem.themeType == Constants.THEME_TYPE_LOCAL) {
                SDKWrapper.addEvent(LockerTheme.this, SDKWrapper.P1,
                        "theme_choice_local", lastSelectedItem.packageName);
            }
            SDKWrapper.addEvent(LockerTheme.this, SDKWrapper.P1,
                    "theme_choice", lastSelectedItem.packageName);
            /* SDK mark user click theme - end */
            if (lastSelectedItem.themeType == Constants.THEME_TYPE_ONLINE) {
                mLockManager.filterSelfOneMinites();
                if (lastSelectedItem.packageName.equals(ONLINE_AD_NAME)) {
                    return;
                }
                if (AppUtil
                        .appInstalled(LockerTheme.this, Constants.GP_PACKAGE)) {
                    try {
                        AppwallHttpUtil.requestGp(LockerTheme.this,
                                lastSelectedItem.packageName);
                    } catch (Exception e) {
                        AppwallHttpUtil.requestUrl(LockerTheme.this,
                                lastSelectedItem.downloadUrl);
                    }
                } else {
                    AppwallHttpUtil.requestUrl(LockerTheme.this,
                            lastSelectedItem.downloadUrl);
                }
            } else if (!lastSelectedItem.packageName
                    .equals(AppMasterApplication.usedThemePackage)) {
                if (lastSelectedItem.packageName.equals(LOCAL_AD_NAME)) {
                    return;
                }
                if (lastSelectedItem.packageName
                        .equals("com.leo.theme.default")) {
                    showAlarmDialogWith2Button(lastSelectedItem.themeName, lastSelectedItem.themeLogo);
                } else {
                    showAlarmDialog(lastSelectedItem.themeName, lastSelectedItem.themeLogo,
                            View.VISIBLE);
                }
            }
        }
    }

    private void createResultToHelpSetting() {
        Intent intent = new Intent();
        intent.putExtra("help_setting_current", mHelpSettingCurrent);
        this.setResult(RESULT_OK, intent);
    }

    private void showProgressDialog(String title, String message,
                                    boolean indeterminate, boolean cancelable) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEOCircleProgressDialog(this);
        }
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setButtonVisiable(cancelable);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(indeterminate);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    @Override
    public void loadTheme() {
        if (mLocalThemeAdapter != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadLocalTheme();
                    mLocalThemeAdapter.notifyDataSetChanged();
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    if (mFromTheme != null && !mFromTheme.equals("")) {
                        tryHideThemeApk(mFromTheme);
                        for (int i = 0; i < mLocalThemes.size(); i++) {
                            if (mLocalThemes.get(i).packageName.equals(mFromTheme)) {
                                number = i;
                                showAlarmDialog(mLocalThemes.get(i).themeName,
                                        mLocalThemes.get(i).themeLogo, View.VISIBLE);
                                lastSelectedItem = mLocalThemes.get(i);
                            }
                        }
                    } else {
                        number = 0;
                    }
                    localThemeList.getRefreshableView().setSelection(number);
                }
            });
        }
    }

    private static class ThemeListener extends RequestListener<LockerTheme> {

        private int loadType;

        public ThemeListener(LockerTheme outerContext, int loadType) {
            super(outerContext);
            this.loadType = loadType;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Context context = AppMasterApplication.getInstance();
            LoadFailUtils.sendLoadFail(context, "theme");

            LockerTheme lockerTheme = getOuterContext();
            if (lockerTheme == null)
                return;

            if (loadType == LOAD_INIT) {
                lockerTheme.mHandler.sendEmptyMessage(MSG_LOAD_INIT_FAILED);
            } else {
                lockerTheme.mHandler.sendEmptyMessage(MSG_LOAD_PAGE_DATA_FAILED);
            }
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            LockerTheme lockerTheme = getOuterContext();
            if (lockerTheme == null)
                return;

            LeoLog.d("response", response.toString());
            int msgId = 0;
            if (loadType == LOAD_INIT) {
                msgId = MSG_LOAD_INIT_SUCCESSED;
            } else {
                msgId = MSG_LOAD_PAGE_DATA_SUCCESS;
            }
            List<ThemeItemInfo> list = ThemeJsonObjectParser
                    .parserJsonObject(lockerTheme, response);
            Message msg = lockerTheme.mHandler.obtainMessage(msgId, list);
            lockerTheme.mHandler.sendMessage(msg);
        }

    }

}
