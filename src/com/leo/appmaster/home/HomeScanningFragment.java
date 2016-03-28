package com.leo.appmaster.home;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.ad.ADEngineWrapper;
import com.leo.appmaster.ad.WrappedCampaign;
import com.leo.appmaster.applocker.lockswitch.BlueToothLockSwitch;
import com.leo.appmaster.applocker.lockswitch.SwitchGroup;
import com.leo.appmaster.applocker.lockswitch.WifiLockSwitch;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
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
import com.leo.appmaster.utils.DataUtils;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmaster.videohide.VideoItemBean;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.ValueAnimator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2015/10/18.
 */
public class HomeScanningFragment extends Fragment implements View.OnClickListener {
    private static final int MAX_HEIGHT_ONE_LINE = 35;
    private static final int MAX_HEIGHT_TWO_LINE = 51;
    private static final int MAX_HEIGHT_THREE_LINE = 72;
    private static final int MAX_HEIGHT_FOUR_LINE = 87;
    private static final int MAX_HEIGHT_OTHERS_LINE = 102;
    private static final int MIN_HEIGHT_DP = 35;
    private static final int TEXT_NO_CONTENT_SCORE = 0;
    private static final int TEXT_HAVE_CONTENT_ONLY = 1;
    private static final int TEXT_HAVE_CONTENT_SCORE = 2;
    private static final int ONE_LINE = 20;
    private static final int TWO_LINE = 37;
    private static final int THREE_LINE = 55;
    private static final int FOUR_LINE = 71;
    private static final int OTHERS_LINE = 88;
    private static final String TAG = "HomeScanningFragment";
    private static final byte[] LOCK = new byte[1];

    private int mMoveHeight = -1;

    // 3.2 advertise
    private static final String AD_AFTER_SCAN = Constants.UNIT_ID_243;
    private static int mAdSource = ADEngineWrapper.SOURCE_MOB; // 默认值
    private boolean mDidLoadAd = false;
    private boolean mAdLoaded;
    private View mRootView;
    private View mAdLayout;

    private View mCancelBtn;
    private TextView mCancelTv;

    private View mProcessBtn;
    private TextView mProcessTv;

    private TextView mScannTitleTv;
    private TextView mProgressTv;

    private HomeActivity mActivity;

    private List<AppItemInfo> mAppList;
    private PhotoList mPhotoList;
    private List<VideoItemBean> mVideoList;
    private List<ContactBean> mContactList;

    private boolean mAppScanFinish;
    private boolean mPhotoScanFinish;
    private boolean mVideoScanFinish;
    private boolean mContactScanFinish;

    private boolean mScanning;

    private PrivacyHelper mPrivacyHelper;
    private HomeScanningController mController;
    private int mPicScore;
    private int mAppScore;
    private int mVidScore;

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
    private TextView mNewLostContent;
    private TextView mNewLostScore;
    private LoadingView mNewLostLoading;
    private LinearLayout mNewLostLayout;

    private ImageView mNewWifiImg;
    private LoadingView mNewWifiLoading;
    private LinearLayout mNewWifiLayout;

    private ImageView mNewInstructImg;
    private TextView mNewInstructContent;
    private TextView mNewInstructScore;
    private LoadingView mNewInstructLoading;
    private LinearLayout mNewInstructLayout;

    private ImageView mNewContactImg;
    private LoadingView mNewContactLoading;
    private LinearLayout mNewContactLayout;

    private String mScanAppName;
    private String mScanAppNameStep;
    private RelativeLayout mScrollLayout;
    private LayoutTransition mTransition;
    private View mBackView;
    private int mBackViewHeight = 0;
    private int mNowHeight;
    private int mScreenHeight;

    private int normalTypeHeight = 0;
    private int shortTypeHeight = 0;
    private int adTypeHeight = 0;
    private boolean mIsNeedContractVisible = true;
    private boolean mIsExit;
    private boolean mIsInsAvaliable = true;

    private View mWifiScanBottomLine;
	private View mAdView;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            updateNewContactList();
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

        if (AppMasterPreference.getInstance(mActivity).getIsNeedCutBackupUninstallAndPrivacyContact()) {
            mIsNeedContractVisible = false;
        }

        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                long start = SystemClock.elapsedRealtime();
                mScannTitleTv = (TextView) view.findViewById(R.id.scan_title_tv);
                mProgressTv = (TextView) view.findViewById(R.id.scan_progress_tv);
                mScrollLayout = (RelativeLayout) view.findViewById(R.id.scrollView_layout);

                mCancelBtn = view.findViewById(R.id.scan_cancel_rv);
                mCancelTv = (TextView) view.findViewById(R.id.scan_cancel_tv);

                mProcessBtn = view.findViewById(R.id.scan_process_rv);
                mProcessTv = (TextView) view.findViewById(R.id.scan_process_tv);

                mCancelTv.setOnClickListener(HomeScanningFragment.this);
                mProcessTv.setOnClickListener(HomeScanningFragment.this);

                mNowHeight = DipPixelUtil.dip2px(mActivity, 74);

                initAppLayout(view);

                initPicLayout(view);

                initVidLayout(view);

                initLostLayout(view);

                initInstructLayout(view);

                initWifiLayout(view);

                initContactLayout(view);

