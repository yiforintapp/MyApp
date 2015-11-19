package com.leo.appmaster.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.PrefConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/30.
 */
public class FolderPicFragment extends FolderFragment<PhotoItem> {
    private static final String TAG = "FolderVidFragment";

    public static FolderPicFragment newInstance() {
        return new FolderPicFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FolderPicAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privacy_pic_folder, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProcessTv.setText(R.string.pri_pro_hide_pic);
    }

    @Override
    protected int getListViewId() {
        return R.id.expand_video_gv;
    }

    private void hideAllPicBackground(final List<String> photoItems, final int incScore) {
        mHidingTimeout = false;
        mHidingFinish = false;
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                pdm.onHideAllPic(photoItems);
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
    protected void onProcessClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_hide_cnts");
        mActivity.onProcessClick(this);
        PreferenceTable.getInstance().putBoolean(PrefConst.KEY_SCANNED_VID, true);
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                List<PhotoItem> list = mAdapter.getSelectData();
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);

                List<String> photos = new ArrayList<String>(list.size());
                for (PhotoItem videoItemBean : list) {
                    photos.add(videoItemBean.getPath());
                }
                final int incScore = pdm.haveCheckedPic();
                hideAllPicBackground(photos, incScore);
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
    }

    @Override
    protected void onIgnoreClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_cnts");
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
    protected void onIgnoreConfirmClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_confirm");
        mActivity.onIgnoreClick(0, MgrContext.MGR_PRIVACY_DATA);
    }

    @Override
    protected void onIgnoreCancelClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_cancel");
    }

    @Override
    protected void onFloatingCheckClick() {
        int groupPos = mCurrentGroup;
        if (mFloatingCb.isChecked()) {
            mAdapter.selectAll(groupPos);
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process",
                    "pic_folder_full_" + mAdapter.getGroupName(groupPos));
        } else {
            mAdapter.deselectAll(groupPos);
        }
    }

    @Override
    public void onSelectionChange(boolean selectAll, int selectedCount) {
        super.onSelectionChange(selectAll, selectedCount);
        String str = mActivity.getString(R.string.pri_pro_hide_pic);
        if (selectedCount > 0) {
            str += " (" + selectedCount + ")";
        }
        mProcessTv.setText(str);
    }
}
