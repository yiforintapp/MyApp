package com.leo.appmaster.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.videohide.VideoItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/22.
 */
public class FolderVidFragment extends FolderFragment<VideoItemBean> implements ExpandableListView.OnChildClickListener {
    private static final String TAG = "FolderVidFragment";

    public static FolderVidFragment newInstance() {
        return new FolderVidFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FolderVidAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vid_folder, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProcessTv.setText(R.string.pri_pro_hide_vid);
        mListView.setOnChildClickListener(this);
    }

    private void hideAllVidBackground(final List<String> photoItems, final int incScore) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                pdm.onHideAllVid(photoItems);
                onProcessFinish(incScore);
            }
        });
    }

    private void onProcessFinish(final int incScore) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mFinishNotified) {
                    mActivity.onProcessFinish(incScore, MgrContext.MGR_PRIVACY_DATA);
                    mFinishNotified = true;
                }
            }
        });
    }

    @Override
    protected int getListViewId() {
        return R.id.floating_video_lv;
    }

    @Override
    protected void onIgnoreClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_cnts");
        if (mIgnoreDlg == null) {
            initIgnoreDlg();
        }
        if (mActivity.shownIgnoreDlg()) {
            mActivity.onIgnoreClick(0, MgrContext.MGR_PRIVACY_DATA);
        } else {
            mIgnoreDlg.show();
            mActivity.setShownIngoreDlg();
        }
    }

    @Override
    protected void onProcessClick() {
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
                final int incScore = pdm.haveCheckedVid();
                hideAllVidBackground(photos, incScore);
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onProcessFinish(incScore);
                    }
                }, 8000);
            }
        });
    }

    @Override
    protected void onIgnoreConfirmClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_confirm");
        onIgnoreClick();
    }

    @Override
    protected void onIgnoreCancelClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_cancel");
    }

    @Override
    protected void onFloatingCheckClick() {
        int groupPos = mCurrentGroup;
        if (mFloatingCb.isChecked()) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process",
                    "vid_folder_full_" + mAdapter.getGroupName(groupPos));
            mAdapter.selectAll(groupPos);
        } else {
            mAdapter.deselectAll(groupPos);
        }
    }

    @Override
    public void onSelectionChange(boolean selectAll, int selectedCount) {
        super.onSelectionChange(selectAll, selectedCount);
        String str = mActivity.getString(R.string.pri_pro_hide_vid);
        if (selectedCount > 0) {
            str += " (" + selectedCount + ")";
        }
        mProcessTv.setText(str);
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        VideoItemBean info = (VideoItemBean) mAdapter.getChild(i, i1);
        mAdapter.toggle(info);
        mAdapter.setCheck(view, mAdapter.isChildChecked(info));
        return false;
    }
}
