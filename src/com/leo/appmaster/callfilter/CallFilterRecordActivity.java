package com.leo.appmaster.callfilter;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.RippleView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callfilter_record);
        handleIntent();
        initUI();
        processUI();
    }

    private void processUI() {
        String numberName = info.getNumberName();
        String number = info.getNumber();
        if (Utilities.isEmpty(numberName) || numberName.equals(number)) {
            mTvTitleName.setVisibility(View.GONE);
        } else {
            mMark.setVisibility(View.GONE);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        info = (CallFilterInfo) intent.getExtras().get("data");
    }

    private void initUI() {
        mTvTitleName = (TextView) findViewById(R.id.tv_callfilter_record_title_name);
        mTvTitleName.setText(info.getNumberName());
        mTvTitleNumber = (TextView) findViewById(R.id.tv_callfilter_record_title_number);
        mTvTitleNumber.setText(info.getNumber());

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
    }

    private void fillData() {
        mRecordTime.clear();
        mRecordTime = mCallManger.getFilterDetListFroNum(info.getNumber());
//        mRecordTime.add("4:43 am");
//        mRecordTime.add("5:10 am");
//        mRecordTime.add("6:28 am");
//        mRecordTime.add("9:28 am");
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
                MultiChoicesWitchSummaryDialog dialog = CallFIlterUIHelper.getInstance().
                        getCallHandleDialogWithSummary(title, this, false);
                dialog.show();

                break;
            default:
                break;
        }
    }

    private void showRemoveDialog() {
        final LEOWithSingleCheckboxDialog mDialog = CallFIlterUIHelper.getInstance().
                getConfirmRemoveFromBlacklistDialog(this);
        mDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                showRemoveDialog();
                List<BlackListInfo> list = new ArrayList<BlackListInfo>();
                BlackListInfo blacklistInfo = new BlackListInfo();
                blacklistInfo.setNumber(info.getNumber());
                list.add(blacklistInfo);
                mCallManger.removeBlackList(list);

                List<CallFilterInfo> removeFilterList = new ArrayList<CallFilterInfo>();
                removeFilterList.add(info);
                mCallManger.removeFilterGr(removeFilterList);

                mDialog.dismiss();
                onBackPressed();
            }
        });
        mDialog.show();
    }


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
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
            ItemHolder holder = null;
            if (convertView == null) {
                holder = new ItemHolder();
                View view = LayoutInflater.from(CallFilterRecordActivity.this).inflate(R.layout.item_callfilter_record, null);
                holder.tv = (TextView) view.findViewById(R.id.tv_record_time);
                view.setTag(holder);
                convertView = view;
            } else {
                holder = (ItemHolder) convertView.getTag();
            }
            holder.tv.setText(mRecordTime.get(position).getTimeLong() + "");
            return convertView;
        }
    }

    class ItemHolder {
        public TextView tv;
    }
}
