
package com.leo.appmaster.callfilter;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;

public class CallFilterFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView mCallListView;
    private View mNothingToShowView;
    private RippleView mClearAll;
    private ProgressBar mProgressBar;
    private CallFilterFragmentAdapter mAdapter;
    private ArrayList<CallFilterInfo> mFilterList;
    private boolean isFristIn = true;
    private String[] mStringName = new String[]{
            "nameA_13632840685",
            "_15466879846",
            "nameC_13665432165",
            "_15444688987",
            "nameE_15925465487",
            "nameF_15844546873"
    };

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CallFilterConstants.CALL_FILTER_LIST_LOAD_DONE:
                    loadDone();
                    break;
            }
        }
    };

    private void loadDone() {
        mProgressBar.setVisibility(View.GONE);
        if (mFilterList.size() < 1) {
            showEmpty();
        } else {
            mCallListView.setVisibility(View.VISIBLE);
            mAdapter.setFlag(CallFilterConstants.ADAPTER_FLAG_CALL_FILTER);
            mAdapter.setData(mFilterList);
        }
    }

    public void showEmpty() {
        mCallListView.setVisibility(View.GONE);
        mNothingToShowView.setVisibility(View.VISIBLE);
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.call_filter_fragment;
    }

    @Override
    protected void onInitUI() {
        mClearAll = (RippleView) findViewById(R.id.clear_all);
        mClearAll.setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);

        mCallListView = (ListView) findViewById(R.id.list_call_filter);
        mAdapter = new CallFilterFragmentAdapter(mActivity);
        mCallListView.setAdapter(mAdapter);
        mCallListView.setVisibility(View.GONE);

        mCallListView.setOnItemClickListener(this);
        mCallListView.setOnItemLongClickListener(this);
        mNothingToShowView = findViewById(R.id.content_show_nothing);

        loadData();
    }

    private void loadData() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                //load
                //TODO
                mFilterList = getBlackListData();
                //load done
                handler.sendEmptyMessageDelayed(CallFilterConstants.CALL_FILTER_LIST_LOAD_DONE,
                        500);
                LeoLog.d("testBlackList", "send!");
            }
        });
    }

    public ArrayList<CallFilterInfo> getBlackListData() {
        ArrayList<CallFilterInfo> list;
        String blackNums = PreferenceTable.getInstance().getString("blackList");
        if (!Utilities.isEmpty(blackNums)) {
            list = StringToList(blackNums);
        } else {
            String mLongStr = "";
            for (int i = 0; i < mStringName.length; i++) {
                String mStr = mStringName[i];
                if (i == 0) {
                    mLongStr = mStr;
                } else {
                    mLongStr = mLongStr + ":" + mStr;
                }
            }
            LeoLog.d("testBlackList", "number : " + mLongStr);
            PreferenceTable.getInstance().putString("blackList", mLongStr);
            list = StringToList(mLongStr);
        }
        return list;
    }

    private ArrayList<CallFilterInfo> StringToList(String mStrings) {
        ArrayList<CallFilterInfo> list = new ArrayList<CallFilterInfo>();
        String[] strings = mStrings.split(":");
        for (int i = 0; i < strings.length; i++) {
            CallFilterInfo info = new CallFilterInfo();
            info.numberName = getNumberName(i, strings);
            LeoLog.d("testBlackList", "numberName : " + info.numberName);
            info.number = getNumber(i, strings);
            LeoLog.d("testBlackList", "number : " + info.number);
            list.add(info);
        }
        return list;
    }

    private String getNumberName(int i, String[] strings) {
        String mStr = strings[i];
        String name = mStr.split("_")[0];
        return name;
    }

    private String getNumber(int i, String[] strings) {
        String mStr = strings[i];
        String number = mStr.split("_")[1];
        return number;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isFristIn) {
            loadData();
        }
        isFristIn = false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear_all:
                clearAll();
                break;
        }
    }

    private void clearAll() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(mActivity, CallFilterRecordActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mFilterList.size() > 0) {
            CallFilterInfo info = mFilterList.get(i);
            final LEOChoiceDialog dialog = CallFIlterUIHelper.getInstance().
                    getCallHandleDialog(info.number, mActivity);
            dialog.getItemsListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LeoLog.d("testPosition", "position : " + position);
                    dialog.dismiss();
                }
            });
            dialog.show();

        }
        return false;
    }
}
