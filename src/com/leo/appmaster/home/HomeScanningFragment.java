package com.leo.appmaster.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.LostSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyContactManager;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.mgr.WifiSecurityManager;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.LoadingView;
import com.leo.appmaster.ui.ScanningImageView;
import com.leo.appmaster.ui.ScanningTextView;
import com.leo.appmaster.utils.DataUtils;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.videohide.VideoItemBean;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.mobvista.sdk.m.core.entity.Campaign;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/18.
 */
public class HomeScanningFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "HomeScanningFragment";
    private static final byte[] LOCK = new byte[1];

    // 3.2 advertise
    private static final String AD_AFTER_SCAN = Constants.UNIT_ID_243;
    private boolean mAdLoaded;
    private View mRootView;

    private View mCancelBtn;
    private TextView mCancelTv;

    private View mProcessBtn;
    private TextView mProcessTv;

    private TextView mScannTitleTv;
    private TextView mProgressTv;

    private ScanningImageView mNewAppIv;
    private ScanningImageView mNewPhotoIv;
    private ScanningImageView mNewVideoIv;
    private ScanningImageView mNewPrivacyIv;

    private ScanningTextView mNewAppText;
    private ScanningTextView mNewPhotoText;
    private ScanningTextView mNewVideoText;
    private ScanningTextView mNewPrivacyText;

    // 扫描icon右下角图标
    private TextView mAppCountTv;
    private TextView mPicCountTv;
    private TextView mVidCountTv;
    private ImageView mPrivacyCountIv;
    private ImageView mAppCountIv;
    private ImageView mPicCountIv;
    private ImageView mVidCountIv;

    private HomeActivity mActivity;

    private List<AppItemInfo> mAppList;
    private PhotoList mPhotoList;
    private List<VideoItemBean> mVideoList;

    private boolean mAppScanFinish;
    private boolean mPhotoScanFinish;
    private boolean mVideoScanFinish;

    private boolean mScanning;

    private PrivacyHelper mPrivacyHelper;
    private HomeScanningController mController;
    private int mPicScore;

    private ImageView mNewAppImg;
    private TextView mNewAppTitle;
    private TextView mNewAppContent;
    private TextView mNewAppScore;
    private LoadingView mNewAppLoading;
    private LinearLayout mNewAppLayout;

    private ImageView mNewPicImg;
    private TextView mNewPicTitle;
    private TextView mNewPicContent;
    private TextView mNewPicScore;
    private LoadingView mNewPicLoading;
    private LinearLayout mNewPicLayout;

    private ImageView mNewVidImg;
    private TextView mNewVidTitle;
    private TextView mNewVidContent;
    private TextView mNewVidScore;
    private LoadingView mNewVidLoading;
    private LinearLayout mNewVidLayout;

    private ImageView mNewLostImg;
    private TextView mNewLostTitle;
    private TextView mNewLostContent;
    private TextView mNewLostScore;
    private LoadingView mNewLostLoading;
    private LinearLayout mNewLostLayout;

    private ImageView mNewWifiImg;
    private TextView mNewWifiTitle;
    private TextView mNewWifiContent;
    private TextView mNewWifiScore;
    private LoadingView mNewWifiLoading;
    private LinearLayout mNewWifiLayout;

    private ImageView mNewInstructImg;
    private TextView mNewInstructTitle;
    private TextView mNewInstructContent;
    private TextView mNewInstructScore;
    private LoadingView mNewInstructLoading;
    private LinearLayout mNewInstructLayout;

    private ImageView mNewContactImg;
    private TextView mNewContactTitle;
    private TextView mNewContactContent;
    private TextView mNewContactScore;
    private LoadingView mNewContactLoading;
    private LinearLayout mNewContactLayout;

    private String mScanAppName;
    private String mAppNotifyText;
    private LinearLayout mScrollLayout;
    private LayoutTransition mTransition;

    private static final int SCAN_NEW_WIFI_DONE = 0;
    private static final int SCAN_NEW_INSTRUCT_DONE = 1;
    private static final int SCAN_NEW_VID_DONE = 2;
    private static final int SCAN_NEW_PIC_DONE = 3;
    private static final int SCAN_NEW_APP_DONE = 4;
    private static final int LOADING_TIME = 500;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (mActivity != null) {
                switch (msg.what) {
                    case SCAN_NEW_WIFI_DONE:
                        mNewWifiLoading.setVisibility(View.GONE);
                        mNewWifiImg.setVisibility(View.VISIBLE);
                        break;
                    case SCAN_NEW_INSTRUCT_DONE:
                        mNewInstructLoading.setVisibility(View.GONE);
                        mNewInstructImg.setVisibility(View.VISIBLE);
                        break;
                    case SCAN_NEW_VID_DONE:
                        mNewVidLoading.setVisibility(View.GONE);
                        mNewVidImg.setVisibility(View.VISIBLE);
                        break;
                    case SCAN_NEW_PIC_DONE:
                        mNewPicLoading.setVisibility(View.GONE);
                        mNewPicImg.setVisibility(View.VISIBLE);
                        break;
                    case SCAN_NEW_APP_DONE:
                        mNewAppLoading.setVisibility(View.GONE);
                        mNewAppImg.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (HomeActivity) activity;
        mPrivacyHelper = PrivacyHelper.getInstance(activity);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mScannTitleTv = (TextView) view.findViewById(R.id.scan_title_tv);
//        mProgressTv = (TextView) view.findViewById(R.id.scan_progress_tv);
//
//        mCancelBtn = view.findViewById(R.id.scan_cancel_rv);
//        mCancelTv = (TextView) view.findViewById(R.id.scan_cancel_tv);
//
//        mProcessBtn = view.findViewById(R.id.scan_process_rv);
//        mProcessTv = (TextView) view.findViewById(R.id.scan_process_tv);
//
//        mCancelTv.setOnClickListener(this);
//        mProcessTv.setOnClickListener(this);
//
//        mNewAppIv = (ScanningImageView) view.findViewById(R.id.scan_new_app_iv);
//        mNewPhotoIv = (ScanningImageView) view.findViewById(R.id.scan_media_iv);
//        mNewVideoIv = (ScanningImageView) view.findViewById(R.id.scan_mobile_iv);
//        mNewPrivacyIv = (ScanningImageView) view.findViewById(R.id.scan_privacy_iv);
//
//        mNewAppText = (ScanningTextView) view.findViewById(R.id.scan_new_app_tv);
//        mNewPhotoText = (ScanningTextView) view.findViewById(R.id.scan_media_tv);
//        mNewVideoText = (ScanningTextView) view.findViewById(R.id.scan_mobile_tv);
//        mNewPrivacyText = (ScanningTextView) view.findViewById(R.id.scan_privacy_tv);
//
//        mAppCountTv = (TextView) view.findViewById(R.id.scan_app_count_tv);
//        mPicCountTv = (TextView) view.findViewById(R.id.scan_media_count_tv);
//        mVidCountTv = (TextView) view.findViewById(R.id.scan_mobile_count_iv);
//        mPrivacyCountIv = (ImageView) view.findViewById(R.id.scan_privacy_count_iv);
//
//        mAppCountIv = (ImageView) view.findViewById(R.id.scan_app_count_iv);
//        mPicCountIv = (ImageView) view.findViewById(R.id.scan_pic_count_iv);
//        mVidCountIv = (ImageView) view.findViewById(R.id.scan_vid_count_iv);
//
//        mController = new HomeScanningController(mActivity, this, mNewAppIv, mNewAppText,
//                mNewPhotoIv, mNewPhotoText, mNewVideoIv, mNewVideoText, mNewPrivacyIv, mNewPrivacyText);
//        startScanController();

        mScannTitleTv = (TextView) view.findViewById(R.id.scan_title_tv);
        mProgressTv = (TextView) view.findViewById(R.id.scan_progress_tv);
        mScrollLayout = (LinearLayout) view.findViewById(R.id.scrollView_layout);

        mCancelBtn = view.findViewById(R.id.scan_cancel_rv);
        mCancelTv = (TextView) view.findViewById(R.id.scan_cancel_tv);

        mProcessBtn = view.findViewById(R.id.scan_process_rv);
        mProcessTv = (TextView) view.findViewById(R.id.scan_process_tv);

        mCancelTv.setOnClickListener(this);
        mProcessTv.setOnClickListener(this);

        mNewAppImg = (ImageView) view.findViewById(R.id.scan_new_app_img);
        mNewAppTitle = (TextView) view.findViewById(R.id.scan_new_app_title);
        mNewAppContent = (TextView) view.findViewById(R.id.scan_new_app_content);
        mNewAppScore = (TextView) view.findViewById(R.id.scan_new_app_score);
        mNewAppLoading = (LoadingView) view.findViewById(R.id.scan_new_app_loading);
        mNewAppLayout = (LinearLayout) view.findViewById(R.id.scan_new_app_layout);

        mNewPicImg = (ImageView) view.findViewById(R.id.scan_new_pic_img);
        mNewPicTitle = (TextView) view.findViewById(R.id.scan_new_pic_title);
        mNewPicContent = (TextView) view.findViewById(R.id.scan_new_pic_content);
        mNewPicScore = (TextView) view.findViewById(R.id.scan_new_pic_score);
        mNewPicLoading = (LoadingView) view.findViewById(R.id.scan_new_pic_loading);
        mNewPicLayout = (LinearLayout) view.findViewById(R.id.scan_new_pic_layout);

        mNewVidImg = (ImageView) view.findViewById(R.id.scan_new_vid_img);
        mNewVidTitle = (TextView) view.findViewById(R.id.scan_new_vid_title);
        mNewVidContent = (TextView) view.findViewById(R.id.scan_new_vid_content);
        mNewVidScore = (TextView) view.findViewById(R.id.scan_new_vid_score);
        mNewVidLoading = (LoadingView) view.findViewById(R.id.scan_new_vid_loading);
        mNewVidLayout = (LinearLayout) view.findViewById(R.id.scan_new_vid_layout);

        mNewInstructImg = (ImageView) view.findViewById(R.id.scan_new_instruct_img);
        mNewInstructTitle = (TextView) view.findViewById(R.id.scan_new_instruct_title);
        mNewInstructContent = (TextView) view.findViewById(R.id.scan_new_instruct_content);
        mNewInstructScore = (TextView) view.findViewById(R.id.scan_new_instruct_score);
        mNewInstructLoading = (LoadingView) view.findViewById(R.id.scan_new_instruct_loading);
        mNewInstructLayout = (LinearLayout) view.findViewById(R.id.scan_new_instruct_layout);

        mNewWifiImg = (ImageView) view.findViewById(R.id.scan_new_wifi_img);
        mNewWifiTitle = (TextView) view.findViewById(R.id.scan_new_wifi_title);
        mNewWifiContent = (TextView) view.findViewById(R.id.scan_new_wifi_content);
        mNewWifiScore = (TextView) view.findViewById(R.id.scan_new_wifi_score);
        mNewWifiLoading = (LoadingView) view.findViewById(R.id.scan_new_wifi_loading);
        mNewWifiLayout = (LinearLayout) view.findViewById(R.id.scan_new_wifi_layout);

        mNewLostImg = (ImageView) view.findViewById(R.id.scan_new_lost_img);
        mNewLostTitle = (TextView) view.findViewById(R.id.scan_new_lost_title);
        mNewLostContent = (TextView) view.findViewById(R.id.scan_new_lost_content);
        mNewLostScore = (TextView) view.findViewById(R.id.scan_new_lost_score);
        mNewLostLoading = (LoadingView) view.findViewById(R.id.scan_new_lost_loading);
        mNewLostLayout = (LinearLayout) view.findViewById(R.id.scan_new_lost_layout);

        mNewContactImg = (ImageView) view.findViewById(R.id.scan_new_contact_img);
        mNewContactTitle = (TextView) view.findViewById(R.id.scan_new_contact_title);
        mNewContactContent = (TextView) view.findViewById(R.id.scan_new_contact_content);
        mNewContactScore = (TextView) view.findViewById(R.id.scan_new_contact_score);
        mNewContactLoading = (LoadingView) view.findViewById(R.id.scan_new_contact_loading);
        mNewContactLayout = (LinearLayout) view.findViewById(R.id.scan_new_contact_layout);

        mTransition = new LayoutTransition();
        mTransition.setAnimator(LayoutTransition.CHANGE_APPEARING,
                mTransition.getAnimator(LayoutTransition.CHANGE_APPEARING));
//        mTransition.setAnimator(LayoutTransition.APPEARING,
//                mTransition.getAnimator(LayoutTransition.APPEARING));
//        mScrollLayout.setLayoutTransition(mTransition);


        mController = new HomeScanningController(mActivity, this, mNewAppLayout, mNewPicLayout,
                mNewVidLayout, mNewLostLayout, mNewWifiLayout, mNewInstructLayout, mNewContactLayout);

             ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startScanController();
            }
        }, 200);

        mRootView = view;
        ThreadManager.executeOnSubThread(new Runnable() {
            @Override
            public void run() {
                loadAd();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_scanning_test, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mController.detachController();
        destroyAd();
        mController.detachTheController();
    }

    private void startScanController() {
        if (mScanning) return;

        mProgressTv.setText(R.string.pri_pro_scanning);
        mCancelBtn.setVisibility(View.VISIBLE);
        mProcessBtn.setVisibility(View.GONE);
        LeoLog.i(TAG, "start to scaning.");
        mController.startScanning();
        ThreadManager.executeOnSubThread(mContactRunnable);
    }

    private void onScannigFinish(final List<AppItemInfo> appList, final PhotoList photoItems,
                                 final List<VideoItemBean> videoItemBeans) {
        if (getActivity() == null || isDetached() || isRemoving()) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mCancelBtn.setVisibility(View.GONE);
                mProcessBtn.setVisibility(View.VISIBLE);
                mScannTitleTv.setText(R.string.pri_pro_scanning_finish);
                mProcessTv.setTextColor(mActivity.getToolbarColor());
                mActivity.onScanningFinish(appList, photoItems, videoItemBeans, mAppNotifyText);
            }
        });
    }

    public void onAnimatorEnd(ScanningImageView imageView) {
        updateUIOnAnimEnd(imageView);
        if (imageView == mNewAppIv) {
            ThreadManager.executeOnAsyncThread(mPhotoRunnable);
        } else if (imageView == mNewPhotoIv) {
            ThreadManager.executeOnAsyncThread(mVidRunnable);
        }
    }

    private void updateUIOnAnimEnd(ScanningImageView imageView) {
        if (isDetached() || isRemoving() || getActivity() == null) return;

        Context context = AppMasterApplication.getInstance();
        if (imageView == mNewAppIv) {
            updateAppList();
            int count = mAppList == null ? 0 : mAppList.size();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 1));
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "app_cnts_" + count);
        } else if (imageView == mNewPhotoIv) {
            updatePhotoList();
            int count = mPhotoList == null ? 0 : mPhotoList.photoItems.size();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 2));
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "pic_cnts_" + count);
        } else if (imageView == mNewVideoIv) {
            updateVideoList();
            int count = mVideoList == null ? 0 : mVideoList.size();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 3));
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "vid_cnts_" + count);
        } else if (imageView == mNewPrivacyIv) {
            LostSecurityManager lsm = (LostSecurityManager) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            boolean lostOpen = lsm.isUsePhoneSecurity();
            IntrudeSecurityManager ism = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
            boolean intruderOpen = ism.getIntruderMode();
            boolean result = lostOpen && intruderOpen;
            mPrivacyCountIv.setImageResource(result ? R.drawable.ic_scan_safe : R.drawable.ic_scan_error);
            mPrivacyCountIv.setVisibility(View.VISIBLE);

            mProgressTv.setText(context.getString(R.string.scanning_pattern, 4));
            onScannigFinish(mAppList, mPhotoList, mVideoList);
        }
    }

    private void updateAppList() {
        int count = mAppList == null ? 0 : mAppList.size();
        if (count == 0) {
            mAppCountIv.setVisibility(View.VISIBLE);
            mAppCountTv.setVisibility(View.GONE);
        } else {
            mAppCountTv.setVisibility(View.VISIBLE);
            mAppCountTv.setText("+" + count);
            mAppCountIv.setVisibility(View.GONE);
        }
    }

    private void updatePhotoList() {
        int count = mPhotoList == null ? 0 : mPhotoList.photoItems.size();
        if (count == 0) {
            mPicCountIv.setVisibility(View.VISIBLE);
            mPicCountTv.setVisibility(View.GONE);
            mPicCountIv.setImageResource(mPhotoScanFinish ? R.drawable.ic_scan_safe : R.drawable.ic_scan_error);
        } else {
            mPicCountIv.setVisibility(View.GONE);
            mPicCountTv.setVisibility(View.VISIBLE);
            mPicCountTv.setText("+" + count);
        }
    }

    private void updateVideoList() {
        int count = mVideoList == null ? 0 : mVideoList.size();
        if (count == 0) {
            mVidCountIv.setVisibility(View.VISIBLE);
            mVidCountTv.setVisibility(View.GONE);
            mVidCountIv.setImageResource(mVideoScanFinish ? R.drawable.ic_scan_safe : R.drawable.ic_scan_error);
        } else {
            mVidCountIv.setVisibility(View.GONE);
            mVidCountTv.setVisibility(View.VISIBLE);
            mVidCountTv.setText("+" + count);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan_cancel_tv:
                mScanning = false;
                mActivity.onExitScanning();
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "scan", "cancel");
                break;
            case R.id.scan_process_tv:
                mActivity.startProcess();
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "instant");
                break;
        }
    }

    public boolean isScanFinish(ScanningImageView imageView) {
        if (imageView == mNewAppIv) {
            return mAppScanFinish;
        } else if (imageView == mNewPhotoIv) {
            return mPhotoScanFinish;
        } else if (imageView == mNewVideoIv) {
            return mVideoScanFinish;
        }

        return false;
    }

    /* 3.2 advertise begin */
    private void loadAd() {
        mAdLoaded = false;
        AppMasterPreference amp = AppMasterPreference.getInstance(mActivity);
        if (amp.getADAfterScan() == 1) {
            MobvistaEngine.getInstance(mActivity).loadMobvista(AD_AFTER_SCAN, new MobvistaEngine.MobvistaListener() {

                @Override
                public void onMobvistaFinished(int code, final Campaign campaign, String msg) {
                    if (code == MobvistaEngine.ERR_OK) {
                        LeoLog.d("AfterPrivacyScan", "onMobvistaFinished: " + campaign.getAppName());
                        sAdImageListener = new AdPreviewLoaderListener(HomeScanningFragment.this, campaign);
                        ImageLoader.getInstance().loadImage(campaign.getImageUrl(), sAdImageListener);
                    }
                }

                @Override
                public void onMobvistaClick(Campaign campaign) {
                    LeoLog.d("AfterPrivacyScan", "onMobvistaClick");
                    LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                    lm.filterSelfOneMinites();
                }
            });
        }
    }

    private void destroyAd() {
        MobvistaEngine.getInstance(mActivity).release(AD_AFTER_SCAN);
    }

    public static class AdPreviewLoaderListener implements ImageLoadingListener {
        WeakReference<HomeScanningFragment> mFragment;
        Campaign mCampaign;

        public AdPreviewLoaderListener(HomeScanningFragment fragment, final Campaign campaign) {
            mFragment = new WeakReference<HomeScanningFragment>(fragment);
            mCampaign = campaign;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            HomeScanningFragment fragment = mFragment.get();
            if (loadedImage != null && fragment != null) {
                fragment.mAdLoaded = true;
                LeoLog.d("AfterPrivacyScan", "[HomeScanningFragment] onLoadingComplete -> " + imageUri);

                fragment.initAdLayout(fragment.mRootView,
                        mCampaign, loadedImage);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }
    private static AdPreviewLoaderListener sAdImageListener;

    private void initAdLayout(View rootView, Campaign campaign, Bitmap previewImage) {
        View adView = rootView.findViewById(R.id.ad_content);
        TextView tvTitle = (TextView) adView.findViewById(R.id.item_title);
        tvTitle.setText(campaign.getAppName());
        Button btnCTA = (Button) adView.findViewById(R.id.ad_result_cta);
        btnCTA.setText(campaign.getAdCall());
        ImageView preview = (ImageView) adView.findViewById(R.id.item_ad_preview);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setImageBitmap(previewImage);
        ImageView iconView = (ImageView) adView.findViewById(R.id.ad_icon);
        ImageLoader.getInstance().displayImage(campaign.getIconUrl(), iconView);
        adView.setVisibility(View.VISIBLE);
        MobvistaEngine.getInstance(mActivity).registerView(AD_AFTER_SCAN, adView);
    }
    /* 3.2 advertise end */

    private WeakRunnable mAppRunnable = new WeakRunnable(this, new Runnable() {
        @Override
        public void run() {
            LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            long start = SystemClock.elapsedRealtime();
            mAppList = lm.getNewAppList();
            try {
                mScanAppName = DataUtils.getThreeRandomAppName(mAppList);
            } catch (Exception e) {
                e.printStackTrace();
                mScanAppName = "Exception.";
            }
            mAppScanFinish = true;
            int appScore = lm.getSecurityScore(mAppList);
            mPrivacyHelper.onSecurityChange(MgrContext.MGR_APPLOCKER, appScore);
//            updateNewAppList();
            LeoLog.i(TAG, "appList, cost: " + (SystemClock.elapsedRealtime() - start));
        }
    });

    private WeakRunnable mPhotoRunnable = new WeakRunnable(this, new Runnable() {
        @Override
        public void run() {
            long start = SystemClock.elapsedRealtime();
            PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
            List<PhotoItem> photoItems = pdm.getAddPic();
            mPhotoList = new PhotoList();
            mPhotoList.photoItems = photoItems;
            mPhotoScanFinish = true;
            mPicScore = pdm.getPicScore(mPhotoList == null ? 0 : mPhotoList.photoItems.size());

            mPhotoList.inDifferentDir = DataUtils.differentDirPic(photoItems);
//            updateNewPicList();
            LeoLog.i(TAG, "photoItems, cost: " + (SystemClock.elapsedRealtime() - start));
        }
    });

    private WeakRunnable mVidRunnable = new WeakRunnable(this, new Runnable() {
        @Override
        public void run() {
            long start = SystemClock.elapsedRealtime();
            PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
            mVideoList = pdm.getAddVid();
            mVideoScanFinish = true;
            int vidScore = pdm.getVidScore(mVideoList == null ? 0 : mVideoList.size());
            LeoLog.i(TAG, "videoItemBeans, cost: " + (SystemClock.elapsedRealtime() - start));

            mPrivacyHelper.onSecurityChange(MgrContext.MGR_PRIVACY_DATA, mPicScore + vidScore);
//            updateNewVidList();
        }
    });

    private WeakRunnable mContactRunnable = new WeakRunnable(this, new Runnable() {
        @Override
        public void run() {
            long start = SystemClock.elapsedRealtime();
            PrivacyContactManager pcm = (PrivacyContactManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
            List<ContactBean> contactBeans = pcm.getFrequentContacts();
            mActivity.setContactList(contactBeans);
//            updateNewContactList();
            LeoLog.i(TAG, "contactBeans, cost: " + (SystemClock.elapsedRealtime() - start));
        }
    });

    private static class WeakRunnable implements Runnable {
        WeakReference<HomeScanningFragment> weakReference;
        Runnable runnable;

        public WeakRunnable(HomeScanningFragment fragment, Runnable runnable) {
            weakReference = new WeakReference<HomeScanningFragment>(fragment);
            this.runnable = runnable;
        }

        @Override
        public void run() {
            HomeScanningFragment fragment = weakReference.get();
            if (fragment == null) {
                LeoLog.d(TAG, "before run, fragment is null.");
                return;
            }

            if (fragment.isRemoving() || fragment.isDetached()) {
                LeoLog.d(TAG, "before run, fragment is removing...");
                fragment.mAppList = null;
                fragment.mPhotoList = null;
                fragment.mVideoList = null;
                return;
            }

            runnable.run();
        }
    }

    public static class PhotoList {
        public List<PhotoItem> photoItems = new ArrayList<PhotoItem>();
        public boolean inDifferentDir;
    }

    public void OnItemAnimationEnd(LinearLayout layout) {
        updateUIOnAnimationEnd(layout);
        if (layout == mNewLostLayout) {
            ThreadManager.executeOnAsyncThread(mVidRunnable);
        } else if (layout == mNewVidLayout) {
            ThreadManager.executeOnAsyncThread(mPhotoRunnable);
        } else if (layout == mNewPicLayout){
            ThreadManager.executeOnAsyncThread(mAppRunnable);
        }
    }

    public void OnItemAnimationStart(LinearLayout layout) {
        if (isDetached() || isRemoving() || getActivity() == null) return;

        Context context = AppMasterApplication.getInstance();
        if (layout == mNewAppLayout) {
            updateAppStartList();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 7));
        } else if (layout == mNewPicLayout) {
            updatePicStartList();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 6));
        } else if (layout == mNewVidLayout) {
            updateVidStartList();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 5));
        } else if (layout == mNewInstructLayout) {
            updateInstructStartList();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 2));
        } else if (layout == mNewWifiLayout) {
            updateWifiStartList();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 3));
        } else if (layout == mNewLostLayout) {
            updateLostStartList();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 4));
        } else if (layout == mNewContactLayout) {
            updateContactStartList();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 1));
        }
    }

    public void updateUIOnAnimationEnd(LinearLayout layout) {
        if (isDetached() || isRemoving() || getActivity() == null) return;

        Context context = AppMasterApplication.getInstance();
        if (layout == mNewAppLayout) {
            updateNewAppList();
            updateNewContactList();
//            mProgressTv.setText(context.getString(R.string.scanning_pattern, 7));
            onViewScanningFinish(mAppList, mPhotoList, mVideoList);
        } else if (layout == mNewPicLayout) {
            updateNewPicList();
//            mProgressTv.setText(context.getString(R.string.scanning_pattern, 6));
        } else if (layout == mNewVidLayout) {
            updateNewVidList();
//            mProgressTv.setText(context.getString(R.string.scanning_pattern, 5));
        } else if (layout == mNewInstructLayout) {
            updateNewInstructList();
//            mProgressTv.setText(context.getString(R.string.scanning_pattern, 2));
        } else if (layout == mNewWifiLayout) {
            updateNewWifiList();
//            mProgressTv.setText(context.getString(R.string.scanning_pattern, 3));
        } else if (layout == mNewLostLayout) {
            updateNewLostList();
//            mProgressTv.setText(context.getString(R.string.scanning_pattern, 4));
        } else if (layout == mNewContactLayout) {
//            mProgressTv.setText(context.getString(R.string.scanning_pattern, 1));
        }
    }

    private void onViewScanningFinish(final List<AppItemInfo> appList, final PhotoList photoItems,
                                 final List<VideoItemBean> videoItemBeans) {
        if (getActivity() == null || isDetached() || isRemoving()) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mCancelBtn.setVisibility(View.GONE);
                mProcessBtn.setVisibility(View.VISIBLE);
                mScannTitleTv.setText(R.string.pri_pro_scanning_finish);
//                mProcessTv.setTextColor(mActivity.getToolbarColor());
                mActivity.onScanningFinish(mAppList, mPhotoList, mVideoList, mAppNotifyText);
            }
        });
    }

    private void updateNewAppList() {
        int count = mAppList == null ? 0 : mAppList.size();
        mNewAppImg.setImageResource(count > 0 ? R.drawable.ic_scan_error : R.drawable.ic_scan_safe);
        mNewAppTitle.setText(mActivity.getResources().getString(R.string.scan_app_title, count));
        if (count == 0) {
            mAppNotifyText = mActivity.getResources().
                    getString(R.string.scan_app_content_zero);
            mNewAppContent.setText(mAppNotifyText);
        } else if (count <= 3){
            mAppNotifyText = mActivity.getResources().
                    getString(R.string.scan_app_content_less_three, mScanAppName);
            mNewAppContent.setText(mAppNotifyText);
        } else {
            mAppNotifyText = mActivity.getResources().
                    getString(R.string.scan_app_content, mScanAppName);
            mNewAppContent.setText(mAppNotifyText);
        }
        mNewAppLoading.setVisibility(View.GONE);
        mNewAppImg.setVisibility(View.VISIBLE);
    }

    private void updateNewPicList() {
        int count = mPhotoList == null ? 0 : mPhotoList.photoItems.size();
        mNewPicImg.setImageResource(count > 0 ? R.drawable.ic_scan_error : R.drawable.ic_scan_safe);
        mNewPicTitle.setText(mActivity.getResources().getString(R.string.scan_pic_title, count));
        mNewPicContent.setText(mActivity.getResources().
                getString(R.string.scan_pic_content));
        mNewPicLoading.setVisibility(View.GONE);
        mNewPicImg.setVisibility(View.VISIBLE);
    }

    private void updateNewVidList() {
        int count = mVideoList == null ? 0 : mVideoList.size();
        mNewVidImg.setImageResource(count > 0 ? R.drawable.ic_scan_error : R.drawable.ic_scan_safe);
        mNewVidTitle.setText(mActivity.getResources().getString(R.string.scan_vid_title, count));
        mNewVidContent.setText(mActivity.getResources().
                getString(R.string.scan_vid_content));
        mNewVidLoading.setVisibility(View.GONE);
        mNewVidImg.setVisibility(View.VISIBLE);
    }

    private void updateNewInstructList() {
        LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        boolean flag = manager.isUsePhoneSecurity();
        mNewInstructImg.setImageResource(flag ? R.drawable.ic_scan_safe : R.drawable.ic_scan_error);
        mNewInstructContent.setText(mActivity.getResources().
                getString(R.string.scan_instruct_content));
        mNewInstructLoading.setVisibility(View.GONE);
        mNewInstructImg.setVisibility(View.VISIBLE);
    }

    private void updateNewWifiList() {
        WifiSecurityManager wsm = (WifiSecurityManager)
                MgrContext.getManager(MgrContext.MGR_WIFI_SECURITY);
        boolean isScnnedEver = wsm.getLastScanState();
        if (wsm.getWifiState() == WifiSecurityManager.NO_WIFI) {
            mNewWifiImg.setImageResource(R.drawable.ic_scan_error);
        } else {
            mNewWifiImg.setImageResource(
                    isScnnedEver ? R.drawable.ic_scan_safe: R.drawable.ic_scan_error);
        }
        mNewWifiContent.setText(mActivity.getResources().
                getString(R.string.scan_wifi_content));
        mNewWifiLoading.setVisibility(View.GONE);
        mNewWifiImg.setVisibility(View.VISIBLE);
    }

    private void updateNewLostList() {
        LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        boolean flag = manager.isUsePhoneSecurity();
        if (flag) {
            mNewLostImg.setImageResource(R.drawable.ic_scan_safe);
        } else {
            mNewLostImg.setImageResource(R.drawable.ic_scan_error);
            mNewLostContent.setText("开启入侵者防护功能");
            mNewLostScore.setText("处理+5分");
        }
        mNewLostLoading.setVisibility(View.GONE);
        mNewLostImg.setVisibility(View.VISIBLE);
    }

    private void updateNewContactList() {
        mNewContactImg.setImageResource(R.drawable.ic_scan_error);
        mNewContactContent.setText("隐私联系人功能");
        mNewContactScore.setText("处理+5分");
        mNewContactLoading.setVisibility(View.GONE);
        mNewContactImg.setVisibility(View.VISIBLE);
    }


    private void startAnimator(final LinearLayout layout) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(layout, "translationY", -100, layout.getTranslationY());
        objectAnimator.setDuration(250);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                layout.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mScrollLayout.setLayoutTransition(null);
            }
        });
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mScrollLayout.setLayoutTransition(mTransition);
            }
        });

        objectAnimator.start();
    }

    private void updateContactStartList() {
        mNewContactTitle.setText("频繁联系人");
        startAnimator(mNewContactLayout);
    }

    private void updateInstructStartList() {
        mNewInstructTitle.setText(mActivity.getResources().getString(R.string.scan_instruct_title));
        startAnimator(mNewInstructLayout);
    }

    private void updateWifiStartList() {
        mNewWifiTitle.setText(mActivity.getResources().getString(R.string.scan_wifi_title));
        startAnimator(mNewWifiLayout);
    }

    private void updateLostStartList() {
        mNewLostTitle.setText("手机防盗");
        startAnimator(mNewLostLayout);
    }

    private void updateVidStartList() {
        mNewVidTitle.setText("正在扫描视频");
        startAnimator(mNewVidLayout);
    }

    private void updatePicStartList() {
        mNewPicTitle.setText("正在扫描图片");
        startAnimator(mNewPicLayout);
    }

    private void updateAppStartList() {
        mNewAppTitle.setText("正在扫描应用");
        startAnimator(mNewAppLayout);
    }
}
