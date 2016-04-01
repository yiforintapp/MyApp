package com.leo.appmaster.home;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;

public class HomeDetectFragment extends Fragment implements View.OnClickListener {
    private static final int SAFT_LEVEL = 0;
    private static final int DANGER_LEVEL = 1;
    private static final long FIR_IN_ANIM_TIME = 1000;

    private Context mContext;
    private RelativeLayout mSfatResultAppLt;
    private RelativeLayout mSfatResultImgLt;
    private RelativeLayout mSfatResultVideoLt;
    private RelativeLayout mDangerResultAppLt;
    private RelativeLayout mDangerResultImgLt;
    private RelativeLayout mDangerResultVideoLt;
    private RelativeLayout mCenterTipRt;
    private ImageView mShieldTopIv;
    private ImageView mShieldRightIv;
    private ImageView mShieldLeftIv;
    private ImageView mShieldCenterIv;
    private AnimatorSet mFirstInAnim;
    //应用检测结果view
    private TextView mDetSaftAppNumTv;
    private TextView mDetSaftAppTv;
    private TextView mDetDagAppNumTv;
    private TextView mDetDagAppTv;
    //图片检测结果view
    private TextView mDetSaftImgNumTv;
    private TextView mDetSaftImgTv;
    private TextView mDetDagImgNumTv;
    private TextView mDetDagImgTv;
    //视频检测结果view
    private TextView mDetSaftVideoNumTv;
    private TextView mDetSaftVideoTv;
    private TextView mDetDagVideoNumTv;
    private TextView mDetDagVideoTv;
    private HomeDetectPresenter mDetectPresenter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity();
        mDetectPresenter = new HomeDetectPresenter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_detect, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDetectPresenter.attachView(this);
        initUI(view);
        initAnim();
        //测试
        startFirstAnim();
        startHomeCenterShieldAnim();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initUI(View view) {
        RelativeLayout resultRootView = (RelativeLayout) view.findViewById(R.id.det_result_ly);

        mSfatResultAppLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_saft_result_app);
        mSfatResultImgLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_saft_result_img);
        mSfatResultVideoLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_saft_result_video);

        mDangerResultAppLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_danger_result_app);
        mDangerResultImgLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_danger_result_img);
        mDangerResultVideoLt = (RelativeLayout) resultRootView.findViewById(R.id.lt_det_danger_result_video);

       /* mSfatResultAppLt.setVisibility(View.INVISIBLE);
        mSfatResultImgLt.setVisibility(View.INVISIBLE);
        mSfatResultVideoLt.setVisibility(View.INVISIBLE);

        mDangerResultAppLt.setVisibility(View.VISIBLE);
        mDangerResultImgLt.setVisibility(View.VISIBLE);
        mDangerResultVideoLt.setVisibility(View.VISIBLE);*/


        mCenterTipRt = (RelativeLayout) view.findViewById(R.id.lt_home_det_tip);
        mSfatResultAppLt.setOnClickListener(this);
        mSfatResultImgLt.setOnClickListener(this);
        mSfatResultVideoLt.setOnClickListener(this);

        mDangerResultAppLt.setOnClickListener(this);
        mDangerResultImgLt.setOnClickListener(this);
        mDangerResultVideoLt.setOnClickListener(this);

        mCenterTipRt.setOnClickListener(this);
        mShieldLeftIv = (ImageView) view.findViewById(R.id.shield_left_iv);
        mShieldRightIv = (ImageView) view.findViewById(R.id.shield_right_iv);
        mShieldTopIv = (ImageView) view.findViewById(R.id.shield_top_iv);
        mShieldCenterIv = (ImageView) view.findViewById(R.id.shield_center_iv);
        //初始化应用检测结果
        mDetSaftAppNumTv = (TextView) resultRootView.findViewById(R.id.det_saft_app_num_tv);
        mDetSaftAppTv = (TextView) resultRootView.findViewById(R.id.det_saft_app_tv);
        mDetDagAppNumTv = (TextView) resultRootView.findViewById(R.id.det_danger_app_num_tv);
        mDetDagAppTv = (TextView) resultRootView.findViewById(R.id.det_danger_app_tv);
        //初始化图片检测结果
        mDetSaftImgNumTv = (TextView) resultRootView.findViewById(R.id.det_saft_img_num_tv);
        mDetSaftImgTv = (TextView) resultRootView.findViewById(R.id.det_saft_img_tv);
        mDetDagImgNumTv = (TextView) resultRootView.findViewById(R.id.det_danger_img_num_tv);
        mDetDagImgTv = (TextView) resultRootView.findViewById(R.id.det_danger_img_tv);
        //初始化图片检测结果
        mDetSaftVideoNumTv = (TextView) resultRootView.findViewById(R.id.det_saft_video_num_tv);
        mDetSaftVideoTv = (TextView) resultRootView.findViewById(R.id.det_saft_video_tv);
        mDetDagVideoNumTv = (TextView) resultRootView.findViewById(R.id.det_danger_video_num_tv);
        mDetDagVideoTv = (TextView) resultRootView.findViewById(R.id.det_danger_video_tv);

        setSfateShieldView();
    }


    private void initAnim() {
        initHomeTopShieldAnim();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDetectPresenter.detachView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lt_det_saft_result_app:
                //应用扫描安全结果
                mDetectPresenter.appSaftHandler();
                break;
            case R.id.lt_det_saft_result_img:
                //图片扫描安全结果
                mDetectPresenter.imageSaftHandler();
                break;
            case R.id.lt_det_saft_result_video:
                //视频扫描安全结果
                mDetectPresenter.videoSaftHandler();
                break;
            case R.id.lt_det_danger_result_app:
                //应用扫描危险结果
                mDetectPresenter.appDangerHandler();
                break;
            case R.id.lt_det_danger_result_img:
                //图片扫描危险结果
                mDetectPresenter.imageDangerHandler();
                break;
            case R.id.lt_det_danger_result_video:
                //视频扫描危险结果
                mDetectPresenter.videoDangerHandler();
                break;
            case R.id.lt_home_det_tip:
                //中间banner
                mDetectPresenter.centerBannerHandler();
                break;
            default:
                break;
        }
    }

    //盾牌位置初始化
    private void shieldPositionInit(int level) {
        Context context = getActivity();
        mShieldTopIv.setTranslationY(-DipPixelUtil.dip2px(context, getResources().getInteger(R.integer.shield_top_offset)));
        mShieldLeftIv.setTranslationX(-DipPixelUtil.dip2px(context, getResources().getInteger(R.integer.shield_left_offset)));
        mShieldRightIv.setTranslationX(DipPixelUtil.dip2px(context, getResources().getInteger(R.integer.shield_right_offset)));
        if (level == SAFT_LEVEL) {
            mShieldCenterIv.setTranslationX(DipPixelUtil.dip2px(context, getResources().getInteger(R.integer.shield_center_blue_offset)));
        } else if (level == DANGER_LEVEL) {
            mShieldCenterIv.setTranslationX(DipPixelUtil.dip2px(context, getResources().getInteger(R.integer.shield_center_red_offset)));
        }

    }

    //危险盾牌设置
    public void setDangerShieldView() {
        shieldPositionInit(DANGER_LEVEL);
        mShieldTopIv.setImageResource(R.drawable.shield_red_top);
        mShieldLeftIv.setImageResource(R.drawable.shield_red_left);
        mShieldRightIv.setImageResource(R.drawable.shield_red_right);
        mShieldCenterIv.setImageResource(R.drawable.shield_bad);
    }

    //安全盾牌设置
    public void setSfateShieldView() {
        shieldPositionInit(SAFT_LEVEL);
        mShieldTopIv.setImageResource(R.drawable.shield_blue_top);
        mShieldLeftIv.setImageResource(R.drawable.shield_blue_left);
        mShieldRightIv.setImageResource(R.drawable.shield_blue_right);
        mShieldCenterIv.setImageResource(R.drawable.shield_good);
    }

    //首次进入主页盾牌动画
    public void initHomeTopShieldAnim() {
        int value2 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_top_offset));
        int value1 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_top_y_trans)) + value2;
        PropertyValuesHolder topTransY = PropertyValuesHolder.ofFloat("translationY", value1, value2);
        PropertyValuesHolder topAlpha = PropertyValuesHolder.ofFloat("alpha", 0, 1);
        ObjectAnimator topAnim = ObjectAnimator.ofPropertyValuesHolder(mShieldTopIv, topTransY, topAlpha);

        int valueLeft2 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_left_offset));
        int valueLeft1 = -DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_left_x_trans)) + value2;
        PropertyValuesHolder leftTransY = PropertyValuesHolder.ofFloat("translationX", valueLeft1, valueLeft2);
        PropertyValuesHolder leftAlpha = PropertyValuesHolder.ofFloat("alpha", 0, 1);
        ObjectAnimator leftAnim = ObjectAnimator.ofPropertyValuesHolder(mShieldLeftIv, leftTransY, leftAlpha);

        int valueRight2 = DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_left_offset));
        int valueRight1 = DipPixelUtil.dip2px(mContext, getResources().getInteger(R.integer.shield_right_x_trans)) + value2;

        PropertyValuesHolder rightTransY = PropertyValuesHolder.ofFloat("translationX", valueRight1, valueRight2);
        PropertyValuesHolder rightAlpha = PropertyValuesHolder.ofFloat("alpha", 0, 1);
        ObjectAnimator rightAnim = ObjectAnimator.ofPropertyValuesHolder(mShieldRightIv, rightTransY, rightAlpha);

        if (mFirstInAnim == null) {
            mFirstInAnim = new AnimatorSet();
        } else {
            mFirstInAnim.cancel();
        }
        mFirstInAnim.playTogether(topAnim, leftAnim, rightAnim);
        mFirstInAnim.setDuration(FIR_IN_ANIM_TIME);

    }

    //启动首次进入动画
    public void startFirstAnim() {
        if (mFirstInAnim == null) {
            return;
        }
        mFirstInAnim.start();
    }

    //结束首次动画
    public void cancelFirstAnim() {
        if (mFirstInAnim == null) {
            return;
        }
        mFirstInAnim.cancel();
    }

    //首次进入主页Center盾牌动画
    public void startHomeCenterShieldAnim() {
        PropertyValuesHolder centerScaleX = PropertyValuesHolder.ofFloat("scaleX", (float) 0.6, (float) 1.06);
        PropertyValuesHolder centerScaleY = PropertyValuesHolder.ofFloat("scaleY", (float) 0.6, (float) 1.06);

        ObjectAnimator centerAnim = ObjectAnimator.ofPropertyValuesHolder(mShieldCenterIv, centerScaleX, centerScaleY);
        centerAnim.setDuration(FIR_IN_ANIM_TIME);
        centerAnim.setStartDelay(FIR_IN_ANIM_TIME);
        centerAnim.start();
    }

    //扫描结果处理后切换动画
    public void detectResultConversionAnim(View up1, View up2, View down1, View down2, Animator.AnimatorListener listener) {

        ObjectAnimator transUp1 = ObjectAnimator.ofFloat(up1, "translationY", -100, 0);
        ObjectAnimator transUp2 = ObjectAnimator.ofFloat(up2, "translationY", -100, 0);

        ObjectAnimator transDown1 = ObjectAnimator.ofFloat(up1, "translationY", 0, 100);
        ObjectAnimator transDown2 = ObjectAnimator.ofFloat(up2, "translationY", 0, 100);

        AnimatorSet set = new AnimatorSet();
        if (listener != null) {
            set.addListener(listener);
        }
        set.playTogether(transUp1, transUp2, transDown1, transDown2);
        set.setDuration(200);
        set.start();
    }
}
