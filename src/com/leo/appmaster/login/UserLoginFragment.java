package com.leo.appmaster.login;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;

/**
 * Created by Administrator on 2016/7/19.
 */
public class UserLoginFragment extends BaseFragment implements View.OnClickListener {

    private EditText mAccountEdt;
    private EditText mPasswordEdt;
    private TextView mLoginRegister;
    private TextView mForgetPassword;
    private TextView mLoginToast;
    private Button mLoginLoad;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_login;
    }

    @Override
    protected void onInitUI() {
        mAccountEdt = (EditText) findViewById(R.id.login_account);
        mPasswordEdt = (EditText) findViewById(R.id.login_password);
        mLoginRegister = (TextView) findViewById(R.id.login_register);
        mForgetPassword = (TextView) findViewById(R.id.login_forget_password);
        mLoginToast = (TextView) findViewById(R.id.login_toast);
        mLoginLoad = (Button) findViewById(R.id.login_load);
        setListener();
    }

    private void setListener() {
        mLoginLoad.setOnClickListener(this);
        mLoginRegister.setOnClickListener(this);
        mForgetPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case  R.id.login_load:
                if(isAccountAndPasswordEmpty()) {
                    return;
                }
                break;
            case R.id.login_register:
                break;
            case R.id.login_forget_password:
                break;
        }
    }

    private boolean isAccountAndPasswordEmpty() {
        if (TextUtils.isEmpty(mAccountEdt.getText().toString().trim())) {
            mLoginToast.setText("账号不能为空  ");
            mLoginToast.setVisibility(View.VISIBLE);
            return  true;
        } else if (TextUtils.isEmpty(mPasswordEdt.getText().toString().trim())) {
            mLoginToast.setText("密码不能为空");
            mLoginToast.setVisibility(View.VISIBLE);
            return  true;
        }
        mLoginToast.setVisibility(View.INVISIBLE);
        return false;
    }

}
