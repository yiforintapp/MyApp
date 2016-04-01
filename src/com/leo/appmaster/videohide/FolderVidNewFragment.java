package com.leo.appmaster.videohide;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.imagehide.FolderNewFragment;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/22.
 */
public class FolderVidNewFragment extends FolderNewFragment<VideoItemBean> implements ExpandableListView.OnChildClickListener {
    private static final String TAG = "FolderVidFragment";

    public static FolderVidNewFragment newInstance() {
        return new FolderVidNewFragment();
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
                        Toast.makeText(mActivity,R.string.hide_complete_new_vid,Toast.LENGTH_LONG).show();
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
                            List<VideoItemBean> list = mAdapter.getSelectData();
                            PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);

                            List<String> photos = new ArrayList<String>(list.size());
                            for (VideoItemBean videoItemBean : list) {
                                photos.add(videoItemBean.getPath());
                            }
                            hideAllVidBackground(photos, 0);
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
        final View view = mActivity.getLayoutInflater().inflate(R.layout.image_folder_header_view, null);
        mNewImageNum = (TextView) view.findViewById(R.id.tv_image_hide_header);
        setLabelCount();
        return view;
    }

    private void setLabelCount() {
        String content = AppMasterApplication.getInstance().getString(R.string.new_vid_num, mDataList == null ? 0 : mDataList.size());
        mNewImageNum.setText(content);
    }
}
