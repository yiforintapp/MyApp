
package com.leo.appmaster.callfilter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.CommonEvent;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
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
    private List<ContactBean> mSysContacts;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CallFilterConstants.CALL_FILTER_LIST_LOAD_DONE:
                    boolean isFistLoad = (Boolean) msg.obj;
                    loadDone(isFistLoad);
                    break;
            }
        }
    };

    private void loadDone(boolean isFirstLoadDone) {
        mProgressBar.setVisibility(View.GONE);
        if (mFilterList.size() < 1) {
            showEmpty();
        } else {
            mClearAll.setBackgroundResource(R.drawable.green_radius_btn_shape);
            mClearAll.setRippleColor(getResources().getColor(R.color.button_green_ripple));
            if (isFirstLoadDone) {
                CallFilterMainActivity activity = (CallFilterMainActivity) mActivity;
                activity.moveToFilterFragment();
            }
            mNothingToShowView.setVisibility(View.GONE);
            mCallListView.setVisibility(View.VISIBLE);
            mAdapter.setFlag(CallFilterConstants.ADAPTER_FLAG_CALL_FILTER);
            mAdapter.setData(mFilterList);
        }
    }

    public void showEmpty() {
        mCallListView.setVisibility(View.GONE);
        mNothingToShowView.setVisibility(View.VISIBLE);
        mClearAll.setBackgroundResource(R.drawable.green_radius_shape_disable);
        mClearAll.setRippleColor(getResources().getColor(R.color.button_gray_ripple));
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.call_filter_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LeoEventBus.getDefaultBus().register(this);
        super.onCreate(savedInstanceState);
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

        loadData(true);
    }

    private void loadData(final boolean isFristLoad) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mFilterList = mCallManger.getCallFilterGrList();

                mSysContacts = PrivacyContactUtils.getSysContact(mActivity, null, null, true);

                //load done
                Message msg = Message.obtain();
                msg.obj = isFristLoad;
                msg.what = CallFilterConstants.CALL_FILTER_LIST_LOAD_DONE;
                handler.sendMessage(msg);


            }
        });
    }

    public void onEventMainThread(CommonEvent event) {
        String msg = event.eventMsg;
        if (CallFilterConstants.EVENT_MSG_LOAD_FIL_GR.equals(msg)) {
            loadData(false);
        }

    }

    @Override
    public void onDestroy() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        LeoLog.d("testResume", "Dialog resume");
        if (!isFristIn) {
            loadData(false);
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
        Intent intent;
        try {
            intent = new Intent(mActivity, CallFilterRecordActivity.class);
            Bundle bundle = new Bundle();
            CallFilterInfo info = mFilterList.get(i);
            info.setIcon(null);
            bundle.putSerializable("data", info);
            intent.putExtras(bundle);
            intent.putExtra("isSysContact", checkIsSysContact(mFilterList.get(i).getNumber()));
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
        if (mFilterList.size() > 0) {
            CallFilterInfo info = mFilterList.get(i);

            boolean isNeedMarkItem = true;
            if (mSysContacts.size() > 0) {
                isNeedMarkItem = !checkIsSysContact(info.getNumber());
            }

            final LEOChoiceDialog dialog = CallFIlterUIHelper.getInstance().
                    getCallHandleDialog(info.numberName, mActivity, isNeedMarkItem);

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

    private boolean checkIsSysContact(String number) {
        for (int i = 0; i < mSysContacts.size(); i++) {
            if (mSysContacts.get(i).getContactNumber().equals(number)) {
                return true;
            }
        }
        return false;
    }

    private void showChoiseDialog(final int i) {
        String title;
        if (Utilities.isEmpty(mFilterList.get(i).getNumberName())) {
            title = mFilterList.get(i).getNumber();
        } else {
            title = mFilterList.get(i).getNumberName();
        }

        final MultiChoicesWitchSummaryDialog dialog = CallFIlterUIHelper.getInstance().
                getCallHandleDialogWithSummary(title, mActivity, false, mFilterList.get(i).getFilterType());

        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                dialog.setNowItemPosition(position);

            }
        });

        dialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {

                CallFilterInfo info = mFilterList.get(i);

                if (position == 0) {
                    info.setFilterType(1);
                } else if (position == 1) {
                    info.setFilterType(2);
                } else if (position == 2) {
                    info.setFilterType(3);
                }

                List<BlackListInfo> list = new ArrayList<BlackListInfo>();
                BlackListInfo newInfo = new BlackListInfo();
                newInfo.setNumber(info.getNumber());
                newInfo.setLocHandlerType(position + 1);
                list.add(newInfo);
                mCallManger.addBlackList(list, true);

                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });


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
        if (mFilterList != null && mFilterList.size() <= 0) {
            showEmpty();
        }
    }
}
