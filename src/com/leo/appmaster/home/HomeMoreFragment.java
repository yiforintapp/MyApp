package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.activity.QuickHelperActivity;
import com.leo.appmaster.airsig.AirSigActivity;
import com.leo.appmaster.airsig.AirSigSettingActivity;
import com.leo.appmaster.appmanage.BackUpActivity;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.appmanage.UninstallActivity;
import com.leo.appmaster.battery.BatteryMainActivity;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.CommonEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.fragment.GuideFragment;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.phoneSecurity.PhoneSecurityActivity;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.phoneSecurity.PhoneSecurityGuideActivity;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.HomeUpArrow;
import com.leo.appmaster.ui.SlidingUpPanelLayout;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.videohide.VideoHideMainActivity;

/**
 * 首页上拉列表，各个模块入口列表
 *
 * @author Jasper
 */
public class HomeMoreFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "HomeMoreFragment";

    private static final int DEFAULT_FADE_COLOR = 0x99000000;

    private SlidingUpPanelLayout mSlidingLayout;
    private View mRootView;
    private View mTouchDelegate;
    private HomeUpArrow mUpArrow;
    private View mUpClickView;

    private HomeMoreAdapter mAdapter;

    public HomeMoreFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home_more, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUpClickView = view.findViewById(R.id.more_up_click_view);
        mUpClickView.setOnClickListener(this);
        mTouchDelegate = view.findViewById(R.id.home_more_empty);
        mTouchDelegate.setOnClickListener(this);
        mRootView = view.findViewById(R.id.home_more_root);
        final ListView lv = (ListView) view.findViewById(R.id.list);
        mAdapter = new HomeMoreAdapter();
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(this);

        mUpArrow = (HomeUpArrow) view.findViewById(R.id.more_up_arrow);
        mSlidingLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        mSlidingLayout.setTabRectFunction(mUpArrow);
        mSlidingLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
