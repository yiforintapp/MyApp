package com.leo.appmaster.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

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
//        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_cnts");
//        if (mIgnoreDlg == null) {
//            initIgnoreDlg();
//        }
//        if (mActivity.shownIgnoreDlg()) {
//            mActivity.onIgnoreClick(0, MgrContext.MGR_PRIVACY_DATA);
//            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_direct");
//        } else {
//            mIgnoreDlg.show();
//            mActivity.setShownIngoreDlg();
//        }
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_direct");
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                final int incScore = pdm.haveCheckedVid();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.onIgnoreClick(incScore, MgrContext.MGR_PRIVACY_DATA);
                    }
                });
            }
        });
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
        mAdapter.toggle(i, i1);
        mAdapter.setCheck(view, mAdapter.isChildChecked(i, i1));
        return false;
    }

    @Override
    protected View getEmptyHeader() {

        View view = mActivity.getLayoutInflater().inflate(R.layout.pri_folder_top_view, null);
        TextView title = (TextView) view.findViewById(R.id.pri_pro_new_label_tv);
        title.setText(Html.fromHtml(mActivity.getResources().getString(
                R.string.scan_find_vid, mDataList.size())));
        TextView content = (TextView) view.findViewById(R.id.pri_pro_new_label_content);
        content.setText(mActivity.getResources().getString(
                R.string.scan_vid_content));

        return view;
    }
}
