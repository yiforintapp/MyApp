package com.leo.appmaster.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.ui.HomeToolbar;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.MyViewPager;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/14.
 */
public class HomeTestActivity extends BaseFragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener {


    private MyViewPager mViewPager;
    private RelativeLayout mHomeTab;
    private RelativeLayout mUserTab;
    private RelativeLayout mTradeTab;
    private RelativeLayout mFindTab;
    private RelativeLayout mPersonalTab;

    private ImageView mHomeTabIv;
    private ImageView mUserTabIv;
    private ImageView mTradeIv;
    private ImageView mFindIv;
    private ImageView mPersonalIv;

    private TextView mHomeTabTv;
    private TextView mUserTabTv;
    private TextView mTradeTv;
    private TextView mFindTv;
    private TextView mPersonalTv;

    private HomeTabFragment mHomeTabFragment;
    private UserTabFragment mUserTabFragment;
    private TradeTabFragment mTradeTabFragment;
    private FindTabFragment mFindTabFragment;
    private PersonalFragment mPersonalFragment;

    private DrawerLayout mDrawerLayout;
    private HomeToolbar mToolBar;
    private ListView mMenuList;
    private List<MenuItem> mMenuItems;
    private MenuAdapter mMenuAdapter;
    private TextView mCenterTitle;
    private HomeFragmentHolder[] mFragmentHolders = new HomeFragmentHolder[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home_test);

