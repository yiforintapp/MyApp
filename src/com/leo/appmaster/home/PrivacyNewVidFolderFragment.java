package com.leo.appmaster.home;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.SecurityNotifyChangeEvent;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.FloatingExpandableListView;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.videohide.VideoItemBean;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Jasper on 2015/10/22.
 */
public class PrivacyNewVidFolderFragment extends Fragment implements AbsListView.OnScrollListener,
        ExpandableListView.OnGroupClickListener, FolderVidAdapter.OnVideoClickListener, RippleView.OnRippleCompleteListener ,
        View.OnClickListener {
    private static final String TAG = "PrivacyNewVidFolderFragment";

    private Dictionary<Integer, Integer> listViewItemHeights = new Hashtable<Integer, Integer>();
    private FloatingExpandableListView mListView;
    private FolderVidAdapter mAdapter;

    private List<VideoItemBean> mDataList;
    private int mEmptyHeight;

    private HomeActivity mActivity;
    private int mToolbarHeight;

    private View mFloatingView;
    private TextView mFloatingTv;
    private CheckBox mFloatingCb;
    private RippleView mFloatingRv;

    private TextView mProcessTv;
    private RippleView mProcessBtn;
    private RippleView mIgnoreBtn;
    private LEOAlarmDialog mIgnoreDlg;

    private boolean mHidingTimeout;
    private boolean mHidingFinish;

    private int mCurrentGroup;
    private int mLastGroup = -1;

    public static PrivacyNewVidFolderFragment newInstance() {
        return new PrivacyNewVidFolderFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (HomeActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FolderVidAdapter();

        mToolbarHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        mEmptyHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.pri_pro_header);

        LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
    }

    public void setData(List<? extends Object> list) {
        if (list == null) return;

        mDataList = new ArrayList<VideoItemBean>();
        for (Object o : list) {
            mDataList.add((VideoItemBean) o);
        }

        if (mAdapter != null) {
            mAdapter.setList(mDataList);
        }
    }

    public void onEventMainThread(SecurityNotifyChangeEvent event) {
        if (!MgrContext.MGR_PRIVACY_DATA.equals(event.mgr)) return;

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                List<VideoItemBean> list = pdm.getAddVid();
                if (list == null) return;

                if (list.size() != mDataList.size()) {
                    mDataList.clear();
                    mDataList.addAll(list);
                    mAdapter.setList(list);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vid_folder, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = (FloatingExpandableListView) view.findViewById(R.id.floating_video_lv);
        mListView.addHeaderView(getEmptyHeader());
        mListView.setAdapter(mAdapter);
        mAdapter.setList(mDataList);
        mAdapter.setOnVideoClickListener(this);

        mListView.setOnScrollListener(this);
        mListView.setOnGroupClickListener(this);

        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            mListView.collapseGroup(i);
        }

        mFloatingView = view.findViewById(R.id.floating_lable);
        mFloatingView.setVisibility(View.INVISIBLE);

        mFloatingTv = (TextView) mFloatingView.findViewById(R.id.pri_pro_new_label_tv);
        mFloatingCb = (CheckBox) mFloatingView.findViewById(R.id.pri_pro_cb);
        mFloatingRv = (RippleView) mFloatingView.findViewById(R.id.pri_pro_click_rv);
        mFloatingRv.setOnRippleCompleteListener(this);
        mFloatingCb.setOnClickListener(this);

        mProcessBtn = (RippleView) view.findViewById(R.id.pp_process_rv);
        mIgnoreBtn = (RippleView) view.findViewById(R.id.pp_process_ignore_rv);
        mProcessTv = (TextView) view.findViewById(R.id.pp_process_tv);

        mProcessBtn.setOnRippleCompleteListener(this);
        mIgnoreBtn.setOnRippleCompleteListener(this);

        mProcessTv.setText(R.string.pri_pro_hide_vid);
    }

    protected View getEmptyHeader() {
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new AbsListView.LayoutParams(1, mEmptyHeight - mToolbarHeight));
        textView.setBackgroundResource(R.color.transparent);
        textView.setClickable(false);
        textView.setEnabled(false);
        textView.setWidth(1);

        return textView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        View c = view.getChildAt(0); //this is the first visible row
        if (c == null) return;

        int scrollY = -c.getTop();
        listViewItemHeights.put(view.getFirstVisiblePosition(), c.getHeight());
        for (int i = 0; i < view.getFirstVisiblePosition(); ++i) {
            if (listViewItemHeights.get(i) != null) // (this is a sanity check)
                scrollY += listViewItemHeights.get(i); //add all heights of the views that are gone
        }

        mActivity.onListScroll(scrollY);
        LeoLog.i(TAG, "onScroll, firstVisibleItem: " + firstVisibleItem + " | visibleItemCount: " + visibleItemCount
                + " | totalItemCount: " + totalItemCount);

        int maxScrollHeight = mEmptyHeight - mToolbarHeight;
        if (scrollY > maxScrollHeight && mListView.isGroupExpanded(0)) {
            mFloatingView.setVisibility(View.VISIBLE);

            int floatingPos = mListView.getFloatingGroupPosition();
            LeoLog.i(TAG, "onScroll, floatingPos: " + floatingPos);
            if (floatingPos == -1) return;
            FolderVidAdapter.VideoItemsWrapper wrapper = (FolderVidAdapter.VideoItemsWrapper) mAdapter.getGroup(floatingPos);
            mCurrentGroup = floatingPos;
            if (mCurrentGroup != mLastGroup) {
                mAdapter.setLableContent(mFloatingTv, wrapper.parentName, wrapper.photoItems.size());
                mFloatingCb.setChecked(mAdapter.isChecked(wrapper));
            }
            mLastGroup = mCurrentGroup;
        } else {
            mFloatingView.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        LeoLog.i(TAG, "onGroupClick, groupPosition: " + groupPosition);
        parent.expandGroup(groupPosition);
        return true;
    }

    @Override
    public void onGroupClick(final int groupPosition, boolean isExpanded) {
        LeoLog.i(TAG, "onGroupClick, groupPosition: " + groupPosition + " | isExpanded: " + isExpanded);
        if (isExpanded) {
            for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                mListView.collapseGroup(i);
            }
        } else {
            for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                mListView.expandGroup(i);
            }
        }
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mListView.setSelectedGroup(groupPosition);
            }
        }, 100);
    }

    @Override
    public void onChildClick(int groupPosition, int childPosition) {

    }

    @Override
    public void onListScroll(int groupPosition, int childPosition) {

    }

    @Override
    public void onGroupCheckChanged(int groupPosition, boolean checked) {
        int group = mListView.getFloatingGroupPosition();
        if (group == groupPosition) {
            mFloatingCb.setChecked(checked);
        }
    }

    @Override
    public void onSelectionChange(boolean selectAll, int selectedCount) {
        if (selectedCount > 0) {
            mProcessBtn.setEnabled(true);
            mProcessBtn.setBackgroundResource(R.drawable.green_radius_btn_shape);
        } else {
            mProcessBtn.setEnabled(false);
            mProcessBtn.setBackgroundResource(R.drawable.green_radius_shape_disable);
        }
    }

    @Override
    public void onRippleComplete(RippleView rippleView) {
        if (mFloatingRv == rippleView) {
            for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                mListView.collapseGroup(i);
                final int group = mListView.getFloatingGroupPosition();
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListView.setSelectedGroup(group);
                    }
                }, 100);
            }
        } else if (mProcessBtn == rippleView) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_hide_cnts");
            mActivity.onProcessClick(this);
            PreferenceTable.getInstance().putBoolean(PrefConst.KEY_SCANNED_VID, true);
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    List<VideoItemBean> list = mAdapter.getSelectData();
                    PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);

                    List<String> photos = new ArrayList<String>(list.size());
                    for (VideoItemBean videoItemBean : list) {
                        photos.add(videoItemBean.getPath());
                    }
