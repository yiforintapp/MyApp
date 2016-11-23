package com.zlf.appmaster.home;

import android.content.Intent;
import android.view.View;

import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.zhibo.VideoLiveActivity;
import com.zlf.appmaster.login.LoginActivity;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.zhibo.WordLiveActivity;

/**
 * Created by Administrator on 2016/10/26.
 */
public class ZhiBoFragment extends BaseFragment implements View.OnClickListener {

    private RippleView mRippleLayout;
    private RippleView mWordRippleLayoutOne;
    private RippleView mWordRippleLayoutTwo;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_zhibo;
    }

    @Override
    protected void onInitUI() {
        mRippleLayout = (RippleView) findViewById(R.id.zhibo_layout);
        mRippleLayout.setOnClickListener(this);
        mWordRippleLayoutOne = (RippleView) findViewById(R.id.word_zhibo_layout_one);
        mWordRippleLayoutOne.setOnClickListener(this);
        mWordRippleLayoutTwo = (RippleView) findViewById(R.id.word_zhibo_layout_two);
        mWordRippleLayoutTwo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.zhibo_layout:
                if (AppUtil.isLogin()) {
                    intent = new Intent(mActivity, VideoLiveActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(mActivity, LoginActivity.class);
                    intent.putExtra(LoginActivity.FROM_LIVE_BTN, true);
                    startActivity(intent);
                }
                break;
            case R.id.word_zhibo_layout_one:
                if (AppUtil.isLogin()) {
                    intent = new Intent(mActivity, WordLiveActivity.class);
                    intent.putExtra(WordLiveActivity.ZHIBO_TYPE, WordLiveActivity.TYPE_ONE);
                    startActivity(intent);
                } else {
                    intent = new Intent(mActivity, LoginActivity.class);
                    intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN, true);
                    intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN_TYPE, WordLiveActivity.TYPE_ONE);
                    startActivity(intent);
                }
                break;
            case R.id.word_zhibo_layout_two:
                if (AppUtil.isLogin()) {
                    intent = new Intent(mActivity, WordLiveActivity.class);
                    intent.putExtra(WordLiveActivity.ZHIBO_TYPE, WordLiveActivity.TYPE_TWO);
                    startActivity(intent);
                } else {
                    intent = new Intent(mActivity, LoginActivity.class);
                    intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN, true);
                    intent.putExtra(LoginActivity.FROM_WORD_LIVE_BTN_TYPE, WordLiveActivity.TYPE_TWO);
                    startActivity(intent);
                }
                break;
        }
    }
}
