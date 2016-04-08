package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.activity.PrivacyOptionActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.PasswdTipActivity;
import com.leo.appmaster.msgcenter.MsgCenterActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.LeoHomePopMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/11.
 */
public class HomeToolbar extends RelativeLayout implements View.OnClickListener {

    private View mMenuRl;
    private View mMsgcenterRl;
//    private View mMoreRl;

    private ImageView mMenuIv;

    // 侧边栏菜单小红点
    private ImageView mMenuRedTipIv;
    private TextView mMsgCenterRedCount;
    private DrawerLayout mDrawerLayout;

    private LeoHomePopMenu mLeoPopMenu;

    private TextView mTitleTv;

    public HomeToolbar(Context context) {
        this(context, null);
    }

    public HomeToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.home_toolbar, this, true);

        mMenuRl = findViewById(R.id.hm_tool_menu_rl);
        mMenuRl.setOnClickListener(this);

        mMsgcenterRl = findViewById(R.id.hm_tool_msgcenter_rl);
        mMsgcenterRl.setOnClickListener(this);

//        mMoreRl = findViewById(R.id.hm_tool_more_rl);
//        mMoreRl.setOnClickListener(this);

        mMenuIv = (ImageView) findViewById(R.id.hm_tool_menu_iv);
        mMenuRedTipIv = (ImageView) findViewById(R.id.hm_tool_menu_red_tip_iv);
        mMsgCenterRedCount = (TextView) findViewById(R.id.hm_tool_mc_unread_tv);

        mTitleTv = (TextView) findViewById(R.id.hm_tool_title_tv);
        mTitleTv.setOnClickListener(this);
    }

    public void setDrawerLayout(DrawerLayout layout) {
        mDrawerLayout = layout;
    }

    public void showMenuRedTip(boolean show) {
        mMenuRedTipIv.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setNavigationLogoResource(int imageResource) {
        mMenuIv.setImageResource(imageResource);
    }

    public void showMsgcenterUnreadCount(boolean show, int count) {
        if (show) {
            mMsgCenterRedCount.setVisibility(View.VISIBLE);
            String unreadCountStr = count + "";
            if (count > 99) {
                unreadCountStr = "99+";
            }
            mMsgCenterRedCount.setText(unreadCountStr);
        } else {
            mMsgCenterRedCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hm_tool_menu_rl:
                Activity ac = (Activity) getContext();
                if (ac instanceof HomeActivity) {
                    if (((HomeActivity) ac).isTabDismiss()) {
                        return;
                    }
                }
                if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
                    mDrawerLayout.closeDrawer(Gravity.START);
                } else {
                    mDrawerLayout.openDrawer(Gravity.START);
                    SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "home", "menu");
                }
                break;
            case R.id.hm_tool_msgcenter_rl:
                // 点击启动消息中心的次数
                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "InfoCtr", "InfoCtr_cnts");
                Intent msgCenter = new Intent();
                msgCenter.setClass(getContext(), MsgCenterActivity.class);
                getContext().startActivity(msgCenter);
                break;
//            case R.id.hm_tool_more_rl:
//                Activity activity = (Activity) getContext();
//                if (activity instanceof HomeActivity) {
//                    if (((HomeActivity) activity).isTabDismiss()) {
//                        return;
//                    }
//                }
//                SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "home", "password");
//                if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
//                    mDrawerLayout.closeDrawer(Gravity.START);
//                }
//                initSettingMenu();
//                mLeoPopMenu.setPopMenuItems(activity, getRightMenuItems(), getRightMenuIcons());
//                mLeoPopMenu.showPopMenu(activity, mMoreRl, null, null);
//                break;
            case R.id.hm_tool_title_tv:
                if (AppMasterConfig.LOGGABLE) {
//                    Intent intent = new Intent(getContext(), DebugActivity.class);
//                    getContext().startActivity(intent);
                }
                break;
        }
    }

    private void initSettingMenu() {
        if (mLeoPopMenu != null) return;

        mLeoPopMenu = new LeoHomePopMenu();
        mLeoPopMenu.setAnimation(R.style.RightEnterAnim);
        mLeoPopMenu.setPopItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Activity activity = (Activity) getContext();
                if (position == 0) {
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "changepwd");
                    Intent intent = new Intent(activity, LockSettingActivity.class);
                    intent.putExtra("reset_passwd", true);
                    activity.startActivity(intent);
                } else if (position == 1) {
                    Intent intent = new Intent(activity, LockSettingActivity.class);
                    intent.putExtra("reset_passwd", true);
                    intent.putExtra("rotate_fragment", true);
                    activity.startActivity(intent);
                } else if (position == 2) {
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "mibao");
                    Intent intent = new Intent(activity, PasswdProtectActivity.class);
                    activity.startActivity(intent);
                } else if (position == 3) {
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "passwdtip");
                    Intent intent = new Intent(activity, PasswdTipActivity.class);
                    activity.startActivity(intent);
                } else if (position == 4) {
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "locksetting");
                    Intent intent = new Intent(activity, PrivacyOptionActivity.class);
//                    intent.putExtra(LockOptionActivity.TAG_COME_FROM,
//                            LockOptionActivity.FROM_HOME);
                    activity.startActivity(intent);
                }
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLeoPopMenu.dismissSnapshotList();
                    }
                }, 500);
            }
        });
        mLeoPopMenu.setListViewDivider(null);
    }

    private List<String> getRightMenuItems() {
        List<String> listItems = new ArrayList<String>();
        Context ctx = getContext();
        listItems.add(ctx.getString(R.string.reset_passwd));
        AppMasterPreference preference = AppMasterPreference.getInstance(ctx);
        if (preference.getLockType() == AppMasterPreference.LOCK_TYPE_PASSWD) {
            listItems.add(ctx.getString(R.string.change_to_gesture));
        } else {
            listItems.add(ctx.getString(R.string.change_to_password));
        }
        listItems.add(ctx.getString(R.string.set_protect_or_not));
        listItems.add(ctx.getString(R.string.passwd_notify));
        listItems.add(ctx.getString(R.string.home_menu_privacy));
        return listItems;
    }

    private List<Integer> getRightMenuIcons() {
        List<Integer> icons = new ArrayList<Integer>();
        icons.add(R.drawable.reset_pasword_icon);
        Context ctx = getContext();
        AppMasterPreference preference = AppMasterPreference.getInstance(ctx);
        if (preference.getLockType() == AppMasterPreference.LOCK_TYPE_PASSWD) {
            icons.add(R.drawable.ic_switch_to_gesture);
        } else {
            icons.add(R.drawable.ic_switch_to_digital);
        }
        icons.add(R.drawable.question_icon);
        icons.add(R.drawable.pasword_icon);
        icons.add(R.drawable.settings);
        return icons;
    }

}
