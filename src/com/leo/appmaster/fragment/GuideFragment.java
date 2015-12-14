package com.leo.appmaster.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.eventbus.event.CommonEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.home.HomeActivity;
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
    private ObjectAnimator mHomeGuideAnim;
    private TextView mVideoText;
    private TextView mPicText;

    /*引导类型*/
    public enum GUIDE_TYPE {
        HOME_MORE_GUIDE, PIC_GUIDE, VIDEO_GUIDE
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

    /*首次处理完图片，视频返回首页引导*/
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
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pic_vid_home_rt:
                mRootView.setVisibility(View.GONE);
                int id = EventId.EVENT_HOME_GUIDE_UP_ARROW;
                String msg = EVENT_HOME_GUIDE_MSG;
                CommonEvent event = new CommonEvent(id, msg);
                LeoEventBus.getDefaultBus().post(event);
                GuideFragment.HOME_GUIDE_SHOW_STATUS = false;
                break;
            case R.id.pic_vid_edit_rt:
                mRootView.setVisibility(View.GONE);
                PreferenceTable pre = PreferenceTable.getInstance();
                pre.putBoolean(PrefConst.KEY_PIC_EDIT_GUIDE, true);

                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidpic_bub_cnts");
                break;
            case R.id.video_edit_rt:
                mRootView.setVisibility(View.GONE);
                PreferenceTable preTab = PreferenceTable.getInstance();
                preTab.putBoolean(PrefConst.KEY_VIDEO_EDIT_GUIDE, true);

                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidvid_bub_cnts");
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
