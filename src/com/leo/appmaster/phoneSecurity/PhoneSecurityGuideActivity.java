package com.leo.appmaster.phoneSecurity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;

public class PhoneSecurityGuideActivity extends BaseActivity implements View.OnClickListener {
    private static final boolean DBG =false;
    private Button mOpenBt;
    private CommonToolbar mCommonBar;
    private TextView mSecurityOpenNumberText;
    private TextView mKnowModelClick;
    private LinearLayout mNoKnowMsmModelTip;
    private LinearLayout mKnowModelRt;
    private TextView mKnowMdContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*push掉起处理*/
        if (!isFormHome()) {
            startPhoneSecurity();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_security_guide);
        initUI();
        intentHandler();
    }

    /**
     * 跳转过来的Intent处理
     */
    private void intentHandler() {
        Intent intent = getIntent();
        String fromWhere = intent.getStringExtra(Constants.FROM_WHERE);
        if (fromWhere != null && fromWhere.equals(Constants.FROM_PUSH)) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh", "push_theft_cnts");
            LeoLog.d("testFromWhere", "PhoneSecurityGuideActivity from push");
        }
    }

    private boolean isFormHome() {
        return getIntent().getBooleanExtra(PhoneSecurityConstants.KEY_FORM_HOME_SECUR, false);
    }


    @Override
    protected void onResume() {
        showMsmPermissTipHandler();
        super.onResume();
    }

    private void initUI() {
        mOpenBt = (Button) findViewById(R.id.security_guide_BT);
        mOpenBt.setOnClickListener(this);
        mCommonBar = (CommonToolbar) findViewById(R.id.phone_security_guide_commonbar);
        mCommonBar.setToolbarTitle(R.string.home_tab_lost);
        mCommonBar.setToolbarColorResource(R.color.ctc);
        mCommonBar.setOptionMenuVisible(false);
        mSecurityOpenNumberText = (TextView) findViewById(R.id.security_guide_open_number_TV);
        int useNumber = ((LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY)).getUsePhoneSecurityCount();
        mSecurityOpenNumberText.setText(String.valueOf(useNumber));
        //已知需要手动打开短信权限机型提示UI
        mKnowModelRt = (LinearLayout) findViewById(R.id.secur_know_msm_LT);
        mKnowMdContent = (TextView) findViewById(R.id.secur_know_msm_content);
        mKnowModelClick = (TextView) findViewById(R.id.secur_know_msm_click);
        mKnowModelClick.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mKnowModelClick.setOnClickListener(this);
        //未知需要手动打开短信权限机型提示UI
        mNoKnowMsmModelTip = (LinearLayout) findViewById(R.id.secur_no_know_msm_LT);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.security_guide_BT:
                LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                boolean isExistSim = mgr.getIsExistSim();
                if (DBG) {
                    isExistSim = true;
                }
                if (isExistSim) {
                    openSecurityHandler();
                } else {
                    String noSimText = getResources().getString(R.string.no_sim_tip);
                    Toast.makeText(PhoneSecurityGuideActivity.this, noSimText, Toast.LENGTH_SHORT).show();
                }
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_enable");
                break;
            case R.id.secur_know_msm_click:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_authoriz_clk");
                //点击立即授权
                new MsmPermisGuideList().executeGuide();
                break;
            default:
                break;
        }

    }

    private void openSecurityHandler() {
        Intent intent = null;
        LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        //获取预留的防盗号码查看是否为空
        String securNum = mgr.getPhoneSecurityNumber();
        if (!TextUtils.isEmpty(securNum)) {
            /*
             *预留号码不为空说明上次有开启过防盗，本次使用上次预留号码直接开启
             */
            mgr.setUsePhoneSecurity(true);
            mgr.setOpenSecurityTime();
            intent = new Intent(this, PhoneSecurityActivity.class);
            intent.putExtra(PhoneSecurityActivity.FROM_SECUR_INTENT,PhoneSecurityActivity.FROM_SECUR_GUIDE_INTENT);
        } else {
            //首次开启
            intent = new Intent(this, AddSecurityNumberActivity.class);
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    /*短信权限提示显示处理*/
    private void showMsmPermissTipHandler() {
        /*是否为已知需要手动开启权限的机型*/
        int result = new MsmPermisGuideList().isMsmPermisListModel(this);
        boolean mIsReadMsm = PhoneSecurityManager.getInstance(this).tryReadSysMsm();
        boolean isKnowMsmPerModel = false;
        if (result != -1) {
            isKnowMsmPerModel = true;
        }
        if (isKnowMsmPerModel) {
            /*有短信权限可以点击,反之则*/
            mKnowModelRt.setVisibility(View.VISIBLE);
            mNoKnowMsmModelTip.setVisibility(View.GONE);
            String content = new MsmPermisGuideList().getModelString();
            mKnowMdContent.setText(content);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_authorizBtn");
        } else {
            mKnowModelRt.setVisibility(View.GONE);
            boolean isSamsung = BuildProperties.isSamSungModel();
            if (!isSamsung) {
                mNoKnowMsmModelTip.setVisibility(View.VISIBLE);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_authoriz");
            } else {
                mNoKnowMsmModelTip.setVisibility(View.GONE);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theft", "theft_authoriz_none");
            }
        }
    }

    /*进入手机防盗*/
    private void startPhoneSecurity() {
        LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        boolean flag = manager.isUsePhoneSecurity();
        Intent intent = null;
        if (flag) {
            finish();
            intent = new Intent(this, PhoneSecurityActivity.class);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
