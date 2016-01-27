package com.leo.appmaster.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/30.
 */
public class FolderPicFragment extends FolderFragment<PhotoItem> {
    private static final String TAG = "FolderPicFragment";

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

        View emptyView = view.findViewById(R.id.pic_loading_rl);
        mListView.setEmptyView(emptyView);
    }

    @Override
    protected int getListViewId() {
        return R.id.expand_video_gv;
    }

    private void hideAllPicBackground(final List<String> photoItems, final int incScore) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                pdm.onHideAllPic(photoItems);
                onProcessFinish(incScore);
            }
        });
    }

    private void onProcessFinish(final int incScore) {
        LeoLog.d(TAG, "onProcessFinish...");
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
    protected void onProcessClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_hide_cnts");
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "handled", "pic_prc_cnts_$"
                + mAdapter.getSelectData().size());
        mActivity.onProcessClick(this);
        PreferenceTable.getInstance().putBoolean(PrefConst.KEY_SCANNED_VID, true);
        mFinishNotified = false;
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                List<PhotoItem> list = mAdapter.getSelectData();
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);

                List<String> photos = new ArrayList<String>(list.size());
                for (PhotoItem videoItemBean : list) {
                    photos.add(videoItemBean.getPath());
                }

                HomeActivity mMianActivity = mActivity;
                mMianActivity.saveHidePicNum(photos);

                final int incScore = pdm.haveCheckedPic();
                hideAllPicBackground(photos, incScore);
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
    protected void onIgnoreClick() {
//        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_cnts");
//        if (mIgnoreDlg == null) {
//            initIgnoreDlg();
//        }
//        if (mActivity.shownIgnoreDlg()) {
//            mActivity.onIgnoreClick(0, MgrContext.MGR_PRIVACY_DATA);
//            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_direct");
//        } else {
//            mIgnoreDlg.show();
//            mActivity.setShownIngoreDlg();
//        }
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_direct");
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                final int incScore = pdm.haveCheckedPic();
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

    @Override
    protected View getEmptyHeader() {

         final View view = mActivity.getLayoutInflater().inflate(R.layout.pri_folder_top_view, null);
         TextView title = (TextView) view.findViewById(R.id.pri_pro_new_label_tv);
         title.setText(Html.fromHtml(mActivity.getResources().getString(
                R.string.scan_find_pic, mDataList.size())));
         TextView content = (TextView) view.findViewById(R.id.pri_pro_new_label_content);
         content.setText(mActivity.getResources().getString(
                 R.string.scan_pic_content));

         view.getViewTreeObserver().addOnGlobalLayoutListener(
                 new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 mTopViewHeight = DipPixelUtil.px2dip(mActivity, view.getHeight());
                 view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
             }
         });
         return view;
    }
}
