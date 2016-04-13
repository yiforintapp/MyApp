package com.leo.appmaster.videohide;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.imagehide.FolderNewFragment;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/22.
 */
public class FolderNewVideoFragment extends FolderNewFragment<VideoItemBean> implements ExpandableListView.OnChildClickListener {
    private static final String TAG = "FolderVidFragment";
    public static boolean mIsFolderVidFromNoti;

    public static FolderNewVideoFragment newInstance() {
        return new FolderNewVideoFragment();
    }

    private LEOAlarmDialog mDialog;
    private LEOCircleProgressDialog mProgressDialog;
    private TextView mNewImageNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FolderVidNewAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_folder_vid_hide, container, false);
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
                LeoLog.v(TAG, "mDataList size :" + mDataList.size());
                for (int i = 0; i < photoItems.size(); i++) {
                    for (int j = 0; j < mDataList.size(); j++) {
                        if (mDataList.get(j).getPath().equals(photoItems.get(i))) {
                            mDataList.remove(j);
                        }
                    }
                }
                LeoLog.v(TAG, "mDataList removed size :" + mDataList.size());
                onProcessFinish(incScore);

                int successnum = pdm.getHideAllVidNum();
                checkLostVid(pdm, successnum);
            }
        });
    }

    private void checkLostVid(PrivacyDataManager pdm, int successnum) {
        int savevidNum = LeoSettings.getInteger(Constants.HIDE_VIDS_NUM, -1);
        LeoLog.d("checkLostPic", "savevidNum : " + savevidNum);
        int vidnum = pdm.getHideVidsRealNum();
        LeoLog.d("checkLostPic", "hide vid num : " + vidnum);
        if (savevidNum != -1) {
            LeoLog.d("checkLostPic", "isHide process num : " + successnum);
            int targetNum = savevidNum + successnum;
            if (vidnum >= targetNum) {
                LeoLog.d("checkLostPic", "everything ok");
                LeoSettings.setInteger(Constants.HIDE_VIDS_NUM, vidnum);
            } else {
                LeoLog.d("checkLostPic", "lost vid");
                pdm.reportDisappearError(false, PrivacyDataManager.LABEL_DEL_BY_SELF);
                LeoSettings.setInteger(Constants.HIDE_VIDS_NUM, vidnum);
            }

        } else {
            LeoSettings.setInteger(Constants.HIDE_VIDS_NUM, vidnum);
        }
    }

    private void onProcessFinish(final int incScore) {
        LeoLog.d(TAG, "onProcessFinish...");
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    if (mDataList.size() > 0) {
                        mAdapter.setList(mDataList);
                        mAdapter.notifyDataSetChanged();
                        setLabelCount();
                    } else {
                        Toast.makeText(mActivity, R.string.hide_complete_new_vid, Toast.LENGTH_LONG).show();
                        mActivity.finish();
                    }
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
    }

    private void showAlarmDialog() {
        if (mDialog == null) {
            mDialog = new LEOAlarmDialog(mActivity);
        }
        mDialog.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    final List<VideoItemBean> list = mAdapter.getSelectData();
                    showProgressDialog(getString(R.string.tips),
                            getString(R.string.app_hide_image) + "...",
                            true, true);
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            List<String> photos = new ArrayList<String>(list.size());
                            for (VideoItemBean videoItemBean : list) {
                                photos.add(videoItemBean.getPath());
                            }
                            hideAllVidBackground(photos, 0);
                        }
                    });
                    if (NewHideVidActivity.mFromNotification) {
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_Video", "vid_noti_hide");
                    } else {
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_pic", "vid_home_hide");
                    }
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_Video", "vid_hide_cnts");
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_vid_operation", "vid_add_cnts");
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_vid_operation", "vid_add_cnts_$" + list.size());
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_vid_operation", "vid_new_$" + list.size());
                    if (mIsFolderVidFromNoti) {
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "prilevel", "prilevel_add_vid");
                        mIsFolderVidFromNoti = false;
                    }
                }
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setTitle(R.string.app_hide_image);
        mDialog.setContent(getString(R.string.app_hide_video_dialog_content));
        mDialog.show();
    }

    private void showProgressDialog(String title, String message,
                                    boolean indeterminate, boolean cancelable) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEOCircleProgressDialog(mActivity);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mSelectBtn.setText(R.string.app_select_all);
                    mSelectBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                            getResources().getDrawable(R.drawable.select_all_selector), null,
                            null);
                }
            });
        }
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setButtonVisiable(cancelable);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(indeterminate);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    @Override
    protected void onProcessClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_vid_operation", "pic_new_$"
                + mAdapter.getSelectData().size());
        showAlarmDialog();
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
        final View view = mActivity.getLayoutInflater().inflate(R.layout.image_folder_header_view, null);
        mNewImageNum = (TextView) view.findViewById(R.id.tv_image_hide_header);
        setLabelCount();
        return view;
    }

    @Override
    protected void onSelectAllClick() {
        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "hide_Video", "vid_hide_all");
    }

    private void setLabelCount() {
        String content = AppMasterApplication.getInstance().getString(R.string.new_vid_num, mDataList == null ? 0 : mDataList.size());
        mNewImageNum.setText(content);
    }
}
