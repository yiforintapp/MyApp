package com.zlf.appmaster.login;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.ui.dialog.LoginProgressDialog;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.StringUtil;
import com.zlf.tools.animator.Animator;
import com.zlf.tools.animator.AnimatorSet;
import com.zlf.tools.animator.ObjectAnimator;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/8/25.
 */
public class RegisterActivity extends Activity implements View.OnClickListener, TextWatcher {

    public static final String FROM_REGISTER = "from_register";
    public static final int REGISTER = 0;  // 注册
    public static final int RESET = 1; // 忘记密码
    public static final String SUCCESS = "OK"; // 成功
    public static final String SAME = "SAME"; // 已注册
    public static final String NONUM = "NONUM"; // 未注册
    public static final String ERROR = "ERROR"; // 出错
    private int mMessageTag;

    public static final int CODE_SUCCESS = 100; // 信息发送成功

    private EditText mUserEt;
    private ImageView mUserClean;
    private EditText mPasswordEt;
    private ImageView mPasswordClean;
    private EditText mNewPasswordEt;
    private ImageView mNewPasswordClean;
    private EditText mCodeEt;
    private RippleView mGetCodeRipple;
    private Button mGetCodeBtn;
    private RippleView mRegisterRipple;
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
    private RippleView mCompleteRipple;
    private Button mComplete;
    private DataHandler mHandler;
    private boolean mHasShow;  // 设置密码已经inflate过
    private boolean mSetPwdPage;  // 是否处于设置密码界面,默认不处于
    private String mCode; // 验证码

    private EditText mUserNameEt;
    private ImageView mUserNameClean;
    private RelativeLayout mUserNameLayout;
    private View mUserNameView;

