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
    private RippleView mWordRippleLayout;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_zhibo;
    }

    @Override
    protected void onInitUI() {
        mRippleLayout = (RippleView) findViewById(R.id.zhibo_layout);
        mRippleLayout.setOnClickListener(this);
        mWordRippleLayout = (RippleView) findViewById(R.id.word_zhibo_layout);
        mWordRippleLayout.setOnClickListener(this);
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
            case R.id.word_zhibo_layout:
                intent = new Intent(mActivity, WordLiveActivity.class);
                startActivity(intent);
                break;
        }
    }
}
