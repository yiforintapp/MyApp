package com.leo.appmaster.videohide;

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
import com.leo.appmaster.home.PrivacyNewVideoFragment;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.privacy.Privacy;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.LeoLog;

import java.util.List;

/**
 * Created by yangyuefeng on 2016/3/29.
 */
public class NewHideVidActivity extends BaseFragmentActivity {

    private static final String TAG = NewHideVidActivity.class.getSimpleName();

    private CommonToolbar mTtileBar;
    private LEOAlarmDialog mDialog;

    private FrameLayout mFragment;
    private ProgressBar mLoading;
    private List<VideoItemBean> mVideoList;

    public final static String FOUND_VID = "found_vid";
    public final static String NEW_ADD_VID = "new_add_vid";

    public static boolean mFromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_hide_image);
        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
//        mTtileBar.setToolbarTitle(R.string.new_hidden_vid);
        if (FOUND_VID.equals(getIntent().getStringExtra(Constants.FIRST_ENTER_VID))) {
            mTtileBar.setToolbarTitle(R.string.new_hidden_vid);
        } else {
            mTtileBar.setToolbarTitle(R.string.new_hidden_vid);
        }
        mFromNotification = getIntent().getBooleanExtra("vid_from_notify", false);
        mLoading = (ProgressBar) findViewById(R.id.pb_loading_pic);
        mFragment = (FrameLayout) findViewById(R.id.fl_image_view);
        initLoadData();

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                pdm.haveCheckedVid();
            }
        });

        SDKWrapper.addEvent(this, SDKWrapper.P1, "hide_Video", "new_vid");
    }

    private void initLoadData() {
//        mLoading.setVisibility(View.VISIBLE);
//        mFragment.setVisibility(View.GONE);
        Privacy privacy = PrivacyHelper.getVideoPrivacy();
        mVideoList = privacy.getNewList();
        Fragment fragment = PrivacyNewVideoFragment.getNewVidFragment(mVideoList);
        LeoLog.v(TAG, "fragment != null : " + (fragment != null));
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_image_view, fragment);
        ft.commit();
        mLoading.setVisibility(View.GONE);
        mFragment.setVisibility(View.VISIBLE);
    }

}
