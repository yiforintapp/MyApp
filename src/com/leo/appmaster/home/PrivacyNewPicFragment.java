package com.leo.appmaster.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.HeaderGridView;
import com.leo.appmaster.utils.PrefConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/16.
 */
public class PrivacyNewPicFragment extends PrivacyNewFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "PrivacyNewPicFragment";

    private HeaderGridView mPicList;
    private PrivacyDataManager mLockMgr;

    private List<PhotoItem> mDataList = new ArrayList<PhotoItem>();

    private boolean mHidingTimeout;
    private boolean mHidingFinish;


    public static Fragment getFragment(HomeScanningFragment.PhotoList list) {
        Fragment fragment = null;
        if (list == null) {
            return fragment;
        }
        if (list.photoItems.size() > 60) {
            if (list.inDifferentDir) {
                fragment = FolderPicFragment.newInstance();
            } else {
                fragment = PrivacyNewPicFragment.newInstance();
            }
        } else {
            fragment = PrivacyNewPicFragment.newInstance();
        }
        if (fragment instanceof FolderPicFragment) {
            ((FolderPicFragment) fragment).setData(list.photoItems);
        } else if (fragment instanceof PrivacyNewPicFragment) {
            ((PrivacyNewPicFragment) fragment).setData(list.photoItems, "");
        }

        return fragment;
    }

    public static PrivacyNewPicFragment newInstance() {
        PrivacyNewPicFragment fragment = new PrivacyNewPicFragment();
        return fragment;
    }

    @Override
    public void setData(List<? extends Object> list, String text) {
        if (list == null) return;

        for (Object o : list) {
            mDataList.add((PhotoItem) o);
        }

        if (mAdaper != null) {
            mAdaper.setList(list);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockMgr = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);

        mAdaper = new PrivacyNewPicAdapter();
        mAdaper.setList(mDataList);
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
        return inflater.inflate(R.layout.fragment_new_pic, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPicList = (HeaderGridView) view.findViewById(R.id.pic_gv);
        mPicList.setOnScrollListener(this);

        mAppName = "";
        mPicList.addHeaderView(getEmptyHeader());
        mPicList.setAdapter(mAdaper);
        mPicList.setOnItemClickListener(this);
        mAppNotifyLayout.setVisibility(View.GONE);

        setLabelCount(mDataList.size());
        setProcessContent(R.string.pri_pro_hide_pic);


    }

    private void setLabelCount(int count) {
        if (isDetached() || isRemoving() || getActivity() == null) return;

        boolean processed = PreferenceTable.getInstance().getBoolean(PrefConst.KEY_SCANNED_PIC, false);
        int stringId = R.string.pri_pro_new_pic;
        if (!processed) {
            stringId = R.string.scan_find_pic;
        }
        String content = AppMasterApplication.getInstance().getString(stringId, count);
        mNewLabelTv.setText(Html.fromHtml(content));
    }

    @Override
    protected void onProcessClick() {
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_hide_cnts");
        mActivity.onProcessClick(this);
        PreferenceTable.getInstance().putBoolean(PrefConst.KEY_SCANNED_PIC, true);
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                List<PhotoItem> list = mAdaper.getSelectedList();

                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);

                List<String> photos = new ArrayList<String>(list.size());
                for (PhotoItem photoItem : list) {
                    photos.add(photoItem.getPath());
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

    @Override
    protected void onIgnoreClick(boolean direct) {
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
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_cnts");
        if (direct) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "process", "pic_skip_direct");
        }
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PhotoItem item = (PhotoItem) mAdaper.getItem(position);
        mAdaper.toggle(item);

    }

    @Override
    public void onSelectionChange(boolean selectAll, int selectedCount) {
        super.onSelectionChange(selectAll, selectedCount);
        String str = mActivity.getString(R.string.pri_pro_hide_pic);
        if (selectedCount > 0) {
            str += " (" + selectedCount + ")";
        }
        setProcessContent(str);
    }
}
