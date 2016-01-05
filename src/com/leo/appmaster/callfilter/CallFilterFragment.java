
package com.leo.appmaster.callfilter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.CommonEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.ui.dialog.LEOWithSingleCheckboxDialog;
import com.leo.appmaster.ui.dialog.MultiChoicesWitchSummaryDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class CallFilterFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final int TYPE_ANNOY = 1;
    public static final int TYPE_AD = 2;
    public static final int TYPE_CHEAT = 3;

    private ListView mCallListView;
    private View mNothingToShowView;
    private RippleView mClearAll;
    private ProgressBar mProgressBar;
    private CallFilterFragmentAdapter mAdapter;
    private List<CallFilterInfo> mFilterList;
    private boolean isFristIn = true;
    private List<ContactBean> mSysContacts;
    private RelativeLayout mRlBottomView;
    private LEOWithSingleCheckboxDialog mDeleteDialog;

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
            mClearAll.setEnabled(true);
            if (isFirstLoadDone) {
                CallFilterMainActivity activity = (CallFilterMainActivity) mActivity;
//                activity.moveToFilterFragment();
            }
            mRlBottomView.setVisibility(View.VISIBLE);
            mNothingToShowView.setVisibility(View.GONE);
            mCallListView.setVisibility(View.VISIBLE);
            mAdapter.setFlag(CallFilterConstants.ADAPTER_FLAG_CALL_FILTER);
            mAdapter.setData(mFilterList);
        }
    }

    public void showEmpty() {
        mCallListView.setVisibility(View.GONE);
        mNothingToShowView.setVisibility(View.VISIBLE);
        //TODO
        mRlBottomView.setVisibility(View.GONE);
        mClearAll.setBackgroundResource(R.drawable.green_radius_shape_disable);
        mClearAll.setEnabled(false);
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
        mRlBottomView = (RelativeLayout) findViewById(R.id.bottom_view);
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
        CallFilterManager.getInstance(getActivity()).setIsFilterTab(false);
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
                isNeedMarkItem = !checkIsSysContact(info.getNumber()) && info.getFilterType() < 1;
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
        String formatNumber = PrivacyContactUtils.formatePhoneNumber(number);
        for (int i = 0; i < mSysContacts.size(); i++) {
            String sysNumber = mSysContacts.get(i).getContactNumber();
            if (sysNumber.contains(formatNumber)) {
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
                getCallHandleDialogWithSummary(title, mActivity, false, mFilterList.get(i).getFilterType(), false);
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
                    info.setFilterType(TYPE_ANNOY);
                } else if (position == 1) {
                    info.setFilterType(TYPE_AD);
                } else if (position == 2) {
                    info.setFilterType(TYPE_CHEAT);
                }

                List<BlackListInfo> list = new ArrayList<BlackListInfo>();
                BlackListInfo newInfo = new BlackListInfo();
                newInfo.setNumber(info.getNumber());
                newInfo.setLocHandlerType(position + 1);
                list.add(newInfo);
                mCallManger.addBlackList(list, true);

                mAdapter.notifyDataSetChanged();
                Toast.makeText(mActivity, R.string.mark_number_from_list, Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });

        dialog.show();
    }


    private void removeBlackList(final int position) {

        if (mDeleteDialog == null) {
            mDeleteDialog = CallFIlterUIHelper.getInstance().
                    getConfirmRemoveFromBlacklistDialog(mActivity);
        }
        mDeleteDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //remove Filter
                List<CallFilterInfo> removeFilterList = new ArrayList<CallFilterInfo>();
                final CallFilterInfo infoFilter = mFilterList.get(position);
                removeFilterList.add(infoFilter);
                mCallManger.removeFilterGr(removeFilterList);

                //remove BlackList
                List<BlackListInfo> removeBlacklist = new ArrayList<BlackListInfo>();
                BlackListInfo infoBlack = new BlackListInfo();
                infoBlack.setNumber(infoFilter.getNumber());
                removeBlacklist.add(infoBlack);
                mCallManger.removeBlackList(removeBlacklist);

                mFilterList.remove(position);
                CallFilterMainActivity callFilterMainActivity =
                        (CallFilterMainActivity) mActivity;
                //black list notify
                callFilterMainActivity.blackListReload();


                boolean restrLog = mDeleteDialog.getCheckBoxState();
                //恢复拦截记录到系统
                if (restrLog) {
                    CallFilterContextManagerImpl cmp = (CallFilterContextManagerImpl)
                            MgrContext.getManager(MgrContext.MGR_CALL_FILTER);

                    List<CallFilterInfo> infos = cmp.getFilterDetListFroNum(infoFilter.getNumber());
                    if (infos != null && infos.size() > 0) {
                        for (CallFilterInfo CallInfo : infos) {
                            cmp.insertCallToSys(CallInfo);
                        }
                    }
                }

                //删除拦截,通知更新拦截列表
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        List<CallFilterInfo> removeFilterList = new ArrayList<CallFilterInfo>();
                        CallFilterInfo callFil = new CallFilterInfo();
                        callFil.setNumber(infoFilter.getNumber());
                        removeFilterList.add(callFil);
                        mCallManger.removeFilterGr(removeFilterList);
                        int id = EventId.EVENT_LOAD_FIL_GR_ID;
                        String msg = CallFilterConstants.EVENT_MSG_LOAD_FIL_GR;
                        CommonEvent event = new CommonEvent(id, msg);
                        LeoEventBus.getDefaultBus().post(event);
                    }
                });

                mAdapter.setData(mFilterList);
                mDeleteDialog.dismiss();
            }
        });
        mDeleteDialog.show();
    }

    private void deleteFilter(int position) {
        List<CallFilterInfo> removeList = new ArrayList<CallFilterInfo>();
        CallFilterInfo info = mFilterList.get(position);
        removeList.add(info);
        mCallManger.removeFilterGr(removeList);

        mFilterList.remove(position);
        mAdapter.setData(mFilterList);
    }
}
