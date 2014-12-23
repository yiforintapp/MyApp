
package com.leo.appmaster.sdk.push.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.ui.LeoPopMenu.LayoutStyles;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;

public class NewYearActivity extends BaseActivity implements View.OnClickListener, PushUIHelper.NewActListener {

    private final static String TAG = "NewYearActivity";

    private boolean mFromStatusBar;
    private String mAdID;
    private EditText mPhoneNumber = null;
    private EditText mCustomMsgET;
    
    /* popup window stuff */
    private LeoPopMenu mLeoPopMenu;
    private ImageView mCategoryImg;
    private TextView mCategory;
    private final static int[] sCategoryIds = {
        R.string.wish_msg1, R.string.wish_msg2, R.string.wish_msg3,
        R.string.wish_msg4, R.string.wish_msg5, R.string.wish_custom
};

private final ArrayList<String> mCategories = new ArrayList<String>();

    private Handler mHandler;

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
        SDKWrapper.addEvent(this, LeoStat.P1, "act", "popup");
        mFromStatusBar = i.getBooleanExtra(PushUIHelper.EXTRA_WHERE, false);
        mAdID = i.getStringExtra(PushUIHelper.EXTRA_AD_ID);
        if (mAdID == null) {
            mAdID = "unknown";
        }
        if (mFromStatusBar) {
            SDKWrapper.addEvent(this, LeoStat.P1, "act", "notbar");
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
        setContentView(R.layout.dialog_newyear_act_input_alarm);

        mPhoneNumber = (EditText) findViewById(R.id.et_input);
        mPhoneNumber.setGravity(Gravity.CENTER_HORIZONTAL);
        mPhoneNumber.setRawInputType(InputType.TYPE_CLASS_PHONE);
        mPhoneNumber.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(INDIAN_MOBILE_LENGTH)
        });
        
        TextView tvInputHeader = (TextView) findViewById(R.id.tv_input_header);
        tvInputHeader.setText(getString(R.string.friend_phone_number));

        TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        tvTitle.setText(title);
        
        TextView tvContent = (TextView) findViewById(R.id.dlg_content);
        tvContent.setText(content);
        tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());

        TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        tvCancel.setText(getString(R.string.cancel));
        tvCancel.setOnClickListener(this);

        TextView tvGO = (TextView) findViewById(R.id.dlg_right_btn);
        tvGO.setText(getString(R.string.send_sms));
        tvGO.setOnClickListener(this);
        
        mCustomMsgET = (EditText) findViewById(R.id.custom_msg_content);
        mCustomMsgET.setVisibility(View.GONE);
        
        /* init popup for default messages */
        View dropView = findViewById(R.id.default_wishes_layout);
        mCategory = (TextView) findViewById(R.id.wishes_title);
        mCategoryImg = (ImageView) findViewById(R.id.feedback_category_arrow);
        for (int i = 0; i < sCategoryIds.length; i++) {
            mCategories.add(getString(sCategoryIds[i]));
        }
        dropView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dlg_left_btn:
                /* user ignore this activity */
                SDKWrapper.addEvent(this, LeoStat.P1, "act", "cancel");
                PushUIHelper.getInstance(this).sendACK(mAdID, false, mFromStatusBar, "");
                finish();
                break;
            case R.id.dlg_right_btn:
                /* get money */
                handleCommit();
                break;
            case R.id.default_wishes_layout:
                showPopup();
                break;
        }
    }
    
    private void showPopup(){
        if (mLeoPopMenu == null) {
            mLeoPopMenu = new LeoPopMenu();
            mLeoPopMenu.setPopMenuItems(mCategories);
            mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    String text = mCategories.get(position);
                    mCategory.setText(text);
                    mLeoPopMenu.dismissSnapshotList();
                    if(NewYearActivity.this.getString(R.string.wish_custom).equals(text)){
                        /* user want to send a custom wish message */
                        mCustomMsgET.setVisibility(View.VISIBLE);
                        mCustomMsgET.requestFocus();
                    }else{
                        mCustomMsgET.setVisibility(View.GONE);
                    }
                }
            });
        }
        LayoutStyles styles = new LayoutStyles();
        styles.width = LayoutParams.MATCH_PARENT;
        styles.height = LayoutParams.WRAP_CONTENT;
        styles.animation = R.style.PopupListAnimUpDown;
        styles.direction = LeoPopMenu.DIRECTION_DOWN;
        mLeoPopMenu.showPopMenu(this, mCategory, styles, new OnDismissListener() {
            @Override
            public void onDismiss() {
                mCategoryImg.setImageResource(R.drawable.choose_normal);
            }
        });
        mCategoryImg.setImageResource(R.drawable.choose_active);
    }
    
    private void sendSMS(String destAddress, String content,
            PendingIntent sentIntent, PendingIntent deliveryIntent) 
    {
        SmsManager smsManager = SmsManager.getDefault();
        List<String> divideContents = smsManager.divideMessage(content);
        for (String text : divideContents) {
            smsManager.sendTextMessage(destAddress, null, text, sentIntent,
                    deliveryIntent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            PushUIHelper.getInstance(this).sendACK(mAdID, false, mFromStatusBar, "");
            SDKWrapper.addEvent(this, LeoStat.P1, "act", "cancel");
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
        
        /* TODO: phone number available, send SMS to user's best friend */
        sendSMS("13632840685", phone, null, null);
        
        SDKWrapper.addEvent(this, LeoStat.P1, "act", "cligp");
        PushUIHelper.getInstance(this).sendACK(mAdID, true, mFromStatusBar, phone);
        finish();
    }

    private void reLayout(boolean fromStatusbar, String adID, String title, String content) {
        mFromStatusBar = fromStatusbar;
        mAdID = adID;
        SDKWrapper.addEvent(this, LeoStat.P1, "act", "popup");
        if (mFromStatusBar) {
            SDKWrapper.addEvent(this, LeoStat.P1, "act", "notbar");
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
