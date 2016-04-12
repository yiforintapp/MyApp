package com.leo.appmaster.imagehide;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.home.FolderPicFragment;
import com.leo.appmaster.home.HomeScanningFragment;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.HeaderGridView;
import com.leo.appmaster.ui.XHeaderView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/16.
 */
public class NewImageFragment extends NewFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = NewImageFragment.class.getSimpleName();

    private HeaderGridView mPicList;
    private PrivacyDataManager mLockMgr;

    private LEOAlarmDialog mDialog;
    private LEOCircleProgressDialog mProgressDialog;

    private TextView mNewImageNum;


    public static Fragment getFragment(HomeScanningFragment.PhotoList list) {
        Fragment fragment = null;
        if (list == null) {
            return fragment;
        }
        if (list.photoItems.size() > 60) {
            if (list.inDifferentDir) {
                fragment = FolderPicFragment.newInstance();
            } else {
                fragment = NewImageFragment.newInstance();
            }
        } else {
            fragment = NewImageFragment.newInstance();
        }
        if (fragment instanceof FolderPicFragment) {
            ((FolderPicFragment) fragment).setData(list.photoItems);
        } else if (fragment instanceof NewImageFragment) {
            ((NewImageFragment) fragment).setData(list.photoItems, "");
        }

        return fragment;
    }

    public static Fragment getImageFragment(HomeScanningFragment.PhotoList list) {
        Fragment fragment = null;
        if (list == null) {
            return fragment;
        }
        if (list.photoItems.size() > 60) {
            if (list.inDifferentDir) {
                fragment = FolderNewImageFragment.newInstance();
            } else {
                fragment = NewImageFragment.newInstance();
            }
        } else {
            fragment = NewImageFragment.newInstance();
        }
        if (fragment instanceof FolderNewImageFragment) {
            ((FolderNewImageFragment) fragment).setData(list.photoItems);
        } else if (fragment instanceof NewImageFragment) {
            ((NewImageFragment) fragment).setData(list.photoItems, "");
        }

        return fragment;
    }

    public static NewImageFragment newInstance() {
        NewImageFragment fragment = new NewImageFragment();
        return fragment;
    }

    @Override
    public void setData(List<? extends Object> list, String text) {
        if (list == null) return;
        mDataList = new ArrayList<PhotoItem>();
        for (Object o : list) {
            mDataList.add((PhotoItem) o);
        }

        if (mAdapter != null) {
            mAdapter.setList(list);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockMgr = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        mAdapter = new NewImageAdapter();
        mAdapter.setList(mDataList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_hide, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPicList = (HeaderGridView) view.findViewById(R.id.pic_gv);
        mPicList.setOnScrollListener(this);
        mAppName = "";
        mPicList.setOnItemClickListener(this);

        setProcessContent(R.string.pri_pro_hide_pic);
        XHeaderView headerView = (XHeaderView) mStickView;
        headerView.setOnHeaderLayoutListener(new XHeaderView.OnHeaderLayoutListener() {
            @Override
            public void onHeaderLayout(int height) {
                if (mPicList.getAdapter() == null) {
                    mStickyHeight = height;
                    mPicList.addHeaderView(getEmptyHeader());
                    mPicList.setAdapter(mAdapter);
                    if (mAdapter != null) {
                        mAdapter.setList(mDataList);
                    }
                }
            }
        });
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_pic", "new_pic");
    }

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


    @Override
    protected void onProcessClick() {
        showAlarmDialog();
    }

    private void showAlarmDialog() {
        if (mDialog == null) {
            mDialog = new LEOAlarmDialog(mActivity);
        }
        final List<PhotoItem> selectList = mAdapter.getSelectedList();
        if (selectList == null) {
            return;
        }
        mDialog.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    showProgressDialog(getString(R.string.tips),
                            getString(R.string.app_hide_image) + "...",
                            true, true);
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_hide_cnts");
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "handled", "pic_prc_cnts_$" + selectList.size());
                    LeoPreference.getInstance().putBoolean(PrefConst.KEY_SCANNED_PIC, true);
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            List<String> photos = new ArrayList<String>(selectList.size());
                            for (PhotoItem photoItem : selectList) {
                                photos.add(photoItem.getPath());
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
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_pic_operation", "pic_add_pics_$" + selectList.size());
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "hide_pic_operation", "pic_new_$" + selectList.size());
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
    public void onDestroy() {
        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        pdm.haveCheckedPic();
        super.onDestroy();
    }

    private void hideAllPicBackground(final List<String> photoItems, final int incScore) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                pdm.onHideAllPic(photoItems);
                for (int i = 0; i < photoItems.size(); i++) {
                    for (int j = 0; j < mDataList.size(); j++) {
                        if (((PhotoItem) mDataList.get(j)).getPath().equals(photoItems.get(i))) {
                            mDataList.remove(j);
                        }
                    }
                }
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
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    if (mDataList.size() > 0) {
                        mAdapter.setList(mDataList);
                        setLabelCount();
                        hideDone();
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(mActivity, R.string.hide_complete_new_image, Toast.LENGTH_LONG).show();
                        mActivity.finish();
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.toggle(position);
    }

    @Override
    public void onSelectionChange(boolean selectAll, int selectedCount) {
        super.onSelectionChange(selectAll, selectedCount);
        if (selectedCount > 0) {
            mHideBtn.setEnabled(true);
        } else {
            mHideBtn.setEnabled(false);
        }
        if (mAdapter.getSelectedList() != null && mAdapter.getSelectedList().size() < mDataList.size()) {
            mSelectBtn.setText(R.string.app_select_all);
            mSelectBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.select_all_selector), null,
                    null);
        } else {
            mSelectBtn.setText(R.string.app_select_none);
            mSelectBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.no_select_all_selector), null,
                    null);
        }
    }

    @Override
    protected void onSelectAllClick() {
        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "hide_pic", "pic_hide_all");
    }
}
