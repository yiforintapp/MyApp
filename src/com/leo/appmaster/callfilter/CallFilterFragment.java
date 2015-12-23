
package com.leo.appmaster.callfilter;

import android.content.DialogInterface;
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
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.ui.dialog.MultiChoicesWitchSummaryDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class CallFilterFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView mCallListView;
    private View mNothingToShowView;
    private RippleView mClearAll;
    private ProgressBar mProgressBar;
    private CallFilterFragmentAdapter mAdapter;
    private List<CallFilterInfo> mFilterList;
    private boolean isFristIn = true;

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
            mNothingToShowView.setVisibility(View.GONE);
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
                mFilterList = mCallManger.getCallFilterGrList();
                //load done
                handler.sendEmptyMessage(CallFilterConstants.CALL_FILTER_LIST_LOAD_DONE);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear_all:
                if (mFilterList.size() > 0) {
                    clearAll();
                }
                break;
        }
    }

    private void clearAll() {
        final LEOAlarmDialog dialog = CallFIlterUIHelper.getInstance().
                getConfirmClearAllRecordDialog(mActivity);
        dialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mCallManger.removeFilterGr(mFilterList);

                mFilterList.clear();
                mAdapter.setData(mFilterList);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(mActivity, CallFilterRecordActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
        if (mFilterList.size() > 0) {
            CallFilterInfo info = mFilterList.get(i);
            final LEOChoiceDialog dialog = CallFIlterUIHelper.getInstance().
                    getCallHandleDialog(info.numberName, mActivity);
            dialog.getItemsListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LeoLog.d("testPosition", "position : " + position);

                    if (position == 0) {
                        deleteFilter(i);
                        dialog.dismiss();
                    } else if (position == 1) {
                        removeBlackList(i);
                        dialog.dismiss();
                    } else if (position == 2) {
                        dialog.dismiss();
                        showChoiseDialog(i);
                    }

                }
            });
            dialog.show();

        }
        return false;
    }

    private void showChoiseDialog(int i) {
        String title;
        if (Utilities.isEmpty(mFilterList.get(i).getNumberName())) {
            title = mFilterList.get(i).getNumber();
        } else {
            title = mFilterList.get(i).getNumberName();
        }

        MultiChoicesWitchSummaryDialog dialog = CallFIlterUIHelper.getInstance().
                getCallHandleDialogWithSummary(title, mActivity, false);
        dialog.show();
    }


    private void removeBlackList(int position) {
        //remove Filter
        List<CallFilterInfo> removeFilterList = new ArrayList<CallFilterInfo>();
        CallFilterInfo infoFilter = mFilterList.get(position);
        removeFilterList.add(infoFilter);
        mCallManger.removeFilterGr(removeFilterList);

        //remove BlackList
        List<BlackListInfo> removeBlacklist = new ArrayList<BlackListInfo>();
        BlackListInfo infoBlack = new BlackListInfo();
        infoBlack.setNumber(infoFilter.getNumber());
        removeBlacklist.add(infoBlack);
        mCallManger.removeBlackList(removeBlacklist);


        mFilterList.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    private void deleteFilter(int position) {
        List<CallFilterInfo> removeList = new ArrayList<CallFilterInfo>();
        CallFilterInfo info = mFilterList.get(position);
        removeList.add(info);
        mCallManger.removeFilterGr(removeList);

        mFilterList.remove(position);
        mAdapter.notifyDataSetChanged();
    }
}
