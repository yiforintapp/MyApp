package com.leo.appmaster.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.CommonEvent;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.PrefConst;
import com.leo.tools.animator.ObjectAnimator;


public class GuideFragment extends Fragment implements View.OnClickListener {
    private static boolean HOME_GUIDE_SHOW_STATUS = false;

    private static final long HOME_GUIDE_ANIM_TIME = 500;
    private static final int HOME_GUIDE_REPEAT_COUNT = -1;
    private static final float HOME_GUIDE_TRANSLA_VALUE1 = 0.0f;
    private static final String ANIME_PROPERTY_NAME = "translationY";
    public static final String EVENT_HOME_GUIDE_MSG = "HOME_GUIDE_MSG";
    public static final String EVETN_HOME_GUIDE_GONE = "HOME_GUIDE_GONE";

    private View mRootView;
    private RelativeLayout mHomeGuideRt;
    private RelativeLayout mPicVideoGuideRt;
    private RelativeLayout mVideoGuideRt;
    private RelativeLayout mBatteryGuideRt;
    private ObjectAnimator mHomeGuideAnim;
    private TextView mVideoText;
    private TextView mPicText;
    private TextView mBatteryText;
    private ObjectAnimator mBatteryGuideAnim;
    private LinearLayout mUninstallLayout;  // 卸载引导布局
    private View mTopView;
    private View mLightView; // 高亮View
    private View mRightView;


    /*引导类型*/
    public enum GUIDE_TYPE {
        HOME_MORE_GUIDE, PIC_GUIDE, VIDEO_GUIDE, BATTERY_GUIDE, UNINSTALL_GUIDE
    }

    public static GuideFragment newInstance() {
        GuideFragment fragment = new GuideFragment();
        return fragment;
    }

    public GuideFragment() {

    }

    public static boolean isHomeGuideShowStatus() {
        return HOME_GUIDE_SHOW_STATUS;
    }

