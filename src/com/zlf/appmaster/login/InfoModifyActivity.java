package com.zlf.appmaster.login;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.home.BaseActivity;
import com.zlf.appmaster.ui.ExpandableLayout;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.ui.stock.LoginProgressDialog;
import com.zlf.appmaster.utils.PrefConst;
import com.zlf.appmaster.utils.StringUtil;
import com.zlf.tools.animator.Animator;
import com.zlf.tools.animator.AnimatorListenerAdapter;
import com.zlf.tools.animator.AnimatorSet;
import com.zlf.tools.animator.ObjectAnimator;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2016/9/12.
 */
public class InfoModifyActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private RippleView mModifyUserName;
    private RippleView mModifyPwd;

    private LinearLayout mOldNameLayout;
    private EditText mOldNameEt;
    private ImageView mOldNameCloseBtn;
    private RippleView mOldNameBtn;

    private LinearLayout mOldPwdLayout;
    private EditText mOldPwdEt;
    private ImageView mOldPwdCloseBtn;
    private EditText mNewPwdEt;
    private ImageView mNewPwdCloseBtn;
    private EditText mQueryNewPwdEt;
    private ImageView mQueryNewPwdCloseBtn;
    private RippleView mQueryNewPwdBtn;
    private Toast mToast;
    private LoginProgressDialog mDialog;
    private boolean mProgressBarShow; // 加载正在进行
    private int mMessageTag;
    private DataHandler mHandle;


    private static final int SHOW_DEFAULT = -1;
    private static final int SHOW_NAME = 2;
    private static final int SHOW_PWD = 3;
    private int mHasShow = SHOW_DEFAULT; //  互斥标志位

    private static final int RESET_NAME =0;
    private static final int RESET_PWD = 1;

    public static final String SUCCESS = "OK"; // 成功
    public static final String NONUM = "NONUM"; // 未注册
    public static final String ERROR = "ERROR"; // 出错

    private ExpandableLayout mExpandableTop;
    private ExpandableLayout mExpandableBottom;


    //用于处理消息的Handler
    private static class DataHandler extends Handler {
        WeakReference<InfoModifyActivity> mActivityReference;

        public DataHandler(InfoModifyActivity activity) {
            super();
            mActivityReference = new WeakReference<InfoModifyActivity>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            InfoModifyActivity activity = mActivityReference.get();
            if (activity == null) {
                return;
            }
            String result = "";

            if (RESET_NAME == msg.what) {
                if (SUCCESS.equals(msg.obj.toString())) {
                    result = activity.getResources().getString(R.string.login_reset_success);
                    LeoSettings.setString(PrefConst.USER_NAME, activity.mOldNameEt.getText().toString().trim());
                    activity.finish();
                } else if (NONUM.equals(msg.obj.toString())) {
                    result = activity.getResources().getString(R.string.login_reset_nunum);
                } else {
                    result = activity.getResources().getString(R.string.reset_error);
                }
            } else if (RESET_PWD == msg.what) {
                if (SUCCESS.equals(msg.obj.toString())) {
                    result = activity.getResources().getString(R.string.login_reset_success);
                    LeoSettings.setString(PrefConst.USER_PWD, activity.mQueryNewPwdEt.getText().toString().trim());
                    activity.finish();
                } else if (NONUM.equals(msg.obj.toString())) {
                    result = activity.getResources().getString(R.string.login_reset_nunum);
                } else {
                    result = activity.getResources().getString(R.string.reset_error);
                }
            }
            Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_info);
        init();
        setListener();
    }

    private void init() {
        mHandle = new DataHandler(this);
        mModifyUserName = (RippleView) findViewById(R.id.modify_user_name);
        mModifyPwd = (RippleView) findViewById(R.id.modify_pwd);

        mOldNameLayout = (LinearLayout) findViewById(R.id.old_layout);
        mOldNameEt = (EditText) findViewById(R.id.old_name_ev);
        mOldNameCloseBtn = (ImageView) findViewById(R.id.old_name_close_iv);
        mOldNameBtn = (RippleView) findViewById(R.id.old_name_complete);

        mOldPwdLayout = (LinearLayout) findViewById(R.id.old_pwd_layout);
        mOldPwdEt = (EditText) findViewById(R.id.old_pwd_ev);
        mOldPwdCloseBtn = (ImageView) findViewById(R.id.old_pwd_close_iv);
        mNewPwdEt = (EditText) findViewById(R.id.new_pwd_ev);
        mNewPwdCloseBtn = (ImageView) findViewById(R.id.new_pwd_close_iv);
        mQueryNewPwdEt = (EditText) findViewById(R.id.new_query_pwd_ev);
        mQueryNewPwdCloseBtn = (ImageView) findViewById(R.id.new_query_pwd_close_iv);
        mQueryNewPwdBtn = (RippleView) findViewById(R.id.old_pwd_complete);

        mExpandableTop = (ExpandableLayout) findViewById(R.id.expandablelayout_top);
        mExpandableBottom = (ExpandableLayout) findViewById(R.id.expandablelayout_bottom);

    }

    private void setListener() {
        mModifyUserName.setOnClickListener(this);
        mModifyPwd.setOnClickListener(this);
        mOldNameCloseBtn.setOnClickListener(this);
        mOldNameBtn.setOnClickListener(this);
        mOldPwdCloseBtn.setOnClickListener(this);
        mNewPwdCloseBtn.setOnClickListener(this);
        mQueryNewPwdCloseBtn.setOnClickListener(this);
        mQueryNewPwdBtn.setOnClickListener(this);

        mOldNameEt.addTextChangedListener(this);
        mOldPwdEt.addTextChangedListener(this);
        mNewPwdEt.addTextChangedListener(this);
        mQueryNewPwdEt.addTextChangedListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.modify_user_name:
                if (mHasShow == SHOW_DEFAULT) {
                    mExpandableTop.toggleExpansion();
                    mHasShow = SHOW_NAME;
                } else if (mHasShow == SHOW_NAME) {
                    mExpandableTop.setExpanded(false);
                    mHasShow = SHOW_DEFAULT;
                } else {
                    mExpandableTop.setExpanded(true);
                    mExpandableBottom.setExpanded(false);
                    mHasShow = SHOW_NAME;
                }
                break;
            case R.id.modify_pwd:
                if (mHasShow == SHOW_DEFAULT) {
                    mExpandableBottom.toggleExpansion();
                    mHasShow = SHOW_PWD;
                } else if (mHasShow == SHOW_NAME) {
                    mExpandableTop.setExpanded(false);
                    mExpandableBottom.setExpanded(true);
                    mHasShow = SHOW_PWD;
                } else {
                    mExpandableBottom.setExpanded(false);
                    mHasShow = SHOW_DEFAULT;
                }
                break;
//            case R.id.modify_user_name:
//                showNameOrPwdView(true);
//                break;
//            case R.id.modify_pwd:
//                showNameOrPwdView(false);
//                break;
            case R.id.old_name_close_iv:
                mOldNameEt.getText().clear();
                break;
            case R.id.old_name_complete:
                resetInfo(true);
                break;
            case R.id.old_pwd_close_iv:
                mOldPwdEt.getText().clear();
                break;
            case R.id.new_pwd_close_iv:
                mNewPwdEt.getText().clear();
                break;
            case R.id.new_query_pwd_close_iv:
                mQueryNewPwdEt.getText().clear();
                break;
            case R.id.old_pwd_complete:
                resetInfo(false);
                break;
        }
    }

    private void resetInfo(boolean resetName) {
        if (resetName) {
            if (isNameValidate()) {
                reset(resetName);
            }
        } else {
            String password = mOldPwdEt.getText().toString().trim();
            String newPassword = mNewPwdEt.getText().toString().trim();
            String queryPassword = mQueryNewPwdEt.getText().toString().trim();

            if (isPwdValidate(password) && isPwdValidate(newPassword)
                    && isPwdValidate(queryPassword)) {

                if (!newPassword.equals(queryPassword)) {
                    showToast(getResources().getString(R.string.error_same_pwd));
                } else {
                    reset(resetName);
                }
            }

        }
    }


    private void reset(boolean name) {
        try {
            String tag;
            if (name) {
                mMessageTag = RESET_NAME;
                tag = Constants.RESET_NAME_TAG;
            } else {
                mMessageTag = RESET_PWD;
                tag = Constants.RESET_TAG;
            }
            if (mDialog == null) {
                mDialog = new LoginProgressDialog(this);
            }
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setLoadingContent(getResources().getString(R.string.modify_loading));
            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mProgressBarShow = false;
                }
            });
            mDialog.show();
            mProgressBarShow = true;
            String userName = "";
            String pwd = "";
            String phone = LeoSettings.getString(PrefConst.USER_PHONE, "");
            if (name) {
                userName = mOldNameEt.getText().toString();
                pwd = LeoSettings.getString(PrefConst.USER_PWD, "");
            } else {
                pwd = mQueryNewPwdEt.getText().toString().trim();
            }
            // 发送请求
            LoginHttpUtil.sendHttpRequest(this, Constants.LOGIN_ADDRESS, tag,
                    phone, pwd, userName,  new HttpCallBackListener() {
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
                                if (mHandle != null) {
                                    mHandle.sendMessage(message);
                                }
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            if (mProgressBarShow) {
                                if (mDialog != null && mDialog.isShowing()) {
                                    mDialog.dismiss();
                                    mDialog = null;
                                }
                                Message message = new Message();
                                message.what = mMessageTag;
                                message.obj = e.toString();
                                if (mHandle != null) {
                                    mHandle.sendMessage(message);
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isPwdValidate(String s) {
        if (TextUtils.isEmpty(s)) {
            showToast(getResources().getString(
                    R.string.login_new_pwd_empty));

            return false;
        }
        if (!StringUtil.isPassWordValidate(s)) {
            showToast(getResources().getString(
                    R.string.login_pwd_unlocal));

            return false;
        }

        return true;
    }

    private boolean isNameValidate() {
        String userName = mOldNameEt.getText().toString().trim();
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

    private void showToast(String s) {
        if (mToast == null) {
            mToast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(s);
        }
        mToast.show();
    }

    private void showNameOrPwdView(boolean showName) {
        if (showName) {
            if (mHasShow == SHOW_DEFAULT) {
                ObjectAnimator objectAnimator = showAnim(mOldNameLayout);
                objectAnimator.start();
                mHasShow = SHOW_NAME;
            } else if (mHasShow == SHOW_NAME) {
                ObjectAnimator objectAnimator = hideAnim(mOldNameLayout);
                objectAnimator.start();
                mHasShow = SHOW_DEFAULT;
            } else if (mHasShow == SHOW_PWD) {
                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator nameAnimator = showAnim(mOldNameLayout);
                ObjectAnimator pwdAnimator = hideAnim(mOldPwdLayout);
                animatorSet.playTogether(nameAnimator, pwdAnimator);
                animatorSet.start();
                mHasShow = SHOW_NAME;
            }
        } else {
            if (mHasShow == SHOW_DEFAULT) {
                ObjectAnimator objectAnimator = showAnim(mOldPwdLayout);
                objectAnimator.start();
                mHasShow = SHOW_PWD;
            } else if (mHasShow == SHOW_NAME) {
                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator nameAnimator = showAnim(mOldPwdLayout);
                ObjectAnimator pwdAnimator = hideAnim(mOldNameLayout);
                animatorSet.playTogether(nameAnimator, pwdAnimator);
                animatorSet.start();
                mHasShow = SHOW_PWD;
            } else if (mHasShow == 3) {
                ObjectAnimator objectAnimator = hideAnim(mOldPwdLayout);
                objectAnimator.start();
                mHasShow = SHOW_DEFAULT;
            }
        }
    }


    private ObjectAnimator showAnim(final View view) {
        view.setPivotY(0);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0f , 1f);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(View.VISIBLE);
            }
        });
        objectAnimator.setDuration(500);
        return objectAnimator;
    }

    private ObjectAnimator hideAnim(final View view) {
        view.setPivotY(0);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f , 0f);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });
        objectAnimator.setDuration(500);
        return objectAnimator;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mHasShow == 2) {
            if (TextUtils.isEmpty(mOldNameEt.getText().toString().trim())) {
                mOldNameCloseBtn.setVisibility(View.INVISIBLE);
            } else {
                mOldNameCloseBtn.setVisibility(View.VISIBLE);
            }
        } else if (mHasShow == 3) {
            if (TextUtils.isEmpty(mOldPwdEt.getText().toString().trim())) {
                mOldPwdCloseBtn.setVisibility(View.INVISIBLE);
            } else {
                mOldPwdCloseBtn.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(mNewPwdEt.getText().toString().trim())) {
                mNewPwdCloseBtn.setVisibility(View.INVISIBLE);
            } else {
                mNewPwdCloseBtn.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(mQueryNewPwdEt.getText().toString().trim())) {
                mQueryNewPwdCloseBtn.setVisibility(View.INVISIBLE);
            } else {
                mQueryNewPwdCloseBtn.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