//                Log.i(TAG, "onPanelSlide, offset " + slideOffset + " | state: " + mSlidingLayout.getPanelState());
                int apha = Color.alpha(DEFAULT_FADE_COLOR);
                int r = Color.red(DEFAULT_FADE_COLOR);
                int g = Color.green(DEFAULT_FADE_COLOR);
                int b = Color.blue(DEFAULT_FADE_COLOR);
                apha *= slideOffset;
                int color = Color.argb(apha, r, g, b);
                mRootView.setBackgroundColor(color);

                mUpArrow.setBgAlpha(255);
            }

            @Override
            public void onPanelExpanded(View panel) {
                // 打开
                Log.i(TAG, "onPanelExpanded, state: " + mSlidingLayout.getPanelState());

                int id = EventId.EVENT_HOME_GUIDE_GONE_ID;
                String msg = GuideFragment.EVETN_HOME_GUIDE_GONE;
                CommonEvent event = new CommonEvent(id, msg);
                LeoEventBus.getDefaultBus().post(event);

                mTouchDelegate.setVisibility(View.VISIBLE);
                mUpArrow.reverse();
                mUpArrow.cancelUpAnimation();

                LeoPreference leoPreference = LeoPreference.getInstance();
                boolean pulledEver = leoPreference.getBoolean(PrefConst.KEY_MORE_PULLED, false);
                if (!pulledEver) {

                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "list_rp_up");

                    leoPreference.putBoolean(PrefConst.KEY_MORE_PULLED, true);
                    updateHideRedTip();
                }
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "home_listup");
                mSlidingLayout.setDragClickEnable(true);
                mUpClickView.setVisibility(View.VISIBLE);

                boolean pulled = leoPreference.getBoolean(PrefConst.KEY_MORE_PULLED, false);
                boolean picReddot = leoPreference.getBoolean(PrefConst.KEY_PIC_REDDOT_EXIST, false);
                boolean vidReddot = leoPreference.getBoolean(PrefConst.KEY_VID_REDDOT_EXIST, false);

                if (pulled) {
                    if (picReddot || vidReddot) {
                        if (picReddot) {
                            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "home_listup");
                            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidpic_rp_cnts");
                        }
                        if (vidReddot) {
                            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "home_listup");
                            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidvid_rp_cnts");
                        }
                    }
                }
                mUpArrow.setBgAlpha(255);

            }

            @Override
            public void onPanelCollapsed(View panel) {
                // 关闭
                Log.i(TAG, "onPanelCollapsed, state: " + mSlidingLayout.getPanelState());
                mRootView.setBackgroundColor(Color.TRANSPARENT);
                mTouchDelegate.setVisibility(View.INVISIBLE);

                mUpArrow.reset();
                mUpArrow.setBgAlpha(0);
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "home_listdown");
                mSlidingLayout.setDragClickEnable(false);
                mUpClickView.setVisibility(View.GONE);
            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i(TAG, "onPanelAnchored");
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.i(TAG, "onPanelHidden");
            }
        });

    }

    public void setEnable(boolean enable) {
        if (enable && mRootView.getVisibility() == View.GONE) {
            mRootView.setVisibility(View.VISIBLE);
        } else {
            mRootView.setVisibility(View.GONE);
        }
    }

    public boolean isEnable() {
        return mRootView.getVisibility() == View.VISIBLE;
    }

    public void onEventMainThread(PrivacyEditFloatEvent event) {
        updateHideRedTip();
    }

    public void updateHideRedTip() {
        AppMasterPreference preference = AppMasterPreference.getInstance(getActivity());
        int msgCount = preference.getMessageNoReadCount();
        /*4.4以上不去做短信操作*/
        boolean isLessLeve19 = PrivacyContactUtils.isLessApiLeve19();
        if (!isLessLeve19) {
            if (msgCount > 0) {
                msgCount = 0;
                preference.setMessageNoReadCount(msgCount);
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                       /*标记为已读*/
                        String selection = "message_is_read = 0";
                        PrivacyContactUtils.updateMessageMyselfIsRead(1, selection, null, getActivity());
                    }
                });
            }
        }
        int callCount = preference.getCallLogNoReadCount();

        LeoPreference leoPreference = LeoPreference.getInstance();
        boolean pulledEver = leoPreference.getBoolean(PrefConst.KEY_MORE_PULLED, false);
        boolean picReddot = leoPreference.getBoolean(PrefConst.KEY_PIC_REDDOT_EXIST, false);
        boolean vidReddot = leoPreference.getBoolean(PrefConst.KEY_VID_REDDOT_EXIST, false);
        boolean intruderReddot = leoPreference.getBoolean(PrefConst.KEY_INTRUDER_REDDOT_CONSUMED, false);
//        if (msgCount > 0 || callCount > 0 || picReddot || vidReddot || !pulledEver ||!intruderReddot) {
        if (msgCount > 0 || callCount > 0 || picReddot || vidReddot || !pulledEver) {
            if (!pulledEver) {
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "list_rp");
            } else {
                if (picReddot || vidReddot) {
                    if (picReddot) {
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidpic_rp");
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "list_rp");
                    }
                    if (vidReddot) {
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidvid_rp");
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "list_rp");
                    }
                }
            }

            mUpArrow.showRedTip(true);
