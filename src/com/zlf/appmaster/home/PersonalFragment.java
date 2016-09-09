package com.zlf.appmaster.home;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlf.appmaster.R;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.login.LoginActivity;
import com.zlf.appmaster.ui.CommonSettingItem;
import com.zlf.appmaster.utils.PrefConst;

/**
 * Created by Administrator on 2016/7/19.
 */
public class PersonalFragment extends BaseFragment implements View.OnClickListener {

    private RelativeLayout mLogin;
    private CommonSettingItem mUser;
    private CommonSettingItem mVip;
    private CommonSettingItem mClient;
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
        mUser = (CommonSettingItem) findViewById(R.id.user);
        mVip = (CommonSettingItem) findViewById(R.id.vip);
        mClient = (CommonSettingItem) findViewById(R.id.client);
        mMessage = (CommonSettingItem) findViewById(R.id.message);
        mSetting = (CommonSettingItem) findViewById(R.id.setting);
        mUser.setIcon(R.drawable.ic_mine_wdzh);
        mVip.setIcon(R.drawable.ic_mine_hydj);
        mClient.setIcon(R.drawable.ic_mine_zxkf);
        mMessage.setIcon(R.drawable.ic_mine_xx);
        mSetting.setIcon(R.drawable.ic_mine_sz);
        mUser.setTitle("我的账户");
        mVip.setTitle("会员等级");
        mClient.setTitle("在线客服");
        mMessage.setTitle("消息");
        mSetting.setTitle("设置");
        View line_one = mClient.findViewById(R.id.line_2);
        line_one.setVisibility(View.VISIBLE);
        View line_two = mSetting.findViewById(R.id.line_2);
        line_two.setVisibility(View.VISIBLE);

        setListener();
    }

    private void setListener() {
        mLogin.setOnClickListener(this);
        mUser.setOnClickListener(this);
        mVip.setOnClickListener(this);
        mClient.setOnClickListener(this);
        mMessage.setOnClickListener(this);
        mSetting.setOnClickListener(this);
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
        } else {
            mClickLogin.setText(getResources().getString(R.string.click_login));
            mLoginIv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                Intent intent = new Intent(mActivity, LoginActivity.class);
                mActivity.startActivity(intent);
                break;
            case R.id.user:
                Toast.makeText(mActivity, "aaaaa", Toast.LENGTH_SHORT).show();
                break;
            case R.id.vip:
                break;
            case R.id.client:
                break;
            case R.id.message:
                break;
            case R.id.setting:
                break;
        }
    }
}
