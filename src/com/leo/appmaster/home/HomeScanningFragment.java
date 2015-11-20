package com.leo.appmaster.home;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.LostSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyContactManager;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.ScanningImageView;
import com.leo.appmaster.ui.ScanningTextView;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.videohide.VideoItemBean;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;

/**
 * Created by Jasper on 2015/10/18.
 */
public class HomeScanningFragment extends Fragment implements
        Animator.AnimatorListener, View.OnClickListener {
    private static final String TAG = "HomeScanningFragment";
    private static final byte[] LOCK = new byte[1];

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

    private Animator mAppAnimator;
    private Animator mPhotoAnimator;
    private Animator mVideoAnimator;
    private Animator mPrivacyAnimator;

    private HomeActivity mActivity;

    private List<AppItemInfo> mAppList;
    private List<PhotoItem> mPhotoList;
    private List<VideoItemBean> mVideoList;

    private boolean mPhotoScanFinish;
    private boolean mVideoScanFinish;

    private boolean mScanning;
    private int mScanningDuration;

    private PrivacyHelper mPrivacyHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (HomeActivity) activity;
        mPrivacyHelper = PrivacyHelper.getInstance(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mScannTitleTv = (TextView) view.findViewById(R.id.scan_title_tv);
        mProgressTv = (TextView) view.findViewById(R.id.scan_progress_tv);

        mCancelBtn = view.findViewById(R.id.scan_cancel_rv);
        MaterialRippleLayout.on(mCancelBtn)
                .rippleColor(getResources().getColor(R.color.white_list_item_ripple))
                .rippleAlpha(0.5f)
                .rippleDuration(250)
                .rippleHover(true)
                .rippleOverlay(true)
                .create();
        mCancelTv = (TextView) view.findViewById(R.id.scan_cancel_tv);

        mProcessBtn = view.findViewById(R.id.scan_process_rv);
        MaterialRippleLayout.on(mProcessBtn)
                .rippleColor(getResources().getColor(R.color.white_list_item_ripple))
                .rippleAlpha(0.5f)
                .rippleDuration(250)
                .rippleHover(true)
                .rippleOverlay(true)
                .create();
        mProcessTv = (TextView) view.findViewById(R.id.scan_process_tv);

        mCancelBtn.setOnClickListener(this);
        mProcessBtn.setOnClickListener(this);

        mNewAppIv = (ScanningImageView) view.findViewById(R.id.scan_new_app_iv);
        mNewPhotoIv = (ScanningImageView) view.findViewById(R.id.scan_media_iv);
        mNewVideoIv = (ScanningImageView) view.findViewById(R.id.scan_mobile_iv);
        mNewPrivacyIv = (ScanningImageView) view.findViewById(R.id.scan_privacy_iv);

        mNewAppText = (ScanningTextView) view.findViewById(R.id.scan_new_app_tv);
        mNewPhotoText = (ScanningTextView) view.findViewById(R.id.scan_media_tv);
        mNewVideoText = (ScanningTextView) view.findViewById(R.id.scan_mobile_tv);
        mNewPrivacyText = (ScanningTextView) view.findViewById(R.id.scan_privacy_tv);

        mAppCountTv = (TextView) view.findViewById(R.id.scan_app_count_tv);
        mPicCountTv = (TextView) view.findViewById(R.id.scan_media_count_tv);
        mVidCountTv = (TextView) view.findViewById(R.id.scan_mobile_count_iv);
        mPrivacyCountIv = (ImageView) view.findViewById(R.id.scan_privacy_count_iv);

        mAppCountIv = (ImageView) view.findViewById(R.id.scan_app_count_iv);
        mPicCountIv = (ImageView) view.findViewById(R.id.scan_pic_count_iv);
        mVidCountIv = (ImageView) view.findViewById(R.id.scan_vid_count_iv);

        startScan();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_scanning, container, false);
    }

