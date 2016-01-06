package com.leo.appmaster.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.XHeaderView;
import com.leo.appmaster.utils.DataUtils;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.videohide.VideoItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/16.
 */
public class PrivacyNewVideoFragment extends PrivacyNewFragment implements AdapterView.OnItemClickListener {
    private static final int FOLDER_VIDEO_COUNT = 35;

    private ListView mVideoList;

    private List<VideoItemBean> mDataList;

    private boolean mHidingTimeout;
    private boolean mHidingFinish;

    public static Fragment getFragment(List<VideoItemBean> list) {
        Fragment fragment = null;
        if (list.size() < FOLDER_VIDEO_COUNT) {
            fragment = PrivacyNewVideoFragment.newInstance();
            ((PrivacyNewVideoFragment) fragment).setData(list, "");
        } else {
            if (DataUtils.differentDirVid(list)) {
                fragment = FolderVidFragment.newInstance();
                ((FolderVidFragment) fragment).setData(list);
            } else {
                fragment = PrivacyNewVideoFragment.newInstance();
                ((PrivacyNewVideoFragment) fragment).setData(list, "");
            }

        }

        return fragment;
    }

    public static PrivacyNewVideoFragment newInstance() {
        PrivacyNewVideoFragment fragment = new PrivacyNewVideoFragment();
        return fragment;
    }

    @Override
    public void setData(List<? extends Object> list, String text) {
        if (list == null) return;

        mDataList = new ArrayList<VideoItemBean>();
        for (Object o : list) {
            mDataList.add((VideoItemBean) o);
        }

        if (mAdaper != null) {
            mAdaper.setList(list);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdaper = new PrivacyNewVideoAdapter();
        mAdaper.setList(mDataList);
    }

    @Override
    protected int getIgnoreStringId() {
        return R.string.pri_pro_ignore_dialog;
    }

    @Override
    protected String getSkipConfirmDesc() {
        return "vid_skip_confirm";
    }

    @Override
    protected String getSkipCancelDesc() {
        return "vid_skip_cancel";
    }

    @Override
    protected String getFolderFullDesc() {
        return "vid_full_cnts";
    }

    private void setLabelCount(int count) {
        if (isDetached() || isRemoving() || getActivity() == null) return;

        boolean processed = PreferenceTable.getInstance().getBoolean(PrefConst.KEY_SCANNED_VID, false);
        int stringId = R.string.pri_pro_new_vid;
        if (!processed) {
            stringId = R.string.scan_find_vid;
        }
        String content = AppMasterApplication.getInstance().getString(stringId, count);
        mNewLabelTv.setText(Html.fromHtml(content));
        mNewLabelContent.setText(AppMasterApplication.
                getInstance().getString(R.string.scan_vid_content));
    }

    @Override
    protected void onProcessClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_hide_cnts");
        mActivity.onProcessClick(this);
        PreferenceTable.getInstance().putBoolean(PrefConst.KEY_SCANNED_VID, true);
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                List<VideoItemBean> list = mAdaper.getSelectedList();
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
                        mHidingTimeout = true;
                        if (!mHidingFinish) {
                            onProcessFinish(incScore);
                        }
                    }
                }, 8000);
            }
        });
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
    protected void onIgnoreClick(boolean direct) {
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
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_cnts");
        if (direct) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "vid_skip_direct");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_video, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVideoList = (ListView) view.findViewById(R.id.video_lv);
        mVideoList.setOnScrollListener(this);
        mVideoList.setOnItemClickListener(this);
        mAppName = "";

        setLabelCount(mDataList == null ? 0 : mDataList.size());
        setProcessContent(R.string.pri_pro_hide_vid);
        XHeaderView headerView = (XHeaderView) mStickView;
        headerView.setOnHeaderLayoutListener(new XHeaderView.OnHeaderLayoutListener() {
            @Override
            public void onHeaderLayout(int height) {
                if (mVideoList.getAdapter() == null) {
                    mStickyHeight = height;
                    mVideoList.addHeaderView(getEmptyHeader());
                    mVideoList.setAdapter(mAdaper);
                }
            }
        });
    }

    @Override
    public void onSelectionChange(boolean selectAll, int selectedCount) {
        super.onSelectionChange(selectAll, selectedCount);
        String str = mActivity.getString(R.string.pri_pro_hide_vid);
        if (selectedCount > 0) {
            str += " (" + selectedCount + ")";
        }
        setProcessContent(str);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        int pos = i - mVideoList.getHeaderViewsCount();
        if (view != null && pos > -1 && mDataList.size() > pos) {
            VideoItemBean info = mDataList.get(pos);
            mAdaper.toggle(pos);
        }
    }
}
