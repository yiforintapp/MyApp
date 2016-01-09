package com.leo.appmaster.callfilter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOWithSingleCheckboxDialog;
import com.leo.appmaster.ui.dialog.MultiChoicesWitchSummaryDialog;
import com.leo.appmaster.utils.Utilities;

public class CallFilterRecordActivity extends BaseActivity implements OnClickListener {
    private TextView mTvTitleName;
    private TextView mTvTitleNumber;
    private ListView mLvMain;
    private RippleView mBackBtn;
    private RippleView mRemoveBlackList;
    private RippleView mMark;
    private List<CallFilterInfo> mRecordTime;
    private CallFilterInfo info;
    private MyAdapter mAdapter;
    private View mClearFilter;
    private boolean isSysContact;
    private List<ContactBean> mSysContacts;
    private boolean isFirstIn = true;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CallFilterConstants.FILTER_INFO_SYS_CONTACT:
                    processUI();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callfilter_record);
        handleIntent();
        initUI();
        processUI();
    }

    private void processUI() {

        int mark = info.getFilterType();
        String name = info.getNumberName();
        String number = info.getNumber();
        if (!isSysContact) {
            if (mark == 0) {
                if (number.equals(name)) {
                    mTvTitleName.setVisibility(View.GONE);
                } else {
                    mTvTitleName.setText(name);
                }
            } else {
                String string;
                if (mark == 1) {
                    string = this.getString(R.string.filter_number_type_saorao);
                } else if (mark == 2) {
                    string = this.getString(R.string.filter_number_type_ad);
                } else {
                    string = this.getString(R.string.filter_number_type_zhapian);
                }
                mTvTitleName.setText(string);
                mMark.setVisibility(View.GONE);
            }
        } else {
            mMark.setVisibility(View.GONE);
            mTvTitleName.setText(name);
        }

    }

    private void handleIntent() {
        Intent intent = getIntent();
        info = (CallFilterInfo) intent.getExtras().get("data");
        isSysContact = intent.getBooleanExtra("isSysContact", false);
    }

    private void initUI() {
        mTvTitleName = (TextView) findViewById(R.id.tv_callfilter_record_title_name);
        mTvTitleName.setText(info.getNumberName());
        mTvTitleNumber = (TextView) findViewById(R.id.tv_callfilter_record_title_number);
        mTvTitleNumber.setText(info.getNumber());

        mClearFilter = findViewById(R.id.clear_all_fliter);
        mClearFilter.setOnClickListener(this);

        mLvMain = (ListView) findViewById(R.id.lv_callfilter_record_main);
        mAdapter = new MyAdapter();

        mBackBtn = (RippleView) findViewById(R.id.rv_back);
        mBackBtn.setOnClickListener(this);
        mRemoveBlackList = (RippleView) findViewById(R.id.remove_black_list);
        mRemoveBlackList.setOnClickListener(this);
        mMark = (RippleView) findViewById(R.id.mark_number);
        mMark.setOnClickListener(this);
        mRecordTime = new ArrayList<CallFilterInfo>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
        if (!isFirstIn) {
            loadSysContact();
        }
        isFirstIn = false;
    }

    private void loadSysContact() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mSysContacts = PrivacyContactUtils.
                        getSysContact(CallFilterRecordActivity.this, null, null, true);
                isSysContact = checkIsSysContact(info.getNumber());
                handler.sendEmptyMessage(CallFilterConstants.FILTER_INFO_SYS_CONTACT);
            }
        });
    }

    private boolean checkIsSysContact(String number) {
        for (int i = 0; i < mSysContacts.size(); i++) {
            if (mSysContacts.get(i).getContactNumber().equals(number)) {
                return true;
            }
        }
        return false;
    }

    private void fillData() {
        List<CallFilterInfo> list = mCallManger.getFilterDetListFroNum(info.getNumber());
        mRecordTime.clear();
        if (list != null) {
            mRecordTime.addAll(list);
        }
        mLvMain.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rv_back:
                finish();
                break;
            case R.id.remove_black_list:
                showRemoveDialog();
                break;
            case R.id.mark_number:

                String title;
                if (Utilities.isEmpty(info.getNumberName())) {
                    title = info.getNumber();
                } else {
                    title = info.getNumberName();
                }

                showMarkDialog(title);
                break;
            case R.id.clear_all_fliter:
                showRemoveAllFilter();
                break;
            default:
                break;
        }
    }

    private void showRemoveAllFilter() {
        final LEOAlarmDialog dialog = CallFIlterUIHelper.getInstance().
                getConfirmClearAllRecordDialog(this);
        dialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                List<CallFilterInfo> removeFilterList = new ArrayList<CallFilterInfo>();
                removeFilterList.add(info);
                mCallManger.removeFilterGr(removeFilterList);

                dialog.dismiss();
                onBackPressed();
            }
        });
        dialog.show();
    }

    private void showMarkDialog(String title) {
        final MultiChoicesWitchSummaryDialog dialog = CallFIlterUIHelper.getInstance().
                getCallHandleDialogWithSummary(title, this, false, info.getFilterType(), false);
        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                dialog.setNowItemPosition(position);
            }
        });

        dialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {

                String string = "";
                if (mTvTitleName.getVisibility() == View.GONE) {
                    mTvTitleName.setVisibility(View.VISIBLE);
                }

                if (position == 0) {
                    info.setFilterType(1);
                    string = CallFilterRecordActivity.this.
                            getString(R.string.filter_number_type_saorao);
                } else if (position == 1) {
                    info.setFilterType(2);
                    string = CallFilterRecordActivity.this.
                            getString(R.string.filter_number_type_ad);
                } else if (position == 2) {
                    info.setFilterType(3);
                    string = CallFilterRecordActivity.this.
                            getString(R.string.filter_number_type_zhapian);
                }
                mTvTitleName.setText(string);
                mMark.setVisibility(View.GONE);

                List<BlackListInfo> list = new ArrayList<BlackListInfo>();
                BlackListInfo newInfo = new BlackListInfo();
                newInfo.setNumber(info.getNumber());
                newInfo.setLocHandlerType(position + 1);
                list.add(newInfo);
                mCallManger.addBlackList(list, true);


                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void showRemoveDialog() {
        final LEOWithSingleCheckboxDialog mDialog = CallFIlterUIHelper.getInstance().
                getConfirmRemoveFromBlacklistDialog(this);
        mDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<BlackListInfo> list = new ArrayList<BlackListInfo>();
                BlackListInfo blacklistInfo = new BlackListInfo();
                blacklistInfo.setNumber(info.getNumber());
                list.add(blacklistInfo);
                mCallManger.removeBlackList(list);
                boolean restrLog = mDialog.getCheckBoxState();
                if (restrLog) {
                    CallFilterContextManagerImpl cmp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);

                    List<CallFilterInfo> infos = cmp.getFilterDetListFroNum(info.getNumber());
                    if (infos != null && infos.size() > 0) {
                        for (CallFilterInfo CallInfo : infos) {
                            cmp.insertCallToSys(CallInfo);
                        }
                }
            }
                List<CallFilterInfo> removeFilterList = new ArrayList<CallFilterInfo>();
                removeFilterList.add(info);
                mCallManger.removeFilterGr(removeFilterList);
            mDialog.dismiss();

            onBackPressed();
        }
    }

    );
    mDialog.show();
}


class MyAdapter extends BaseAdapter {

    @Override
    public int getCount() {
        if (mRecordTime == null) {
            return 0;
        }
        return mRecordTime.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder;
        if (convertView == null) {
            holder = new ItemHolder();
            View view = LayoutInflater.from(CallFilterRecordActivity.this).inflate(R.layout.item_callfilter_record, null);
            holder.tv = (TextView) view.findViewById(R.id.tv_record_time);
            view.setTag(holder);
            convertView = view;
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        long time = mRecordTime.get(position).getTimeLong();
        SimpleDateFormat finalFormat = new SimpleDateFormat("yyyy-MM-dd hh:mma");
        holder.tv.setText(finalFormat.format(time));

        return convertView;
    }
}

class ItemHolder {
    public TextView tv;
}
}