//                    int incScore = pdm.onHideAllVid(photos);
//                    pdm.haveCheckedVid();
//                    onProcessFinish(incScore);
                    final int incScore = pdm.haveCheckedVid();
                    hideAllVidBackground(photos, incScore);
                    ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mHidingTimeout = true;
                            if (!mHidingFinish) {
                                onProcessFinish(incScore);
                            }
                        }
                    }, 8000);
                }
            });
        } else if (mIgnoreBtn == rippleView) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_cnts");
            if (mIgnoreDlg == null) {
                initIgnoreDlg();
            }
            if (mActivity.shownIgnoreDlg()) {
                onIgnoreClick();
            } else {
                mIgnoreDlg.show();
                mActivity.setShownIngoreDlg();
            }
        }
    }

    private void hideAllVidBackground(final List<String> photoItems, final int incScore) {
        mHidingTimeout = false;
        mHidingFinish = false;
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                pdm.onHideAllVid(photoItems);
                mHidingFinish = true;
                if (!mHidingTimeout) {
                    onProcessFinish(incScore);
                }
            }
        });
    }

    private void onProcessFinish(final int incScore) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.onProcessFinish(incScore, MgrContext.MGR_PRIVACY_DATA);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == mFloatingCb) {
            int groupPos = mListView.getFloatingGroupPosition();
            if (mFloatingCb.isChecked()) {
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process",
                        "vid_folder_full_" + mAdapter.getGroupName(groupPos));
                mAdapter.selectAll(groupPos);
            } else {
                mAdapter.deselectAll(groupPos);
            }
        }
    }

    private void initIgnoreDlg() {
        if (mIgnoreDlg != null) return;

        mIgnoreDlg = new LEOAlarmDialog(getActivity());
        String content = getString(R.string.pri_pro_ignore_dialog);
        mIgnoreDlg.setCanceledOnTouchOutside(false);
        mIgnoreDlg.setContent(content);
        mIgnoreDlg.setLeftBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_cancel");
                mIgnoreDlg.dismiss();
            }
        });
        mIgnoreDlg.setRightBtnListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_confirm");
                onIgnoreClick();
                mIgnoreDlg.dismiss();
            }
        });
    }

    protected void onIgnoreClick() {
//        PreferenceTable.getInstance().putBoolean(PrefConst.KEY_SCANNED_VID, true);
//        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
//        int incScore = pdm.haveCheckedVid();
//        mActivity.onIgnoreClick(incScore, MgrContext.MGR_PRIVACY_DATA);
        mActivity.onIgnoreClick(0, MgrContext.MGR_PRIVACY_DATA);
    }}