        init();
        setLister();
    }

    private void init() {
        mViewPager = (MyViewPager) findViewById(R.id.home_ViewPager);

        mHomeTab = (RelativeLayout) findViewById(R.id.home_tab);
        mUserTab = (RelativeLayout) findViewById(R.id.user_tab);
        mTradeTab = (RelativeLayout) findViewById(R.id.trade_tab);
        mFindTab = (RelativeLayout) findViewById(R.id.find_tab);
        mPersonalTab = (RelativeLayout) findViewById(R.id.personal_tab);

        //test something
        mHomeTabIv = (ImageView) findViewById(R.id.home_iv);
        mUserTabIv = (ImageView) findViewById(R.id.user_iv);
        mTradeIv = (ImageView) findViewById(R.id.trade_iv);
        mFindIv = (ImageView) findViewById(R.id.find_iv);
        mPersonalIv = (ImageView) findViewById(R.id.personal_iv);

        mHomeTabTv = (TextView) findViewById(R.id.home_tv);
        mUserTabTv = (TextView) findViewById(R.id.user_tv);
        mTradeTv = (TextView) findViewById(R.id.trade_tv);
        mFindTv = (TextView) findViewById(R.id.find_tv);
        mPersonalTv = (TextView) findViewById(R.id.personal_tv);
        mHomeTabTv.setTextColor(Color.parseColor("#D83A3E"));

        mToolBar = (HomeToolbar) findViewById(R.id.home_toolBar);
        mMenuList = (ListView) findViewById(R.id.menu_list);
        mCenterTitle = (TextView) findViewById(R.id.center_title_tv);
        initFragment();
        mViewPager.setAdapter(new HomeTabAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2); //预加载2个
        mViewPager.setCurrentItem(0);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                if (slideOffset > 0) {
                    mToolBar.setNavigationLogoResource(R.drawable.ic_toolbar_back);
                    mToolBar.setBackgroundColor(getResources().getColor(R.color.ctc));
                } else {
                    mToolBar.setBackgroundColor(getResources().getColor(R.color.ctc));
                    mToolBar.setNavigationLogoResource(R.drawable.ic_toolbar_menu);
                }
            }
        });
        mMenuList = (ListView) findViewById(R.id.menu_list);
        mMenuItems = getMenuItems();
        mMenuAdapter = new MenuAdapter(this, mMenuItems);
        mMenuList.setAdapter(mMenuAdapter);
        mMenuList.setOnItemClickListener(this);
        mToolBar.setDrawerLayout(mDrawerLayout);
    }

    private List<MenuItem> getMenuItems() {
        List<MenuItem> listItems = new ArrayList<MenuItem>();
        Resources resources = AppMasterApplication.getInstance().getResources();
        /* 亲给个好评 */
        listItems.add(new MenuItem(resources.getString(R.string.app_name),
                R.drawable.menu_hot_icon, false));
        /* 点个赞 */
        listItems.add(new MenuItem(resources.getString(R.string.accelerate),
                R.drawable.menu_join_icon, false));
        return listItems;
    }

    private void setLister() {
        mHomeTab.setOnClickListener(this);
        mUserTab.setOnClickListener(this);
        mTradeTab.setOnClickListener(this);
        mFindTab.setOnClickListener(this);
        mPersonalTab.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        LeoLog.e("mMenuList", "onBackPressed");
        if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }
        super.onBackPressed();
    }


    /**
     * 更改tab按钮背景
     */
    private void changeButtonBg(int position) {
        switch (position) {
            case 0:
                mHomeTabTv.setTextColor(Color.parseColor("#D83A3E"));
                mHomeTabIv.setImageResource(R.drawable.icon_price_pre);
                mUserTabTv.setTextColor(getResources().getColor(R.color.black));
                mUserTabIv.setImageResource(R.drawable.icon_trade_nor);
                mTradeTv.setTextColor(getResources().getColor(R.color.black));
                mTradeIv.setImageResource(R.drawable.icon_news_nor);
                mFindTv.setTextColor(getResources().getColor(R.color.black));
                mFindIv.setImageResource(R.drawable.icon_service_nor);
                mPersonalTv.setTextColor(getResources().getColor(R.color.black));
                mPersonalIv.setImageResource(R.drawable.icon_personal_nor);
                mCenterTitle.setText("行情");
                break;
            case 1:
                mHomeTabTv.setTextColor(getResources().getColor(R.color.black));
                mHomeTabIv.setImageResource(R.drawable.icon_price_nor);
                mUserTabTv.setTextColor(Color.parseColor("#D83A3E"));
                mUserTabIv.setImageResource(R.drawable.icon_trade_pre);
                mTradeTv.setTextColor(getResources().getColor(R.color.black));
                mTradeIv.setImageResource(R.drawable.icon_news_nor);
                mFindTv.setTextColor(getResources().getColor(R.color.black));
                mFindIv.setImageResource(R.drawable.icon_service_nor);
                mPersonalTv.setTextColor(getResources().getColor(R.color.black));
                mPersonalIv.setImageResource(R.drawable.icon_personal_nor);
                mCenterTitle.setText("交易");
                break;
            case 2:
                mHomeTabTv.setTextColor(getResources().getColor(R.color.black));
                mHomeTabIv.setImageResource(R.drawable.icon_price_nor);
                mUserTabTv.setTextColor(getResources().getColor(R.color.black));
                mUserTabIv.setImageResource(R.drawable.icon_trade_nor);
                mTradeTv.setTextColor(Color.parseColor("#D83A3E"));
                mTradeIv.setImageResource(R.drawable.icon_news_pre);
                mFindTv.setTextColor(getResources().getColor(R.color.black));
                mFindIv.setImageResource(R.drawable.icon_service_nor);
                mPersonalTv.setTextColor(getResources().getColor(R.color.black));
                mPersonalIv.setImageResource(R.drawable.icon_personal_nor);
                mCenterTitle.setText("资讯");
                break;
            case 3:
                mHomeTabTv.setTextColor(getResources().getColor(R.color.black));
                mHomeTabIv.setImageResource(R.drawable.icon_price_nor);
                mUserTabTv.setTextColor(getResources().getColor(R.color.black));
                mUserTabIv.setImageResource(R.drawable.icon_trade_nor);
                mTradeTv.setTextColor(getResources().getColor(R.color.black));
                mTradeIv.setImageResource(R.drawable.icon_news_nor);
                mFindTv.setTextColor(Color.parseColor("#D83A3E"));
                mFindIv.setImageResource(R.drawable.icon_service_pre);
                mPersonalTv.setTextColor(getResources().getColor(R.color.black));
                mPersonalIv.setImageResource(R.drawable.icon_personal_nor);
                mCenterTitle.setText("服务");
                break;
            case 4:
                mHomeTabTv.setTextColor(getResources().getColor(R.color.black));
                mHomeTabIv.setImageResource(R.drawable.icon_price_nor);
                mUserTabTv.setTextColor(getResources().getColor(R.color.black));
                mUserTabIv.setImageResource(R.drawable.icon_trade_nor);
                mTradeTv.setTextColor(getResources().getColor(R.color.black));
                mTradeIv.setImageResource(R.drawable.icon_news_nor);
                mFindTv.setTextColor(getResources().getColor(R.color.black));
                mFindIv.setImageResource(R.drawable.icon_service_nor);
                mPersonalTv.setTextColor(Color.parseColor("#D83A3E"));
                mPersonalIv.setImageResource(R.drawable.icon_personal_pre);
                mCenterTitle.setText("我的");
                break;
        }
    }


    /**
     * 初始化fragment
     */
    private void initFragment() {
        HomeFragmentHolder holder = new HomeFragmentHolder();
        holder.title = /*this.getString(R.string.call_filter_black_list_tab)*/"首页内容";
        mHomeTabFragment = new HomeTabFragment();
        holder.fragment = mHomeTabFragment;
        mFragmentHolders[0] = holder;

        holder = new HomeFragmentHolder();
        holder.title = "自选";
        mUserTabFragment = new UserTabFragment();
        holder.fragment = mUserTabFragment;
        mFragmentHolders[1] = holder;

        holder = new HomeFragmentHolder();
        holder.title = "交易";
        mTradeTabFragment = new TradeTabFragment();
        holder.fragment = mTradeTabFragment;
        mFragmentHolders[2] = holder;

        holder = new HomeFragmentHolder();
        holder.title = "发现";
        mFindTabFragment = new FindTabFragment();
        holder.fragment = mFindTabFragment;
        mFragmentHolders[3] = holder;

        holder = new HomeFragmentHolder();
        holder.title = "个人";
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
            case R.id.home_tab:
                mViewPager.setCurrentItem(0);
                changeButtonBg(0);
                break;
            case R.id.user_tab:
                mViewPager.setCurrentItem(1);
                changeButtonBg(1);
                break;
            case R.id.trade_tab:
                mViewPager.setCurrentItem(2);
                changeButtonBg(2);
                break;
            case R.id.find_tab:
                mViewPager.setCurrentItem(3);
                changeButtonBg(3);
                break;
            case R.id.personal_tab:
                mViewPager.setCurrentItem(4);
                changeButtonBg(4);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
        }
        Toast.makeText(this, "点击:" + position, Toast.LENGTH_SHORT).show();
    }

    class MenuAdapter extends BaseAdapter {

        List<MenuItem> items;
        LayoutInflater inflater;

        public MenuAdapter(Context ctx, List<MenuItem> items) {
            super();
            this.items = items;
            inflater = LayoutInflater.from(ctx);
        }

        public void setItems(List<MenuItem> list) {
            items = list;
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
//            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.home_menu_item, arg2, false);
            MaterialRippleLayout layout = (MaterialRippleLayout) inflater.inflate(R.layout.home_menu_item, arg2, false);
            TextView tv = (TextView) layout.findViewById(R.id.menu_item_tv);
            tv.setTextColor(getResources().getColor(R.color.black));
            ImageView redTip = (ImageView) layout.findViewById(R.id.update_red_tip);
            /* some item not HTML styled text, such as "check update" item */
            tv.setText(Html.fromHtml(items.get(arg0).itemName));
            if (items.get(arg0).isRedTip) {
                redTip.setVisibility(View.VISIBLE);
            } else {
                redTip.setVisibility(View.GONE);
            }
            /**
             * 类似于阿拉伯语等从右往左显示的处理
             */
//            if (LanguageUtils.isRightToLeftLanguage(null)) {
//                // Log.e(Constants.RUN_TAG, "阿拉伯语");
//                tv.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources()
//                        .getDrawable(items.get(arg0).iconId), null);
//            } else {
            tv.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(items.get(arg0).iconId), null, null, null);
//            }

            return layout;
        }

    }

    class MenuItem {
        String itemName;
        int iconId;
        boolean isRedTip;

        public MenuItem(String itemName, int iconId, boolean isRedTip) {
            super();
            this.itemName = itemName;
            this.iconId = iconId;
            this.isRedTip = isRedTip;
        }
    }
}
