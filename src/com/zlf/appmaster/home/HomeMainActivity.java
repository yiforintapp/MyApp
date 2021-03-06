package com.zlf.appmaster.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.ui.HomeToolbar;
import com.zlf.appmaster.ui.MyViewPager;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.zhibo.WordLiveActivity;

import java.util.List;


/**
 * Created by Administrator on 2016/7/14.
 */
public class HomeMainActivity extends BaseFragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener, ViewPager.OnPageChangeListener {

    public static final int CHECKUPDATE = 1;

    private MyViewPager mViewPager;
    private RelativeLayout mHomeTabReal;
    private RelativeLayout mHomeTab;
    private RelativeLayout mUserTab;
    private RelativeLayout mTradeTab;
    private RelativeLayout mPersonalTab;

    private ImageView mHomeTabIvReal;
    private ImageView mHomeTabIv;
    private ImageView mUserTabIv;
    private ImageView mTradeIv;
    private ImageView mPersonalIv;

    private TextView mHomeTabTvReal;
    private TextView mHomeTabTv;
    private TextView mUserTabTv;
    private TextView mTradeTv;
    private TextView mPersonalTv;

    private HomeTabFragment mHomeTabFragment;
    private TradeTabFragment mTradeTabFragment;
    private UserTabFragment mUserTabFragment;
    //    private StockFavoriteFragment mStockFavoriteFragment;
    private ZhiBoFragment mZhiboFragment;
    private PersonalFragment mPersonalFragment;

    //    private DrawerLayout mDrawerLayout;
    private HomeToolbar mToolBar;
    //    private ListView mMenuList;
//    private List<MenuItem> mMenuItems;
//    private MenuAdapter mMenuAdapter;
    private TextView mCenterTitle;
    private RippleView mRefreshRipple;
    private ImageView mRefreshIv;
    private HomeFragmentHolder[] mFragmentHolders = new HomeFragmentHolder[5];

    private int mIndex = 2;

    private RippleView mOneRipple;

    private boolean mClickHangQin;  // 点击行情按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main);
        init();
        setLister();
        handleIntent();
    }

    private void handleIntent() {

        Intent intent = getIntent();
        String value = intent.getStringExtra(Constants.PUSH_KEY);
        int page;
        if (value != null && !value.isEmpty()) {
            page = Integer.parseInt(value);
            LeoLog.d("JPush", "handle page = " + page);
            mViewPager.setCurrentItem(page);
            changeTabBg(page);
        }


        String room = intent.getStringExtra(Constants.PUSH_ROOM);
        Log.d("JPush","room222 is : " + room);
        if (!TextUtils.isEmpty(room) && !room.equals("0")) {
            Intent roomIntent = new Intent(this, WordLiveActivity.class);
            String title = "";
            if (room.equals("1")) {
                title = getResources().getString(R.string.zhibo_room_one_topic);
            } else if (room.equals("2")) {
                title = getResources().getString(R.string.zhibo_room_two_topic);
            } else if (room.equals("3")) {
                title = getResources().getString(R.string.zhibo_room_thr_topic);
            }

            roomIntent.putExtra(WordLiveActivity.ZHIBO_TYPE, room);
            roomIntent.putExtra(WordLiveActivity.ZHIBO_TITLE, title);
            startActivity(roomIntent);
        }

    }

    private void init() {
        mViewPager = (MyViewPager) findViewById(R.id.home_ViewPager);

        mHomeTabReal = (RelativeLayout) findViewById(R.id.home_tab_real);
        mHomeTab = (RelativeLayout) findViewById(R.id.home_tab);
        mUserTab = (RelativeLayout) findViewById(R.id.user_tab);
        mTradeTab = (RelativeLayout) findViewById(R.id.trade_tab);
        mPersonalTab = (RelativeLayout) findViewById(R.id.personal_tab);

        //test something

        mHomeTabIvReal = (ImageView) findViewById(R.id.home_iv_real);
        mHomeTabIv = (ImageView) findViewById(R.id.home_iv);
        mUserTabIv = (ImageView) findViewById(R.id.user_iv);
        mTradeIv = (ImageView) findViewById(R.id.trade_iv);
        mPersonalIv = (ImageView) findViewById(R.id.personal_iv);

        mHomeTabTvReal = (TextView) findViewById(R.id.home_tv_real);
        mHomeTabTv = (TextView) findViewById(R.id.home_tv);
        mUserTabTv = (TextView) findViewById(R.id.user_tv);
        mTradeTv = (TextView) findViewById(R.id.trade_tv);
        mPersonalTv = (TextView) findViewById(R.id.personal_tv);

//        mHomeTabTvReal.setTextColor(getResources().getColor(R.color.indicator_text_select_color));
        mUserTabTv.setTextColor(getResources().getColor(R.color.indicator_text_select_color));

        mOneRipple = (RippleView) findViewById(R.id.home_tab);

        mToolBar = (HomeToolbar) findViewById(R.id.home_toolBar);
//        mMenuList = (ListView) findViewById(R.id.menu_list);
        mCenterTitle = (TextView) findViewById(R.id.center_title_tv);
        mRefreshRipple = (RippleView) findViewById(R.id.refresh);
        mRefreshRipple.setOnClickListener(this);
        mRefreshIv = (ImageView) findViewById(R.id.refresh_iv);
        initFragment();
        mViewPager.setAdapter(new HomeTabAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2); //预加载2个
        mViewPager.setCurrentItem(2);
        mViewPager.setOnPageChangeListener(this);

//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//
//                if (slideOffset > 0) {
//                    mToolBar.setNavigationLogoResource(R.drawable.ic_toolbar_back);
//                    mToolBar.setBackgroundColor(getResources().getColor(R.color.ctc));
//                } else {
//                    mToolBar.setBackgroundColor(getResources().getColor(R.color.ctc));
//                    mToolBar.setNavigationLogoResource(R.drawable.ic_toolbar_menu);
//                }
//            }
//        });
//        mMenuList = (ListView) findViewById(R.id.menu_list);
//        mMenuItems = getMenuItems();
//        mMenuAdapter = new MenuAdapter(this, mMenuItems);
//        mMenuList.setAdapter(mMenuAdapter);
//        mMenuList.setOnItemClickListener(this);
//        mToolBar.setDrawerLayout(mDrawerLayout);
    }