    public static void setHomeGuideShowStatus(boolean homeGuideShowStatus) {
        HOME_GUIDE_SHOW_STATUS = homeGuideShowStatus;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_guide, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView = view.findViewById(R.id.guide_ft);
        initUI();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mHomeGuideAnim != null) {
            if (mHomeGuideAnim.isRunning()) {
                mHomeGuideAnim.cancel();
            }
            mHomeGuideAnim = null;
        }
        LeoEventBus.getDefaultBus().unregister(this);
    }


    private void initUI() {
        if (mRootView != null) {
            initHomeMoreGuide(mRootView);
            initPicVidEditGuide(mRootView);
            initVideoEditGuide(mRootView);
            initBatteryGuide(mRootView);
            initUninstallGuide(mRootView);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHomeGuideAnim != null) {
            if (mHomeGuideAnim.isRunning()) {
                mHomeGuideAnim.cancel();
            }
            mHomeGuideAnim = null;
        }
    }

    /**
     * PG3.6版本改为“更多”按钮提示
     * 原：首次处理完图片，视频返回首页引导
     */
    private void initHomeMoreGuide(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.home_more_guide);
        if (viewStub == null) {
            return;
        }
        View inClude = viewStub.inflate();
        mHomeGuideRt = (RelativeLayout) inClude.findViewById(R.id.pic_vid_home_rt);
        mHomeGuideRt.setOnClickListener(this);
    }

    /*图片隐藏页编辑按钮引导*/
    private void initPicVidEditGuide(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.pic_vid_edit_guide);
        if (viewStub == null) {
            return;
        }
        View inCloude = viewStub.inflate();
        mPicVideoGuideRt = (RelativeLayout) inCloude.findViewById(R.id.pic_vid_edit_rt);
        mPicVideoGuideRt.setOnClickListener(this);
        mPicText = (TextView) inCloude.findViewById(R.id.guide_text_tip);
    }

    /*视频隐藏页编辑按钮引导*/
    private void initVideoEditGuide(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.video_edit_guide);
        if (viewStub == null) {
            return;
        }
        View inCloude = viewStub.inflate();
        mVideoGuideRt = (RelativeLayout) inCloude.findViewById(R.id.video_edit_rt);
        mVideoGuideRt.setOnClickListener(this);
        mVideoText = (TextView) inCloude.findViewById(R.id.video_guide_text_tip);
    }

    /*屏幕保护进入设置引导*/
    private void initBatteryGuide(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.battery_protect_guide);
        if (viewStub == null) {
            return;
        }
        View inCloude = viewStub.inflate();
        mBatteryGuideRt = (RelativeLayout) inCloude.findViewById(R.id.bay_edit_rt);
        // mBatteryGuideRt.setOnClickListener(this);
        mBatteryGuideRt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mRootView.setVisibility(View.GONE);
                return false;
            }
        });
        mBatteryText = (TextView) inCloude.findViewById(R.id.bay_edit_guide_text_tip);
    }

    private void initUninstallGuide(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.uninstall_guide_layout);
        if (viewStub == null) {
            return;
        }
        View inCloude = viewStub.inflate();
        mUninstallLayout = (LinearLayout) inCloude.findViewById(R.id.uninstall_guide);
        mUninstallLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mRootView.setVisibility(View.GONE);
                return false;
            }
        });
        mTopView = (View) inCloude.findViewById(R.id.empty_view);
        mLightView = (View) inCloude.findViewById(R.id.light_view);
        mRightView = (View) inCloude.findViewById(R.id.right_view);

    }

    // 设置卸载引导布局属性
    public void setUninstallParams(int topHeight, int height) {
        if (mTopView == null || mLightView == null || mRightView == null) {
            return;
        }
        ViewGroup.LayoutParams topParams = mTopView.getLayoutParams();
        topParams.height = topHeight;
        mTopView.setLayoutParams(topParams);

        ViewGroup.LayoutParams lightParams = mLightView.getLayoutParams();
        lightParams.height = height;
        mLightView.setLayoutParams(lightParams);

        ViewGroup.LayoutParams rightParams = mRightView.getLayoutParams();
        rightParams.height = height;
        mRightView.setLayoutParams(rightParams);

    }

    public void setEnable(boolean enable, GUIDE_TYPE type) {
        if (enable) {
            mRootView.setVisibility(View.VISIBLE);
            setVisiblityGuide(type);
        } else {
            if (mRootView.getVisibility() != View.GONE) {
                mRootView.setVisibility(View.GONE);
            }
            if (mHomeGuideAnim != null) {
                if (mHomeGuideAnim.isRunning()) {
                    mHomeGuideAnim.cancel();
                }
                mHomeGuideRt.clearAnimation();
                mHomeGuideAnim = null;
            }
            if (mBatteryGuideAnim != null) {
                if (mBatteryGuideAnim.isRunning()) {
                    mBatteryGuideAnim.cancel();
                }
                mBatteryGuideAnim = null;
            }
        }
    }

    private void setVisiblityGuide(GUIDE_TYPE type) {
        if (type == null) {
            return;
        }
        if (GUIDE_TYPE.HOME_MORE_GUIDE == type) {
            if (mHomeGuideRt != null) {

                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "list_bub");

                mHomeGuideRt.setVisibility(View.VISIBLE);
                float tranY = getActivity().getResources().getDimension(R.dimen.home_guide_trans_y);
                showHomeGuideAnim(mHomeGuideRt, HOME_GUIDE_TRANSLA_VALUE1, -tranY);
            }
        } else if (GUIDE_TYPE.PIC_GUIDE == type) {
            if (mPicVideoGuideRt != null) {

                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidpic_bub");

                mPicVideoGuideRt.setVisibility(View.VISIBLE);
                mPicText.setText(R.string.pic_video_guide_txt);
                float tranY = getActivity().getResources().getDimension(R.dimen.home_guide_trans_y);
                showHomeGuideAnim(mPicVideoGuideRt, HOME_GUIDE_TRANSLA_VALUE1, tranY);
            }

        } else if (GUIDE_TYPE.VIDEO_GUIDE == type) {
            if (mVideoGuideRt != null) {

                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidvid_bub");

                mVideoGuideRt.setVisibility(View.VISIBLE);
                mVideoText.setText(R.string.video_guide_txt);
                float tranY = getActivity().getResources().getDimension(R.dimen.home_guide_trans_y);
                showHomeGuideAnim(mVideoGuideRt, HOME_GUIDE_TRANSLA_VALUE1, tranY);
            }
        } else if (GUIDE_TYPE.BATTERY_GUIDE == type) {
            if (mBatteryGuideRt != null) {
                mBatteryGuideRt.setVisibility(View.VISIBLE);
                mBatteryText.setText(R.string.batteryview_pop_setting);
                float tranY = getActivity().getResources().getDimension(R.dimen.home_guide_trans_y);
                showBatteryGudeAnim(mBatteryGuideRt, mBatteryGuideRt.getTop(), tranY);
            }
        } else if (GUIDE_TYPE.UNINSTALL_GUIDE == type) {
            if (mUninstallLayout != null) {
                mUninstallLayout.setVisibility(View.VISIBLE);
            }
        }

    }

    private void showBatteryGudeAnim(RelativeLayout view, int top, float tranY) {
        if (view != null) {
            view.clearAnimation();
            if (mBatteryGuideAnim != null) {
                if (mBatteryGuideAnim.isRunning()) {
                    mBatteryGuideAnim.cancel();
                }
                mBatteryGuideAnim = null;
            }
            mBatteryGuideAnim = ObjectAnimator.ofFloat(mBatteryGuideRt, ANIME_PROPERTY_NAME, top, tranY);
            mBatteryGuideAnim.setFloatValues();
            mBatteryGuideAnim.setRepeatCount(HOME_GUIDE_REPEAT_COUNT);
            mBatteryGuideAnim.setRepeatMode(ObjectAnimator.REVERSE);
            mBatteryGuideAnim.setDuration(HOME_GUIDE_ANIM_TIME);

            mBatteryGuideAnim.start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pic_vid_home_rt:
                mRootView.setVisibility(View.GONE);
//                int id = EventId.EVENT_HOME_GUIDE_UP_ARROW;
//                String msg = EVENT_HOME_GUIDE_MSG;
//                CommonEvent event = new CommonEvent(id, msg);
//                LeoEventBus.getDefaultBus().post(event);
//                GuideFragment.HOME_GUIDE_SHOW_STATUS = false;
                LeoSettings.setBoolean(PrefConst.KEY_HOME_MORE_TIP, true);
                break;
            case R.id.pic_vid_edit_rt:
                mRootView.setVisibility(View.GONE);
                LeoPreference pre = LeoPreference.getInstance();
                pre.putBoolean(PrefConst.KEY_PIC_EDIT_GUIDE, true);

                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidpic_bub_cnts");
                break;
            case R.id.video_edit_rt:
                mRootView.setVisibility(View.GONE);
                LeoPreference preTab = LeoPreference.getInstance();
                preTab.putBoolean(PrefConst.KEY_VIDEO_EDIT_GUIDE, true);

                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidvid_bub_cnts");
                break;
            case R.id.bay_edit_rt:
                mRootView.setVisibility(View.GONE);
                break;
            default:
                break;
        }

    }

    /*首页引导动画*/
    private void showHomeGuideAnim(View view, float value1, float value2) {
        if (view != null) {
            view.clearAnimation();
            if (mHomeGuideAnim != null) {
                if (mHomeGuideAnim.isRunning()) {
                    mHomeGuideAnim.cancel();
                }
                mHomeGuideAnim = null;
            }
            mHomeGuideAnim = ObjectAnimator.ofFloat(view, ANIME_PROPERTY_NAME, value1, value2);
            mHomeGuideAnim.setFloatValues();
            mHomeGuideAnim.setRepeatCount(HOME_GUIDE_REPEAT_COUNT);
            mHomeGuideAnim.setRepeatMode(ObjectAnimator.REVERSE);
            mHomeGuideAnim.setDuration(HOME_GUIDE_ANIM_TIME);

            mHomeGuideAnim.start();
        }
    }

    public void onEventMainThread(CommonEvent event) {
        String msg = event.eventMsg;
        if (GuideFragment.EVETN_HOME_GUIDE_GONE.equals(msg)) {
            if (mRootView != null) {
                if (mRootView.getVisibility() == View.GONE) {
                    return;
                }
                mRootView.setVisibility(View.GONE);
            }
        }
    }

}
