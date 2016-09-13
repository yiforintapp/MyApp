package com.zlf.appmaster.home;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.login.FeedbackActivity;
import com.zlf.appmaster.login.InfoModifyActivity;
import com.zlf.appmaster.login.LoginActivity;
import com.zlf.appmaster.ui.CommonSettingItem;
import com.zlf.appmaster.utils.PrefConst;

/**
 * Created by Administrator on 2016/7/19.
 */
public class PersonalFragment extends BaseFragment implements View.OnClickListener {

    private RelativeLayout mLogin;
    private CommonSettingItem mModify;
    private CommonSettingItem mFeedback;
    private CommonSettingItem mMessage;
    private CommonSettingItem mSetting;
    private TextView mClickLogin;
    private RelativeLayout mLoginIv;
    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_personal;
    }

    @Override
    protected void onInitUI() {
        mLogin = (RelativeLayout) findViewById(R.id.login);
        mClickLogin = (TextView) mLogin.findViewById(R.id.tv_title);
        mLoginIv = (RelativeLayout) mLogin.findViewById(R.id.rl_content_tip);
        mModify = (CommonSettingItem) findViewById(R.id.modify);
        mMessage = (CommonSettingItem) findViewById(R.id.message);
        mSetting = (CommonSettingItem) findViewById(R.id.setting);
        mFeedback = (CommonSettingItem) findViewById(R.id.feedback);

        mFeedback.setIcon(R.drawable.menu_feedbacks_icon);
        mModify.setIcon(R.drawable.ic_mine_wdzh);
        mMessage.setIcon(R.drawable.ic_mine_xx);
        mSetting.setIcon(R.drawable.ic_mine_sz);

        mFeedback.setTitle(mActivity.getString(R.string.fb_toolbar));
        mModify.setTitle("修改资料");
        mMessage.setTitle("消息");
        mSetting.setTitle("设置");
        View line_two = mSetting.findViewById(R.id.line_2);
        line_two.setVisibility(View.VISIBLE);

        setListener();
    }

    private void setListener() {
        mLogin.setOnClickListener(this);
        mModify.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, InfoModifyActivity.class);
                mActivity.startActivity(intent);
            }
        });
        mFeedback.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, FeedbackActivity.class);
                startActivity(intent);
            }
        });
        mMessage.setRippleViewOnClickLinstener(this);
        mSetting.setRippleViewOnClickLinstener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        isLogin();
    }

    private void isLogin() {
        String userName = LeoSettings.getString(PrefConst.USER_NAME, "");
        if (!TextUtils.isEmpty(userName)) {
            mClickLogin.setText(userName);
            mLoginIv.setVisibility(View.GONE);
            mModify.setVisibility(View.VISIBLE);
            mLogin.setOnClickListener(null);
        } else {
            mClickLogin.setText(getResources().getString(R.string.click_login));
            mLoginIv.setVisibility(View.VISIBLE);
            mModify.setVisibility(View.GONE);
            mLogin.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.login:
                intent = new Intent(mActivity, LoginActivity.class);
                mActivity.startActivity(intent);
                break;
        }
    }
}
