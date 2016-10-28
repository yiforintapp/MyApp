package com.zlf.appmaster.home;

import android.content.Intent;
import android.view.View;

import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.zhibo.LiveViewActivity;
import com.zlf.appmaster.login.LoginActivity;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.AppUtil;

/**
 * Created by Administrator on 2016/10/26.
 */
public class ZhiBoFragment extends BaseFragment implements View.OnClickListener {

    private RippleView mRippleLayout;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_zhibo;
    }

    @Override
    protected void onInitUI() {
        mRippleLayout = (RippleView) findViewById(R.id.zhibo_layout);
        mRippleLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zhibo_layout:
                Intent intent;
                if (AppUtil.isLogin()) {
                    intent = new Intent(mActivity, LiveViewActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(mActivity, LoginActivity.class);
                    intent.putExtra(LoginActivity.FROM_LIVE_BTN, true);
                    startActivity(intent);
                }
                break;
        }
    }
}