                mAdLayout = view.findViewById(R.id.ad_content);
                mAdLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        adTypeHeight = mAdLayout.getHeight();
                    }
                });

                mBackView = view.findViewById(R.id.back_view);
                ViewGroup.LayoutParams params = mBackView.getLayoutParams();
                mBackViewHeight = params.height;


                WindowManager wm = (WindowManager) mActivity
                        .getSystemService(Context.WINDOW_SERVICE);
                mScreenHeight = wm.getDefaultDisplay().getHeight();
                LeoLog.e("theDipHeight", "" + mScreenHeight + wm.getDefaultDisplay().getWidth());

                IntrudeSecurityManager manager = (IntrudeSecurityManager)
                        MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
                mIsInsAvaliable = manager.getIsIntruderSecurityAvailable();

                mTransition = new LayoutTransition();
                mTransition.setAnimator(LayoutTransition.CHANGE_APPEARING,
                        mTransition.getAnimator(LayoutTransition.CHANGE_APPEARING));

                LeoLog.d(TAG, "zany, cost: " + (SystemClock.elapsedRealtime() - start));
                mController = new HomeScanningController(mActivity, HomeScanningFragment.this, mNewAppLayout, mNewPicLayout,
                        mNewVidLayout, mNewLostLayout, mNewWifiLayout, mNewInstructLayout,
                        mNewContactLayout, mIsInsAvaliable);

                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startScanController();
                    }
                }, 200);

                mRootView = view;