//    private List<MenuItem> getMenuItems() {
//        List<MenuItem> listItems = new ArrayList<MenuItem>();
//        Resources resources = AppMasterApplication.getInstance().getResources();
//        /* 关于兆利丰 */
//        listItems.add(new MenuItem(resources.getString(R.string.about_zlf),
//                R.drawable.menu_hot_icon, false));
//        /* 点个赞 */
////        listItems.add(new MenuItem(resources.getString(R.string.accelerate),
////                R.drawable.menu_join_icon, false));
//        /* 检查升级 */
//        listItems.add(new MenuItem(resources.getString(R.string.update_date),
//                R.drawable.menu_updates_icon, false));
//
//        return listItems;
//    }

    private void setLister() {
        mHomeTabReal.setOnClickListener(this);
        mHomeTab.setOnClickListener(this);
        mUserTab.setOnClickListener(this);
        mTradeTab.setOnClickListener(this);
        mPersonalTab.setOnClickListener(this);
    }

    public MyViewPager getViewPager() {
        if (mViewPager != null) {
            mClickHangQin = true;
        }
        return mViewPager;
    }

    @Override
    public void onBackPressed() {
        LeoLog.e("mMenuList", "onBackPressed");
//        if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
//            mDrawerLayout.closeDrawer(Gravity.START);
//            return;
//        }
        super.onBackPressed();
    }


    /**
     * 更改tab背景
     */
    private void changeTabBg(int position) {
        switch (position) {
            case 0:
                mHomeTabTvReal.setTextColor(getResources().getColor(R.color.indicator_text_select_color));
                mHomeTabIvReal.setImageResource(R.drawable.icon_service_pre);
                changeUnSelectBg(mIndex);
                mIndex = 0;
                mCenterTitle.setText(getResources().getString(R.string.main_tab_home_real));
                mRefreshRipple.setVisibility(View.VISIBLE);
                break;
            case 1:
                mHomeTabTv.setTextColor(getResources().getColor(R.color.indicator_text_select_color));
                mHomeTabIv.setImageResource(R.drawable.icon_price_pre);
                changeUnSelectBg(mIndex);
                mIndex = 1;
                mCenterTitle.setText(getResources().getString(R.string.main_tab_home));
                mRefreshRipple.setVisibility(View.GONE);
                break;
            case 2:
                mUserTabTv.setTextColor(getResources().getColor(R.color.indicator_text_select_color));
                mUserTabIv.setImageResource(R.drawable.icon_trade_pre);
                changeUnSelectBg(mIndex);
                mIndex = 2;
                mCenterTitle.setText(getResources().getString(R.string.main_tab_zhibo));
                mRefreshRipple.setVisibility(View.GONE);
                break;
            case 3:
                mTradeTv.setTextColor(getResources().getColor(R.color.indicator_text_select_color));
                mTradeIv.setImageResource(R.drawable.icon_news_pre);
                changeUnSelectBg(mIndex);
                mIndex = 3;
                mCenterTitle.setText(getResources().getString(R.string.main_tab_info));
                mRefreshRipple.setVisibility(View.GONE);
                break;
            case 4:
                mPersonalTv.setTextColor(getResources().getColor(R.color.indicator_text_select_color));
                mPersonalIv.setImageResource(R.drawable.icon_personal_pre);
                changeUnSelectBg(mIndex);
                mIndex = 4;
                mCenterTitle.setText(getResources().getString(R.string.main_tab_personal));
                mRefreshRipple.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 改变上次选中tab颜色为未选中
     */
    private void changeUnSelectBg(int position) {
        switch (position) {
            case 0:
                mHomeTabTvReal.setTextColor(getResources().getColor(R.color.indicator_text_color));
                mHomeTabIvReal.setImageResource(R.drawable.icon_service_nor);
                break;
            case 1:
                mHomeTabTv.setTextColor(getResources().getColor(R.color.indicator_text_color));
                mHomeTabIv.setImageResource(R.drawable.icon_price_nor);
                break;
            case 2:
                mUserTabTv.setTextColor(getResources().getColor(R.color.indicator_text_color));
                mUserTabIv.setImageResource(R.drawable.icon_trade_nor);
                break;
            case 3:
                mTradeTv.setTextColor(getResources().getColor(R.color.indicator_text_color));
                mTradeIv.setImageResource(R.drawable.icon_news_nor);
                break;
            case 4:
                mPersonalTv.setTextColor(getResources().getColor(R.color.indicator_text_color));
                mPersonalIv.setImageResource(R.drawable.icon_personal_nor);
                break;
        }
    }


    /**
     * 初始化fragment
     */
    private void initFragment() {
        HomeFragmentHolder holder = new HomeFragmentHolder();
        holder.title = getResources().getString(R.string.main_tab_home_real);
        mHomeTabFragment = new HomeTabFragment();
        holder.fragment = mHomeTabFragment;
        mFragmentHolders[0] = holder;


        holder = new HomeFragmentHolder();
        holder.title = getResources().getString(R.string.main_tab_home);
        mTradeTabFragment = new TradeTabFragment();
        holder.fragment = mTradeTabFragment;
        mFragmentHolders[1] = holder;

        holder = new HomeFragmentHolder();
        holder.title = getResources().getString(R.string.deal_live);
        mZhiboFragment = new ZhiBoFragment();
        holder.fragment = mZhiboFragment;
        mFragmentHolders[2] = holder;

        holder = new HomeFragmentHolder();
        holder.title = getResources().getString(R.string.main_tab_info);
        mUserTabFragment = new UserTabFragment();
        holder.fragment = mUserTabFragment;
        mFragmentHolders[3] = holder;

        holder = new HomeFragmentHolder();
        holder.title = getResources().getString(R.string.main_tab_personal);
        mPersonalFragment = new PersonalFragment();
        holder.fragment = mPersonalFragment;
        mFragmentHolders[4] = holder;

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
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        if (i == 1) {
            if (mIndex == 1) {
                return;
            }
            if (mClickHangQin) {
                changeTabBg(1);
                mClickHangQin = false;
                mTradeTabFragment.startTimer();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    class HomeTabAdapter extends FragmentPagerAdapter {
        public HomeTabAdapter(FragmentManager fm) {
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

    class HomeFragmentHolder {
        String title;
        BaseFragment fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_tab_real:
                if (mIndex == 0) {
                    return;
                }
                mViewPager.setCurrentItem(0);
                changeTabBg(0);
//                mTradeTabFragment.stopTimer();
                break;
            case R.id.home_tab:
                if (mIndex == 1) {
                    return;
                }
                mViewPager.setCurrentItem(1);
                if (!mClickHangQin) {
                    changeTabBg(1);
                }
//                mTradeTabFragment.startTimer();
                break;
            case R.id.user_tab:
                if (mIndex == 2) {
                    return;
                }
                mViewPager.setCurrentItem(2);
                changeTabBg(2);
//                mTradeTabFragment.stopTimer();
                break;
            case R.id.trade_tab:
                if (mIndex == 3) {
                    return;
                }
                mViewPager.setCurrentItem(3);
                changeTabBg(3);
//                mTradeTabFragment.stopTimer();
                break;
            case R.id.personal_tab:
                if (mIndex == 4) {
                    return;
                }
                mViewPager.setCurrentItem(4);
                changeTabBg(4);
//                mTradeTabFragment.stopTimer();
                break;
            case R.id.refresh:
                Animation refreshAnim = AnimationUtils.loadAnimation(this, R.anim.refresh_anim);
                if (mRefreshIv != null) {
                    mRefreshIv.setImageResource(R.drawable.juhua_loading);
                    mRefreshIv.startAnimation(refreshAnim);
                }
                mRefreshRipple.setEnabled(false);
                if (mIndex == 0) {
                    if (mHomeTabFragment != null) {
                        mHomeTabFragment.requestData();
                        mHomeTabFragment.requestHomeData();
                    }
                } else if (mIndex == 1) {
                    if (mTradeTabFragment != null) {
                        mTradeTabFragment.initFragment();
                    }
                } else if (mIndex == 3) {
                    if (mUserTabFragment != null) {
                        mUserTabFragment.initFragment();
                    }
                }
                break;
        }
    }

    public void stopRefreshAnim() {
        mRefreshRipple.setEnabled(true);
        if (mRefreshIv != null) {
            mRefreshIv.setImageResource(R.drawable.ic_msg_center_refresh);
            mRefreshIv.clearAnimation();
        }
    }


    public HomeTabFragment getHomeTabFragment() {
        return mHomeTabFragment;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
//            mDrawerLayout.closeDrawer(Gravity.START);
//        }
//        switch (position){
//            case CHECKUPDATE:
//                checkUpdate();
//                break;
//            case 0:
//                startActivity(intent);
//                break;
//        }
    }

//    private void checkUpdate() {
//        Intent intent = new Intent(HomeMainActivity.this, UpdateActivity.class);
//        intent.putExtra(UpdateActivity.UPDATETYPE,UpdateActivity.CHECK_UPDATE);
//        startActivity(intent);
//    }

//    class MenuAdapter extends BaseAdapter {
//
//        List<MenuItem> items;
//        LayoutInflater inflater;
//
//        public MenuAdapter(Context ctx, List<MenuItem> items) {
//            super();
//            this.items = items;
//            inflater = LayoutInflater.from(ctx);
//        }
//
//        public void setItems(List<MenuItem> list) {
//            items = list;
//        }
//
//        @Override
//        public int getCount() {
//            return items.size();
//        }
//
//        @Override
//        public Object getItem(int arg0) {
//            return items.get(arg0);
//        }
//
//        @Override
//        public long getItemId(int arg0) {
//            return arg0;
//        }
//
//        /**
//         * need not ViewHolder here
//         */
//        @SuppressLint("ViewHolder")
//        @Override
//        public View getView(int arg0, View arg1, ViewGroup arg2) {
////            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.home_menu_item, arg2, false);
//            MaterialRippleLayout layout = (MaterialRippleLayout) inflater.inflate(R.layout.home_menu_item, arg2, false);
//            TextView tv = (TextView) layout.findViewById(R.id.menu_item_tv);
//            tv.setTextColor(getResources().getColor(R.color.black));
//            ImageView redTip = (ImageView) layout.findViewById(R.id.update_red_tip);
//            /* some item not HTML styled text, such as "check update" item */
//            tv.setText(Html.fromHtml(items.get(arg0).itemName));
//            if (items.get(arg0).isRedTip) {
//                redTip.setVisibility(View.VISIBLE);
//            } else {
//                redTip.setVisibility(View.GONE);
//            }
//            /**
//             * 类似于阿拉伯语等从右往左显示的处理
//             */
////            if (LanguageUtils.isRightToLeftLanguage(null)) {
////                // Log.e(Constants.RUN_TAG, "阿拉伯语");
////                tv.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources()
////                        .getDrawable(items.get(arg0).iconId), null);
////            } else {
//            tv.setCompoundDrawablesWithIntrinsicBounds(
//                    getResources().getDrawable(items.get(arg0).iconId), null, null, null);
////            }
//
//            return layout;
//        }
//
//    }

//    class MenuItem {
//        String itemName;
//        int iconId;
//        boolean isRedTip;
//
//        public MenuItem(String itemName, int iconId, boolean isRedTip) {
//            super();
//            this.itemName = itemName;
//            this.iconId = iconId;
//            this.isRedTip = isRedTip;
//        }
//    }
}
