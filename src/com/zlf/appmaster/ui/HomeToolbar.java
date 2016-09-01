package com.zlf.appmaster.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.msgcenter.MsgCenterActivity;

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

//    private LeoHomePopMenu mLeoPopMenu;

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

//        mMsgcenterRl = findViewById(R.id.hm_tool_msgcenter_rl);
//        mMsgcenterRl.setOnClickListener(this);


        mMenuIv = (ImageView) findViewById(R.id.hm_tool_menu_iv);
        mMenuRedTipIv = (ImageView) findViewById(R.id.hm_tool_menu_red_tip_iv);
//        mMsgCenterRedCount = (TextView) findViewById(R.id.hm_tool_mc_unread_tv);

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

                if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
                    mDrawerLayout.closeDrawer(Gravity.START);
                } else {
                    mDrawerLayout.openDrawer(Gravity.START);
                }
                break;
//            case R.id.hm_tool_msgcenter_rl:
//                // 点击启动消息中心的次数
//                Intent msgCenter = new Intent();
//                msgCenter.setClass(getContext(), MsgCenterActivity.class);
//                getContext().startActivity(msgCenter);
//                break;

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
        }
    }

//    private void initSettingMenu() {
//        if (mLeoPopMenu != null) return;
//
//        mLeoPopMenu = new LeoHomePopMenu();
//        mLeoPopMenu.setAnimation(R.style.RightEnterAnim);
//        mLeoPopMenu.setPopItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Activity activity = (Activity) getContext();
//                if (position == 0) {
//                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "changepwd");
//                    Intent intent = new Intent(activity, LockSettingActivity.class);
//                    intent.putExtra("reset_passwd", true);
//                    activity.startActivity(intent);
//                } else if (position == 1) {
//                    Intent intent = new Intent(activity, LockSettingActivity.class);
//                    intent.putExtra("reset_passwd", true);
//                    intent.putExtra("rotate_fragment", true);
//                    activity.startActivity(intent);
//                } else if (position == 2) {
//                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "mibao");
//                    Intent intent = new Intent(activity, PasswdProtectActivity.class);
//                    activity.startActivity(intent);
//                } else if (position == 3) {
//                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "passwdtip");
//                    Intent intent = new Intent(activity, PasswdTipActivity.class);
//                    activity.startActivity(intent);
//                } else if (position == 4) {
//                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "locksetting");
//                    Intent intent = new Intent(activity, PrivacyOptionActivity.class);
////                    intent.putExtra(LockOptionActivity.TAG_COME_FROM,
////                            LockOptionActivity.FROM_HOME);
//                    activity.startActivity(intent);
//                }
//                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mLeoPopMenu.dismissSnapshotList();
//                    }
//                }, 500);
//            }
//        });
//        mLeoPopMenu.setListViewDivider(null);
//    }

}
