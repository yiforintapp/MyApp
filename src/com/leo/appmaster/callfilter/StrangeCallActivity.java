
package com.leo.appmaster.callfilter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.text.TextUtils;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterManagerImpl;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEORoundProgressDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;

public class StrangeCallActivity extends BaseActivity implements OnItemClickListener, OnClickListener {
    private static final String TAG = "AddFromCallLogListActivity";
    private static final int MINUTE = 60;
    private static final int STRA_MAX_COUNT = 100;
    private static final int HAVE_BLACK_LIST = -2;
    private static final int CUT_PROCESS = -3;
    private List<ContactCallLog> mCallLogList;
    private List<ContactCallLog> mSrcBackupList;
    private CommonToolbar mTitleBar;
    private CallLogAdapter mCallLogAdapter;
    private ListView mListCallLog;
    private LEORoundProgressDialog mProgressDialog;
    private List<ContactCallLog> mAddPrivacyCallLog;
    private boolean mLogFlag = false;
    private ProgressBar mProgressBar;
    private ImageView mAddAll;
    private View mSelectAll;
    private View mEmptyView;
    private RippleView mAddBtn;
    private final int MAX_ITEM_SIZE = 100;
    private boolean mLoadDone = false;
    private AddFromCallHandler mAddFromCallHandler = new AddFromCallHandler();
    private RelativeLayout mRlBottomView;
    private boolean isCutProgress = false;

    private LEOAlarmDialog mShareDialog;

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

