
package com.leo.appmaster.privacycontact;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrivacyCallLogListActivity extends BaseActivity implements OnClickListener {
    private ListView mContactCallLog;
    private static final String CONTACT_CALL_LOG = "contact_call_log";
    private static final String UPDATE_CALL_LOG_FRAGMENT = "update_call_log_fragment";
    private String mCallLogNumber;
    private CallLogAdapter mAdapter;
    private CommonToolbar mComTitle;
    private ArrayList<ContactCallLog> mContactCallLogs;

    @Override
    public void onBackgroundVisibleBehindChanged(boolean visible) {
        super.onBackgroundVisibleBehindChanged(visible);
    }

    private ImageView mSendMessageView, mCallPhone;
    private CircleImageView mContactImage;
    private String mName;
    private TextView mNameTV, mNumberTV;
    private SimpleDateFormat mSimpleDate = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat mDateFormatSecond = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Date mDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_call_log_item);
        mContactCallLogs = new ArrayList<ContactCallLog>();
        mComTitle = (CommonToolbar) findViewById(R.id.privacy_call_log_item_title_bar);
        mComTitle.setToolbarColorResource(R.color.ctc);
        mComTitle.setToolbarTitle(R.string.privacy_contact_calllog);
        mComTitle.setOptionMenuVisible(false);
        mContactImage = (CircleImageView) findViewById(R.id.contactIV);
        mNameTV = (TextView) findViewById(R.id.add_from_call_log_item_nameTV);
        mNumberTV = (TextView) findViewById(R.id.add_from_call_log_item_dateTV);
        mContactCallLog = (ListView) findViewById(R.id.contactLV);
        mSendMessageView = (ImageView) findViewById(R.id.calllog_item_sendmessage);
        mCallPhone = (ImageView) findViewById(R.id.calllog_item_call);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String[] bundleData = bundle.getStringArray(CONTACT_CALL_LOG);
            mName = bundleData[0];
            mCallLogNumber = bundleData[1];
            if (mCallLogNumber != null) {
                if (mName != null && !"".equals(mName)) {
                    mNameTV.setText(mName);
                } else {
                    mNameTV.setText(mCallLogNumber);
                }
                mNumberTV.setText(mCallLogNumber);
            }
        }
        // 获取头像
        Bitmap icon = PrivacyContactUtils.getContactIcon(this, mCallLogNumber);
        if (icon != null) {
            int size = (int) this.getResources().getDimension(R.dimen.contact_icon_scale_size);
            icon = PrivacyContactUtils.getScaledContactIcon(icon, size);
            mContactImage.setImageBitmap(icon);
        } else {
            mContactImage.setImageResource(R.drawable.default_user_avatar);
        }
        LeoEventBus.getDefaultBus().register(this);
        mComTitle.setNavigationClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LeoEventBus.getDefaultBus().post(
                        new PrivacyEditFloatEvent(
                                PrivacyContactUtils.UPDATE_CALL_LOG_FRAGMENT));
                PrivacyCallLogListActivity.this.finish();
            }
        });
        getCallLog(mCallLogNumber);
        mAdapter = new CallLogAdapter(mContactCallLogs);
        mContactCallLog.setAdapter(mAdapter);
        mCallPhone.setOnClickListener(this);
        mSendMessageView.setOnClickListener(this);
        // 处于隐私通话详情查看状态不发送通知
        AppMasterPreference.getInstance(this).setCallLogItemRuning(false);
    }

    public void onEventMainThread(PrivacyEditFloatEvent event) {
        if (UPDATE_CALL_LOG_FRAGMENT.equals(event.editModel)) {
            mContactCallLogs.clear();
            getCallLog(mCallLogNumber);
            mAdapter.notifyDataSetChanged();
        } else if (PrivacyContactUtils.PRIVACY_ALL_CALL_NOTIFICATION_HANG_UP
                .equals(event.editModel)) {
            mContactCallLogs.clear();
            getCallLog(mCallLogNumber);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LeoEventBus.getDefaultBus().post(
                new PrivacyEditFloatEvent(
                        PrivacyContactUtils.UPDATE_CALL_LOG_FRAGMENT));
        PrivacyCallLogListActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        LeoEventBus.getDefaultBus().unregister(this);
        // 标识该Activity销毁通知
        AppMasterPreference.getInstance(this).setCallLogItemRuning(true);
        super.onDestroy();
    }

    @Override
    public void onClick(View arg0) {
        int flag = arg0.getId();
        switch (flag) {
            case R.id.calllog_item_sendmessage:
                String[] bundleData = new String[]{
                        mName, mCallLogNumber
                };
                Bundle bundle = new Bundle();
                bundle.putStringArray(Constants.LOCK_MESSAGE_THREAD_ID, bundleData);
                Intent intentSendMessage = new Intent(this, PrivacyMessageItemActivity.class);
                intentSendMessage.putExtras(bundle);
                try {
                    startActivity(intentSendMessage);
                    /* SDK */
                    SDKWrapper.addEvent(PrivacyCallLogListActivity.this, SDKWrapper.P1, "sendmesg",
                            "call");
                } catch (Exception e) {
                }
                break;
            case R.id.calllog_item_call:
                // 查询该号码是否为隐私联系人
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(mCallLogNumber);
                PrivacyContactManager pm = PrivacyContactManager.getInstance(PrivacyCallLogListActivity.this);
                ContactBean privacyConatact = pm.getPrivateMessage(formateNumber, PrivacyCallLogListActivity.this);
                PrivacyContactManager.getInstance(PrivacyCallLogListActivity.this).setLastCall(
                        privacyConatact);
                Uri uri = Uri.parse("tel:" + mCallLogNumber);
                // Intent intent = new Intent(Intent.ACTION_CALL, uri);

                mLockManager.filterSelfOneMinites();
                mLockManager.filterPackage(Constants.PKG_CONTACTS, 1000);
                mLockManager.filterPackage(Constants.PKG_DIALER, 1000);

                Intent intent = new Intent(Intent.ACTION_DIAL,
                        uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                    /* SDK */
                    SDKWrapper.addEvent(PrivacyCallLogListActivity.this, SDKWrapper.P1, "call",
                            "call");
                } catch (Exception e) {
                }
                break;
            default:
                break;
        }

    }

    @SuppressLint("CutPasteId")
    private class CallLogAdapter extends BaseAdapter {
        LayoutInflater relativelayout;
        ArrayList<ContactCallLog> contacts;

        public CallLogAdapter(ArrayList<ContactCallLog> contacts) {
            relativelayout = LayoutInflater.from(PrivacyCallLogListActivity.this);
            this.contacts = contacts;
        }

        @Override
        public int getCount() {

            return (contacts != null) ? contacts.size() : 0;
        }

        @Override
        public Object getItem(int position) {

            return contacts.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        class ViewHolder {
            ImageView typeImage, lineImage, bottomLineImage;
            TextView date, showDate, type, call_duration;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = relativelayout.inflate(R.layout.activity_privacy_call_log_list_item,
                        null);
                vh.date = (TextView) convertView.findViewById(R.id.message_item_date);
                vh.typeImage = (ImageView) convertView.findViewById(R.id.message_item_typeIM);
                vh.type = (TextView) convertView.findViewById(R.id.message_item_typeTV);
                vh.showDate = (TextView) convertView.findViewById(R.id.message_item_nameTV);
                vh.lineImage = (ImageView) convertView
                        .findViewById(R.id.call_log_item_top_bottom_line);
                vh.bottomLineImage = (ImageView) convertView
                        .findViewById(R.id.call_log_item_bottom_line);
                vh.call_duration = (TextView) convertView.findViewById(R.id.call_duration_TV);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            ContactCallLog mb = contacts.get(position);
            String time = null;
            try {
                mDate = mDateFormatSecond.parse(mb.getClallLogDate());
                time = mSimpleDate.format(mDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            vh.date.setText(time);
            if (mb != null) {
                vh.typeImage.setVisibility(View.VISIBLE);
                if (mb.getClallLogType() == CallLog.Calls.INCOMING_TYPE) {
                    vh.typeImage.setImageResource(R.drawable.into_icon);
                    vh.type.setText(getResources().getString(R.string.privacy_contact_callog_in));
                } else if (mb.getClallLogType() == CallLog.Calls.OUTGOING_TYPE) {
                    vh.typeImage.setImageResource(R.drawable.exhale_icon);
                    vh.type.setText(getResources().getString(R.string.privacy_contact_callog_out));
                } else if (mb.getClallLogType() == CallLog.Calls.MISSED_TYPE) {
                    vh.typeImage.setImageResource(R.drawable.into_icon);
                    vh.type.setText(getResources().getString(R.string.privacy_contact_callog_in));
                }
            }
            if (mb.getShowDate() != null && !"".equals(mb.getShowDate())) {
                vh.showDate.setVisibility(View.VISIBLE);
                vh.showDate.setText(mb.getShowDate());
                if (position == 0) {
                    vh.lineImage.setVisibility(View.GONE);
                } else {

                    vh.lineImage.setVisibility(View.VISIBLE);
                    if (position == mContactCallLogs.size() - 1) {
                        vh.bottomLineImage.setVisibility(View.VISIBLE);
                    } else {
                        vh.bottomLineImage.setVisibility(View.GONE);
                    }
                }
            } else {
                vh.showDate.setVisibility(View.GONE);
            }
            long duration = mb.getCallLogDuraction();

            if (duration > 0) {
                int m = (int) (duration / 60);
                int s = (int) (duration % 60);
                if (m > 0) {
                    if (s > 0) {
                        vh.call_duration.setText(getResources().getString(R.string.call_duration, m, s));
                    } else {
                        vh.call_duration.setText(getResources().getString(R.string.call_duration, m, 0));
                    }
                } else {
                    vh.call_duration.setText(getResources().getString(R.string.call_duration, 0, s));
                }
            } else {
                vh.call_duration.setText(getResources().getString(R.string.call_duration, 0, 0));
            }
            return convertView;
        }
    }

    /**
     * getCallLog
     *
     * @param phoneNumber
     * @return
     */
    private void getCallLog(String phoneNumber) {
        List<String> showDate = new ArrayList<String>();
        String number = PrivacyContactUtils.formatePhoneNumber(phoneNumber);
        Cursor cursor = getContentResolver().query(Constants.PRIVACY_CALL_LOG_URI, null,
                Constants.COLUMN_CALL_LOG_PHONE_NUMBER + " LIKE ? ", new String[]{
                        "%" + number
                }, Constants.COLUMN_CALL_LOG_DATE + " desc");
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    ContactCallLog callLog = new ContactCallLog();
                    callLog.setCallLogCount(cursor.getCount());
                    String numberCall = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_CALL_LOG_PHONE_NUMBER));
                    callLog.setCallLogNumber(numberCall);
                    callLog.setCallLogName(cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_CALL_LOG_CONTACT_NAME)));
                    String date = cursor
                            .getString(cursor.getColumnIndex(Constants.COLUMN_CALL_LOG_DATE));
                    callLog.setClallLogDate(date);
                    callLog.setClallLogType(cursor.getInt(cursor
                            .getColumnIndex(Constants.COLUMN_CALL_LOG_TYPE)));
                    int isRead = cursor
                            .getInt(cursor.getColumnIndex(Constants.COLUMN_CALL_LOG_IS_READ));
                    callLog.setCallLogDuraction(cursor
                            .getInt(cursor.getColumnIndex(Constants.COLUMN_CALL_LOG_DURATION)));
                    if (isRead == 0) {
                        String readNumberFlag = PrivacyContactUtils.formatePhoneNumber(numberCall);
                        PrivacyCalllogFragment.updateCallLogMyselfIsRead(1,
                                "call_log_phone_number LIKE ? ",
                                new String[]{
                                        "%" + readNumberFlag
                                }, this);
                    }
                    try {
                        // 同一天只显示一次
                        Date time = new Date(date);
                        SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd");
                        String showDateTemp = sd.format(time);
                        if (showDate == null || showDate.size() == 0) {
                            showDate.add(showDateTemp);
                            callLog.setShowDate(showDateTemp);
                        } else {
                            if (!showDate.contains(showDateTemp)) {
                                showDate.add(showDateTemp);
                                callLog.setShowDate(showDateTemp);
                            }
                        }
                        mContactCallLogs.add(callLog);
                    } catch (Exception e) {

                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

}
