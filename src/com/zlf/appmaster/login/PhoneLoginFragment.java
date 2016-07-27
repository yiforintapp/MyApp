package com.zlf.appmaster.login;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;

/**
 * Created by Administrator on 2016/7/19.
 */
public class PhoneLoginFragment extends BaseFragment implements View.OnClickListener {

    private Button mLoginGetCodeBtn;
    private EditText mPhoneNumberEdt;
    private EditText mRequestCodeEdt;
    private TextView mLoginRegister;
    private TextView mForgetPassword;
    private Button mLoginLoad;
    private TextView mLoginToast;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_login;
    }

    @Override
    protected void onInitUI() {
        mLoginGetCodeBtn = (Button) findViewById(R.id.login_get_code);
        mLoginGetCodeBtn.setVisibility(View.VISIBLE);
        mPhoneNumberEdt = (EditText) findViewById(R.id.login_account);
        mRequestCodeEdt = (EditText) findViewById(R.id.login_password);
        mLoginRegister = (TextView) findViewById(R.id.login_register);
        mForgetPassword = (TextView) findViewById(R.id.login_forget_password);
        mLoginToast = (TextView) findViewById(R.id.login_toast);
        mLoginLoad = (Button) findViewById(R.id.login_load);
        mPhoneNumberEdt.setHint("请输入手机号");
        mRequestCodeEdt.setHint("请输入验证码");
        setListener();
    }

    private void setListener() {
        mLoginRegister.setOnClickListener(this);
        mLoginGetCodeBtn.setOnClickListener(this);
        mLoginLoad.setOnClickListener(this);
        mForgetPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_load:
                break;
            case  R.id.login_get_code:
                break;
            case R.id.login_register:
                break;
            case R.id.login_forget_password:
                break;
        }
    }
}
