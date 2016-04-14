package com.leo.appmaster.imagehide;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.privacy.Privacy;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.DataUtils;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangyuefeng on 2016/3/29.
 */
public class NewHideImageActivity extends BaseFragmentActivity {

    private static final String TAG = NewHideImageActivity.class.getSimpleName();

    private CommonToolbar mTtileBar;
    private LEOAlarmDialog mDialog;

    private FrameLayout mFragment;
    private ProgressBar mLoading;
    private List<PhotoItem> mPhotoItems;

    public static boolean mFromNotification;

    public final static String FOUND_PIC = "found_pic";
    public final static String NEW_ADD_PIC = "new_add_pic";

    public static class PhotoList {
        public List<PhotoItem> photoItems = new ArrayList<PhotoItem>();
        public boolean inDifferentDir;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_hide_image);
        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
//        mTtileBar.setToolbarTitle(R.string.new_hidden_image);
        if (FOUND_PIC.equals(getIntent().getStringExtra(Constants.FIRST_ENTER_PIC))) {
            mTtileBar.setToolbarTitle(R.string.new_hidden_image);
        } else {
            mTtileBar.setToolbarTitle(R.string.new_hidden_image);
        }
        mFromNotification = getIntent().getBooleanExtra("pic_from_notify", false);
        NewImageFragment.mIsNewImgFromNoti = getIntent().getBooleanExtra(StatusBarEventService.NEW_IMG_HIDE_FROM_NOTIF, false);
        FolderNewImageFragment.mIsNewFolderImgFromNoti = getIntent().getBooleanExtra(StatusBarEventService.NEW_IMG_HIDE_FROM_NOTIF, false);
        mLoading = (ProgressBar) findViewById(R.id.pb_loading_pic);
        mFragment = (FrameLayout) findViewById(R.id.fl_image_view);
        initLoadData();
        LeoSettings.setBoolean(PrefConst.KEY_PIC_COMSUMED, true);

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyHelper.getImagePrivacy().ignoreNew();
            }
        });
        SDKWrapper.addEvent(this, SDKWrapper.P1, "hide_pic", "new_pic");
    }

    private void initLoadData() {
        Privacy privacy = PrivacyHelper.getImagePrivacy();
        mPhotoItems = privacy.getNewList();
        PhotoList mPhotoList = new PhotoList();
        mPhotoList.photoItems = mPhotoItems;
        mPhotoList.inDifferentDir = DataUtils.differentDirPic(mPhotoItems);
        Fragment fragment = getNewHideImageFragment(mPhotoList);
        LeoLog.v(TAG, "fragment != null : " + (fragment != null));
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_image_view, fragment);
        ft.commit();
        mLoading.setVisibility(View.GONE);
        mFragment.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static Fragment getNewHideImageFragment(PhotoList list) {
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
}
