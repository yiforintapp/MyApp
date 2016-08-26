package com.zlf.appmaster.login;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.home.BaseActivity;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.ui.stock.LoginProgressDialog;
import com.zlf.appmaster.utils.StringUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.sms.SMSSDK;
import cn.jpush.sms.listener.SmscheckListener;
import cn.jpush.sms.listener.SmscodeListener;

/**
 * Created by Administrator on 2016/8/25.
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    public static final String FROM_REGISTER = "from_register";

    private EditText mUserEt;
    private ImageView mUserClean;
    private EditText mPasswordEt;
    private ImageView mPasswordClean;
    private EditText mNewPasswordEt;
    private ImageView mNewPasswordClean;
    private EditText mCodeEt;
    private Button mGetCodeBtn;
    private Button mRegisterBtn;
    private CommonToolbar mToolBar;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private int mTimes;
    private LoginProgressDialog mDialog;
    private boolean mFromRegister;
    private RelativeLayout mResetNewLayout;
    private View mResetNewView;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
        setListener();

    }

    private void init() {
        mUserEt = (EditText) findViewById(R.id.user_ev);
        mUserClean = (ImageView) findViewById(R.id.user_close_iv);
        mPasswordEt = (EditText) findViewById(R.id.pwd_ev);
        mPasswordClean = (ImageView) findViewById(R.id.pwd_close_iv);
        mNewPasswordEt = (EditText) findViewById(R.id.new_pwd_ev);
        mNewPasswordClean = (ImageView) findViewById(R.id.new_pwd_close_iv);
        mCodeEt = (EditText) findViewById(R.id.code_ev);
        mGetCodeBtn = (Button) findViewById(R.id.login_code);
        mRegisterBtn = (Button) findViewById(R.id.register);
        mToolBar = (CommonToolbar) findViewById(R.id.register_toolbar);
        mResetNewLayout = (RelativeLayout) findViewById(R.id.reset_new_pwd);
        mResetNewView = (View) findViewById(R.id.new_pwd_view);

        mFromRegister = getIntent().getBooleanExtra(FROM_REGISTER, false);
        if(mFromRegister) {
            mToolBar.setToolbarTitle(getResources().getString(R.string.register));
            mRegisterBtn.setText(getResources().getString(R.string.register));
            mPasswordEt.setHint(getResources().getString(R.string.login_pwd_hint));
            mResetNewView.setVisibility(View.GONE);
            mResetNewLayout.setVisibility(View.GONE);
        } else {
            mToolBar.setToolbarTitle(getResources().getString(R.string.login_find_password));
            mRegisterBtn.setText(getResources().getString(R.string.login_reset_complete));
            mPasswordEt.setHint(getResources().getString(R.string.login_reset_password));
            mResetNewView.setVisibility(View.VISIBLE);
            mResetNewLayout.setVisibility(View.VISIBLE);
        }

        mUserEt.requestFocus();
        mUserEt.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) RegisterActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mUserEt, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
    }

    private void setListener() {
        mUserClean.setOnClickListener(this);
        mPasswordClean.setOnClickListener(this);
        mGetCodeBtn.setOnClickListener(this);
        mRegisterBtn.setOnClickListener(this);
        mUserEt.addTextChangedListener(this);
        mPasswordEt.addTextChangedListener(this);
        mNewPasswordEt.addTextChangedListener(this);
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

        if (TextUtils.isEmpty(mNewPasswordEt.getText().toString().trim())) {
            mNewPasswordClean.setVisibility(View.INVISIBLE);
        } else {
            mNewPasswordClean.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_close_iv:
                mUserEt.getText().clear();
                mUserClean.setVisibility(View.INVISIBLE);
                break;
            case R.id.pwd_close_iv:
                mPasswordEt.getText().clear();
                mPasswordClean.setVisibility(View.INVISIBLE);
                break;
            case R.id.login_code:

//               getCode();
                break;
            case R.id.register:
                if (!isValidate()) {
                    return;
                }
                validateCode();
                break;
        }
    }

    private void getFirstCode() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                //新建一个StringBuffer链接
                StringBuffer buffer = new StringBuffer();

                //String encode = "GBK"; //页面编码和短信内容编码为GBK。重要说明：如提交短信后收到乱码，请将GBK改为UTF-8测试。如本程序页面为编码格式为：ASCII/GB2312/GBK则该处为GBK。如本页面编码为UTF-8或需要支持繁体，阿拉伯文等Unicode，请将此处写为：UTF-8

                String encode = "UTF-8";

                String username = "13027964843";  //用户名

                String password_md5 = "461423d17d3720a76a1cfc0c1725807e";  //密码

                String mobile = mUserEt.getText().toString();  //手机号,只发一个号码：13800000001。发多个号码：13800000001,13800000002,...N 。使用半角逗号分隔。

                String apikey = "0cdd79dcb62b67efc7a8e6c75942f716";  //apikey秘钥（请登录 http://m.5c.com.cn 短信平台-->账号管理-->我的信息 中复制apikey）

                String content = "您好，您的验证码是：12345【兆利丰】";  //要发送的短信内容，特别注意：签名必须设置，网页验证码应用需要加添加【图形识别码】。



                try {


                    String contentUrlEncode = URLEncoder.encode(content,encode);  //对短信内容做Urlencode编码操作。注意：如

                    //把发送链接存入buffer中，如连接超时，可能是您服务器不支持域名解析，请将下面连接中的：【m.5c.com.cn】修改为IP：【115.28.23.78】
                    buffer.append("http://115.28.23.78/api/send/index.php?username="+username+"&password_md5="+password_md5+"&mobile="+mobile+"&apikey="+apikey+"&content="+contentUrlEncode+"&encode="+encode);

                    //System.out.println(buffer); //调试功能，输入完整的请求URL地址

                    //把buffer链接存入新建的URL中
                    URL url = new URL(buffer.toString());

                    //打开URL链接
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                    //使用POST方式发送
                    connection.setRequestMethod("POST");

                    //使用长链接方式
                    connection.setRequestProperty("Connection", "Keep-Alive");

                    //发送短信内容
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                    //获取返回值
                    final String result = reader.readLine();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //输出result内容，查看返回值，成功为success，错误为error，详见该文档起始注释
                            showToast(result);
                        }
                    });

                } catch (Exception e) {
                    Log.e("fjfgjfgj",e + "");
                    e.printStackTrace();
                }
            }
        });
    }

    private void getCode() {
        String phoneNum = mUserEt.getText().toString();
        if (!isPhoneNumberValidate()) {
            return;
        }
        mGetCodeBtn.setClickable(false);
        //开始倒计时
        startTimer();
        SMSSDK.getInstance().getSmsCodeAsyn(phoneNum, 1 + "", new SmscodeListener() {
            @Override
            public void getCodeSuccess(final String uuid) {
//                                Toast.makeText(MainActivity.this,uuid,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void getCodeFail(int errCode, final String errmsg) {
                //失败后停止计时
                if (errCode != 2996) {
                    stopTimer();
                }
                if (errCode == 3002) {
                    mUserEt.setEnabled(true);
                }
                showToast(errmsg + "|code=" + errCode);
            }
        });
    }

    private void startTimer() {
        mTimes = 60;
        mGetCodeBtn.setText(mTimes + "s");
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTimes--;
                            if (mTimes <= 0) {
                                stopTimer();
                                return;
                            }
                            mGetCodeBtn.setText(mTimes + "s");
                        }
                    });
                }
            };
        }
        if (mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(mTimerTask, 1000, 1000);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        mGetCodeBtn.setText("重新获取");
        mGetCodeBtn.setClickable(true);
    }

    private void showToast(String s) {
        if (mToast == null) {
            mToast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(s);
        }
        mToast.show();
    }

    private boolean isPhoneNumberValidate() {
        String userName = mUserEt.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            showToast(getResources().getString(R.string.login_user_empty));
            return false;
        }
        if (!StringUtil.isPhoneNumberValid(userName)) {
            showToast(getResources().getString(R.string.login_user_unlocal));
            return false;
        }

        return true;
    }

    private void validateCode() {
        String code = mCodeEt.getText().toString();
        String phoneNum = mUserEt.getText().toString();
//        progressDialog.setTitle("正在验证...");
        if (mDialog == null) {
            mDialog = new LoginProgressDialog(this);
        }
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        SMSSDK.getInstance().checkSmsCodeAsyn(phoneNum, code, new SmscheckListener() {
            @Override
            public void checkCodeSuccess(final String code) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                showToast(code);
            }

            @Override
            public void checkCodeFail(int errCode, final String errmsg) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                showToast(errmsg + "|code=" + errCode);
            }
        });
    }

    private boolean isValidate() {
        String password = mPasswordEt.getText().toString().trim();
        String code = mCodeEt.getText().toString().trim();
        String newPassword = mNewPasswordEt.getText().toString().trim();

        if (!isPhoneNumberValidate()) {
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            showToast(getResources().getString(
                    R.string.login_pwd_empty));

            return false;
        }

        if (!mFromRegister && TextUtils.isEmpty(newPassword)) {
            showToast(getResources().getString(
                    R.string.login_new_pwd_empty));

            return false;
        }

        if (TextUtils.isEmpty(code)) {
            showToast(getResources().getString(
                    R.string.login_code_empty));

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
