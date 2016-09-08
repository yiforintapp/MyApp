package com.zlf.appmaster.login;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.home.BaseActivity;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.ui.stock.LoginProgressDialog;
import com.zlf.appmaster.utils.StringUtil;
import com.zlf.tools.animator.Animator;
import com.zlf.tools.animator.AnimatorSet;
import com.zlf.tools.animator.ObjectAnimator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;
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
    public static final int REGISTER = 0;  // 注册
    public static final int RESET = 1; // 忘记密码
    public static final String SUCCESS = "OK"; // 成功
    public static final String SAME = "SAME"; // 已注册
    public static final String NONUM = "NONUM"; // 未注册
    private int mMessageTag;

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
    private RelativeLayout mPhoneLayout;
    private RelativeLayout mPwdLayout;
    private Button mComplete;
    private DataHandler mHandler;
    private boolean mHasShow;  // 设置密码已经inflate过
    private boolean mSetPwdPage;  // 是否处于设置密码界面,默认不处于
    private String mCode; // 验证码

    private EditText mUserNameEt;
    private ImageView mUserNameClean;
    private RelativeLayout mUserNameLayout;
    private View mUserNameView;

    //用于处理消息的Handler
    private static class DataHandler extends Handler {
        WeakReference<RegisterActivity> mActivityReference;

        public DataHandler(RegisterActivity activity) {
            super();
            mActivityReference = new WeakReference<RegisterActivity>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RegisterActivity activity = mActivityReference.get();
            if (activity == null) {
                return;
            }
            String result = "";

            if (REGISTER == msg.what) {
                if (SUCCESS.equals(msg.obj.toString())) {
                    result = activity.getResources().getString(R.string.login_register_success);
                    activity.finish();
                } else if (SAME.equals(msg.obj.toString())) {
                    result = activity.getResources().getString(R.string.login_register_same);
                    activity.startPhoneAnim();
                } else {
                    result = msg.obj.toString();
                }
            } else if (RESET == msg.what) {
                if (SUCCESS.equals(msg.obj.toString())) {
                    result = activity.getResources().getString(R.string.login_reset_success);
                    activity.finish();
                } else if (NONUM.equals(msg.obj.toString())) {
                    result = activity.getResources().getString(R.string.login_reset_nunum);
                    activity.startPhoneAnim();
                } else {
                    result = msg.obj.toString();
                }
            }
            Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        mHandler = new DataHandler(this);
        mToolBar = (CommonToolbar) findViewById(R.id.register_toolbar);
        mFromRegister = getIntent().getBooleanExtra(FROM_REGISTER, false);

        ViewStub userStub = (ViewStub) findViewById(R.id.user_viewStub);
        userStub.inflate();
        mPhoneLayout = (RelativeLayout) findViewById(R.id.phone_set_layout);
        mUserEt = (EditText) findViewById(R.id.user_ev);
        mUserClean = (ImageView) findViewById(R.id.user_close_iv);
        mCodeEt = (EditText) findViewById(R.id.code_ev);
        mGetCodeBtn = (Button) findViewById(R.id.login_code);
        mRegisterBtn = (Button) findViewById(R.id.register);
        mUserClean.setOnClickListener(this);
        mGetCodeBtn.setOnClickListener(this);
        mRegisterBtn.setOnClickListener(this);
        mUserEt.addTextChangedListener(this);
        mToolBar.setNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSetPwdPage) {
                    startPhoneAnim();
                } else {
                    finish();
                }
            }
        });

        setToolBar(true);

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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mUserEt != null) {
            if (TextUtils.isEmpty(mUserEt.getText().toString().trim())) {
                mUserClean.setVisibility(View.INVISIBLE);
            } else {
                mUserClean.setVisibility(View.VISIBLE);
            }
        }

        if (mPasswordEt != null) {
            if (TextUtils.isEmpty(mPasswordEt.getText().toString().trim())) {
                mPasswordClean.setVisibility(View.INVISIBLE);
            } else {
                mPasswordClean.setVisibility(View.VISIBLE);
            }
        }

        if (mNewPasswordEt != null) {
            if (TextUtils.isEmpty(mNewPasswordEt.getText().toString().trim())) {
                mNewPasswordClean.setVisibility(View.INVISIBLE);
            } else {
                mNewPasswordClean.setVisibility(View.VISIBLE);
            }
        }

        if (mUserNameEt != null) {
            if (TextUtils.isEmpty(mUserNameEt.getText().toString().trim())) {
                mUserNameClean.setVisibility(View.INVISIBLE);
            } else {
                mUserNameClean.setVisibility(View.VISIBLE);
            }
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
            case R.id.new_pwd_ev:
                mNewPasswordEt.getText().clear();
                mNewPasswordClean.setVisibility(View.INVISIBLE);
                break;
            case R.id.login_code:
                if(!isPhoneNumberValidate()) {
                    return ;
                }
//               getCode();
                break;
            case R.id.register:
//                validateCode();
                showPwdLayout();
                break;
            case R.id.register_complete:
                register();
                break;
            case R.id.user_name_close_iv:
                mUserNameEt.getText().clear();
                mUserNameClean.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void register() {
        try {
            String tag;
            if (mFromRegister) {
                mMessageTag = REGISTER;
                tag = Constants.REGISTER_TAG;
            } else {
                mMessageTag = RESET;
                tag = Constants.RESET_TAG;
            }
            // 发送请求
            LoginHttpUtil.sendHttpRequest(Constants.LOGIN_ADDRESS, tag,
                    mUserEt.getText().toString(), mPasswordEt.getText().toString(),  new HttpCallBackListener() {
                        @Override
                        public void onFinish(String response) {
                            Message message = new Message();
                            message.what = mMessageTag;
                            message.obj = response;
                            mHandler.sendMessage(message);
                        }

                        @Override
                        public void onError(Exception e) {
                            Message message = new Message();
                            message.what = mMessageTag;
                            message.obj = e.toString();
                            mHandler.sendMessage(message);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPwdLayout() {
        mSetPwdPage = true;
        if (!mHasShow) {
            ViewStub pwdStub = (ViewStub) findViewById(R.id.pwd_viewStub);
            pwdStub.inflate();
            mPwdLayout = (RelativeLayout) findViewById(R.id.pwd_set_layout);
            mPasswordEt = (EditText) findViewById(R.id.pwd_ev);
            mPasswordClean = (ImageView) findViewById(R.id.pwd_close_iv);
            mNewPasswordEt = (EditText) findViewById(R.id.new_pwd_ev);
            mNewPasswordClean = (ImageView) findViewById(R.id.new_pwd_close_iv);
            mResetNewLayout = (RelativeLayout) findViewById(R.id.reset_new_pwd);
            mResetNewView = (View) findViewById(R.id.view_five);
            mComplete = (Button) findViewById(R.id.register_complete);
            mUserNameLayout = (RelativeLayout) findViewById(R.id.user_name);
            mUserNameView = (View) findViewById(R.id.view_four);
            mUserNameClean = (ImageView) findViewById(R.id.user_name_close_iv);
            mUserNameEt = (EditText) findViewById(R.id.user_name_ev);
            mUserNameClean.setOnClickListener(this);
            mComplete.setOnClickListener(this);
            mPasswordClean.setOnClickListener(this);
            mNewPasswordClean.setOnClickListener(this);
            mPasswordEt.addTextChangedListener(this);
            mNewPasswordEt.addTextChangedListener(this);
            mUserNameEt.addTextChangedListener(this);

            mHasShow = true;
        }


        setToolBar(false);
        startPwdAnim();

    }

    private void startPwdAnim() {
        ObjectAnimator disAppearAnim = ObjectAnimator.ofFloat(mPhoneLayout, "translationX", 0f, -mPhoneLayout.getWidth());
        ObjectAnimator showAnim = ObjectAnimator.ofFloat(mPwdLayout, "translationX", mPhoneLayout.getWidth(), 0f);
        showAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mPwdLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPasswordEt.requestFocus();
                mPasswordEt.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) RegisterActivity.this
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(mPasswordEt, InputMethodManager.SHOW_IMPLICIT);
                    }
                }, 200);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(disAppearAnim, showAnim);
        animatorSet.setDuration(300);
        animatorSet.start();
    }


    private void setToolBar(boolean phone) {
        if (phone) {  // 设置验证码阶段
            if(mFromRegister) {
                mToolBar.setToolbarTitle(getResources().getString(R.string.register));
                mRegisterBtn.setText(getResources().getString(R.string.register));
            } else {
                mToolBar.setToolbarTitle(getResources().getString(R.string.login_find_password));
                mRegisterBtn.setText(getResources().getString(R.string.login_reset_complete));
            }
        } else {
            if (mFromRegister) {
                mToolBar.setToolbarTitle(getResources().getString(R.string.login_register_pwd_set));
                mResetNewLayout.setVisibility(View.GONE);
                mResetNewView.setVisibility(View.GONE);
                mUserNameLayout.setVisibility(View.VISIBLE);
                mUserNameView.setVisibility(View.VISIBLE);
            } else {
                mToolBar.setToolbarTitle(getResources().getString(R.string.login_find_password));
                mResetNewLayout.setVisibility(View.VISIBLE);
                mResetNewView.setVisibility(View.VISIBLE);
                mUserNameLayout.setVisibility(View.GONE);
                mUserNameView.setVisibility(View.GONE);
            }
        }
    }

    private void startPhoneAnim() {
        mSetPwdPage = false;
        setToolBar(true);
        ObjectAnimator disAppearAnim = ObjectAnimator.ofFloat(mPhoneLayout, "translationX", -mPhoneLayout.getWidth(), 0f);
        ObjectAnimator showAnim = ObjectAnimator.ofFloat(mPwdLayout, "translationX", 0f, mPwdLayout.getWidth());
        showAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
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

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(disAppearAnim, showAnim);
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    /** 得到随机验证码 */
    private void getRandomCode() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < 6; i++) {
            Random random = new Random();// 定义随机类
            int result = random.nextInt(10);// 返回[0,10)集合中的整数，注意不包括10
            s.append(String.valueOf(result));
        }
        mCode = s.toString();
    }

    private void getFirstCode() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                // 新建一个StringBuffer链接
                StringBuffer buffer = new StringBuffer();

                // String encode = "GBK"; //页面编码和短信内容编码为GBK。重要说明：如提交短信后收到乱码，请将GBK改为UTF-8测试。如本程序页面为编码格式为：ASCII/GB2312/GBK则该处为GBK。如本页面编码为UTF-8或需要支持繁体，阿拉伯文等Unicode，请将此处写为：UTF-8

                String encode = "UTF-8";

                String username = "13027964843";  //用户名

                String password_md5 = "461423d17d3720a76a1cfc0c1725807e";  //密码

                String mobile = mUserEt.getText().toString();  //手机号,只发一个号码：13800000001。发多个号码：13800000001,13800000002,...N 。使用半角逗号分隔。

                String apikey = "0cdd79dcb62b67efc7a8e6c75942f716";  //apikey秘钥（请登录 http://m.5c.com.cn 短信平台-->账号管理-->我的信息 中复制apikey）
                getRandomCode();
                String content = getResources().getString(R.string.code_start_content)
                        + mCode +  getResources().getString(R.string.code_end_content);  //要发送的短信内容，特别注意：签名必须设置，网页验证码应用需要加添加【图形识别码】。

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
    public void onBackPressed() {
        if (mSetPwdPage) {
            startPhoneAnim();
        } else {
            super.onBackPressed();
        }
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