//    @Override
//    public void onRippleComplete(RippleView rippleView) {
//        switch (rippleView.getId()) {
//            case R.id.scan_cancel_rv:
//                mScanning = false;
//                mActivity.onExitScanning();
//                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "cancel");
//                break;
//            case R.id.scan_process_rv:
//                mActivity.startProcess();
//                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "instant");
//                break;
//        }
//    }


    public void startScan() {
        if (mScanning) return;

        startScanAnim();
        mProgressTv.setText(R.string.pri_pro_scanning);
        mCancelBtn.setVisibility(View.VISIBLE);
        mProcessBtn.setVisibility(View.GONE);
        LeoLog.i(TAG, "start to scaning.");
        mScanning = true;
        int score = mPrivacyHelper.getSecurityScore();
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "prilevel", "prilevel_" + score);
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                long start = SystemClock.elapsedRealtime();
                mAppList = lm.getNewAppList();
                int appScore = lm.getSecurityScore(mAppList);
                mPrivacyHelper.onSecurityChange(MgrContext.MGR_APPLOCKER, appScore);
                LeoLog.i(TAG, "appList, cost: " + (SystemClock.elapsedRealtime() - start));
            }
        });
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                long start = SystemClock.elapsedRealtime();
                PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                mPhotoList = pdm.getAddPic();
                mPhotoScanFinish = true;
                int picScore = pdm.getPicScore(mPhotoList == null ? 0 : mPhotoList.size());
                LeoLog.i(TAG, "photoItems, cost: " + (SystemClock.elapsedRealtime() - start));
                if (!mAppAnimator.isRunning() && !mPhotoAnimator.isRunning()) {
                    ThreadManager.getUiThreadHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            updatePhotoList();
                        }
                    });
                }

                start = SystemClock.elapsedRealtime();
                mVideoList = pdm.getAddVid();
                mVideoScanFinish = true;
                int vidScore = pdm.getVidScore(mVideoList == null ? 0 : mVideoList.size());
                LeoLog.i(TAG, "videoItemBeans, cost: " + (SystemClock.elapsedRealtime() - start));
                if (!mAppAnimator.isRunning() && !mPhotoAnimator.isRunning() && !mVideoAnimator.isRunning()) {
                    ThreadManager.getUiThreadHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            updateVideoList();
                            if (!mPrivacyAnimator.isRunning()) {
                                onScannigFinish(mAppList, mPhotoList, mVideoList);
                            }
                        }
                    });
                }

                int dataScore = picScore + vidScore;
                mPrivacyHelper.onSecurityChange(MgrContext.MGR_PRIVACY_DATA, dataScore);

                if ((mAppList == null || mAppList.isEmpty())
                        && (mPhotoList == null || mPhotoList.isEmpty())
                        && (mVideoList == null || mVideoList.isEmpty())) {
                    start = SystemClock.elapsedRealtime();
                    PrivacyContactManager pcm = (PrivacyContactManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                    List<ContactBean> contactBeans = pcm.getFrequentContacts();
                    mActivity.setContactList(contactBeans);
                    LeoLog.i(TAG, "contactBeans, cost: " + (SystemClock.elapsedRealtime() - start));
                } else {
                    loadContacts();
                }
            }
        });
    }

    private void startScanAnim() {
        mAppAnimator = getAnimtion(mNewAppIv, mNewAppText, 3);
        mPhotoAnimator = getAnimtion(mNewPhotoIv, mNewPhotoText, 3);
        mVideoAnimator = getAnimtion(mNewVideoIv, mNewVideoText, 1);
        mPrivacyAnimator = getAnimtion(mNewPrivacyIv, mNewPrivacyText, 1);

        AnimatorSet allAnim = new AnimatorSet();
        allAnim.playSequentially(mAppAnimator, mPhotoAnimator, mVideoAnimator, mPrivacyAnimator);
        mScanningDuration = (int) allAnim.getDuration();

        allAnim.start();
    }

    private AnimatorSet getAnimtion(ScanningImageView scanningImageView,
                                    ScanningTextView scanningTextView,
                                    int repeatCount) {
        AnimatorSet appAnimSet = new AnimatorSet();
        // 外环放大
        ObjectAnimator scaleImgAnim = ObjectAnimator.ofFloat(scanningImageView, "scaleRatio", 0f, 1f);
        scaleImgAnim.setInterpolator(new LinearInterpolator());
        scaleImgAnim.setDuration(300);

        ObjectAnimator scaleTextAnim = ObjectAnimator.ofFloat(scanningTextView, "scaleRatio", 0f, 1f);
        scaleTextAnim.setInterpolator(new LinearInterpolator());
        scaleTextAnim.setDuration(300);

        AnimatorSet scaleAnim = new AnimatorSet();
        scaleAnim.playTogether(scaleImgAnim, scaleTextAnim);

        float maxRotateDegree = 360 * (repeatCount + 1);
        scanningImageView.setMaxRotate(maxRotateDegree);

        int duration = 400 * (repeatCount + 1);
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(scanningImageView, "rotateDegree", 1f, maxRotateDegree);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.setDuration(duration);

        ObjectAnimator innerScaleAnim = ObjectAnimator.ofFloat(scanningImageView, "innerDrawableScale",
                ScanningImageView.INNER_SCALE, 1f);
        innerScaleAnim.setInterpolator(new LinearInterpolator());
        innerScaleAnim.setDuration(300);
        appAnimSet.playSequentially(scaleAnim, rotateAnim, innerScaleAnim);

        appAnimSet.addListener(this);
        return appAnimSet;
    }

    private void loadContacts() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                long start = SystemClock.elapsedRealtime();
                PrivacyContactManager pcm = (PrivacyContactManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                List<ContactBean> contactBeans = pcm.getFrequentContacts();
                mActivity.setContactList(contactBeans);
                LeoLog.i(TAG, "contactBeans, cost: " + (SystemClock.elapsedRealtime() - start));
            }
        });
    }

    private void onScannigFinish(final List<AppItemInfo> appList, final List<PhotoItem> photoItems,
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
                mActivity.onScanningFinish(appList, photoItems, videoItemBeans);
            }
        });
    }

    @Override
    public void onAnimationStart(Animator animation) {
        if (animation == mAppAnimator) {
            mActivity.onScanningStart(7200);
        }
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (isDetached() || isRemoving() || getActivity() == null) return;
        Context context = AppMasterApplication.getInstance();
        if (animation == mAppAnimator) {
            updateAppList();
            int count = mAppList == null ? 0 : mAppList.size();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 1));
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "app_cnts_" + count);
        } else if (animation == mPhotoAnimator) {
            updatePhotoList();
            int count = mPhotoList == null ? 0 : mPhotoList.size();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 2));
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "pic_cnts_" + count);
        } else if (animation == mVideoAnimator) {
            updateVideoList();
            int count = mVideoList == null ? 0 : mVideoList.size();
            mProgressTv.setText(context.getString(R.string.scanning_pattern, 3));
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "vid_cnts_" + count);
        } else if (animation == mPrivacyAnimator) {
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
        int count = mPhotoList == null ? 0 : mPhotoList.size();
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
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    public boolean isScanning() {
        return mScanning;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan_cancel_rv:
                mScanning = false;
                mActivity.onExitScanning();
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "cancel");
                break;
            case R.id.scan_process_rv:
                mActivity.startProcess();
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "scan", "instant");
                break;
        }
    }
}
