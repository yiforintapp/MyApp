package com.zlf.appmaster.login;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.home.BaseFragmentActivity;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.ui.stock.LoginProgressDialog;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.StringUtil;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/19.
 */
public class FeedbackActivity extends BaseFragmentActivity implements View.OnClickListener {

    public static final String FEED_BACK = "feedback";
    public static final String UPLOAD_DONE = "ERROR"; // 出错
    public static final String UPLOAD_ERR = "WRONG"; // 手机号或密码错误


    private EditText mFeedback;
    private EditText mEmail;

    private CommonToolbar mToolBar;
    private Toast mToast;

    private DataHandler mHandler;
    private TextView mTvSubmit;

    private LoginProgressDialog mDialog;
    private boolean mProgressBarShow; // 加载正在进行
    private long mSubmitTime = 0;

    //用于处理消息的Handler
    private static class DataHandler extends Handler {
        WeakReference<FeedbackActivity> mActivityReference;

        public DataHandler(FeedbackActivity activity) {
            super();
            mActivityReference = new WeakReference<FeedbackActivity>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FeedbackActivity activity = mActivityReference.get();
            if (activity == null) {
                return;
            }

            if (UPLOAD_DONE.equals(msg.obj.toString())){
                String result = activity.getResources().getString(R.string.feedback_upload_done);
                Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
            }else if(UPLOAD_ERR.equals(msg.obj.toString())){
                String result = activity.getResources().getString(R.string.feedback_upload_err);
                Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        init();
        setListener();
    }

    private void init() {
        mHandler = new DataHandler(this);

        mToolBar = (CommonToolbar) findViewById(R.id.fb_toolbar);
        mToolBar.setToolbarTitle(getResources().getString(R.string.fb_toolbar));
        mTvSubmit = (TextView) findViewById(R.id.tv_submit);
        mFeedback = (EditText) findViewById(R.id.ed_fb);
        mEmail = (EditText) findViewById(R.id.ed_fb_2);

        mFeedback.requestFocus();
        mFeedback.postDelayed(new Runnable() {
            @Override
            public void run() {
                //
                InputMethodManager imm = (InputMethodManager) FeedbackActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mFeedback, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
    }


    private void setListener() {
        mTvSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_submit:
                submit();
                break;
        }
    }

    private void submit() {
        //检查
        if (!isValidate()) {
            return;
        } else {

            if (mDialog == null) {
                mDialog = new LoginProgressDialog(this);
            }

            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setLoadingContent(getResources().getString(R.string.feedback_dialog));
            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mProgressBarShow = false;
                }
            });
            mDialog.show();
            mProgressBarShow = true;

            //upload
            ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                @Override
                public void run() {
                    sendFeedback();
                }
            }, 1500);


        }
    }

    private void sendFeedback() {
        String urlString = Constants.ADDRESS + "work";
        LeoLog.d("FeedbackActivity", "url : " + urlString);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constants.FEEDBACK_TYPE, FEED_BACK);
        params.put(Constants.FEEDBACK_CONTENT, mFeedback.getText().toString().trim());
        params.put(Constants.FEEDBACK_CONTACT, mEmail.getText().toString().trim());

        String encode = "utf-8";
        byte[] data = getRequestData(params, encode).toString().getBytes();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            //使用HttpURLConnection
            connection = (HttpURLConnection) url.openConnection();
            //设置方法和参数
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);               //使用Post方式不能使用缓存

            //设置请求体的类型是文本类型
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            //设置请求体的长度
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data);

            int response = connection.getResponseCode();            //获得服务器的响应码
            if (response == HttpURLConnection.HTTP_OK) {
                LeoLog.d("FeedbackActivity", "HTTP_OK");

                mSubmitTime = System.currentTimeMillis();
                if (mProgressBarShow) {
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                    Message message = new Message();
                    message.obj = UPLOAD_DONE;
                    if (mHandler != null) {
                        mHandler.sendMessage(message);
                    }
                }

//                InputStream inptStream = connection.getInputStream();
//                dealResponseResult(inptStream);
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (mProgressBarShow) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                    mDialog = null;
                }
                Message message = new Message();
                message.obj = UPLOAD_ERR;
                if (mHandler != null) {
                    mHandler.sendMessage(message);
                }
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
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
        String feedback = mFeedback.getText().toString().trim();
        String contact = mEmail.getText().toString().trim();
        if (TextUtils.isEmpty(feedback)) {
            showToast(getResources().getString(
                    R.string.feedback_toast_1));
            return false;
        }

        if (!StringUtil.isNoChinese(contact)) {
            mEmail.getText().clear();
            showToast(getResources().getString(
                    R.string.feedback_ed_3));
            return false;
        }


        long now = System.currentTimeMillis();
        if(now - mSubmitTime < 10000){
            showToast(getResources().getString(
                    R.string.feedback_submit_too_much));
            return false;
        }


        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
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
