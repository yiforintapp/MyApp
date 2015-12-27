
package com.leo.appmaster.callfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.privacycontact.AddFromContactListActivity;
import com.leo.appmaster.privacycontact.CircleImageView;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.privacycontact.PrivacyContactManager;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEORoundProgressDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

public class StrangeCallActivity extends BaseActivity implements OnItemClickListener, OnClickListener {
    private static final String TAG = "AddFromCallLogListActivity";
    private static final int HAVE_BLACK_LIST = -2;
    private List<ContactCallLog> mCallLogList;
    private CommonToolbar mTitleBar;
    private CallLogAdapter mCallLogAdapter;
    private ListView mListCallLog;
    private LEORoundProgressDialog mProgressDialog;
    private List<ContactCallLog> mAddPrivacyCallLog;
    private boolean mLogFlag = false;
    private ProgressBar mProgressBar;
    private String mFrom;
    private ImageView mAddAll;
    private View mEmptyView;
    private RippleView mAddBtn;
    private boolean mLoadDone = false;
    private AddFromCallHandler mAddFromCallHandler = new AddFromCallHandler();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentValue = msg.what;

            if (currentValue == HAVE_BLACK_LIST) {
                Context context = StrangeCallActivity.this;
                String str = getResources().getString(R.string.call_filter_have_add_black_num);
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                return;
            }


