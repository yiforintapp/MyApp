package com.leo.appmaster.home;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.ListAppLockAdapter;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.SecurityNotifyChangeEvent;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.PrefConst;

/**
 * 应用新增
 *
 * @author Jasper
 */
public class PrivacyNewAppFragment extends PrivacyNewFragment {
    private static final String TAG = "PrivacyNewAppFragment";

    private ListView mAppList;
    private LockManager mLockMgr;

    private List<AppItemInfo> mDataList;

    public static PrivacyNewAppFragment newInstance() {
        PrivacyNewAppFragment fragment = new PrivacyNewAppFragment();
        return fragment;
    }

    @Override
    public void setData(List<? extends Object> list) {
        if (list == null) return;

        mDataList = new ArrayList<AppItemInfo>();
        for (Object o : list) {
            mDataList.add((AppItemInfo) o);
        }

        //change the topPos
        mDataList = changeTopPos(mDataList);

        Collections.sort(mDataList, new RecommentAppLockListActivity.DefalutAppComparator());

        if (mAdaper != null) {
            mAdaper.setList(list);
        }
    }

    //AM 3076
    private List<AppItemInfo> changeTopPos(List<AppItemInfo> mDataList) {

        for (int i = 0; i < mDataList.size(); i++) {
            AppItemInfo info = mDataList.get(i);
            info.topPos = ListAppLockAdapter.fixPosEqules(info);
        }

        return mDataList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockMgr = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);

        mAdaper = new PrivacyNewAppAdapter();
        mAdaper.setList(mDataList);

        LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
    }

    public void onEventMainThread(SecurityNotifyChangeEvent event) {
        if (!MgrContext.MGR_APPLOCKER.equals(event.mgr)) return;

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                List<AppItemInfo> list = lm.getNewAppList();
                mDataList.clear();
                mDataList.addAll(list);
                Collections.sort(mDataList, new RecommentAppLockListActivity.DefalutAppComparator());

                if (mAdaper != null) {
                    mAdaper.setList(mDataList);
                }
                ThreadManager.getUiThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        setLabelCount(mDataList.size());
                    }
                });
            }
        });
    }

    @Override
    protected void onProcessClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "app_lock_cnts");
        mActivity.onProcessClick(this);
        PreferenceTable.getInstance().putBoolean(PrefConst.KEY_SCANNED_APP, true);
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                List<AppItemInfo> list = mAdaper.getSelectedList();

                List<String> pkgList = new ArrayList<String>(list.size());
                for (AppItemInfo info : list) {
                    pkgList.add(info.packageName);
                }

                LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                int incScore = lm.lockAddedApp(pkgList);
                onProcessFinish(incScore);
            }
        });
    }

    private void onProcessFinish(final int incScore) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.onProcessFinish(incScore, MgrContext.MGR_APPLOCKER);
            }
        });
    }

    @Override
    protected void onIgnoreClick() {
        mActivity.onIgnoreClick(0, MgrContext.MGR_APPLOCKER);
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "app_skip_cnts");
    }

    @Override
    protected int getIgnoreStringId() {
        return R.string.pri_pro_ignore_dialog;
    }

    @Override
    protected String getSkipConfirmDesc() {
        return "app_skip_confirm";
    }

    @Override
    protected String getSkipCancelDesc() {
        return "app_skip_cancel";
    }

    @Override
    protected String getFolderFullDesc() {
        return "app_full_cnts";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_app, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAppList = (ListView) view.findViewById(R.id.app_lv);
        mAppList.setOnScrollListener(this);

        mAppList.addHeaderView(getEmptyHeader());
        mAppList.setAdapter(mAdaper);

        boolean processed = PreferenceTable.getInstance().getBoolean(PrefConst.KEY_SCANNED_APP, false);
        int stringId = R.string.pri_pro_new_app;
        if (!processed) {
            stringId = R.string.pri_pro_scan_app;
        }
        String content = AppMasterApplication.getInstance().getString(stringId, mDataList.size());
        mNewLabelTv.setText(Html.fromHtml(content));
//        setNewLableContent(content, mDataList.size(), processed);
        setProcessContent(R.string.pri_pro_lock_app);
    }

    private void setLabelCount(int count) {
        if (isDetached() || isRemoving() || getActivity() == null) return;

        boolean processed = PreferenceTable.getInstance().getBoolean(PrefConst.KEY_SCANNED_APP, false);
        int stringId = R.string.pri_pro_new_app;
        if (!processed) {
            stringId = R.string.pri_pro_scan_app;
        }
        String content = AppMasterApplication.getInstance().getString(stringId, count);
        mNewLabelTv.setText(Html.fromHtml(content));
    }

}
