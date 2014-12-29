
package com.leo.appmaster.sdk.push.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.SmsManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;

public class NewYearActivity extends BaseActivity implements
        View.OnClickListener, PushUIHelper.NewActListener {

    private final static String TAG = "NewYearActivity";

    private boolean mFromStatusBar;
    private String mAdID;
    private EditText mPhoneNumber = null;

    private Handler mHandler;

    /* view pager */
    private ViewpagerView view1;
    private ViewPager mViewPager;
    private List<View> view_List;
    private List<String> msg_Strings;
    private ImageView img_left;
    private ImageView img_right;
    private int currentPosition;

    /* change this */
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
        setContentView(R.layout.activity_new_year);

        /* add cancel area */
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout cancelLayout = (LinearLayout) inflater.inflate(R.layout.newyear_cancel_layout, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addContentView(cancelLayout, lp);
        ImageView cancelImg = (ImageView) findViewById(R.id.img_cancel);
        cancelImg.setOnClickListener(this);

        TextView tvInfo = (TextView) findViewById(R.id.tv_info);
        String info = getString(R.string.newyear_act_info);
        String keyInfo = getString(R.string.key_info);
        int bias = info.indexOf(keyInfo);
        int len = keyInfo.length();
        SpannableString sInfo = new SpannableString(info);
        sInfo.setSpan(new StyleSpan(Typeface.BOLD), bias, bias + len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sInfo.setSpan(new ForegroundColorSpan(Color.argb(255, 254, 205, 50)), bias, bias + len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvInfo.setText(sInfo);

        mPhoneNumber = (EditText) findViewById(R.id.et_phone);
        mPhoneNumber.setRawInputType(InputType.TYPE_CLASS_PHONE);
        mPhoneNumber
                .setFilters(new InputFilter[] {
                    new InputFilter.LengthFilter(
                            INDIAN_MOBILE_LENGTH)
                });

        Button btnSend = (Button) findViewById(R.id.btn_send_sms);
        btnSend.setOnClickListener(this);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        img_left = (ImageView) findViewById(R.id.img_bt_left);
        img_left.setOnClickListener(this);
        img_right = (ImageView) findViewById(R.id.img_bt_right);
        img_right.setOnClickListener(this);

        msg_Strings = new ArrayList<String>();
        msg_Strings.add(this.getString(R.string.wish_msg1));
        msg_Strings.add(this.getString(R.string.wish_msg2));
        msg_Strings.add(this.getString(R.string.wish_msg3));
        msg_Strings.add(this.getString(R.string.wish_msg4));
        msg_Strings.add(this.getString(R.string.wish_msg5));
        msg_Strings.add(this.getString(R.string.wish_msg6));
        msg_Strings.add(this.getString(R.string.wish_msg7));
        msg_Strings.add(this.getString(R.string.wish_msg8));
        msg_Strings.add(this.getString(R.string.wish_msg9));
        msg_Strings.add(this.getString(R.string.wish_msg10));
        msg_Strings.add(this.getString(R.string.wish_custom));

        view_List = new ArrayList<View>();
        for (int i = 0; i < msg_Strings.size(); i++) {
            view1 = new ViewpagerView(NewYearActivity.this, i, msg_Strings);
            view_List.add(view1);
        }

        MyAdapter adapter = new MyAdapter();
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

    }

    private class MyAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(view_List.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(view_List.get(position));
            return view_List.get(position);
        }

        @Override
        public int getCount() {
            return msg_Strings.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_cancel:
                /* user ignore this activity */
                SDKWrapper.addEvent(this, LeoStat.P1, "act", "cancel");
                PushUIHelper.getInstance(this).sendACK(mAdID, false,
                        mFromStatusBar, "");
                finish();
                break;
            case R.id.btn_send_sms:
                /* get money */
                handleCommit();
                break;
            case R.id.img_bt_left:
                if (currentPosition >= 0) {
                    mViewPager.setCurrentItem(currentPosition - 1);
                }
                break;
            case R.id.img_bt_right:
                if (currentPosition <= msg_Strings.size()) {
                    mViewPager.setCurrentItem(currentPosition + 1);
                }
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
        getContentResolver().insert(Uri.parse("content://sms"), values);
    }

    private void sendSMS(String destAddress, String content,
            PendingIntent sentIntent, PendingIntent deliveryIntent) {
        SmsManager smsManager = SmsManager.getDefault();
        List<String> divideContents = smsManager.divideMessage(content);
        for (String text : divideContents) {
            smsManager.sendTextMessage(destAddress, null, text, sentIntent,
                    deliveryIntent);
        }
        LeoLog.d(TAG, "sms=" + content);
        LeoLog.d(TAG, "phone_num=" + destAddress);

        /* add this SMS to OUTBOX */
        addToOutbox(destAddress, content);

        /* send toast */
        Toast.makeText(this, getString(R.string.wish_sms_sent),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            PushUIHelper.getInstance(this).sendACK(mAdID, false,
                    mFromStatusBar, "");
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

        if (phone.length() != INDIAN_MOBILE_LENGTH
                || !p.matcher(phone).matches()) {
            /* invalid indian phone number */
            Toast.makeText(this, getString(R.string.invalid_number),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        /* TODO: phone number available, send SMS to user's best friend */
        String content = view1.getMsgContent(currentPosition);
        if (content == null || content.trim().length() <= 0) {
            Toast.makeText(this, getString(R.string.invalid_wish_msg),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String smsTail = getString(R.string.sms_tail);
        String smsBody = content + smsTail;
        sendSMS(phone, smsBody, null, null);

        SDKWrapper.addEvent(this, LeoStat.P1, "act", "cligp");
        PushUIHelper.getInstance(this).sendACK(mAdID, true, mFromStatusBar,
                phone);
        finish();
    }

    private void reLayout(boolean fromStatusbar, String adID, String title,
            String content) {
        mFromStatusBar = fromStatusbar;
        mAdID = adID;
        SDKWrapper.addEvent(this, LeoStat.P1, "act", "popup");
        if (mFromStatusBar) {
            SDKWrapper.addEvent(this, LeoStat.P1, "act", "notbar");
        }
        initUI(title, content);
    }

    @Override
    public void onNewAct(final boolean fromStatusbar, final String adID,
            final String title, final String content) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                LeoLog.d(TAG, "new act arrived: adID=" + adID + ";    title="
                        + title + ";    content=" + content);
                reLayout(fromStatusbar, adID, title, content);
            }
        });
    }

}
