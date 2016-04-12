package com.leo.appmaster.imagehide;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/30.
 */
public class FolderNewImageFragment extends FolderNewFragment<PhotoItem> {
    private static final String TAG = FolderNewImageFragment.class.getSimpleName();

    private LEOAlarmDialog mDialog;
    private LEOCircleProgressDialog mProgressDialog;
    private TextView mNewImageNum;

    public static FolderNewImageFragment newInstance() {
        return new FolderNewImageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FolderNewImageAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_folder_hide_image, container, false);
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

                int successnum = pdm.getHideAllPicNum();
                checkLostPic(pdm, successnum);
            }
        });
    }

    private void checkLostPic(PrivacyDataManager pdm, int successnum) {
        int saveNum = LeoSettings.getInteger(Constants.HIDE_PICS_NUM, -1);
        LeoLog.d("checkLostPic", "savenum : " + saveNum);
        int num = pdm.getHidePicsRealNum();
        LeoLog.d("checkLostPic", "hide pic num : " + num);
        if (saveNum != -1) {
            LeoLog.d("checkLostPic", "isHide process num : " + successnum);
            int targetNum = saveNum + successnum;
            if (num >= targetNum) {
                LeoLog.d("checkLostPic", "everything ok");
                LeoSettings.setInteger(Constants.HIDE_PICS_NUM, num);
            } else {
                LeoLog.d("checkLostPic", "lost pic");
                pdm.reportDisappearError(true, PrivacyDataManager.LABEL_DEL_BY_SELF);
                LeoSettings.setInteger(Constants.HIDE_PICS_NUM, num);
            }
        } else {
            LeoSettings.setInteger(Constants.HIDE_PICS_NUM, num);
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
                        hideDone();
                        setLabelCount();
                    } else {
                        Toast.makeText(mActivity, R.string.hide_complete_new_image, Toast.LENGTH_LONG).show();
                        mActivity.finish();
                    }
                }
            }
        });
    }

    private void showAlarmDialog() {
        if (mDialog == null) {
            mDialog = new LEOAlarmDialog(mActivity);
        }
        mDialog.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    final List<PhotoItem> selectList = mAdapter.getSelectData();
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_hide_cnts");
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "handled", "pic_prc_cnts_$" + mAdapter.getSelectData().size());
                    showProgressDialog(getString(R.string.tips),
                            getString(R.string.app_hide_image) + "...",
                            true, true);
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            List<String> photos = new ArrayList<String>(selectList.size());
                            for (PhotoItem videoItemBean : selectList) {
                                photos.add(videoItemBean.getPath());
                                LeoLog.v(TAG, "path：" + videoItemBean.getPath());
                            }
                            hideAllPicBackground(photos, 0);
                        }
                    });
                    if (NewHideImageActivity.mFromNotification) {
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_pic", "pic_noti_hide");
                    } else {
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_pic", "pic_home_hide");
                    }
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_pic", "pic_hide_cnts");
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_pic_operation", "pic_add_cnts");
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_pic_operation", "pic_add_pics_" + mAdapter.getSelectData().size());
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_pic_operation", "pic_new_$" + mAdapter.getSelectData().size());
                }
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setTitle(R.string.app_hide_image);
        mDialog.setContent(getString(R.string.app_hide_pictures_dialog_content));
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
        showAlarmDialog();
        LeoPreference.getInstance().putBoolean(PrefConst.KEY_SCANNED_PIC, true);
        mFinishNotified = false;

    }

    @Override
    protected void onIgnoreClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_direct");
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
        final View view = mActivity.getLayoutInflater().inflate(R.layout.image_folder_header_view, null);
        mNewImageNum = (TextView) view.findViewById(R.id.tv_image_hide_header);
        setLabelCount();
        return view;
    }

    @Override
    protected void onSelectAllClick() {
        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "hide_pic", "pic_hide_all");
    }

    private void setLabelCount() {
        String content = AppMasterApplication.getInstance().getString(R.string.new_image_num, mDataList == null ? 0 : mDataList.size());
        mNewImageNum.setText(content);
    }
}