    private boolean mProgressBarShow; // 加载正在进行



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
                    activity.mPasswordEt.getText().clear();
                    activity.mUserNameEt.getText().clear();
                    activity.mCodeEt.getText().clear();
                    activity.mCode = "";
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
                    activity.mCodeEt.getText().clear();
                    activity.mCode = "";
                    activity.mPasswordEt.getText().clear();
                    activity.mNewPasswordEt.getText().clear();
                    activity.startPhoneAnim();
                } else {
                    result = activity.getResources().getString(R.string.login_error);
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
        mGetCodeRipple = (RippleView) findViewById(R.id.login_code_ripple);
        mGetCodeBtn = (Button) findViewById(R.id.login_code);
        mRegisterRipple = (RippleView) findViewById(R.id.register_ripple);
        mRegisterBtn = (Button) findViewById(R.id.register);
        mUserClean.setOnClickListener(this);
        mGetCodeRipple.setOnClickListener(this);
        mRegisterRipple.setOnClickListener(this);
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
            case R.id.new_pwd_close_iv:
                mNewPasswordEt.getText().clear();
                mNewPasswordClean.setVisibility(View.INVISIBLE);
                break;
            case R.id.login_code_ripple:
                if (isPhoneNumberValidate()) {
                    getCode();
                }
                break;
            case R.id.register_ripple:
                if (isPhoneOrCodeValidate()) {
                    validateCode();
                }
                break;
            case R.id.register_complete_ripple:
                if (isValidate()) {
                    if (AppUtil.hasInternet(this)) {
                        register();
                    } else {
                        showToast(getResources().getString(R.string.feedback_upload_err));
                    }
                }
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
            if (mDialog == null) {
                mDialog = new LoginProgressDialog(this);
            }
            if (mFromRegister) {
                mMessageTag = REGISTER;
                tag = Constants.REGISTER_TAG;
                mDialog.setLoadingContent(getResources().getString(R.string.register_loading));
            } else {
                mMessageTag = RESET;
                tag = Constants.RESET_TAG;
                mDialog.setLoadingContent(getResources().getString(R.string.modify_loading));
            }
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mProgressBarShow = false;
                }
            });
            mDialog.show();
            mProgressBarShow = true;
            String password = StringUtil.retMd5Pwd(mPasswordEt.getText().toString().trim()).toLowerCase();
            // 发送请求
            LoginHttpUtil.sendLoginHttpRequest(this, Constants.LOGIN_ADDRESS, tag,
                    mUserEt.getText().toString().trim(), password, mUserNameEt.getText().toString().trim(), new HttpCallBackListener() {
                        @Override
                        public void onFinish(String response) {
                            if (mProgressBarShow) {
                                if (mDialog != null && mDialog.isShowing()) {
                                    mDialog.dismiss();
                                    mDialog = null;
                                }
                                Message message = new Message();
                                message.what = mMessageTag;
                                message.obj = response;
                                if (mHandler != null) {
                                    mHandler.sendMessage(message);
                                }
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            if (mProgressBarShow) {
                                if (mDialog != null && mDialog.isShowing()) {
                                    mDialog.dismiss();
                                }
                                Message message = new Message();
                                message.what = mMessageTag;
                                message.obj = e.toString();
                                if (mHandler != null) {
                                    mHandler.sendMessage(message);
                                }
                            }
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
            mCompleteRipple = (RippleView) findViewById(R.id.register_complete_ripple);
            mComplete = (Button) findViewById(R.id.register_complete);
            mUserNameLayout = (RelativeLayout) findViewById(R.id.user_name);
            mUserNameView = (View) findViewById(R.id.view_four);
            mUserNameClean = (ImageView) findViewById(R.id.user_name_close_iv);
            mUserNameEt = (EditText) findViewById(R.id.user_name_ev);
            mUserNameClean.setOnClickListener(this);
            mCompleteRipple.setOnClickListener(this);
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
            if (mFromRegister) {
                mToolBar.setToolbarTitle(getResources().getString(R.string.register));
                mRegisterBtn.setText(getResources().getString(R.string.register));
            } else {
                mToolBar.setToolbarTitle(getResources().getString(R.string.login_find_password));
                mRegisterBtn.setText(getResources().getString(R.string.login_reset_complete));
            }
        } else {
            if (mFromRegister) {
                mToolBar.setToolbarTitle(getResources().getString(R.string.login_register_pwd_set));
                mResetNewLayout.setVisibility(View.VISIBLE);
                mResetNewView.setVisibility(View.VISIBLE);
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

    /**
     * 得到随机验证码
     */
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

                String encode = "utf-8";
                String username = "szzlftxgxyxgshy";  //用户名
                String password_md5 = "8d3571f58020db2246b20706e3c4dee1";  //密码
                String mobile = mUserEt.getText().toString();  //手机号,只发一个号码：13800000001。发多个号码：13800000001,13800000002,...N 。使用半角逗号分隔。
                getRandomCode();
                String content = getResources().getString(R.string.code_end_content)
                        + getResources().getString(R.string.code_start_content)
                        + mCode;  //要发送的短信内容，特别注意：签名必须设置，网页验证码应用需要加添加【图形识别码】。


                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.GET_CODE_PWD_TAG, password_md5);
                params.put(Constants.GET_CODE_USER_TAG, username);
                params.put(Constants.GET_CODE_STYLE_TAG, "utf");
                params.put(Constants.GET_CODE_PHONE_TAG, mobile);
                params.put(Constants.GET_CODE_MSG_TAG, content);

                byte[] data = getRequestData(params, encode).toString().getBytes();

                InputStream inputStream = null;
                InputStreamReader inputReader = null;
                BufferedReader reader = null;
                try {

                    URL url = new URL(Constants.GET_CODE_HOST);

                    //打开URL链接
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    //使用POST方式发送
                    connection.setRequestMethod("POST");

                    //使用长链接方式
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    //设置请求体的长度
                    connection.setRequestProperty("Content-Length", String.valueOf(data.length));
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);

                    //获得输出流，向服务器写入数据
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(data);

                    int response = connection.getResponseCode();            //获得服务器的响应码
                    if (response == HttpURLConnection.HTTP_OK) {

                        InputStream inptStream = connection.getInputStream();
                        String result = dealResponseResult(inptStream);

                        JSONObject object = new JSONObject(result);
                        if (object == null) {
                            stopTimer();
                            return;
                        }

                        if (!object.isNull("status")) {
                            int status = object.getInt("status");
                            if (CODE_SUCCESS == status) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, getResources().getString(R.string.code_send_success), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                stopTimer();
                            }
                        } else {
                            stopTimer();
                        }
                    } else {
                        stopTimer();
                    }

                } catch (Exception e) {
                    stopTimer();
                    e.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (inputReader != null) {
                            inputReader.close();
                        }
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /*
* Function  :   处理服务器的响应结果（将输入流转化成字符串）
* Param     :   inputStream服务器的响应输入流
*/
    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
            resultData = new String(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultData;
    }

    /*
* Function  :   封装请求体信息
* Param     :   params请求体内容，encode编码格式
*/
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    private void getCode() {
        mGetCodeRipple.setEnabled(false);
        //开始倒计时
        startTimer();
        getFirstCode();
    }

    private void startTimer() {
        mTimes = 60;
        mGetCodeBtn.setText(mTimes + "s");
        mGetCodeRipple.setEnabled(false);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                if (mTimerTask != null) {
                    mTimerTask.cancel();
                    mTimerTask = null;
                }
                mGetCodeBtn.setText("重新获取");
                mGetCodeRipple.setEnabled(true);
            }
        });
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
        String phoneNumber = mUserEt.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            showToast(getResources().getString(R.string.login_user_empty));
            return false;
        }
        if (!StringUtil.isPhoneNumberValid(phoneNumber)) {
            showToast(getResources().getString(R.string.login_user_unlocal));
            return false;
        }

        return true;
    }

    private boolean isUserNameValidate() {
        String userName = mUserNameEt.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            showToast(getResources().getString(R.string.login_user_name_empty));
            return false;
        }
        if (!StringUtil.isUserNameValid(userName)) {
            showToast(getResources().getString(R.string.login_user_name_unlocal));
            return false;
        }

        return true;
    }

    private void validateCode() {
        if (TextUtils.isEmpty(mCode)) {
            showToast(getResources().getString(R.string.login_code_error));
            return;
        }
        final String code = mCodeEt.getText().toString();
        String phoneNum = mUserEt.getText().toString();
        if (mDialog == null) {
            mDialog = new LoginProgressDialog(this);
        }
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setLoadingContent(getResources().getString(R.string.code_loading));
        mDialog.show();
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                    mDialog = null;
                }
                if (!mCode.equals(code)) {
                    mCodeEt.getText().clear();
                    showToast(getResources().getString(R.string.login_code_error));
                } else {
                    showPwdLayout();
                }
            }
        }, 1000);
    }


    private boolean isPhoneOrCodeValidate() {
        String code = mCodeEt.getText().toString().trim();

        if (!isPhoneNumberValidate()) {
            return false;
        }

        if (TextUtils.isEmpty(code)) {
            showToast(getResources().getString(
                    R.string.login_code_empty));

            return false;
        }

        return true;
    }

    private boolean isValidate() {
        String password = mPasswordEt.getText().toString().trim();
        String newPassword = mNewPasswordEt.getText().toString().trim();

        if (!isPhoneNumberValidate()) {
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            showToast(getResources().getString(
                    R.string.login_pwd_empty));

            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            showToast(getResources().getString(
                    R.string.login_new_pwd_empty));

            return false;
        }

        if (!StringUtil.isPassWordValidate(password, 1)) {
            showToast(getResources().getString(
                    R.string.login_pwd_unlocal));

            return false;
        }

        if (!StringUtil.isPassWordValidate(newPassword, 1)) {
            showToast(getResources().getString(
                    R.string.login_pwd_unlocal));

            return false;
        }

        if (!password.equals(newPassword)) {
            showToast(getResources().getString(
                    R.string.error_same_pwd));
            return false;
        }

        if (mFromRegister && !isUserNameValidate()) {

            return false;
        }

        if (!isCodeValidate()) {
            return false;
        }


        return true;
    }

    private boolean isCodeValidate() {
        String code = mCodeEt.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            showToast(getResources().getString(
                    R.string.login_code_empty));

            return false;
        }

        if (!StringUtil.isCodeValidate(code)) {

            showToast(getResources().getString(
                    R.string.login_code_unlocal));
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
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
