package com.zlf.appmaster.home;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.login.AboutusActivity;
import com.zlf.appmaster.login.ClientOnlineActivity;
import com.zlf.appmaster.login.FeedbackActivity;
import com.zlf.appmaster.login.InfoModifyActivity;
import com.zlf.appmaster.login.LoginActivity;
import com.zlf.appmaster.login.ProtocolActivity;
import com.zlf.appmaster.setting.SettingActivity;
import com.zlf.appmaster.ui.CommonSettingItem;
import com.zlf.appmaster.update.UpdateActivity;
import com.zlf.appmaster.utils.PrefConst;
import com.zlf.appmaster.zhibo.VideoZhiBoActivity;

/**
 * Created by Administrator on 2016/7/19.
 */
public class PersonalFragment extends BaseFragment implements View.OnClickListener {

    private RelativeLayout mLogin;

    private CommonSettingItem mModify;
    private CommonSettingItem mFeedback;
    private CommonSettingItem mClient;
//    private CommonSettingItem mAbout;
//    private CommonSettingItem mRule;
    private CommonSettingItem mSetting;
    private CommonSettingItem mUpdate;

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
//        mAbout = (CommonSettingItem) findViewById(R.id.about);
//        mRule = (CommonSettingItem) findViewById(R.id.rule);
        mSetting = (CommonSettingItem) findViewById(R.id.setting);
        mFeedback = (CommonSettingItem) findViewById(R.id.feedback);
        mUpdate = (CommonSettingItem) findViewById(R.id.update);
        mClient = (CommonSettingItem) findViewById(R.id.client);

        mClient.setIcon(R.drawable.mxxxx_icon_about);
        mFeedback.setIcon(R.drawable.menu_feedbacks_icon);
//        mAbout.setIcon(R.drawable.menu_about_icon);
        mModify.setIcon(R.drawable.ic_mine_wdzh);
//        mRule.setIcon(R.drawable.ic_mine_xx);
        mSetting.setIcon(R.drawable.ic_mine_sz);
        mUpdate.setIcon(R.drawable.icon_update);


        mClient.setTitle(mActivity.getString(R.string.client_online));
        mModify.setTitle(mActivity.getString(R.string.personal_modify));
        mFeedback.setTitle(mActivity.getString(R.string.fb_toolbar));
//        mAbout.setTitle(mActivity.getString(R.string.personal_about));
//        mRule.setTitle(mActivity.getString(R.string.personal_use));
        mSetting.setTitle(mActivity.getString(R.string.personal_setting));
        mUpdate.setTitle(mActivity.getString(R.string.check_update));

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
//        mAbout.setRippleViewOnClickLinstener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mActivity, AboutusActivity.class);
//                startActivity(intent);
//            }
//        });
        mUpdate.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUpdate();
            }
        });
//        mRule.setRippleViewOnClickLinstener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mActivity, ProtocolActivity.class);
//                startActivity(intent);
//            }
//        });
        mSetting.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(mActivity, SettingActivity.class));
                startActivity(new Intent(mActivity, VideoZhiBoActivity.class));
            }
        });
        mClient.setRippleViewOnClickLinstener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, ClientOnlineActivity.class));
            }
        });
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
            mLogin.setEnabled(false);
        } else {
            mClickLogin.setText(getResources().getString(R.string.click_login));
            mLoginIv.setVisibility(View.VISIBLE);
            mModify.setVisibility(View.GONE);
            mLogin.setEnabled(true);
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

    private void checkUpdate() {
        Intent intent = new Intent(mActivity, UpdateActivity.class);
        intent.putExtra(UpdateActivity.UPDATETYPE,UpdateActivity.CHECK_UPDATE);
        startActivity(intent);
    }
}
