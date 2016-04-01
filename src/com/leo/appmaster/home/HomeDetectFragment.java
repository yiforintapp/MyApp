package com.leo.appmaster.home;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.Property;
import com.leo.tools.animator.PropertyValuesHolder;

public class HomeDetectFragment extends Fragment implements View.OnClickListener {
    private static final int SAFT_LEVEL = 0;
    private static final int DANGER_LEVEL = 1;
    private static final long FIR_IN_ANIM_TIME = 1000;

    private Context mContext;
    private LinearLayout mResultAppLt;
    private LinearLayout mResultImgLt;
    private LinearLayout mResultVideoLt;
    private RelativeLayout mCenterTipRt;
    private ImageView mShieldTopIv;
    private ImageView mShieldRightIv;
    private ImageView mShieldLeftIv;
    private ImageView mShieldCenterIv;
    private AnimatorSet mFirstInAnim;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_detect, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        LinearLayout rootView = (LinearLayout) view.findViewById(R.id.det_result_ly);
        mResultAppLt = (LinearLayout) rootView.findViewById(R.id.lt_det_result_app);
        mResultImgLt = (LinearLayout) rootView.findViewById(R.id.lt_det_result_img);
        mResultVideoLt = (LinearLayout) rootView.findViewById(R.id.lt_det_result_video);
        mCenterTipRt = (RelativeLayout) view.findViewById(R.id.lt_home_det_tip);
        mResultAppLt.setOnClickListener(this);
        mResultImgLt.setOnClickListener(this);
        mResultVideoLt.setOnClickListener(this);
        mCenterTipRt.setOnClickListener(this);
        mShieldLeftIv = (ImageView) view.findViewById(R.id.shield_left_iv);
        mShieldRightIv = (ImageView) view.findViewById(R.id.shield_right_iv);
        mShieldTopIv = (ImageView) view.findViewById(R.id.shield_top_iv);
        mShieldCenterIv = (ImageView) view.findViewById(R.id.shield_center_iv);
        setSfateShieldView();
    }


    private void initAnim() {
        initHomeTopShieldAnim();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        Activity activity = getActivity();
        switch (v.getId()) {
            case R.id.lt_det_result_app:
                //应用扫描结果
                Toast.makeText(activity, "应用扫描结果", Toast.LENGTH_SHORT).show();
                break;
            case R.id.lt_det_result_img:
                //图片扫描结果
                Toast.makeText(activity, "图片扫描结果", Toast.LENGTH_SHORT).show();
                break;
            case R.id.lt_det_result_video:
                //视频扫描结果
                Toast.makeText(activity, "视频扫描结果", Toast.LENGTH_SHORT).show();
                break;
            case R.id.lt_home_det_tip:
                //中间banner
                Toast.makeText(activity, "中间banner", Toast.LENGTH_SHORT).show();
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
}
