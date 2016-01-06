
package com.leo.appmaster.callfilter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import android.provider.CallLog;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
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
    private static final int MINUTE = 60;
    private static final int HOUR = 60 * 60;
    private static final int STRA_MAX_COUNT = 100;

    private static final int HAVE_BLACK_LIST = -2;
    private static final int PAGE_SIZE = 100;
    private List<ContactCallLog> mCallLogList;
    private List<ContactCallLog> mSrcBackupList;
    private CommonToolbar mTitleBar;
    private CallLogAdapter mCallLogAdapter;
    private ListView mListCallLog;
    private LEORoundProgressDialog mProgressDialog;
    private List<ContactCallLog> mAddPrivacyCallLog;
    private boolean mLogFlag = false;
    private ProgressBar mProgressBar;
    private String mFrom;
    private ImageView mAddAll;
    private View mSelectAll;
    private View mEmptyView;
    private RippleView mAddBtn;
    private final int MAX_ITEM_SIZE = 100;
    private boolean mLoadDone = false;
    private AddFromCallHandler mAddFromCallHandler = new AddFromCallHandler();
    private RelativeLayout mRlBottomView;

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

        cal(55);
        cal(88);
        cal(131);
        cal(2400);
        cal(3665);
        cal(665846);

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
        mRlBottomView = (RelativeLayout) findViewById(R.id.rl_bottomview);
        mAddAll = (ImageView) findViewById(R.id.iv_add_all_black);
        mAddAll.setTag(false);
//        mAddAll.setOnClickListener(this);
        mSelectAll = findViewById(R.id.click_check_box);
        mSelectAll.setOnClickListener(this);
        mSelectAll.setEnabled(false);

        mEmptyView = findViewById(R.id.add_call_log_default_tv);
        mAddBtn = (RippleView) findViewById(R.id.rv_button_backup);
        mAddBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_radius_shape_disable));
        mAddBtn.setEnabled(false);
        mAddBtn.setOnClickListener(this);

        mListCallLog = (ListView) findViewById(R.id.add_privacy_call_logLV);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_loading);
        mListCallLog.setOnItemClickListener(this);
        mCallLogList = new ArrayList<ContactCallLog>();
        mSrcBackupList = new ArrayList<ContactCallLog>();
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
        if (mAddPrivacyCallLog.size() != 0) {
            mAddBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_radius_btn_shape));
            mAddBtn.setEnabled(true);
        } else {
            mAddBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_radius_shape_disable));
            mAddBtn.setEnabled(false);
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
            case R.id.click_check_box:
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
                if (mAddPrivacyCallLog.size() != 0) {
                    mAddBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_radius_btn_shape));
                    mAddBtn.setEnabled(true);
                } else {
                    mAddBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_radius_shape_disable));
                    mAddBtn.setEnabled(false);
                }
            } else {
                mAddBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_radius_shape_disable));
                mAddBtn.setEnabled(false);
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
            if (callLog != null) {
                return Math.min(callLog.size(), MAX_ITEM_SIZE);
            } else {
                return 0;
            }
//            return (callLog != null) ? callLog.size() : 0;
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
            ImageView contactIcon;
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
                vh.date = (TextView) convertView
                        .findViewById(R.id.time_strange);
                vh.callduration = (TextView) convertView
                        .findViewById(R.id.duration_strange);
                vh.checkImage = (ImageView) convertView
                        .findViewById(R.id.calllog_item_check_typeIV);
                vh.contactIcon = (ImageView) convertView.findViewById(R.id.contactIV);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            ContactCallLog mb = callLog.get(position);
            vh.name.setText(mb.getCallLogNumber());

            int addToBlackNum = mb.getAddBlackNumber();
            if (addToBlackNum > 0) {
                CallFilterContextManagerImpl cmp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                int showPar = cmp.getBlackMarkTipParam();
                vh.addnum.setVisibility(View.VISIBLE);
                vh.addnum.setText(StrangeCallActivity.this.getString(
                        R.string.call_filter_add_to_blacklist_people_tips, addToBlackNum * showPar));
            } else {
                vh.addnum.setVisibility(View.GONE);
            }

            String newDate = changeToNewDate(mb.getClallLogDate());
            vh.date.setText(newDate);
            vh.callduration.setText(getRightTime((int) mb.getCallLogDuraction()));

            if (mb.getCallLogDuraction() > 0) {
                vh.contactIcon.setImageResource(R.drawable.pick_up_call);
            } else {
                vh.contactIcon.setImageResource(R.drawable.no_pick_up_call);
            }

            if (mb.isCheck()) {
                vh.checkImage.setImageResource(R.drawable.select);
            } else {
                vh.checkImage.setImageResource(R.drawable.unselect);
            }