//            mAdapter.notifyDataSetInvalidated();
            mAdapter.notifyDataSetChanged();
        } else {
            mUpArrow.showRedTip(false);
//            mAdapter.notifyDataSetInvalidated();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHideRedTip();

        boolean pulledEver = LeoPreference.getInstance().getBoolean(PrefConst.KEY_MORE_PULLED, false);
        if (!pulledEver) {
            mUpArrow.setCancelledFalse();
            mUpArrow.startUpAnimation();
            if (GuideFragment.isHomeGuideShowStatus()) {
                mUpArrow.cancelUpAnimation();
            }
        } else {
            mUpArrow.cancelUpAnimation();
        }

        if (isPanelOpen()) {
            mUpArrow.setBgAlpha(255);
        } else {
            mUpArrow.setBgAlpha(0);
        }
        LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        LeoEventBus.getDefaultBus().unregister(this);
    }

    public boolean isPanelOpen() {
        return mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED;
    }

    public void closePanel() {
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.more_up_click_view:
            case R.id.home_more_empty:
                closePanel();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Activity activity = getActivity();
        if (activity != null) {
            int itemId = (int) mAdapter.getItemId(position);
            Intent intent = null;
            LeoPreference table = LeoPreference.getInstance();
            switch (itemId) {
                case R.string.hp_hide_img:
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "hidpic");
//                    int count = table.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC, 0);
//                    table.putInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC, count+1);
                    table.putBoolean(PrefConst.KEY_PIC_REDDOT_EXIST, false);
                    intent = new Intent(activity, ImageHideMainActivity.class);
                    activity.startActivity(intent);
                    // 隐藏图片
                    break;
                case R.string.hp_hide_video:
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "hidvideo");
//                    int count2 = table.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_VIDEO, 0);
//                    table.putInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_VIDEO, count2 + 1);
                    table.putBoolean(PrefConst.KEY_VID_REDDOT_EXIST, false);
                    intent = new Intent(activity, VideoHideMainActivity.class);
                    activity.startActivity(intent);
                    // 隐藏视频
                    break;
                case R.string.privacy_contacts:
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "pricall");
                    intent = new Intent(activity, PrivacyContactActivity.class);
                    activity.startActivity(intent);
                    // 隐私通话
                    break;
                case R.string.hp_contact_sms:
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "primesg");
                    intent = new Intent(activity, PrivacyContactActivity.class);
                    intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                            PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
                    LeoLog.i(TAG, "scan uri: " + intent.toURI());
                    activity.startActivity(intent);
                    // 隐私短信
                    break;
                case R.string.hp_app_manage_del:
                    // 应用卸载
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "newuninstall");
                    intent = new Intent(activity, UninstallActivity.class);
                    startActivity(intent);
                    break;
                case R.string.hp_app_manage_back:
                    // 应用备份
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "backup");
                    intent = new Intent(activity, BackUpActivity.class);
                    startActivity(intent);
                    break;
                case R.string.hp_device_gprs:
                    // 流量监控
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "data");
                    intent = new Intent(activity, FlowActivity.class);
                    startActivity(intent);
                    break;
                case R.string.hp_device_power:
                    // 电量管理
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "boost", "battery");
                    Intent dlIntent = new Intent(activity, BatteryMainActivity.class);
                    startActivity(dlIntent);
                    break;
                case R.string.hp_helper_shot:
                    // 快捷小助手
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "boost", "home_shortcutsAssistant");
                    Intent qhintent = new Intent(activity, QuickHelperActivity.class);
                    startActivity(qhintent);
                    break;
                /*case R.string.game_box_one:
                    SDKWrapper.showGameBoxHome(getActivity());
                    SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "home_gameboost");
                    break;*/
                case R.string.home_tab_instruder:
                    // 手机入侵者
                    LeoLog.i(TAG, "start i");
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "home_theft");
                    intent = new Intent(getActivity(), IntruderprotectionActivity.class);
                    startActivity(intent);
                    if (table.getBoolean(PrefConst.KEY_INTRUDER_REDDOT_CONSUMED, false)) {
                        SDKWrapper.addEvent(activity, SDKWrapper.P1, "home", "intruder_rp_cnts");
                    }
                    table.putBoolean(PrefConst.KEY_INTRUDER_REDDOT_CONSUMED, true);
                    break;
            }
        }
    }

    /*进入手机防盗*/
    private void startPhoneSecurity() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            boolean flag = manager.isUsePhoneSecurity();
            Intent intent = null;
            if (!flag) {
                intent = new Intent(activity, PhoneSecurityGuideActivity.class);
                intent.putExtra(PhoneSecurityConstants.KEY_FORM_HOME_SECUR, true);
            } else {
                intent = new Intent(activity, PhoneSecurityActivity.class);
            }
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUpArrow.release();
    }

    public void cancelUpArrowAnim() {
        if (mUpArrow != null) {
            mUpArrow.cancelUpAnimation();
        }
    }

    public void onEventMainThread(CommonEvent event) {
        String msg = event.eventMsg;
        if (GuideFragment.EVENT_HOME_GUIDE_MSG.equals(msg)) {
            if (mSlidingLayout != null) {
                mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        }
    }
}
