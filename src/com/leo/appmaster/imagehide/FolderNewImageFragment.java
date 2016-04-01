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
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
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
        return inflater.inflate(R.layout.fragment_new_folder_vid_hide, container, false);
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
            }
        });
    }

    private void onProcessFinish(final int incScore) {
        LeoLog.d(TAG, "onProcessFinish...");
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    if(mDataList.size() > 0) {
                        mAdapter.setList(mDataList);
                        mAdapter.notifyDataSetChanged();
                        setLabelCount();
                    }else{
                        Toast.makeText(mActivity,R.string.hide_complete_new_image,Toast.LENGTH_LONG).show();
                        mActivity.finish();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        pdm.haveCheckedPic();
        super.onDestroy();
    }

    private void showAlarmDialog() {
        if (mDialog == null) {
            mDialog = new LEOAlarmDialog(mActivity);
        }
        mDialog.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    showProgressDialog(getString(R.string.tips),
                            getString(R.string.app_hide_image) + "...",
                            true, true);
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            List<PhotoItem> list = mAdapter.getSelectData();
                            List<String> photos = new ArrayList<String>(list.size());
                            for (PhotoItem videoItemBean : list) {
                                photos.add(videoItemBean.getPath());
                                LeoLog.v(TAG, "path：" + videoItemBean.getPath());
                            }
//                            final int incScore = pdm.haveCheckedPic();
                            hideAllPicBackground(photos, 0);
                        }
                    });
//                            SDKWrapper.addEvent(ImageGridActivity.this,
//                                    SDKWrapper.P1, "hide_pic", "used");
//                            SDKWrapper.addEvent(ImageGridActivity.this,
//                                    SDKWrapper.P1, "hide_pic_operation", "pic_add_cnts");
//                            SDKWrapper.addEvent(ImageGridActivity.this,
//                                    SDKWrapper.P1, "hide_pic_operation", "pic_add_pics_" + size);
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
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_hide_cnts");
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "handled", "pic_prc_cnts_$"
                + mAdapter.getSelectData().size());
        showAlarmDialog();
        LeoPreference.getInstance().putBoolean(PrefConst.KEY_SCANNED_PIC, true);
        mFinishNotified = false;

    }

    @Override
    protected void onIgnoreClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_direct");
    }

    @Override
    protected void onIgnoreConfirmClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_confirm");
//        mActivity.onIgnoreClick(0, MgrContext.MGR_PRIVACY_DATA);
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
        final View view = mActivity.getLayoutInflater().inflate(R.layout.image_folder_header_view, null);
        mNewImageNum = (TextView) view.findViewById(R.id.tv_image_hide_header);
        setLabelCount();
        return view;
    }

    private void setLabelCount() {
        String content = AppMasterApplication.getInstance().getString(R.string.new_image_num, mDataList == null ? 0 : mDataList.size());
        mNewImageNum.setText(content);
    }
}
