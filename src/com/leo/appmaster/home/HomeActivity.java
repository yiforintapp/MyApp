
package com.leo.appmaster.home;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockOptionActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.PasswdTipActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.appmanage.view.HomeAppManagerFragment;
import com.leo.appmaster.appsetting.AboutActivity;
import com.leo.appmaster.appwall.AppWallActivity;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.feedback.FeedbackHelper;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.fragment.HomeLockFragment;
import com.leo.appmaster.fragment.HomePravicyFragment;
import com.leo.appmaster.fragment.Selectable;
import com.leo.appmaster.home.HomeShadeView.OnShaderColorChangedLisetner;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.DrawerArrowDrawable;
import com.leo.appmaster.ui.IconPagerAdapter;
import com.leo.appmaster.ui.LeoPagerTab;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.RootChecker;

public class HomeActivity extends BaseFragmentActivity implements OnClickListener,
        OnItemClickListener,
        OnPageChangeListener, OnShaderColorChangedLisetner {

    private final static String KEY_ROOT_CHECK = "root_check";

    private ViewStub mViewStub;
    private MultiModeView mMultiModeView;
    private DrawerLayout mDrawerLayout;
    private ImageView mLeftMenu;
    private ListView mMenuList;
    private HomeTitleBar mTtileBar;
    private LeoPagerTab mPagerTab;
    private ViewPager mViewPager;
    private View mBgStatusbar, mFgStatusbar;
    private HomeShadeView mShadeView;
    private LeoPopMenu mLeoPopMenu;

    private float mDrawerOffset;
    private Handler mHandler = new Handler();
    private DrawerArrowDrawable mDrawerArrowDrawable;
    private HomeFragmentHoler[] mFragmentHolders = new HomeFragmentHoler[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initUI();
        tryTransStatusbar();
        // installShortcut();
        FeedbackHelper.getInstance().tryCommit();
        shortcutAndRoot();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "enter");
    }

    private void initUI() {
        mViewStub = (ViewStub) findViewById(R.id.viewstub);
        mPagerTab = (LeoPagerTab) findViewById(R.id.tab_indicator);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        initFragment();
        mViewPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mPagerTab.setViewPager(mViewPager);
        mPagerTab.setOnPageChangeListener(this);
        mPagerTab.setCurrentItem(0);

        mDrawerArrowDrawable = new DrawerArrowDrawable(getResources());
        mDrawerArrowDrawable.setStrokeColor(getResources()
                .getColor(R.color.white));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mDrawerOffset = slideOffset;
                mDrawerArrowDrawable.setParameter(mDrawerOffset);
            }
        });

        mMenuList = (ListView) findViewById(R.id.menu_list);
        mMenuList.setAdapter(new MenuAdapter(this, getMenuItems()));
        mMenuList.setOnItemClickListener(this);

        mTtileBar = (HomeTitleBar) findViewById(R.id.layout_title_bar);
        mLeftMenu = (ImageView) findViewById(R.id.iv_menu);
        mLeftMenu.setOnClickListener(this);
        mLeftMenu.setImageDrawable(mDrawerArrowDrawable);
        mTtileBar.setOptionClickListener(this);

        mBgStatusbar = findViewById(R.id.bg_statusbar);
        mFgStatusbar = findViewById(R.id.fg_statusbar);
        mShadeView = (HomeShadeView) findViewById(R.id.shadeview);
        mShadeView.setPosition(0);
        mShadeView.setColorChangedListener(this);
    }

    public void showModePages(boolean show/* , int[] center */) {
        if (mMultiModeView == null) {
            mMultiModeView = (MultiModeView) mViewStub.inflate();
        }

        if (show) {
            mMultiModeView.show();
        } else {
            mMultiModeView.hide();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            int oldTab = savedInstanceState.getInt("current_tap");
            mViewPager.setCurrentItem(oldTab);
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public int getShaderColor() {
        return mShadeView.getCurColor();
    }

    private void initFragment() {
        HomeFragmentHoler holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.lock_tab);
        HomeLockFragment lockFragment = new HomeLockFragment();
        holder.fragment = lockFragment;
        holder.iconId = R.drawable.lock_active_icon;
        mFragmentHolders[0] = holder;

        holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.pravicy_protect);
        HomePravicyFragment pravicyFragment = new HomePravicyFragment();
        holder.fragment = pravicyFragment;
        holder.iconId = R.drawable.protction_active_icon;
        mFragmentHolders[1] = holder;

        holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.app_manager);
        HomeAppManagerFragment appManagerFragment = new HomeAppManagerFragment();
        holder.fragment = appManagerFragment;
        holder.iconId = R.drawable.my_apps_active_icon;
        mFragmentHolders[2] = holder;

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

    @Override
    protected void onResume() {
        /* check if there is force update when showing HomeActivity */
        SDKWrapper.checkForceUpdate();

        judgeShowGradeTip();
        // compute privacy level here to avoid unknown change, such as file
        // deleted outside of your phone.
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        if (amp.getFromOther()) {
            PrivacyHelper.getInstance(this).computePrivacyLevel(PrivacyHelper.VARABLE_ALL);
            amp.setFromOther(false);
        }

        if (mViewPager != null) {
            int current = mViewPager.getCurrentItem();
            if (current < mFragmentHolders.length) {
                HomeFragmentHoler hfh = mFragmentHolders[current];
                if (hfh != null && hfh.fragment instanceof Selectable) {
                    ((Selectable) (hfh.fragment)).onSelected();
                }
            }
        }
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("current_tap", mViewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mMenuList)) {
            mDrawerLayout.closeDrawer(mMenuList);
            return;
        }

        if (mMultiModeView != null && mMultiModeView.getVisibility() == View.VISIBLE) {
            showModePages(false/* , new int[]{1,1} */);
            return;
        }

        super.onBackPressed();

        // ===== AMAM-1336 ========
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_menu:
                if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
                    mDrawerLayout.closeDrawer(Gravity.START);
                } else {
                    mDrawerLayout.openDrawer(Gravity.START);
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "menu");
                }
                break;
            case R.id.iv_option_image:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "password");
                if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
                    mDrawerLayout.closeDrawer(Gravity.START);
                }
                if (mLeoPopMenu == null) {
                    mLeoPopMenu = new LeoPopMenu();
                }
                mLeoPopMenu.setAnimation(R.style.RightEnterAnim);
                mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        if (position == 0) {
                            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "home",
                                    "changepwd");
                            Intent intent = new Intent(HomeActivity.this, LockSettingActivity.class);
                            intent.putExtra("reset_passwd", true);
                            startActivity(intent);
                        } else if (position == 1) {
                            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "home", "mibao");
                            Intent intent = new Intent(HomeActivity.this,
                                    PasswdProtectActivity.class);
                            startActivity(intent);
                        } else if (position == 2) {
                            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "home",
                                    "passwdtip");
                            Intent intent = new Intent(HomeActivity.this, PasswdTipActivity.class);
                            startActivity(intent);
                        } else if (position == 3) {
                            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "home",
                                    "locksetting");
                            Intent intent = new Intent(HomeActivity.this, LockOptionActivity.class);
                            intent.putExtra(LockOptionActivity.TAG_COME_FROM,
                                    LockOptionActivity.FROM_HOME);
                            HomeActivity.this.startActivity(intent);
                        }
                        mLeoPopMenu.dismissSnapshotList();
                    }
                });
                mLeoPopMenu.setPopMenuItems(getRightMenuItems());
                mLeoPopMenu.showPopMenu(this,
                        mTtileBar.findViewById(R.id.iv_option_image), null, null);
                break;

            default:
                break;
        }
    }

    private List<String> getRightMenuItems() {
        List<String> listItems = new ArrayList<String>();
        listItems.add(getString(R.string.reset_passwd));
        listItems.add(getString(R.string.set_protect_or_not));
        listItems.add(getString(R.string.passwd_notify));
        listItems.add(getString(R.string.lock_setting));
        return listItems;
    }

    private List<MenuItem> getMenuItems() {
        List<MenuItem> listItems = new ArrayList<MenuItem>();
        Resources resources = AppMasterApplication.getInstance().getResources();
        /* 亲给个好评 */
        listItems.add(new MenuItem(resources.getString(R.string.grade),
                R.drawable.menu_star_icon_menu));
        /* 点个赞 */
        listItems
                .add(new MenuItem(resources.getString(R.string.about_praise),
                        R.drawable.menu_hot_icon));
        /* 加入粉丝团 */
        listItems.add(new MenuItem(resources.getString(R.string.about_group),
                R.drawable.menu_join_icon));
        /* 吐个槽 */
        listItems.add(new MenuItem(resources.getString(R.string.feedback),
                R.drawable.menu_feedbacks_icon));
        /* 检查升级 */
        if (SDKWrapper.isUpdateAvailable()) {
            listItems.add(new MenuItem(resources.getString(R.string.app_setting_has_update),
                    R.drawable.menu_updates_icon));
        } else {
            listItems.add(new MenuItem(resources.getString(R.string.app_setting_update),
                    R.drawable.menu_updates_icon));
        }
        /* 关于 */
        listItems.add(new MenuItem(resources.getString(R.string.app_setting_about),
                R.drawable.menu_about_icon));
        return listItems;
    }

    private void shortcutAndRoot() {
        AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefernece = PreferenceManager
                        .getDefaultSharedPreferences(HomeActivity.this);
                boolean installed = prefernece.getBoolean("shortcut", false);
                if (!installed) {
                    Intent shortcutIntent = new Intent(HomeActivity.this, SplashActivity.class);
                    shortcutIntent.setAction(Intent.ACTION_MAIN);
                    shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                    Intent shortcut = new Intent(
                            "com.android.launcher.action.INSTALL_SHORTCUT");
                    shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                            getString(R.string.app_name));
                    shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                    ShortcutIconResource iconRes = Intent.ShortcutIconResource
                            .fromContext(HomeActivity.this, R.drawable.ic_launcher);
                    shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
                    shortcut.putExtra("duplicate", false);
                    shortcut.putExtra("from_shortcut", true);
                    sendBroadcast(shortcut);
                    prefernece.edit().putBoolean("shortcut", true).commit();
                }
                boolean appwallFlag = prefernece.getBoolean("shortcut_appwall", true);
                if (appwallFlag) {
                    Intent appWallShortIntent = new Intent(HomeActivity.this, AppWallActivity.class);
                    appWallShortIntent.putExtra("from_appwall_shortcut", true);
                    appWallShortIntent.setAction(Intent.ACTION_MAIN);
                    appWallShortIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    appWallShortIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Intent appWallShortcut = new Intent(
                            "com.android.launcher.action.UNINSTALL_SHORTCUT");
                    appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                            getString(R.string.appwall_name));
                    appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, appWallShortIntent);
                    appWallShortcut.putExtra("duplicate", true);
                    sendBroadcast(appWallShortcut);
                    prefernece.edit().putBoolean("shortcut_appwall", false);
                }
                if (prefernece.getBoolean(KEY_ROOT_CHECK, true)) {
                    boolean root = RootChecker.isRoot();
                    if (root) {
                        SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1,
                                KEY_ROOT_CHECK, "root");
                    }
                    prefernece.edit().putBoolean(KEY_ROOT_CHECK, false).commit();
                }
            }

        });
    }

    private void judgeShowGradeTip() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ActivityManager mActivityManager = (ActivityManager) HomeActivity.this
                        .getSystemService(Context.ACTIVITY_SERVICE);

                @SuppressWarnings("deprecation")
                RunningTaskInfo topTaskInfo = mActivityManager.getRunningTasks(
                        1).get(0);

                String pkg = HomeActivity.this.getPackageName();
                if (pkg.equals(topTaskInfo.baseActivity.getPackageName())) {
                    long count = AppMasterPreference.getInstance(
                            HomeActivity.this).getUnlockCount();
                    boolean haveTip = AppMasterPreference.getInstance(
                            HomeActivity.this).getGoogleTipShowed();
                    if (count >= 50 && !haveTip) {
                        LockManager.getInstatnce().timeFilterSelf();
                        Intent intent = new Intent(HomeActivity.this,
                                GradeTipActivity.class);
                        HomeActivity.this.startActivity(intent);
                    }
                }
            }
        }, 5000);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Intent intent = null;
        if (position == 2) {
            /* 加入粉丝团 */

            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "google+");
            Intent intentBeta = null;
            LockManager.getInstatnce().timeFilterSelf();
            if (AppUtil.appInstalled(getApplicationContext(),
                    "com.google.android.apps.plus")) {
                intentBeta = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri
                        .parse("https://plus.google.com/u/0/communities/112552044334117834440");
                intentBeta.setData(uri);
                ComponentName cn = new ComponentName(
                        "com.google.android.apps.plus",
                        "com.google.android.libraries.social.gateway.GatewayActivity");
                intentBeta.setComponent(cn);
                intentBeta.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intentBeta);
                } catch (Exception e) {
                    intentBeta = new Intent(Intent.ACTION_VIEW, uri);
                    ComponentName componentName = new ComponentName(
                            "com.google.android.apps.plus",
                            "com.google.android.apps.plus.phone.UrlGatewayActivity");
                    intentBeta.setComponent(componentName);
                    intentBeta.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intentBeta);
                    } catch (Exception e1) {
                        intentBeta = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intentBeta);
                    }
                }
            } else {
                Uri uri = Uri
                        .parse("https://plus.google.com/u/0/communities/112552044334117834440");
                intentBeta = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intentBeta);
            }
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "about", "like");

        } else if (position == 1) {
            /* Facebook */

            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "Facebook");
            Intent intentLikeUs = null;
            LockManager.getInstatnce().timeFilterSelf();
            if (AppUtil.appInstalled(getApplicationContext(),
                    "com.facebook.katana")) {
                intentLikeUs = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri
                        .parse("fb://page/1709302419294051");
                intentLikeUs.setData(uri);
                ComponentName cn = new ComponentName("com.facebook.katana",
                        "com.facebook.katana.IntentUriHandler");
                intentLikeUs.setComponent(cn);
                intentLikeUs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intentLikeUs);
                } catch (Exception e) {
                }
            } else {
                intentLikeUs = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri
                        .parse("https://www.facebook.com/pages/App-Master/1709302419294051");
                intentLikeUs.setData(uri);
                intentLikeUs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentLikeUs);
            }
        } else if (position == 0) {
            /* google play */
            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "googleplay");
            LockManager.getInstatnce().timeFilterSelf();
            if (AppUtil.appInstalled(getApplicationContext(),
                    "com.android.vending")) {
                intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri
                        .parse("market://details?id=com.leo.appmaster&referrer=utm_source=AppMaster");
                intent.setData(uri);
                ComponentName cn = new ComponentName(
                        "com.android.vending",
                        "com.google.android.finsky.activities.MainActivity");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                    // mHandler.postDelayed(new Runnable() {
                    // @Override
                    // public void run() {
                    // String lastActivity =
                    // LockManager.getInstatnce().getLastActivity();
                    // if (lastActivity != null
                    // && lastActivity
                    // .equals("com.google.android.finsky.activities.MainActivity"))
                    // {
                    // // Intent intent2 = new Intent(
                    // // HomeActivity.this,
                    // // GooglePlayGuideActivity.class);
                    // // startActivity(intent2);
                    // }
                    // }
                    // }, 1000);
                } catch (Exception e) {

                }
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri
                        .parse("https://play.google.com/store/apps/details?id=com.leo.appmaster&referrer=utm_source=AppMaster");
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else if (position == 3) {
            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "feedback");
            intent = new Intent(HomeActivity.this,
                    FeedbackActivity.class);
            startActivity(intent);
        } else if (position == 6) {
            /* 游戏中心 */

            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "gamecenter");
            intent = new Intent(HomeActivity.this,
                    AppWallActivity.class);
            intent.putExtra(Constants.HOME_TO_APP_WALL_FLAG,
                    Constants.HOME_TO_APP_WALL_FLAG_VALUE);
            startActivity(intent);
        } else if (position == 4) {
            /* 检查更新 */

            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "update");
            SDKWrapper.checkUpdate();
        } else if (position == 5) {
            /* 关于 */

            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "about");
            intent = new Intent(HomeActivity.this,
                    AboutActivity.class);
            startActivity(intent);
        }
    }

    private void tryTransStatusbar() {
        if (VERSION.SDK_INT >= 19) {
            // change status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // change nav bar
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        } else {
            mBgStatusbar.setVisibility(View.GONE);
            mFgStatusbar.setVisibility(View.GONE);
        }
    }

    class HomeFragmentHoler {
        String title;
        int iconId;
        BaseFragment fragment;
    }

    class HomePagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
        public HomePagerAdapter(FragmentManager fm) {
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

        @Override
        public int getIconResId(int index) {
            return mFragmentHolders[index].iconId;
        }

        @Override
        public boolean getRedTip(int index) {
            return false;
        }
    }

    class MenuAdapter extends BaseAdapter {

        List<MenuItem> items;
        LayoutInflater inflater;

        public MenuAdapter(Context ctx, List<MenuItem> items) {
            super();
            this.items = items;
            inflater = LayoutInflater.from(ctx);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            return items.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        /**
         * need not ViewHolder here
         */
        @SuppressLint("ViewHolder")
        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.home_menu_item, arg2,
                    false);
            TextView tv = (TextView) layout.findViewById(R.id.menu_item_tv);
            /* some item not HTML styled text, such as "check update" item */
            tv.setText(Html.fromHtml(items.get(arg0).itemName));
            tv.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(items.get(arg0).iconId), null, null,
                    null);
            return layout;
        }

    }

    class MenuItem {
        String itemName;
        int iconId;

        public MenuItem(String itemName, int iconId) {
            super();
            this.itemName = itemName;
            this.iconId = iconId;
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (arg0 == 1 && mFragmentHolders[arg0].fragment instanceof Selectable) {
            ((Selectable) mFragmentHolders[mViewPager.getCurrentItem()].fragment).onScrolling();
        } else {
            ((Selectable) mFragmentHolders[mViewPager.getCurrentItem()].fragment).onSelected();
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        mShadeView.setPosition(arg0 + arg1);
    }

    @Override
    public void onPageSelected(int arg0) {
        if (mFragmentHolders[arg0].fragment instanceof Selectable) {
            ((Selectable) mFragmentHolders[arg0].fragment).onSelected();
        }
    }

    @Override
    public void onShaderColorChanged(int color) {
        if (mBgStatusbar.getVisibility() == View.VISIBLE) {
            mBgStatusbar.setBackgroundColor(color);
        }
        if (mFragmentHolders[1] != null && mFragmentHolders[1].fragment != null) {
            mFragmentHolders[1].fragment.onBackgroundChanged(color);
        }

    }

}
