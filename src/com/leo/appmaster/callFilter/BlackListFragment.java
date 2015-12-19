
package com.leo.appmaster.callFilter;

import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;

public class BlackListFragment extends BaseFragment {

    private ListView mBlackListView;
    private View mNothingToShowView;
    private BlackListAdapter mBlackListAdapter;
    private ProgressBar mProgressBar;
    private ArrayList<CallFilterInfo> mBlackList;
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
                case CallFilterConstants.BLACK_LIST_LOAD_DONE:
                    loadDone();
                    break;
            }
        }
    };


    private void loadDone() {
        mProgressBar.setVisibility(View.GONE);
        if (mBlackList.size() < 1) {
            mBlackListView.setVisibility(View.GONE);
            mNothingToShowView.setVisibility(View.VISIBLE);
        } else {
            mBlackListView.setVisibility(View.VISIBLE);
            mBlackListAdapter.setFlag(CallFilterConstants.ADAPTER_FLAG_BLACK_LIST);
            LeoLog.d("testBlackList", "list size : " + mBlackList.size());
            mBlackListAdapter.setData(mBlackList);
        }
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.black_list_fragment;
    }

    @Override
    protected void onInitUI() {
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);
        mBlackListView = (ListView) findViewById(R.id.list_black_list);
        mBlackListAdapter = new BlackListAdapter(mActivity);
        mBlackListView.setAdapter(mBlackListAdapter);
        mBlackListView.setVisibility(View.GONE);

        mNothingToShowView = findViewById(R.id.content_show_nothing);

        loadData();
    }

    private void loadData() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                //load
                //TODO
                mBlackList = getBlackListData();
                //load done
                handler.sendEmptyMessageDelayed(CallFilterConstants.BLACK_LIST_LOAD_DONE,
                        1000);
                LeoLog.d("testBlackList", "send!");
            }
        });
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

    public ArrayList<CallFilterInfo> getBlackListData() {
        ArrayList<CallFilterInfo> list = new ArrayList<CallFilterInfo>();
        for (int i = 0; i < mStringName.length; i++) {
            CallFilterInfo info = new CallFilterInfo();
            info.numberName = getNumberName(i);
            LeoLog.d("testBlackList", "name:" + info.numberName);
            info.number = getNumber(i);
            LeoLog.d("testBlackList", "number:" + info.number);
            list.add(info);
        }
        return list;
    }

    private String getNumberName(int i) {
        String mStr = mStringName[i];
        String name = mStr.split("_")[0];
        return name;
    }

    private String getNumber(int i) {
        String mStr = mStringName[i];
        String number = mStr.split("_")[1];
        return number;
    }
}