            if (currentValue >= mAddPrivacyCallLog.size()) {
                if (!mLogFlag) {
                    if (mProgressDialog != null) {
                        mProgressDialog.cancel();
                    }
                    Context context = StrangeCallActivity.this;
                    String str = getResources().getString(R.string.add_black_list_done);
                    Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                    StrangeCallActivity.this.finish();
                } else {
                    if (mProgressDialog != null) {
                        mProgressDialog.cancel();
                    }
                    mLogFlag = false;
                }
            } else {
                mProgressDialog.setProgress(currentValue);
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_strange_call_log);
        handleIntent();
        initUI();
        sendMsgHandler();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mFrom = intent.getStringExtra(CallFilterConstants.FROMWHERE);
    }

    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.add_privacy_call_log_title_bar);
        mTitleBar.setToolbarTitle(R.string.call_filter_black_list_unknow_num);
        mTitleBar.setToolbarColorResource(R.color.cb);

        mAddAll = (ImageView) findViewById(R.id.iv_add_all_black);
        mAddAll.setTag(false);
        mAddAll.setOnClickListener(this);
        mEmptyView = findViewById(R.id.add_call_log_default_tv);
        mAddBtn = (RippleView) findViewById(R.id.rv_button_backup);
        mAddBtn.setOnClickListener(this);
        mListCallLog = (ListView) findViewById(R.id.add_privacy_call_logLV);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_loading);
        mListCallLog.setOnItemClickListener(this);
        mCallLogList = new ArrayList<ContactCallLog>();
        mAddPrivacyCallLog = new ArrayList<ContactCallLog>();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
        mListCallLog.post(new Runnable() {
            @Override
            public void run() {
                for (ContactCallLog callLog : mCallLogList) {
                    callLog.setCheck(false);
                }
            }
        });
        mHandler = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        Boolean isAllCheck = (Boolean) mAddAll.getTag();

        ContactCallLog callLog = mCallLogList.get(position);
        ImageView image = (ImageView) view.findViewById(R.id.calllog_item_check_typeIV);
        if (!callLog.isCheck()) {
            mAddPrivacyCallLog.add(callLog);
            image.setImageDrawable(getResources().getDrawable(R.drawable.select));
            callLog.setCheck(true);

            if (isAllCheck()) {
                mAddAll.setTag(true);
                mAddAll.setImageDrawable(getResources().getDrawable(R.drawable.select));
            }

        } else {
            mAddPrivacyCallLog.remove(callLog);
            image.setImageDrawable(getResources().getDrawable(R.drawable.unselect));
            callLog.setCheck(false);

            if (isAllCheck) {
                mAddAll.setTag(false);
                mAddAll.setImageDrawable(getResources().getDrawable(R.drawable.unselect));
            }

        }
    }

    private boolean isAllCheck() {
        for (ContactCallLog callLog : mCallLogList) {
            if (!callLog.isCheck()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_add_all_black:
                if (mLoadDone) {
                    boolean isCheck = (Boolean) mAddAll.getTag();
                    if (isCheck) {
                        mAddAll.setTag(false);
                        mAddAll.setImageDrawable(getResources().getDrawable(R.drawable.unselect));
                        selectAll(false);
                    } else {
                        mAddAll.setTag(true);
                        mAddAll.setImageDrawable(getResources().getDrawable(R.drawable.select));
                        selectAll(true);
                    }
                }
                break;
            case R.id.rv_button_backup:
                addToBlackList();
                break;
        }
    }

    private void addToBlackList() {
        if (mLoadDone) {
            if (mCallLogList != null && mCallLogList.size() > 0) {
                if (mAddPrivacyCallLog.size() > 0 && mAddPrivacyCallLog != null) {

                    if (mAddPrivacyCallLog.size() == 1) {
                        boolean isHaveBlackNum = mCallManger.
                                isExistBlackList(mAddPrivacyCallLog.get(0).getCallLogNumber());
                        if (isHaveBlackNum) {
                            Message messge = new Message();
                            messge.what = HAVE_BLACK_LIST;
                            if (messge != null && mHandler != null) {
                                mHandler.sendMessage(messge);
                            }
                        } else {
                            showProgressDialog(mAddPrivacyCallLog.size(), 0);
                            sendImpLogHandler(CallFilterConstants.ADD_BLACK_LIST_MODEL);
                        }
                    } else {
                        showProgressDialog(mAddPrivacyCallLog.size(), 0);
                        sendImpLogHandler(CallFilterConstants.ADD_BLACK_LIST_MODEL);
                    }

                } else {
                    Toast.makeText(StrangeCallActivity.this,
                            getResources().getString(R.string.privacy_contact_toast_no_choose),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void selectAll(boolean isCheck) {

        mAddPrivacyCallLog.clear();

        for (ContactCallLog callLog : mCallLogList) {
            if (isCheck) {
                callLog.setCheck(true);
                mAddPrivacyCallLog.add(callLog);
            } else {
                callLog.setCheck(false);
            }
        }


        mCallLogAdapter.notifyDataSetChanged();
    }

    private class CallLogAdapter extends BaseAdapter {
        LayoutInflater relativelayout;
        List<ContactCallLog> callLog;

        public CallLogAdapter(List<ContactCallLog> callLog) {
            relativelayout = LayoutInflater.from(StrangeCallActivity.this);
            this.callLog = callLog;
        }

        @Override
        public int getCount() {
            return (callLog != null) ? callLog.size() : 0;
        }

        @Override
        public Object getItem(int position) {

            return callLog.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        class ViewHolder {
            CircleImageView contactIcon;
            TextView name, date, addnum, callduration;
            ImageView checkImage, typeImage;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = relativelayout.inflate(R.layout.activity_add_strange_call_log_item,
                        null);
                vh.name = (TextView) convertView.findViewById(R.id.add_from_call_log_item_nameTV);

                vh.addnum = (TextView) convertView
                        .findViewById(R.id.add_from_call_log_item_dateTV);

//                vh.date = (TextView) convertView
//                        .findViewById(R.id.tv_call_time);
//
//                vh.callduration = (TextView) convertView
//                        .findViewById(R.id.tv_call_total_time);

                vh.checkImage = (ImageView) convertView
                        .findViewById(R.id.calllog_item_check_typeIV);
                vh.contactIcon = (CircleImageView) convertView.findViewById(R.id.contactIV);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            ContactCallLog mb = callLog.get(position);
            if (mb.getCallLogName() != null && !mb.getCallLogName().equals("")) {
                vh.name.setText(mb.getCallLogName());
            } else {
                vh.name.setText(mb.getCallLogNumber());
            }

//            vh.date.setText(mb.getClallLogDate());


            if (mb.isCheck()) {
                vh.checkImage.setImageResource(R.drawable.select);
            } else {
                vh.checkImage.setImageResource(R.drawable.unselect);
            }
            Bitmap icon = mb.getContactIcon();
            vh.contactIcon.setImageBitmap(icon);

            return convertView;
        }
    }

    /*加载通话通话列表*/
    private void sendImpLogHandler(final String model) {
        if (mAddFromCallHandler != null) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    int isExistLog = PrivacyContactUtils.NO_EXIST_LOG;
                    try {
                        int count = 0;
                        ContentResolver cr = getContentResolver();
                        if (CallFilterConstants.ADD_BLACK_LIST_MODEL.equals(model)) {
                            List<BlackListInfo> blackList = new ArrayList<BlackListInfo>();
                            for (ContactCallLog contact : mAddPrivacyCallLog) {
                                String name = contact.getCallLogName();
                                String contactNumber = contact.getCallLogNumber();
                                String number = PrivacyContactUtils.simpleFromateNumber(contact.getCallLogNumber());
                                /*隐私联系人去重,判断是否为隐私联系人*/
                                boolean isPryCont = PrivacyContactUtils.pryContRemovSame(contactNumber);
                                if (!isPryCont) {
                                    boolean isHaveBlackNum = mCallManger.isExistBlackList(number);
                                    if (!isHaveBlackNum) {

                                        BlackListInfo info = new BlackListInfo();
                                        info.setNumberName(name);
                                        info.setNumber(number);
                                        blackList.add(info);

                                    }
                                }
                                Message messge = new Message();
                                count = count + 1;
                                messge.what = count;
                                if (messge != null && mHandler != null) {
                                    mHandler.sendMessage(messge);
                                }
                                mCallManger.addBlackList(blackList, false);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_CALL_QU;
                    msg.arg1 = isExistLog;
                    mAddFromCallHandler.sendMessage(msg);
                }
            });
        }
    }


    private void showProgressDialog(int maxValue, int currentValue) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEORoundProgressDialog(this);
        }
        String title = getResources().getString(R.string.privacy_contact_progress_dialog_title);
        String content = getResources().getString(R.string.privacy_contact_progress_dialog_content);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(content);
        mProgressDialog.setMax(maxValue);
        mProgressDialog.setProgress(currentValue);
        mProgressDialog.setButtonVisiable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        try {
            mProgressDialog.show();
        } catch (Exception e) {

        }
    }


    private class AddFromCallHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PrivacyContactUtils.MSG_ADD_CALL:
                    if (msg.obj != null) {
                        LeoLog.i(TAG, "load  calls finish !");
                        List<ContactCallLog> calls = (List<ContactCallLog>) msg.obj;
                        if (mCallLogList != null) {
                            mCallLogList.clear();
                        }
                        mCallLogList = calls;
                        try {
                            mProgressBar.setVisibility(View.GONE);
                            if (mCallLogList != null && mCallLogList.size() > 0) {
                                mEmptyView.setVisibility(View.GONE);
                            } else {
                                mEmptyView.setVisibility(View.VISIBLE);
                            }
                            mCallLogAdapter = new CallLogAdapter(mCallLogList);
                            mListCallLog.setAdapter(mCallLogAdapter);
                            mLoadDone = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case PrivacyContactUtils.MSG_CALL_QU:
                    try {
                        LeoLog.d("testCallLog", "EXIST_LOG else");
                        if (mProgressDialog != null) {
                            mProgressDialog.cancel();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /*加载通话通话列表*/
    private void sendMsgHandler() {
        if (mAddFromCallHandler != null) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.VISIBLE);
//                    List<ContactCallLog> callLogList = PrivacyContactUtils.
//                            getSysCallLog(StrangeCallActivity.this, null, null, false, false);
                    List<ContactCallLog> callLogList = PrivacyContactUtils.
                            getSysCallLogNoContact(StrangeCallActivity.this, null, null, false, false);
                    if (callLogList != null && callLogList.size() > 0) {
                        Collections.sort(callLogList, PrivacyContactUtils.mCallLogCamparator);
                    }

                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_ADD_CALL;
                    msg.obj = callLogList;
                    mAddFromCallHandler.sendMessage(msg);
                }
            });
        }
    }

}