//            Bitmap icon = mb.getContactIcon();
//            vh.contactIcon.setImageBitmap(icon);

            return convertView;
        }

    }

    public String cal(int second) {
        String strings;
        int h = 0;
        int d = 0;
        int s = 0;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }

        if (h == 0) {
            strings = StrangeCallActivity.this.getString(
                    R.string.number_call_duration_m_s, d, s);
        } else {
            strings = StrangeCallActivity.this.getString(
                    R.string.number_call_duration_h_m, h, d);
        }

        LeoLog.d("testTime", h + "时" + d + "分" + s + "秒");
        return strings;
    }

    private String getRightTime(int mSecond) {
        String string;

        if (mSecond < MINUTE) {
            if (mSecond == -1) {
                string = StrangeCallActivity.this.getString(
                        R.string.number_call_duration_s, 0);
            } else {
                string = StrangeCallActivity.this.getString(
                        R.string.number_call_duration_s, mSecond);
            }
        } else {
            string = cal(mSecond);
        }


//        if (mSecond < MINUTE) {
//            if (mSecond == -1) {
//                string = StrangeCallActivity.this.getString(
//                        R.string.number_call_duration_s, 0);
//            } else {
//                string = StrangeCallActivity.this.getString(
//                        R.string.number_call_duration_s, mSecond);
//            }
//        } else if (mSecond < HOUR) {
//            string = StrangeCallActivity.this.getString(
//                    R.string.number_call_duration_m, mSecond / MINUTE);
//        } else {
//            string = StrangeCallActivity.this.getString(
//                    R.string.number_call_duration_h, mSecond / HOUR);
//        }

        return string;
    }

    private String changeToNewDate(String clallLogDate) {
        String newString = clallLogDate;
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            Date date = sfd.parse(clallLogDate);
            long time = date.getTime();

            newString = getTime(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newString;
    }

    private String getTime(long time) {
        boolean showDay = false;
        boolean showYear = false;
        SimpleDateFormat finalFormat;
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        String thatYear = yearFormat.format(time);
        String toYear = yearFormat.format(System.currentTimeMillis());
        if (!thatYear.equals(toYear)) {
            showYear = true;
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        String thatDay = dayFormat.format(time);
        String toDay = dayFormat.format(System.currentTimeMillis());
        if (!thatDay.equals(toDay)) {
            showDay = true;
        }


        if (showYear) {
            finalFormat = new SimpleDateFormat("yyyy-MM-dd hh:mma");
        } else if (showDay) {
            finalFormat = new SimpleDateFormat("MM-dd hh:mma");
        } else {
            finalFormat = new SimpleDateFormat("hh:mma");
        }

        String finalString = finalFormat.format(time);

        return finalString;
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
                            for (ContactCallLog contact : mAddPrivacyCallLog) {
                                List<BlackListInfo> blackList = new ArrayList<BlackListInfo>();
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
                        //备份一份onCreate时获取的原始数据
                        if (mSrcBackupList != null) {
                            mSrcBackupList.clear();
                            mSrcBackupList.addAll(mCallLogList);
                        }
                        try {
                            mProgressBar.setVisibility(View.GONE);
                            mCallLogAdapter = new CallLogAdapter(mCallLogList);
                            fillOutBlacklistNumber(mCallLogList);

                            if (mCallLogList != null && mCallLogList.size() > 0) {
                                mEmptyView.setVisibility(View.GONE);
                                mSelectAll.setEnabled(true);
                                mRlBottomView.setVisibility(View.VISIBLE);
                            } else {
                                mEmptyView.setVisibility(View.VISIBLE);
                                mSelectAll.setEnabled(false);
                                mRlBottomView.setVisibility(View.GONE);
                            }

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

    private void fillOutBlacklistNumber(List<ContactCallLog> mCallLogList) {
        LeoLog.i(TAG, "start fill " + mCallLogList.size());
        List<BlackListInfo> blackList = mCallManger.getBlackList();
        List<String> numbers = new ArrayList<String>();
        List<ContactCallLog> toRemove = new ArrayList<ContactCallLog>();
        for (int i = 0; i < blackList.size(); i++) {
            numbers.add(blackList.get(i).getNumber());
            LeoLog.i(TAG, "numbers " + i + " : " + numbers.get(numbers.size() - 1));
        }
        for (int i = 0; i < mCallLogList.size(); i++) {
            LeoLog.i(TAG, "phoneNumber " + i + " : " + mCallLogList.get(i).getCallLogNumber());
            if (numbers.contains(mCallLogList.get(i).getCallLogNumber())) {
//                mCallLogList.remove(i);
                toRemove.add(mCallLogList.get(i));
            }
        }
        mCallLogList.removeAll(toRemove);
        LeoLog.i(TAG, "end fill " + mCallLogList.size());
    }

    /*加载通话通话列表*/
    private void sendMsgHandler() {
        if (mAddFromCallHandler != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    String selection = null;
                    String[] selectionArgs = null;
                    String sortOrder = null;
                    List<ContactCallLog> callLogList = PrivacyContactUtils.
                            getSysCallLogNoContact(StrangeCallActivity.this, selection, selectionArgs, sortOrder, false, true);
                    if (callLogList != null && callLogList.size() > 0) {
                        Collections.sort(callLogList, PrivacyContactUtils.mCallLogCamparator);
                        List<ContactCallLog> calls = new ArrayList<ContactCallLog>();
                        for (ContactCallLog call : callLogList) {

                            LeoLog.d("testAddBlack", "go item");

                            if (CallLog.Calls.OUTGOING_TYPE != call.getClallLogType()
                                    && !mCallManger.isPrivacyConUse(call.getCallLogNumber())) {

                                //add to black list num
                                CallFilterContextManagerImpl cmp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                                BlackListInfo info = cmp.getSerBlackForNum(call.getCallLogNumber());

                                if (info != null) {
                                    int[] type = cmp.isCallFilterTip(call.getCallLogNumber());
                                    if (type[0] != CallFilterConstants.IS_TIP_DIA[0]) {
                                        int addToBlackNum = info.getAddBlackNumber();
                                        LeoLog.d("testAddBlack", "addToBlackNum:" + addToBlackNum);
                                        if (addToBlackNum > 0) {
                                            call.setAddBlackNumber(addToBlackNum);
                                        }
                                    }
                                }
                                calls.add(call);
                            }
                        }
                        callLogList.clear();
                        callLogList.addAll(calls);
                    }

                    //取该集合前100条数据
                    List<ContactCallLog> calls = new ArrayList<ContactCallLog>();
                    if (callLogList != null && callLogList.size() > 0) {
                        int end = callLogList.size() > STRA_MAX_COUNT ? STRA_MAX_COUNT : callLogList.size();
                        calls = callLogList.subList(0, end);
                    }
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_ADD_CALL;
                    msg.obj = calls;
                    mAddFromCallHandler.sendMessage(msg);
                }
            });
        }
    }

}
