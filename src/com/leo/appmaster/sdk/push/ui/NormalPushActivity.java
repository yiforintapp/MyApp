
package com.leo.appmaster.sdk.push.ui;

import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;


public class NormalPushActivity extends BaseActivity implements View.OnClickListener, PushUIHelper.NewActListener{

    private final static String TAG = NormalPushActivity.class.getSimpleName();

    private boolean mFromStatusBar;
    private String mAdID;
    private EditText mPhoneNumber = null;
    
    private Handler mHandler;

    private final static String GP_MARKET_PACKAGE_NAME = "com.android.vending";
    private final static String GP_WEB_URL = "https://play.google.com/store/apps/details?id=com.leo.appmaster";
    private final static int INDIAN_MOBILE_LENGTH = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(getMainLooper());
        PushUIHelper.getInstance(this).registerNewActListener(this);
        handleIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        PushUIHelper.getInstance(this).unRegisterNewActListener(this);
        super.onDestroy();
    }

    private void handleIntent(Intent i) {
        SDKWrapper.addEvent(this, SDKWrapper.P1, "act", "popup");
        mFromStatusBar = i.getBooleanExtra(PushUIHelper.EXTRA_WHERE, false);
        mAdID = i.getStringExtra(PushUIHelper.EXTRA_AD_ID);
        if (mAdID == null) {
            mAdID = "unknown";
        }
        if (mFromStatusBar) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "act", "notbar");
        }
        String title = i.getStringExtra(PushUIHelper.EXTRA_TITLE);
        String content = i.getStringExtra(PushUIHelper.EXTRA_CONTENT);
        initUI(title, content);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initUI(String title, String content) {
        setContentView(R.layout.dialog_single_input_alarm);

        mPhoneNumber = (EditText) findViewById(R.id.et_input);
        mPhoneNumber.setGravity(Gravity.CENTER_HORIZONTAL);
        mPhoneNumber.setRawInputType(InputType.TYPE_CLASS_PHONE);
        mPhoneNumber.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(INDIAN_MOBILE_LENGTH)
        });

        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(title);
        TextView tvContent = (TextView) findViewById(R.id.dlg_content);
        tvContent.setText(content);

        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel));
        tvCancel.setOnClickListener(this);

        TextView tvGO = (TextView) findViewById(R.id.dlg_right_btn);
        tvGO.setText(getString(R.string.goto_gp));
        tvGO.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dlg_left_btn:
                /* user ignore this activity */
                SDKWrapper.addEvent(this, SDKWrapper.P1, "act", "cancel");
                PushUIHelper.getInstance(this).sendACK(mAdID, false, mFromStatusBar, "");
                finish();
                break;
            case R.id.dlg_right_btn:
                /* get money */
                handleCommit();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            PushUIHelper.getInstance(this).sendACK(mAdID, false, mFromStatusBar, "");
            SDKWrapper.addEvent(this, SDKWrapper.P1, "act", "cancel");
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isOutOfBounds(Activity context, MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(context)
                .getScaledWindowTouchSlop();
        final View decorView = context.getWindow().getDecorView();
        return (x < -slop) || (y < -slop)
                || (x > (decorView.getWidth() + slop))
                || (y > (decorView.getHeight() + slop));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && isOutOfBounds(this, event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void handleCommit() {
        String phone = mPhoneNumber.getText().toString().trim();
        String regexp = "([0-9]+)";
        Pattern p = Pattern.compile(regexp);

        if (phone.length() != INDIAN_MOBILE_LENGTH || !p.matcher(phone).matches()) {
            /* invalid indian phone number */
            Toast.makeText(this, getString(R.string.invalid_number), Toast.LENGTH_SHORT).show();
            return;
        }
        boolean showGP = false;
        if (hasGPInstalled()) {
            /* go to GP client */
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id="
                            + getPackageName()));
            i.setPackage(GP_MARKET_PACKAGE_NAME);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(i);
                showGP = true;
            }catch (Exception e) {
                
            }
        } 
        if(!showGP) {
            /* go to GP website */
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(GP_WEB_URL);
            intent.setData(content_url);
            try {
                startActivity(intent);
            } catch (Exception e) {
            }
        }
        SDKWrapper.addEvent(this, SDKWrapper.P1, "act", "cligp");
        PushUIHelper.getInstance(this).sendACK(mAdID, true, mFromStatusBar, phone);
        finish();
    }

    private boolean hasGPInstalled() {
        PackageManager pm = this.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm
                .queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resolveInfos) {
            LeoLog.d(TAG, "name=" + info.activityInfo.packageName);
            if (info.activityInfo.packageName.equals(GP_MARKET_PACKAGE_NAME)) {
                return true;
            }
        }
        return false;
    }

    private void reLayout(boolean fromStatusbar, String adID, String title, String content) {
        mFromStatusBar = fromStatusbar;
        mAdID = adID;
        SDKWrapper.addEvent(this, SDKWrapper.P1, "act", "popup");
        if (mFromStatusBar) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "act", "notbar");
        }
        initUI(title, content);
    }

    @Override
    public void onNewAct(final boolean fromStatusbar, final String adID, final String title, final String content) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                LeoLog.d(TAG, "new act arrived: adID=" + adID + ";    title=" + title + ";    content=" + content);
                reLayout(fromStatusbar, adID, title, content);
            }
        });
    }
}
