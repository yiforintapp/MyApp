
package com.leo.appmaster.sdk.push.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
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
    private TextView mMaxCharHint;

    /* popup window stuff */
    private LeoPopMenu mLeoPopMenu;
    private ImageView mCategoryImg;
    private TextView mCategory;
    private final static int[] sCategoryIds = {
            R.string.wish_msg1, R.string.wish_msg2, R.string.wish_msg3,
            R.string.wish_msg4, R.string.wish_msg5, R.string.wish_custom
    };
    private String mWishMessage = "Happy new year!";

    private final ArrayList<String> mCategories = new ArrayList<String>();

    private Handler mHandler;

    /* view pager */
    private View view1, view2, view3;
    private ViewPager viewPager; // 对应的viewPager
    private List<View> viewList;// view数组

    private final static int INDIAN_MOBILE_LENGTH = 10;
    private final static int CUSTOM_WISH_CHAR_LIMITED = 79;

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
        setContentView(R.layout.activity_new_year);

        mPhoneNumber = (EditText) findViewById(R.id.et_phone);
        mPhoneNumber.setRawInputType(InputType.TYPE_CLASS_PHONE);
        mPhoneNumber.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(INDIAN_MOBILE_LENGTH)
        });
        //
        // TextView tvInputHeader = (TextView)
        // findViewById(R.id.tv_input_header);
        // tvInputHeader.setText(getString(R.string.friend_phone_number));
        //
        // TextView tvTitle = (TextView) findViewById(R.id.dlg_title);
        // tvTitle.setText(title);
        //
        // TextView tvContent = (TextView) findViewById(R.id.dlg_content);
        // tvContent.setText(content);
        // tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        //
        // TextView tvCancel = (TextView) findViewById(R.id.dlg_left_btn);
        // tvCancel.setText(getString(R.string.cancel));
        // tvCancel.setOnClickListener(this);
        //
        Button btnSend = (Button) findViewById(R.id.btn_send_sms);
        btnSend.setOnClickListener(this);

        testViewPager();
        //
        // /* init custom wish mesage input box and max char hint */
        // mMaxCharHint = (TextView) findViewById(R.id.tv_max_char_hint);
        // mMaxCharHint.setVisibility(View.GONE);
        // mCustomMsgET = (EditText) findViewById(R.id.custom_msg_content);
        // mCustomMsgET.setVisibility(View.GONE);
        // mCustomMsgET.setFilters(new InputFilter[] {
        // new InputFilter.LengthFilter(CUSTOM_WISH_CHAR_LIMITED)
        // });
        // mCustomMsgET.addTextChangedListener(new TextWatcher() {
        //
        // @Override
        // public void onTextChanged(CharSequence arg0, int arg1, int arg2, int
        // arg3) {
        // }
        //
        // @Override
        // public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
        // int arg3) {
        // }
        //
        // @Override
        // public void afterTextChanged(Editable arg0) {
        // LeoLog.d(TAG, "length = " + arg0.length());
        // handleWishMsgLimit(arg0.length());
        // }
        // });
        //
        // /* init popup for default messages */
        // View dropView = findViewById(R.id.default_wishes_layout);
        // mCategory = (TextView) findViewById(R.id.wishes_title);
        // mCategoryImg = (ImageView)
        // findViewById(R.id.feedback_category_arrow);
        // for (int i = 0; i < sCategoryIds.length; i++) {
        // mCategories.add(getString(sCategoryIds[i]));
        // }
        // dropView.setOnClickListener(this);
    }

    private void handleWishMsgLimit(int length) {
        if (CUSTOM_WISH_CHAR_LIMITED - length < 10) {
            String hint = getString(R.string.wish_char_hint, CUSTOM_WISH_CHAR_LIMITED - length);
            mMaxCharHint.setText(hint);
            mMaxCharHint.setVisibility(View.VISIBLE);
        } else {
            mMaxCharHint.setVisibility(View.GONE);
        }
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
            case R.id.btn_send_sms:
                /* get money */
                handleCommit();
                break;
        }
    }

    private void addToOutbox(String address, String content) {
        final String ADDRESS = "address";
        final String DATE = "date";
        final String READ = "read";
        final String STATUS = "status";
        final String TYPE = "type";
        final String BODY = "body";
        int MESSAGE_TYPE_SENT = 2;
        ContentValues values = new ContentValues();
        /* 手机号 */
        values.put(ADDRESS, address);
        /* 时间 */
        values.put(DATE, System.currentTimeMillis());
        values.put(READ, 1);
        values.put(STATUS, -1);
        /* 类型1为收件箱，2为发件箱 */
        values.put(TYPE, MESSAGE_TYPE_SENT);
        /* 短信体内容 */
        values.put(BODY, content);
        /* 插入数据库操作 */
        Uri inserted = getContentResolver().insert(Uri.parse("content://sms"),
                values);
    }

    private void sendSMS(String destAddress, String content,
            PendingIntent sentIntent, PendingIntent deliveryIntent) {
        // SmsManager smsManager = SmsManager.getDefault();
        // List<String> divideContents = smsManager.divideMessage(content);
        // for (String text : divideContents) {
        // smsManager.sendTextMessage(destAddress, null, text, sentIntent,
        // deliveryIntent);
        // }
        LeoLog.d(TAG, "sms=" + content);
        LeoLog.d(TAG, "phone_num=" + destAddress);

        /* add this SMS to OUTBOX */
        addToOutbox(destAddress, content);

        /* send toast */
        Toast.makeText(this, getString(R.string.wish_sms_sent), Toast.LENGTH_SHORT).show();
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

    private void testViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        LayoutInflater inflater = getLayoutInflater();

        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        for (int i = 0; i < 10; i++) {
            view1 = new ImageView(this);
//            view1.setBackgroundResource(R.drawable.newyear_act_poster);
            viewList.add(view1);
        }

        PagerAdapter pagerAdapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                // TODO Auto-generated method stub
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return viewList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                    Object object) {
                // TODO Auto-generated method stub
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // TODO Auto-generated method stub
                container.addView(viewList.get(position));
                ImageView imgView = (ImageView) viewList.get(position);
                imgView.setBackgroundResource(R.drawable.newyear_act_poster);
                return viewList.get(position);
            }
        };

        viewPager.setAdapter(pagerAdapter);
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
        String smsTail = getString(R.string.sms_tail);
        String smsBody = mWishMessage + smsTail;
        sendSMS(phone, smsBody, null, null);

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