//                ThreadManager.executeOnSubThread(runnable)
                ThreadManager.executeOnSubThread(new Runnable() {
                    @Override
                    public void run() {
                        LeoLog.d("AfterPrivacyScan", "onMobvistaFinished: start");
                        loadAd();
                    }
                });
            }
        }, 200);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_scanning_test, container, false);
    }

    private void initAppLayout(View parent) {
        ViewStub viewStub = (ViewStub) parent.findViewById(R.id.scan_new_app_layout);
        if (viewStub == null) {
            return;
        }
        View view = viewStub.inflate();
        mNewAppImg = (ImageView) view.findViewById(R.id.scan_new_app_img);
        mNewAppTitle = (TextView) view.findViewById(R.id.scan_new_app_title);
        mNewAppContent = (TextView) view.findViewById(R.id.scan_new_app_content);
        mNewAppScore = (TextView) view.findViewById(R.id.scan_new_app_score);
        mNewAppLoading = (LoadingView) view.findViewById(R.id.scan_new_app_loading);
        mNewAppLayout = (LinearLayout) view.findViewById(R.id.scan_new_app_layout);
        mNewAppLayout.post(new Runnable() {
            @Override
            public void run() {
                shortTypeHeight = mNewAppLayout.getHeight();
            }
        });
    }

    private void initPicLayout(View parent) {
        ViewStub viewStub = (ViewStub) parent.findViewById(R.id.scan_new_pic_layout);
        if (viewStub == null) {
            return;
        }
        View view = viewStub.inflate();
        mNewPicImg = (ImageView) view.findViewById(R.id.scan_new_pic_img);
        mNewPicTitle = (TextView) view.findViewById(R.id.scan_new_pic_title);
        mNewPicContent = (TextView) view.findViewById(R.id.scan_new_pic_content);
        mNewPicScore = (TextView) view.findViewById(R.id.scan_new_pic_score);
        mNewPicLoading = (LoadingView) view.findViewById(R.id.scan_new_pic_loading);
        mNewPicLayout = (LinearLayout) view.findViewById(R.id.scan_new_pic_layout);
        mNewPicLayout.post(new Runnable() {
            @Override
            public void run() {
                normalTypeHeight = mNewPicLayout.getHeight();
            }
        });
    }

    private void initVidLayout(View parent) {
        ViewStub viewStub = (ViewStub) parent.findViewById(R.id.scan_new_vid_layout);
        if (viewStub == null) {
            return;
        }
        View view = viewStub.inflate();
        mNewVidImg = (ImageView) view.findViewById(R.id.scan_new_vid_img);
        mNewVidTitle = (TextView) view.findViewById(R.id.scan_new_vid_title);
        mNewVidContent = (TextView) view.findViewById(R.id.scan_new_vid_content);
        mNewVidScore = (TextView) view.findViewById(R.id.scan_new_vid_score);
        mNewVidLoading = (LoadingView) view.findViewById(R.id.scan_new_vid_loading);
        mNewVidLayout = (LinearLayout) view.findViewById(R.id.scan_new_vid_layout);
    }

    private void initLostLayout(View parent) {
        ViewStub viewStub = (ViewStub) parent.findViewById(R.id.scan_new_lost_layout);
        if (viewStub == null) {
            return;
        }
        View view = viewStub.inflate();
        mNewLostImg = (ImageView) view.findViewById(R.id.scan_new_lost_img);
        mNewLostContent = (TextView) view.findViewById(R.id.scan_new_lost_content);
        mNewLostScore = (TextView) view.findViewById(R.id.scan_new_lost_score);
        mNewLostLoading = (LoadingView) view.findViewById(R.id.scan_new_lost_loading);
        mNewLostLayout = (LinearLayout) view.findViewById(R.id.scan_new_lost_layout);
    }

    private void initInstructLayout(View parent) {
        ViewStub viewStub = (ViewStub) parent.findViewById(R.id.scan_new_instruct_layout);
        if (viewStub == null) {
            return;
        }
        View view = viewStub.inflate();
        mNewInstructImg = (ImageView) view.findViewById(R.id.scan_new_instruct_img);
        mNewInstructContent = (TextView) view.findViewById(R.id.scan_new_instruct_content);
        mNewInstructScore = (TextView) view.findViewById(R.id.scan_new_instruct_score);
        mNewInstructLoading = (LoadingView) view.findViewById(R.id.scan_new_instruct_loading);
        mNewInstructLayout = (LinearLayout) view.findViewById(R.id.scan_new_instruct_layout);
    }

    private void initWifiLayout(View parent) {
        ViewStub viewStub = (ViewStub) parent.findViewById(R.id.scan_new_wifi_layout);
        if (viewStub == null) {
            return;
        }
        View view = viewStub.inflate();
        mNewWifiImg = (ImageView) view.findViewById(R.id.scan_new_wifi_img);
        mNewWifiLoading = (LoadingView) view.findViewById(R.id.scan_new_wifi_loading);
        mNewWifiLayout = (LinearLayout) view.findViewById(R.id.scan_new_wifi_layout);
        mWifiScanBottomLine = (View) view.findViewById(R.id.scan_new_wifi_bottom_line);
    }

    private void initContactLayout(View parent) {
        ViewStub viewStub = (ViewStub) parent.findViewById(R.id.scan_new_contact_layout);
        if (viewStub == null) {
            return;
        }
        View view = viewStub.inflate();
        mNewContactImg = (ImageView) view.findViewById(R.id.scan_new_contact_img);
        mNewContactLoading = (LoadingView) view.findViewById(R.id.scan_new_contact_loading);
        mNewContactLayout = (LinearLayout) view.findViewById(R.id.scan_new_contact_layout);
        if (AppMasterPreference.getInstance(mActivity).getIsNeedCutBackupUninstallAndPrivacyContact()) {
            viewStub.setVisibility(View.GONE);
            if (mWifiScanBottomLine != null) {
                mWifiScanBottomLine.setVisibility(View.INVISIBLE);
                mNowHeight = DipPixelUtil.dip2px(mActivity, 14);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        destroyAd();
        if (mController != null) {
            mController.detachTheController();
        }
        mIsExit = true;
        mActivity.onMemoryLessScanCancel();
    }

    private void startScanController() {
        if (mScanning) return;

        mProgressTv.setText(R.string.pri_pro_scanning);
        mCancelBtn.setVisibility(View.VISIBLE);
        mProcessBtn.setVisibility(View.GONE);
        LeoLog.i(TAG, "start to scaning.");
        mController.startScanning();
        ThreadManager.executeOnSubThread(mContactRunnable);
        ThreadManager.executeOnAsyncThread(mPhotoRunnable);
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
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "scan", "instant");
                break;
        }
    }

    /* 3.2 advertise begin */
    private void loadAd() {
        mAdLoaded = false;
        // TODO: 从SP里面去读实际的开关值
        mAdSource = ADEngineWrapper.SOURCE_MOB;
        AppMasterPreference amp = AppMasterPreference.getInstance(mActivity);
        LeoLog.d("AfterPrivacyScan", "" + amp.getADAfterScan());
        if (amp.getADAfterScan() == 1) {
            /* 3.3.2 封装Max与Mob SDK */
            mDidLoadAd = true;
            ADEngineWrapper.getInstance(mActivity).loadAd(mAdSource, AD_AFTER_SCAN,
                    new ADEngineWrapper.WrappedAdListener() {
                        @Override
                        public void onWrappedAdLoadFinished(int code, List<WrappedCampaign> campaign, String msg) {
                            if (code == MobvistaEngine.ERR_OK) {
                                LeoLog.d("AfterPrivacyScan", "Ad data loaded: " + campaign.get(0).getAppName());
                                sAdImageListener = new AdPreviewLoaderListener(HomeScanningFragment.this, campaign.get(0));
                                ImageLoader.getInstance().loadImage(campaign.get(0).getImageUrl(), sAdImageListener);
                            }
                        }

                        @Override
                        public void onWrappedAdClick(WrappedCampaign campaign, String unitID) {
                            LeoLog.d("AfterPrivacyScan", "onMobvistaClick");
                            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "ad_cli", "adv_cnts_scan");
                            SDKWrapper.addEvent(HomeScanningFragment.this.getActivity().getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_click", "ad pos: " + unitID + " click", mAdSource, null);
                            LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                            lm.filterSelfOneMinites();
                        }
                    });
        }
    }

    private void destroyAd() {
        if (mAdLayout != null) {
            mAdLayout.setVisibility(View.GONE);
            mAdLayout.setScaleY(0.0f);
        }
        /* 3.3.2 封装Max与Mob SDK */
        if (mDidLoadAd) {
            ADEngineWrapper.getInstance(mActivity).releaseAd(mAdSource, AD_AFTER_SCAN, mAdView);
        }
    }

    public static class AdPreviewLoaderListener implements ImageLoadingListener {
        WeakReference<HomeScanningFragment> mFragment;
        /* 3.3.2 封装Max与Mob SDK */
        WrappedCampaign mCampaign;

        public AdPreviewLoaderListener(HomeScanningFragment fragment, final WrappedCampaign campaign) {
            mFragment = new WeakReference<HomeScanningFragment>(fragment);
            mCampaign = campaign;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            SDKWrapper.addEvent(AppMasterApplication.getInstance().getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_load_image", "ad pos: " + AD_AFTER_SCAN + " prepare for load image", mAdSource, null);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            SDKWrapper.addEvent(AppMasterApplication.getInstance().getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_load_image", "ad pos: " + AD_AFTER_SCAN + " load image failed", mAdSource, null);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
            final HomeScanningFragment fragment = mFragment.get();
            if (loadedImage != null && fragment != null) {
                fragment.mAdLoaded = true;
                SDKWrapper.addEvent(AppMasterApplication.getInstance().getApplicationContext(), "max_ad", SDKWrapper.P1, "ad_load_image", "ad pos: " + AD_AFTER_SCAN + " image size: " + loadedImage.getByteCount(), mAdSource, null);
                LeoLog.d("AfterPrivacyScan", "[HomeScanningFragment] onLoadingComplete -> " + imageUri);
                ThreadManager.getUiThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        fragment.initAdLayout(fragment.mRootView, mCampaign, loadedImage);
                    }
                });

            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

    private static AdPreviewLoaderListener sAdImageListener;

    private void initAdLayout(View rootView, WrappedCampaign campaign, Bitmap previewImage) {
        View adView = rootView.findViewById(R.id.ad_content);
//        mAdLayout = adView;
        TextView tvTitle = (TextView) adView.findViewById(R.id.item_title);
        tvTitle.setText(campaign.getAppName());
        Button btnCTA = (Button) adView.findViewById(R.id.ad_result_cta);
        btnCTA.setText(campaign.getAdCall());
        ImageView preview = (ImageView) adView.findViewById(R.id.item_ad_preview);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setImageBitmap(previewImage);
        ImageView iconView = (ImageView) adView.findViewById(R.id.ad_icon);
        ImageLoader.getInstance().displayImage(campaign.getIconUrl(), iconView);
        /* 3.3.2 封装Max与Mob SDK */
		mAdView = adView;
        ADEngineWrapper.getInstance(mActivity).registerView(mAdSource, adView, AD_AFTER_SCAN);
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "ad_act", "adv_shws_scan");
        SDKWrapper.addEvent(AppMasterApplication.getInstance(), "max_ad", SDKWrapper.P1, "ad_show", "ad pos: " + AD_AFTER_SCAN + " adShow",  mAdSource, null);
    }
    /* 3.2 advertise end */

    private List<AppItemInfo> switchList;
    private WeakRunnable mAppRunnable = new WeakRunnable(this, new Runnable() {
        @Override
        public void run() {
            LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            long start = SystemClock.elapsedRealtime();
            mAppList = lm.getNewAppList();


            //first time , show wifi && bluetooth
            WifiLockSwitch wifiSwitch = new WifiLockSwitch();
            BlueToothLockSwitch blueToothLockSwitch = new BlueToothLockSwitch();
            boolean isShowed = wifiSwitch.getScreenShowed();
            if (!isShowed) {
                switchList = getSwitchList(wifiSwitch, blueToothLockSwitch);
                if (switchList != null && switchList.size() > 0) {
                    mAppList.addAll(0, switchList);
                }
            }

            try {
                mScanAppName = DataUtils.getThreeRandomAppName(mAppList, mActivity).get(0);
                mScanAppNameStep = DataUtils.getThreeRandomAppName(mAppList, mActivity).get(1);
            } catch (Exception e) {
                e.printStackTrace();
                mScanAppName = "";
            }
            mAppScanFinish = true;

            //wifi && blue

            mAppScore = lm.getSecurityScore(mAppList);
            mPrivacyHelper.onSecurityChange(MgrContext.MGR_APPLOCKER, mAppScore);
//            updateNewAppList();
//            com.leo.tools.animator.ObjectAnimator appAnim = mController.getNewAppAnim();
//            if (appAnim != null) {
//                appAnim.end();
//                appAnim.cancel();
//            }
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
            if (mVideoScanFinish) {
                mPrivacyHelper.onSecurityChange(MgrContext.MGR_PRIVACY_DATA, mPicScore + mVidScore);
            }
//            com.leo.tools.animator.ObjectAnimator picAnim = mController.getNewPicAnim();
//            if (picAnim != null) {
//                picAnim.end();
//                picAnim.cancel();
//            }
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
            mVidScore = pdm.getVidScore(mVideoList == null ? 0 : mVideoList.size());
            LeoLog.i(TAG, "videoItemBeans, cost: " + (SystemClock.elapsedRealtime() - start));

            if (mPhotoScanFinish) {
                mPrivacyHelper.onSecurityChange(MgrContext.MGR_PRIVACY_DATA, mPicScore + mVidScore);
            }
//            updateNewVidList();
//            com.leo.tools.animator.ObjectAnimator vidAnim = mController.getNewVidAnim();
//            if (vidAnim != null) {
//                vidAnim.end();
//                vidAnim.cancel();
//            }
        }
    });

    private WeakRunnable mContactRunnable = new WeakRunnable(this, new Runnable() {
        @Override
        public void run() {
            long start = SystemClock.elapsedRealtime();
            PrivacyContactManager pcm = (PrivacyContactManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
            mContactList = pcm.getFrequentContacts();
            mActivity.setContactList(mContactList);
            mContactScanFinish = true;
            if (mHandler != null) {
//                mHandler.obtainMessage().sendToTarget();
            }
            LeoLog.i(TAG, "contactBeans, cost: " + (SystemClock.elapsedRealtime() - start));
        }
    });

    public boolean isItemScanFinish(LinearLayout layout) {
        if (layout == mNewAppLayout) {
            return mAppScanFinish;
        } else if (layout == mNewPicLayout) {
            return mPhotoScanFinish;
        } else if (layout == mNewVidLayout) {
            return mVideoScanFinish;
        }

        return false;
    }

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
                return;
            }

            if (fragment.isRemoving() || fragment.isDetached()) {
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

        } else if (layout == mNewPicLayout) {
            ThreadManager.executeOnAsyncThread(mAppRunnable);
        } else if (layout == mNewAppLayout) {

        }
    }

    private int changItem = 0;
    private int currentPosit = 0;

    public void updateUIOnAnimationEnd(final LinearLayout layout) {
        if (isDetached() || isRemoving() || getActivity() == null || layout == null) return;

        final int firLocation = layout.getHeight();
        Context context = AppMasterApplication.getInstance();

        if (layout == mNewAppLayout) {
            if ((mIsInsAvaliable && currentPosit != 6) ||
                    (!mIsInsAvaliable && currentPosit != 5)) {
                return;
            }
            currentPosit++;
            LeoLog.e("theDipHeight", "enterlayout: mNewAppLayout");
            final int needLongHeight = updateNewAppList();
            if (!mContactScanFinish) {
                updateNewContactList();
            }
            if (mIsInsAvaliable) {
                mProgressTv.setText(context.getString(R.string.scanning_pattern, 7));
            } else {
                mProgressTv.setText(context.getString(R.string.scanning_pattern, 6));
            }
            int score = mPrivacyHelper.getSecurityScore();
            /* show Ad here */
            if (mAdLoaded && score != 100 && adTypeHeight != 0) {
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startAnimator(mAdLayout);
                    }
                }, 1000);
            }
            ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onViewScanningFinish(mAppList, mPhotoList, mVideoList);
                }
            }, 300);

            layout.post(new Runnable() {
                @Override
                public void run() {
                    setItemPlace(layout, changItem, firLocation, needLongHeight);
                }
            });

        } else if (layout == mNewPicLayout) {
            if ((mIsInsAvaliable && currentPosit != 5) ||
                    (!mIsInsAvaliable && currentPosit != 4)) {
                return;
            }
            currentPosit++;
            LeoLog.e("theDipHeight", "enterlayout: mNewPicLayout");
            final int needLongHeight = updateNewPicList();
            if (mIsInsAvaliable) {
                mProgressTv.setText(context.getString(R.string.scanning_pattern, 6));
            } else {
                mProgressTv.setText(context.getString(R.string.scanning_pattern, 5));
            }
            layout.post(new Runnable() {
                @Override
                public void run() {
                    setItemPlace(layout, changItem, firLocation, needLongHeight);
                }
            });
        } else if (layout == mNewVidLayout) {
            if ((mIsInsAvaliable && currentPosit != 4) ||
                    (!mIsInsAvaliable && currentPosit != 3)) {
                return;
            }
            currentPosit++;
            LeoLog.e("theDipHeight", "enterlayout: mNewVidLayout");
            final int needLongHeight = updateNewVidList();
            if (mIsInsAvaliable) {
                mProgressTv.setText(context.getString(R.string.scanning_pattern, 5));
            } else {
                mProgressTv.setText(context.getString(R.string.scanning_pattern, 4));
            }
            layout.post(new Runnable() {
                @Override
                public void run() {
                    setItemPlace(layout, changItem, firLocation, needLongHeight);
                }
            });
        } else if (layout == mNewInstructLayout) {
            if (currentPosit != 2) {
                return;
            }
            currentPosit++;
            LeoLog.e("theDipHeight", "enterlayout: mNewInstructLayout");
            final int needLongHeight = updateNewInstructList();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 3));
            layout.post(new Runnable() {
                @Override
                public void run() {
                    setItemPlace(layout, changItem, firLocation, needLongHeight);
                }
            });
        } else if (layout == mNewWifiLayout) {
            if (currentPosit != 1) {
                return;
            }
            currentPosit++;
            LeoLog.e("theDipHeight", "enterlayout: mNewWifiLayout");
            final int needLongHeight = updateNewWifiList();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 2));

            layout.post(new Runnable() {
                @Override
                public void run() {
                    setItemPlace(layout, changItem, firLocation, needLongHeight);
                }
            });

        } else if (layout == mNewLostLayout) {
            if ((mIsInsAvaliable && currentPosit != 3) ||
                    (!mIsInsAvaliable && currentPosit != 2)) {
                return;
            }
            currentPosit++;
            LeoLog.e("theDipHeight", "enterlayout: mNewLostLayout");
            final int needLongHeight = updateNewLostList();
            if (mIsInsAvaliable) {
                mProgressTv.setText(context.getString(R.string.scanning_pattern, 4));
            } else {
                mProgressTv.setText(context.getString(R.string.scanning_pattern, 3));
            }
//            changItem ++;

            layout.post(new Runnable() {
                @Override
                public void run() {
                    setItemPlace(layout, changItem, firLocation, needLongHeight);
                }
            });
        } else if (layout == mNewContactLayout) {
            if (currentPosit != 0) {
                return;
            }
            currentPosit++;
            LeoLog.e("theDipHeight", "enterlayout: mNewContactLayout");
            final int needLongHeight = updateNewContactList();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 1));

            layout.post(new Runnable() {
                @Override
                public void run() {
                    setItemPlace(layout, changItem, firLocation, needLongHeight);
                }
            });
        }
    }

    public void setItemPlace(View nowLayout, int position, int height, int isNeedLongHeight) {
        if (null == nowLayout) {
            return;
        }
        int moveDistance = 0;

        if (nowLayout.getHeight() != 0 && height != 0) {
            if (nowLayout.getHeight() == height && isNeedLongHeight != TEXT_NO_CONTENT_SCORE) {
                if (isNeedLongHeight == TEXT_HAVE_CONTENT_ONLY) {
                    moveDistance = DipPixelUtil.dip2px(mActivity, MIN_HEIGHT_DP);
                } else if (isNeedLongHeight == TEXT_HAVE_CONTENT_SCORE) {
                    if (nowLayout == mNewAppLayout) {
                        moveDistance = getContentHeight(mNewAppContent);
                    } else if (nowLayout == mNewPicLayout) {
                        moveDistance = getContentHeight(mNewPicContent);
                    } else if (nowLayout == mNewVidLayout) {
                        moveDistance = getContentHeight(mNewVidContent);
                    } else if (nowLayout == mNewLostLayout) {
                        moveDistance = getContentHeight(mNewLostContent);
                    } else if (nowLayout == mNewInstructLayout) {
                        moveDistance = getContentHeight(mNewInstructContent);
                    }
                }
                LeoLog.e("theDipHeight", "== height;mNewAppLayout:" + position + ";" + moveDistance);
            } else {
                moveDistance = nowLayout.getHeight() - height;
                LeoLog.e("theDipHeight", "!!= height;mNewAppLayout:" + position + ";" + moveDistance);
            }
        } else {
            if (isNeedLongHeight == TEXT_NO_CONTENT_SCORE) {
                moveDistance = 0;
            } else if (isNeedLongHeight == TEXT_HAVE_CONTENT_ONLY) {
                moveDistance = DipPixelUtil.dip2px(mActivity, MIN_HEIGHT_DP);
            } else if (isNeedLongHeight == TEXT_HAVE_CONTENT_SCORE) {
                if (nowLayout == mNewAppLayout) {
                    moveDistance = getContentHeight(mNewAppContent);
                } else if (nowLayout == mNewPicLayout) {
                    moveDistance = getContentHeight(mNewPicContent);
                } else if (nowLayout == mNewVidLayout) {
                    moveDistance = getContentHeight(mNewVidContent);
                } else if (nowLayout == mNewLostLayout) {
                    moveDistance = getContentHeight(mNewLostContent);
                } else if (nowLayout == mNewInstructLayout) {
                    moveDistance = getContentHeight(mNewInstructContent);
                }
            }
            LeoLog.e("theDipHeight", "else;mNewAppLayout:" + nowLayout + moveDistance);
        }
        if (lists.size() > position) {

            for (int i = 0; i < lists.size(); i++) {
                if (i < position) {
                    View oldlayout = lists.get(i);
                    startItemExpand(oldlayout, moveDistance);
                }
            }

        }

        mNowHeight = mNowHeight + moveDistance;
        if (mNowHeight > mBackViewHeight) {
            ViewGroup.LayoutParams params = mBackView.getLayoutParams();
            params.height = mNowHeight;
            mBackView.setLayoutParams(params);
        }
        changItem++;
    }

    public void startItemExpand(View oldLayout, int moveDistance) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                oldLayout, "translationY", oldLayout.getTranslationY(), oldLayout.getTranslationY() + moveDistance);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                if (nowItem == moveItem) {
//                    oldLayout.setY(layout.getY() + layout.getHeight());
//                }
            }
        });
        objectAnimator.setDuration(200);
        objectAnimator.start();
    }


    private void onViewScanningFinish(final List<AppItemInfo> appList, final PhotoList photoItems,
                                      final List<VideoItemBean> videoItemBeans) {
        if (getActivity() == null || isDetached() || isRemoving()) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                int score = mPrivacyHelper.getSecurityScore();
                if (score == 100) {
                    mCancelBtn.setVisibility(View.VISIBLE);
                    mProcessBtn.setVisibility(View.GONE);
                } else {
                    mCancelBtn.setVisibility(View.GONE);
                    mProcessBtn.setVisibility(View.VISIBLE);
                }
                mScannTitleTv.setText(R.string.pri_pro_scanning_finish);
//                mProcessTv.setTextColor(mActivity.getToolbarColor());
                mActivity.onScanningFinish(mAppList, mPhotoList, mVideoList, mScanAppNameStep);
            }
        });
    }

    private int updateNewAppList() {
        int isNeedLongHeight = TEXT_NO_CONTENT_SCORE;
        int count = mAppList == null ? 0 : mAppList.size();
        if (count > 0) {
            mNewAppImg.setImageResource(R.drawable.ic_scan_error);
            String text;
            if (switchList != null && switchList.size() > 0) {
                text = mActivity.getResources().getString(R.string.scan_app_title_switch, count);
            } else {
                text = mActivity.getResources().getString(R.string.scan_app_title, count);
            }
            mNewAppTitle.setText(text);
            mNewAppContent.setText(mScanAppName);
            mNewAppContent.setVisibility(View.VISIBLE);
            isNeedLongHeight = TEXT_HAVE_CONTENT_ONLY;
            LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            int score = lm.getIncreaseScore(count);
            if (score > 0) {
                mNewAppScore.setText(mActivity.getResources().
                        getString(R.string.scan_score, score));
                mNewAppScore.setVisibility(View.VISIBLE);
                isNeedLongHeight = TEXT_HAVE_CONTENT_SCORE;
            }
        } else {
            mNewAppImg.setImageResource(R.drawable.ic_scan_safe);
            mNewAppTitle.setText(mActivity.getResources().getString(R.string.scan_app_none));
        }
        mNewAppLoading.setVisibility(View.GONE);
        mNewAppImg.setVisibility(View.VISIBLE);
        return isNeedLongHeight;
    }

    private int updateNewPicList() {
        int isNeedLongHeight = TEXT_NO_CONTENT_SCORE;
        int count = mPhotoList == null ? 0 : mPhotoList.photoItems.size();
        if (count > 0) {
            mNewPicImg.setImageResource(R.drawable.ic_scan_error);
            mNewPicTitle.setText(mActivity.getResources().getString(R.string.scan_pic_title, count));
            mNewPicContent.setText(mActivity.getResources().
                    getString(R.string.scan_pic_content));
            mNewPicContent.setVisibility(View.VISIBLE);
            isNeedLongHeight = TEXT_HAVE_CONTENT_ONLY;
            PrivacyDataManager pdm = (PrivacyDataManager)
                    MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
            int score = pdm.getPicShouldScore(count);
            if (score > 0) {
                mNewPicScore.setText(mActivity.getResources().
                        getString(R.string.scan_score, score));
                mNewPicScore.setVisibility(View.VISIBLE);
                isNeedLongHeight = TEXT_HAVE_CONTENT_SCORE;
            }
        } else {
            mNewPicImg.setImageResource(R.drawable.ic_scan_safe);
            mNewPicTitle.setText(mActivity.getResources().getString(R.string.scan_pic_none));
        }
        mNewPicLoading.setVisibility(View.GONE);
        mNewPicImg.setVisibility(View.VISIBLE);
        return isNeedLongHeight;
    }

    private int updateNewVidList() {
        int isNeedLongHeight = TEXT_NO_CONTENT_SCORE;
        int count = mVideoList == null ? 0 : mVideoList.size();
        if (count > 0) {
            mNewVidImg.setImageResource(R.drawable.ic_scan_error);
            mNewVidTitle.setText(mActivity.getResources().getString(R.string.scan_vid_title, count));
            mNewVidContent.setText(mActivity.getResources().
                    getString(R.string.scan_vid_content));
            mNewVidContent.setVisibility(View.VISIBLE);
            isNeedLongHeight = TEXT_HAVE_CONTENT_ONLY;
            PrivacyDataManager pdm = (PrivacyDataManager)
                    MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
            int score = pdm.getVidShouldScore(count);
            if (score > 0) {
                mNewVidScore.setText(mActivity.getResources().
                        getString(R.string.scan_score, score));
                mNewVidScore.setVisibility(View.VISIBLE);
                isNeedLongHeight = TEXT_HAVE_CONTENT_SCORE;
            }
        } else {
            mNewVidImg.setImageResource(R.drawable.ic_scan_safe);
            mNewVidTitle.setText(mActivity.getResources().getString(R.string.scan_vid_none));
        }
        mNewVidLoading.setVisibility(View.GONE);
        mNewVidImg.setVisibility(View.VISIBLE);
        return isNeedLongHeight;
    }

    private int updateNewInstructList() {

        int isNeedLongHeight = TEXT_NO_CONTENT_SCORE;
        IntrudeSecurityManager manager = (IntrudeSecurityManager)
                MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        boolean flag = manager.getIntruderMode();
        if (flag) {
            mNewInstructImg.setImageResource(R.drawable.ic_scan_safe);
        } else {
            mNewInstructImg.setImageResource(R.drawable.ic_scan_error);
            mNewInstructContent.setText(mActivity.getResources().
                    getString(R.string.scan_instruct_content));
            mNewInstructContent.setVisibility(View.VISIBLE);
            isNeedLongHeight = TEXT_HAVE_CONTENT_ONLY;
            if (manager.getMaxScore() > 0) {
                mNewInstructScore.setText(mActivity.getResources().
                        getString(R.string.scan_score, manager.getMaxScore()));
                mNewInstructScore.setVisibility(View.VISIBLE);
                isNeedLongHeight = TEXT_HAVE_CONTENT_SCORE;
            }
        }
        mNewInstructLoading.setVisibility(View.GONE);
        mNewInstructImg.setVisibility(View.VISIBLE);
        return isNeedLongHeight;
    }

    private int updateNewWifiList() {
        int isNeedLongHeight = TEXT_NO_CONTENT_SCORE;
        WifiSecurityManager wsm = (WifiSecurityManager)
                MgrContext.getManager(MgrContext.MGR_WIFI_SECURITY);
        boolean isScnnedEver = wsm.getLastScanState();
        if (wsm.getWifiState() == WifiSecurityManager.NO_WIFI) {
            mNewWifiImg.setImageResource(R.drawable.ic_scan_error);

        } else {
            if (isScnnedEver) {
                int type = wsm.getWifiSafety();
                if (type == 0) {
                    mNewWifiImg.setImageResource(R.drawable.ic_scan_error);
                } else {
                    mNewWifiImg.setImageResource(R.drawable.ic_scan_safe);
                }
            } else {
                mNewWifiImg.setImageResource(R.drawable.ic_scan_error);
            }

        }
        mNewWifiLoading.setVisibility(View.GONE);
        mNewWifiImg.setVisibility(View.VISIBLE);
        return isNeedLongHeight;
    }

    private int updateNewLostList() {
        int isNeedLongHeight = TEXT_NO_CONTENT_SCORE;
        LostSecurityManagerImpl manager = (LostSecurityManagerImpl)
                MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        boolean flag = manager.isUsePhoneSecurity();
        if (flag) {
            mNewLostImg.setImageResource(R.drawable.ic_scan_safe);
        } else {
            mNewLostImg.setImageResource(R.drawable.ic_scan_error);
            mNewLostContent.setText(R.string.scan_lost_content);
            mNewLostContent.setVisibility(View.VISIBLE);
            isNeedLongHeight = TEXT_HAVE_CONTENT_ONLY;
            if (manager.getMaxScore() > 0) {
                mNewLostScore.setText(mActivity.getResources().
                        getString(R.string.scan_score, manager.getMaxScore()));
                mNewLostScore.setVisibility(View.VISIBLE);
                isNeedLongHeight = TEXT_HAVE_CONTENT_SCORE;
            }
        }
        mNewLostLoading.setVisibility(View.GONE);
        mNewLostImg.setVisibility(View.VISIBLE);
        return isNeedLongHeight;
    }

    private int updateNewContactList() {
        int isNeedLongHeight = TEXT_NO_CONTENT_SCORE;
        if (mContactScanFinish) {
            if (mContactList == null) {
                mNewContactImg.setImageResource(R.drawable.ic_scan_safe);
            } else if (mContactList != null && mContactList.size() == 0) {
                mNewContactImg.setImageResource(R.drawable.ic_scan_safe);
            } else if (mContactList != null && mContactList.size() > 0) {
                mNewContactImg.setImageResource(R.drawable.ic_scan_error);
            }
        } else {
            mNewContactImg.setImageResource(R.drawable.ic_scan_error);
        }
        mNewContactLoading.setVisibility(View.GONE);
        mNewContactImg.setVisibility(View.VISIBLE);
        return isNeedLongHeight;
    }


    public void onLayoutAnimEnd(View layout) {
        if (this.isRemoving() || this.isDetached()) return;

        if (layout == mNewContactLayout) {
            startAnimator(mNewWifiLayout);
        } else if (layout == mNewWifiLayout) {

            if (!mIsInsAvaliable) {
                startAnimator(mNewLostLayout);
            } else {
                startAnimator(mNewInstructLayout);
            }
        } else if (layout == mNewInstructLayout) {
            startAnimator(mNewLostLayout);
        } else if (layout == mNewLostLayout) {
            startAnimator(mNewVidLayout);
        } else if (layout == mNewVidLayout) {
            startAnimator(mNewPicLayout);
        } else if (layout == mNewPicLayout) {
            startAnimator(mNewAppLayout);
        }
    }


    public void startAnimator(final View layout) {
        layout.post(new Runnable() {
            @Override
            public void run() {
                startRealAnimator(layout);
            }
        });
    }

    private List<View> lists = new ArrayList<View>();
    int i = 0;

    private void startRealAnimator(final View layout) {
        LeoLog.d("testLayout", "item come :" + i);
        if (layout == mNewContactLayout && !mIsNeedContractVisible) {
            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.VISIBLE);
        }
        int layoutHeight;
        if (layout == mNewAppLayout) {
            layoutHeight = shortTypeHeight;
        } else if (layout == mAdLayout) {
            layoutHeight = adTypeHeight;
        } else {
            layoutHeight = normalTypeHeight;
        }

        mMoveHeight = layoutHeight;
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(layout, "translationY", -layoutHeight, layout.getTranslationY());
        objectAnimator.setDuration(300);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(layout, "alpha", 0f, 0.0f, 0.3f, 1f);
        alpha.setDuration(400);
        animatorSet.playTogether(objectAnimator, alpha);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (lists.size() > 0) {
                    makeItemMove(layout);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (layout != mAdLayout) {
                    layout.setBackgroundResource(R.color.white);
                }
                lists.add(layout);
                mScrollLayout.setLayoutTransition(null);

                if (!mIsExit) {
                    if (layout == mNewAppLayout) {
                        mController.startItemScanning();
                    } else {
                        onLayoutAnimEnd(layout);
                    }
                }

                i++;
            }
        });
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (layout != mNewContactLayout) {
                    mScrollLayout.setLayoutTransition(mTransition);
                }
            }
        });

        animatorSet.start();
    }

    private void makeItemMove(View layout) {
        for (int i = 0; i < lists.size(); i++) {
            View oldLayout = lists.get(i);
            oldLayoutAnimation(oldLayout, layout, i, lists.size() - 1);
        }

        int addSome;
        if (mScreenHeight < 800) {
            addSome = 5;
        } else if (mScreenHeight < 1280) {
            addSome = 10;
        } else {
            addSome = 15;
        }

        int backHeight;
        if (layout == mNewAppLayout) {
            backHeight = shortTypeHeight;
        } else if (layout == mAdLayout) {
            backHeight = adTypeHeight;
        } else {
            backHeight = normalTypeHeight;
        }

        mNowHeight = mNowHeight + backHeight /*+ addSome*/;
        if (mNowHeight > mBackViewHeight) {
            ViewGroup.LayoutParams params = mBackView.getLayoutParams();
            params.height = mNowHeight;
            mBackView.setLayoutParams(params);
        }
    }

    private void oldLayoutAnimation(final View oldLayout, final View layout, final int nowItem, final int moveItem) {
        final int layoutHeight;
        if (layout == mNewAppLayout) {
            layoutHeight = shortTypeHeight;
        } else if (layout == mAdLayout) {
            layoutHeight = adTypeHeight;
        } else {
            layoutHeight = normalTypeHeight;
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                oldLayout, "translationY", oldLayout.getTranslationY(), oldLayout.getTranslationY() + layoutHeight);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (nowItem == moveItem) {
                    oldLayout.setY(layout.getY() + layoutHeight);
                }
            }
        });


        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    private int getContentHeight(View v) {
        int theDipHeight = Utilities.getScanContentHeight(v, mActivity);
        if (theDipHeight <= ONE_LINE) {
            return DipPixelUtil.dip2px(mActivity, MAX_HEIGHT_ONE_LINE);
        } else if (theDipHeight <= TWO_LINE) {
            return DipPixelUtil.dip2px(mActivity, MAX_HEIGHT_TWO_LINE);
        } else if (theDipHeight <= THREE_LINE) {
            return DipPixelUtil.dip2px(mActivity, MAX_HEIGHT_THREE_LINE);
        } else if (theDipHeight <= FOUR_LINE) {
            return DipPixelUtil.dip2px(mActivity, MAX_HEIGHT_FOUR_LINE);
        } else {
            return DipPixelUtil.dip2px(mActivity, MAX_HEIGHT_OTHERS_LINE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMoveHeight == 0) {
            mActivity.onExitScanning();
        }
    }

    public List<AppItemInfo> getSwitchList(WifiLockSwitch wifiSwitch, BlueToothLockSwitch blueToothLockSwitch) {
        List<AppItemInfo> switchList = new ArrayList<AppItemInfo>();
        LockManager mLockMgr = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);

        boolean isWifiLock = wifiSwitch.isLockNow(mLockMgr.getCurLockMode());
//        if (!isWifiLock) {
        AppItemInfo wifiInfo = new AppItemInfo();
        wifiInfo.label = AppMasterApplication.getInstance().getResources().getString(R.string.app_lock_list_switch_wifi);
        wifiInfo.packageName = SwitchGroup.WIFI_SWITCH;
        wifiInfo.icon = AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.lock_wifi);
        wifiInfo.isLocked = false;
        wifiInfo.topPos = wifiSwitch.getLockNum();
//        switchList.add(wifiInfo);
//        }

        boolean isBlueToothLock = blueToothLockSwitch.isLockNow(mLockMgr.getCurLockMode());
//        if (!isBlueToothLock) {
        AppItemInfo bluetoothInfo = new AppItemInfo();
        bluetoothInfo.label = AppMasterApplication.getInstance().getString(R.string.app_lock_list_switch_bluetooth);
        bluetoothInfo.packageName = SwitchGroup.BLUE_TOOTH_SWITCH;
        bluetoothInfo.icon = AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.lock_bluetooth);
        bluetoothInfo.isLocked = false;
        bluetoothInfo.topPos = blueToothLockSwitch.getLockNum();
//        switchList.add(bluetoothInfo);
//        }


        if (!isWifiLock) {
            if (!isBlueToothLock) {
                if (wifiInfo.topPos >= bluetoothInfo.topPos) {
                    switchList.add(wifiInfo);
                    switchList.add(bluetoothInfo);
                } else {
                    switchList.add(bluetoothInfo);
                    switchList.add(wifiInfo);
                }
            } else {
                switchList.add(wifiInfo);
            }
        } else {
            if (!isBlueToothLock) {
                switchList.add(bluetoothInfo);
            }
        }

        return switchList;
    }

}
