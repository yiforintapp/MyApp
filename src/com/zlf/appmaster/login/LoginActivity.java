package com.zlf.appmaster.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.home.BaseFragmentActivity;
import com.zlf.appmaster.model.LoginUser;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.utils.StringUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/7/19.
 */
public class LoginActivity extends BaseFragmentActivity implements View.OnClickListener, TextWatcher {

    private EditText mUserEt;
    private ImageView mUserClean;
    private EditText mPasswordEt;
    private ImageView mPasswordClean;
    private Button mLoginBtn;
    private TextView mRegisterTv;
    private TextView mForgetPwdTv;
    private CommonToolbar mToolBar;
    private Toast mToast;
    private DataHandler mHandler;


    //用于处理消息的Handler
    private static class DataHandler extends Handler {
        WeakReference<LoginActivity> mActivityReference;

        public DataHandler(LoginActivity activity) {
            super();
            mActivityReference = new WeakReference<LoginActivity>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LoginActivity activity = mActivityReference.get();
            if (activity == null) {
                return;
            }
            String result = "";

            if ("OK".equals(msg.obj.toString())){
                result = "success";
            }else if ("Wrong".equals(msg.obj.toString())){
                result = "fail";
            }else {
                result = msg.obj.toString();
            }
            Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        setListener();
    }

    private void init() {
        mHandler = new DataHandler(this);

        mUserEt = (EditText) findViewById(R.id.user_ev);
        mUserClean = (ImageView) findViewById(R.id.user_close_iv);
        mPasswordEt = (EditText) findViewById(R.id.pwd_ev);
        mPasswordClean = (ImageView) findViewById(R.id.pwd_close_iv);
        mLoginBtn = (Button) findViewById(R.id.login);
        mRegisterTv = (TextView) findViewById(R.id.register);
        mForgetPwdTv = (TextView) findViewById(R.id.forget_pwd);
        mToolBar = (CommonToolbar) findViewById(R.id.login_toolbar);
        mToolBar.setToolbarTitle(getResources().getString(R.string.login_logining));

        mUserEt.requestFocus();
        mUserEt.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) LoginActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mUserEt, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
    }


    private void setListener() {
        mUserClean.setOnClickListener(this);
        mPasswordClean.setOnClickListener(this);
        mLoginBtn.setOnClickListener(this);
        mRegisterTv.setOnClickListener(this);
        mForgetPwdTv.setOnClickListener(this);
        mUserEt.addTextChangedListener(this);
        mPasswordEt.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.user_close_iv:
                mUserEt.getText().clear();
                mUserClean.setVisibility(View.INVISIBLE);
                break;
            case R.id.pwd_close_iv:
                mPasswordEt.getText().clear();
                mPasswordClean.setVisibility(View.INVISIBLE);
                break;
            case R.id.login:
                login();
                break;
            case R.id.register:
                intent = new Intent(this, RegisterActivity.class);
                intent.putExtra(RegisterActivity.FROM_REGISTER, true);
                startActivity(intent);
                break;
            case R.id.forget_pwd:
                intent = new Intent(this, RegisterActivity.class);
                intent.putExtra(RegisterActivity.FROM_REGISTER, false);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(mUserEt.getText().toString().trim())) {
            mUserClean.setVisibility(View.INVISIBLE);
        } else {
            mUserClean.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(mPasswordEt.getText().toString().trim())) {
            mPasswordClean.setVisibility(View.INVISIBLE);
        } else {
            mPasswordClean.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void login() {
        //检查用户输入的账号和密码的合法性
        if (!isValidate()) {
            return;
        }
        //构造HashMap
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(LoginUser.PHONENUMBER, mUserEt.getText().toString());
        params.put(LoginUser.PASSWORD, mPasswordEt.getText().toString());
        try {
            //构造完整URL
            String compeletedURL = LoginHttpUtil.getURLWithParams(Constants.LoginAddress, params);
            //发送请求
            LoginHttpUtil.sendHttpRequest(compeletedURL, new HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    Message message = new Message();
                    message.obj = response;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onError(Exception e) {
                    Message message = new Message();
                    message.obj = e.toString();
                    mHandler.sendMessage(message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(String s) {
        if (mToast == null) {
            mToast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(s);
        }
        mToast.show();
    }

    private boolean isValidate() {
        String userName = mUserEt.getText().toString().trim();
        String password = mPasswordEt.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            showToast(getResources().getString(
                    R.string.login_user_empty));
            return false;
        }
        if (!StringUtil.isPhoneNumberValid(userName)) {
            showToast(getResources().getString(
                    R.string.login_user_unlocal));
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            showToast(getResources().getString(
                    R.string.login_pwd_empty));

            return false;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideIME();
    }

    private void hideIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mUserEt.getWindowToken(), 0);
    }


}
