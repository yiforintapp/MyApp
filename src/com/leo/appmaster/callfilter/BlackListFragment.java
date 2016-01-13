
package com.leo.appmaster.callfilter;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.CommonEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.privacycontact.AddFromCallLogListActivity;
import com.leo.appmaster.privacycontact.AddFromContactListActivity;
import com.leo.appmaster.privacycontact.AddFromMessageListActivity;
import com.leo.appmaster.privacycontact.AddPrivacyContactDialog;
import com.leo.appmaster.privacycontact.PrivacyContactInputActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.LeoLog;

public class BlackListFragment extends BaseFragment implements View.OnClickListener {

    private ListView mBlackListView;
    private View mNothingToShowView;
    private BlackListAdapter mBlackListAdapter;
    private ProgressBar mProgressBar;
    private List<BlackListInfo> mBlackList;
    private RippleView mAddBlackNum;
    private RippleView mUnknowCall;
    private boolean isFristIn = true;
    private AddPrivacyContactDialog mAddPrivacyContact;

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
        if (mBlackList == null || mBlackList.isEmpty()) {
            showEmpty();
        } else {
            mNothingToShowView.setVisibility(View.GONE);
            mBlackListView.setVisibility(View.VISIBLE);
            mBlackListAdapter.setFlag(CallFilterConstants.ADAPTER_FLAG_BLACK_LIST);
            if (mBlackList != null) {
                LeoLog.d("testBlackList", "list size : " + mBlackList.size());
            }
            mBlackListAdapter.setData(mBlackList);
        }

    }

    public void showEmpty() {
        mBlackListView.setVisibility(View.GONE);
        mNothingToShowView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.black_list_fragment;
    }

    @Override
    protected void onInitUI() {
        mAddBlackNum = (RippleView) findViewById(R.id.black_list_add);
        mAddBlackNum.setOnClickListener(this);
        mUnknowCall = (RippleView) findViewById(R.id.black_list_unknow_num);
        mUnknowCall.setOnClickListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);
        mBlackListView = (ListView) findViewById(R.id.list_black_list);
        mBlackListAdapter = new BlackListAdapter(mActivity);
        mBlackListView.setAdapter(mBlackListAdapter);
        mBlackListView.setVisibility(View.GONE);

        mNothingToShowView = findViewById(R.id.content_show_nothing);

        loadData();
    }

    public void loadData() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                mBlackList = mCallManger.getBlackList();
                //load done
                handler.sendEmptyMessage(CallFilterConstants.BLACK_LIST_LOAD_DONE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
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
            case R.id.black_list_add:
                showAddContentDialog();
                break;
            case R.id.black_list_unknow_num:
                showStrangeCallPage();
                break;
        }
    }

    private void showStrangeCallPage() {

        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "block", "recently_cnts");

        Intent intent = new Intent(mActivity,
                StrangeCallActivity.class);
        intent.putExtra(CallFilterConstants.FROMWHERE,
                CallFilterConstants.FROM_BLACK_LIST);
        startActivity(intent);
    }

    public void showAddContentDialog() {
        if (mAddPrivacyContact == null) {
            mAddPrivacyContact = new AddPrivacyContactDialog(mActivity);
        }
        mAddPrivacyContact.setTitle(mActivity.getString(R.string.call_filter_dialog_add_black));
        // 通话记录添加
        mAddPrivacyContact.setCallLogListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SDKWrapper.addEvent(getActivity(),SDKWrapper.P1,"block","add_call");
                mAddPrivacyContact.cancel();
                Intent intent = new Intent(mActivity,
                        AddFromCallLogListActivity.class);
                intent.putExtra(CallFilterConstants.FROMWHERE,
                        CallFilterConstants.FROM_BLACK_LIST);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                } finally {
                    intent = null;
                }
            }
        });
        // 联系人列表添加
        mAddPrivacyContact.setContactListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SDKWrapper.addEvent(getActivity(),SDKWrapper.P1,"block","add_contacts");
                mAddPrivacyContact.cancel();
                Intent intent = new Intent(mActivity,
                        AddFromContactListActivity.class);
                intent.putExtra(CallFilterConstants.FROMWHERE,
                        CallFilterConstants.FROM_BLACK_LIST);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                } finally {
                    intent = null;
                }
            }
        });
        // 短信列表添加
        mAddPrivacyContact.setSmsListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SDKWrapper.addEvent(getActivity(),SDKWrapper.P1,"block","add_sms");
                mAddPrivacyContact.cancel();
                Intent intent = new Intent(mActivity,
                        AddFromMessageListActivity.class);
                intent.putExtra(CallFilterConstants.FROMWHERE,
                        CallFilterConstants.FROM_BLACK_LIST);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                } finally {
                    intent = null;
                }
            }
        });
        // 手动输入
        mAddPrivacyContact.setInputListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SDKWrapper.addEvent(getActivity(),SDKWrapper.P1,"block","add_inputs");
                mAddPrivacyContact.cancel();
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "contactsadd",
                        "handadd");
                Intent intent = new Intent(mActivity,
                        PrivacyContactInputActivity.class);
                intent.putExtra(CallFilterConstants.FROMWHERE,
                        CallFilterConstants.FROM_BLACK_LIST);
                try {
                    startActivity(intent);
                } catch (Exception e) {

                } finally {
                    intent = null;
                }
            }
        });
        mAddPrivacyContact.show();
    }

    public void onEventMainThread(CommonEvent event) {
        String msg = event.eventMsg;
        if (CallFilterConstants.EVENT_MSG_LOAD_BLACK.equals(msg)) {
            loadData();
        }

    }
}