            if (currentValue == CUT_PROCESS) {
                Context context = StrangeCallActivity.this;
                String str = getResources().getString(R.string.add_black_list_done);
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                StrangeCallActivity.this.finish();
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
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.setProgress(currentValue);
                } else {
                    isCutProgress = true;
                }

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
        Intent intent = getIntent();
        boolean fromNotif = intent.getBooleanExtra("fromNotif", false); //是否从陌生人通知进入
        if (fromNotif) {
            showShareDialog();
        }
    }

    private void showShareDialog() {
        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        int currentTimes = preferenceTable.getInt(PrefConst.ENTER_CALL_FILTER_TIMES, 1);
        int limitTimes = preferenceTable.getInt(PrefConst.KEY_CALL_FILTER_SHARE_TIMES, 10);
        if (currentTimes < limitTimes) {  // 小于限制次数
            preferenceTable.putInt(PrefConst.ENTER_CALL_FILTER_TIMES, currentTimes + 1);
            return;
        }
        if (preferenceTable.getBoolean(PrefConst.CALL_FILTER_SHOW, false)) {
            return;
        }
        if (mShareDialog == null) {
            mShareDialog = new LEOAlarmDialog(StrangeCallActivity.this);
            mShareDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mShareDialog != null) {
                        mShareDialog = null;
                    }
                }
            });
        }
        String content = getString(R.string.callfilter_share_dialog_content);
        String shareButton = getString(R.string.share_dialog_btn_query);
        String cancelButton = getString(R.string.share_dialog_query_btn_cancel);
        mShareDialog.setContent(content);
        mShareDialog.setLeftBtnStr(cancelButton);
        mShareDialog.setRightBtnStr(shareButton);
        mShareDialog.setLeftBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SDKWrapper.addEvent(StrangeCallActivity.this, SDKWrapper.P1, "block", "block_noShare");
                if (mShareDialog != null && mShareDialog.isShowing()) {
                    mShareDialog.dismiss();
                    mShareDialog = null;
                }
            }
        });
        mShareDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mShareDialog != null && mShareDialog.isShowing()) {
                    mShareDialog.dismiss();
                    mShareDialog = null;
                }
                shareApps();
            }
        });
        mShareDialog.show();
        preferenceTable.putBoolean(PrefConst.CALL_FILTER_SHOW, true);
    }


    /** 分享应用 */
    private void shareApps() {
        SDKWrapper.addEvent(StrangeCallActivity.this, SDKWrapper.P1, "block", "block_share");
        mLockManager.filterSelfOneMinites();
        PreferenceTable sharePreferenceTable = PreferenceTable.getInstance();
        boolean isContentEmpty = TextUtils.isEmpty(
                sharePreferenceTable.getString(PrefConst.KEY_CALL_FILTER_SHARE_CONTENT));
        boolean isUrlEmpty = TextUtils.isEmpty(
                sharePreferenceTable.getString(PrefConst.KEY_CALL_FILTER_SHARE_URL));

        StringBuilder shareBuilder = new StringBuilder();
        if (!isContentEmpty && !isUrlEmpty) {
            shareBuilder.append(sharePreferenceTable.getString(PrefConst.KEY_CALL_FILTER_SHARE_CONTENT))
                        .append(" ")
                        .append(sharePreferenceTable.getString(PrefConst.KEY_CALL_FILTER_SHARE_URL));
        } else {
            shareBuilder.append(getResources().getString(R.string.callfilter_share_content))
                        .append(" ")
                        .append(Constants.DEFAULT_SHARE_URL);
        }
        Utilities.toShareApp(shareBuilder.toString(), getTitle().toString(), StrangeCallActivity.this);
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
        for (ContactCallLog callLog : mCallLogList) {
            callLog.setCheck(false);
        }
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

                    SDKWrapper.addEvent(StrangeCallActivity.this, SDKWrapper.P1, "block", "recently_all");

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
            SDKWrapper.addEvent(StrangeCallActivity.this, SDKWrapper.P1, "block", "recently_add");
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
            ImageView checkImage;
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
            String number = mb.getCallLogNumber();

            //android.content.res.Resources$NotFoundException: String resource ID #0x7f0c0315
            vh.name.setText(number);

            try {
                int addToBlackNum = mb.getAddBlackNumber();
                if (addToBlackNum > 0) {
                    CallFilterManagerImpl cmp = (CallFilterManagerImpl)
                            MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                    int showPar = cmp.getBlackMarkTipParam();
                    vh.addnum.setVisibility(View.VISIBLE);
                    String text = getString(R.string.call_filter_add_to_blacklist_people_tips, addToBlackNum * showPar + "");
                    vh.addnum.setText(text);
                } else {
                    vh.addnum.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                vh.addnum.setVisibility(View.GONE);
            }


            String newDate = changeToNewDate(mb.getClallLogDate());
            vh.date.setText(newDate);
            String duration = getRightTime((int) mb.getCallLogDuraction());
            vh.callduration.setText(duration);

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
                        if (CallFilterConstants.ADD_BLACK_LIST_MODEL.equals(model)) {
                            for (ContactCallLog contact : mAddPrivacyCallLog) {
                                if (!isCutProgress) {
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
                                            info.name = name;
                                            info.number = number;
                                            blackList.add(info);

                                        }
                                    }
                                    Message messge = new Message();
                                    count = count + 1;
                                    messge.what = count;
                                    if (messge != null && mHandler != null) {
                                        mHandler.sendMessage(messge);
                                    }
                                    boolean inerFlag = mCallManger.addBlackList(blackList, false);
                                    if (!inerFlag) {
                                        CallFilterHelper cm = CallFilterHelper.getInstance(StrangeCallActivity.this);
                                        cm.addBlackFailTip();
                                    }
                                } else {
                                    Message curMsg = new Message();
                                    curMsg.what = CUT_PROCESS;
                                    if (curMsg != null && mHandler != null) {
                                        mHandler.sendMessage(curMsg);
                                    }
                                    break;
                                }
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
            numbers.add(blackList.get(i).number);
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
                    String selection = CallLog.Calls.TYPE + " = ? or " + CallLog.Calls.TYPE + " = ? ";
                    String[] selectionArgs = new String[]{String.valueOf(CallLog.Calls.INCOMING_TYPE),
                            String.valueOf(CallLog.Calls.MISSED_TYPE)};
                    String sortOrder = null;
                    List<ContactCallLog> callLogList = PrivacyContactUtils.
                            getSysCallLogNoContact(StrangeCallActivity.this, selection, selectionArgs, sortOrder, false, true);
                    // LeoLog.d(TAG, "zany, getSysCallLogNoContact: " + (SystemClock.elapsedRealtime() - start));
                    if (callLogList != null && callLogList.size() > 0) {
                        Collections.sort(callLogList, PrivacyContactUtils.mCallLogCamparator);
                        List<ContactCallLog> calls = new ArrayList<ContactCallLog>();
                        for (ContactCallLog call : callLogList) {
                            if (!mCallManger.isPrivacyConUse(call.getCallLogNumber())) {

                                //add to black list num
                                CallFilterManagerImpl cmp = (CallFilterManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                                BlackListInfo info = cmp.getSerBlackForNum(call.getCallLogNumber());
                                if (info != null) {
                                    int[] type = cmp.isCallFilterTip(call.getCallLogNumber());
                                    if (type[0] != CallFilterConstants.IS_TIP_DIA[0]) {
                                        int addToBlackNum = info.blackNum;
                                        // LeoLog.d("testAddBlack", "addToBlackNum:" + addToBlackNum);
                                        if (addToBlackNum > 0) {
                                            call.setAddBlackNumber(addToBlackNum);
                                        }
                                    }
                                }
                                calls.add(call);
                            }
                        }
                        // LeoLog.d(TAG, "zany, getSerBlackForNum total: " + (SystemClock.elapsedRealtime() - tStart));
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
