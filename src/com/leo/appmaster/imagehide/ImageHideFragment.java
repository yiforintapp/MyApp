package com.leo.appmaster.imagehide;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.home.FolderPicFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.home.HomeScanningFragment;
import com.leo.appmaster.home.PrivacyNewFragment;
import com.leo.appmaster.home.PrivacyNewPicAdapter;
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
public class ImageHideFragment extends ImageFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = ImageHideFragment.class.getSimpleName();

    private HeaderGridView mPicList;
    private PrivacyDataManager mLockMgr;

    private LEOAlarmDialog mDialog;
    private LEOCircleProgressDialog mProgressDialog;

    private boolean mHidingTimeout;
    private boolean mHidingFinish;

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
                fragment = ImageHideFragment.newInstance();
            }
        } else {
            fragment = ImageHideFragment.newInstance();
        }
        if (fragment instanceof FolderPicFragment) {
            ((FolderPicFragment) fragment).setData(list.photoItems);
        } else if (fragment instanceof ImageHideFragment) {
            ((ImageHideFragment) fragment).setData(list.photoItems, "");
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
                fragment = FolderHideImageFragment.newInstance();
            } else {
                fragment = ImageHideFragment.newInstance();
            }
        } else {
            fragment = ImageHideFragment.newInstance();
        }
        if (fragment instanceof FolderHideImageFragment) {
            ((FolderHideImageFragment) fragment).setData(list.photoItems);
        } else if (fragment instanceof ImageHideFragment) {
            ((ImageHideFragment) fragment).setData(list.photoItems, "");
        }

        return fragment;
    }

    public static ImageHideFragment newInstance() {
        ImageHideFragment fragment = new ImageHideFragment();
        return fragment;
    }

    @Override
    public void setData(List<? extends Object> list, String text) {
        if (list == null) return;

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

        mAdapter = new ImageHideAdapter();
        mAdapter.setList(mDataList);
    }

    @Override
    protected int getIgnoreStringId() {
        return R.string.pri_pro_ignore_dialog;
    }

    @Override
    protected String getSkipConfirmDesc() {
        return "pic_skip_confirm";
    }

    @Override
    protected String getSkipCancelDesc() {
        return "pic_skip_cancel";
    }

    @Override
    protected String getFolderFullDesc() {
        return "pic_full_cnts";
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
        mDialog.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    showProgressDialog(getString(R.string.tips),
                            getString(R.string.app_hide_image) + "...",
                            true, true);
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_hide_cnts");
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "handled", "pic_prc_cnts_$"
                            + mAdapter.getSelectedList().size());
                    LeoPreference.getInstance().putBoolean(PrefConst.KEY_SCANNED_PIC, true);
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            List<PhotoItem> list = mAdapter.getSelectedList();
                            List<String> photos = new ArrayList<String>(list.size());
                            for (PhotoItem photoItem : list) {
                                photos.add(photoItem.getPath());
                            }

                            hideAllPicBackground(photos, 0);
                            ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mHidingTimeout = true;
                                    if (!mHidingFinish) {
                                        onProcessFinish(0);
                                    }
                                }
                            }, 8000);
                        }
                    });
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
    public void onDestroy() {
        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        pdm.haveCheckedPic();
        super.onDestroy();
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
                for (int i = 0; i < photoItems.size(); i++) {
                    for (int j = 0; j < mDataList.size(); j++) {
                        if (mDataList.get(j).getPath().equals(photoItems.get(i))) {
                            mDataList.remove(j);
                        }
                    }
                }
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
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    if(mDataList.size() > 0) {
                        mAdapter.setList(mDataList);
                        setLabelCount();
                        mAdapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(mActivity,R.string.hide_complete_new_image,Toast.LENGTH_LONG).show();
                        mActivity.finish();
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        PhotoItem item = (PhotoItem) mAdapter.getItem(position);
        mAdapter.toggle(position);

    }

    @Override
    public void onSelectionChange(boolean selectAll, int selectedCount) {
        super.onSelectionChange(selectAll, selectedCount);
        if (selectedCount > 0) {
            mHideBtn.setEnabled(true);
//            mSelectBtn.setEnabled(true);
//            mProcessBtn.setBackgroundResource(R.drawable.green_radius_btn_shape);
        } else {
            mHideBtn.setEnabled(false);
//            mSelectBtn.setEnabled(false);
//            mProcessBtn.setBackgroundResource(R.drawable.green_radius_shape_disable);
        }
        if (mAdapter.getSelectedList() != null && mAdapter.getSelectedList().size() < mDataList.size()) {
            mSelectBtn.setText(R.string.app_select_all);
        } else {
            mSelectBtn.setText(R.string.app_select_none);
        }
//        setProcessContent(str);
    }
}
