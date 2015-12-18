package com.leo.appmaster.wifiSecurity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.FiveStarsLayout;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageScaleType;
import com.mobvista.sdk.m.core.entity.Campaign;

import java.lang.ref.WeakReference;

/**
 * Created by qili on 15-10-27.
 */
public class WifiResultFrangment extends Fragment implements View.OnClickListener {
    private WifiSecurityActivity mActivity;
    private View mRootView, mSafeView, mUnsafeView, mBottomView;
    private View mUnsafeViewBottom;
    private RippleView mTrafficBtn, mOtherWifiBtn;
    protected LockManager mLockManager;
    private boolean  mOneLoadState, mTwoLoadState, mThridLoadState, mFourLoadState;
    private ImageView mOneImg, mTwoImg, mThreeImg, mFourImg;
    private ImageLoader mImageLoader;
    private PreferenceTable mPt; 
    /**
     * Swifty
     */
    private ViewStub mSwiftyViewStub;
    private TextView mSwiftyTitle;
    private ImageView mSwiftyImg;
    private TextView mSwiftyContent;
    private RippleView mSwiftyBtnLt;

    /**
     * WifiMaster
     */
    private ViewStub mWifiMasterViewStub;
    private TextView mWifiMasterTitle;
    private ImageView mWifiMasterImg;
    private TextView mWifiMasterContent;
    private RippleView mWifiMasterBtnLt;

    /**
     * Fb
     */
    private ViewStub mFbViewStub;
    private TextView mFbTitle;
    private ImageView mFbImg;
    private TextView mFbContent;
    private RippleView mFbBtnLt;

    /**
     * 评分
     */
    private ViewStub mGradeViewStub;
    private TextView mGradeTitle;
    private ImageView mGradeImg;
    private TextView mGradeContent;
    private RippleView mGradeBtnLt;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (WifiSecurityActivity) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockManager = mActivity.getLockManager();
        mImageLoader = ImageLoader.getInstance();
        mPt = PreferenceTable.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wifi_result_fragment, container, false);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MobvistaEngine.getInstance(mActivity).release(Constants.UNIT_ID_60);
        ImageLoader.getInstance().clearMemoryCache();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView = view;
        mRootView.setVisibility(View.INVISIBLE);

        mSafeView = mRootView.findViewById(R.id.safe_content);
        mUnsafeView = mRootView.findViewById(R.id.unsafe_result_content);
        mUnsafeViewBottom = mRootView.findViewById(R.id.bottom_content_empty_unsafe);

        mBottomView = mRootView.findViewById(R.id.wifi_below_view_recomment);

        mTrafficBtn = (RippleView) mRootView.findViewById(R.id.wifi_resulte_sure);
        mTrafficBtn.setOnClickListener(this);
        mOtherWifiBtn = (RippleView) mRootView.findViewById(R.id.wifi_resulte_other_wifi);
        mOtherWifiBtn.setOnClickListener(this);

        mOneImg = (ImageView) mUnsafeView.findViewById(R.id.unsafe_wifi_is_connect_icon);
        mTwoImg = (ImageView) mUnsafeView.findViewById(R.id.unsafe_wifi_second_connect_icon);
        mThreeImg = (ImageView) mUnsafeView.findViewById(R.id.unsafe_wifi_ssl_icon);
        mFourImg = (ImageView) mUnsafeView.findViewById(R.id.unsafe_wifi_pas_type_icon);
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                initSwiftyLayout(mRootView);
                initWifiMasterLayout(mRootView);
                initGradeLayout(mRootView);
                initFbLayout(mRootView);
            }
        });
        ThreadManager.executeOnSubThread(new Runnable() {
            @Override
            public void run() {
                loadAd(mRootView);
            }
        });
    }

    @Override
    public void onClick(View view) {
        LockManager lockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        switch (view.getId()) {
            case R.id.wifi_resulte_sure:
                Intent intent = new Intent(mActivity, FlowActivity.class);
                mActivity.startActivity(intent);
                SDKWrapper.addEvent(mActivity,
                        SDKWrapper.P1, "wifi_rst", "wifi_rst_safe_dataflow");
                break;
            case R.id.wifi_resulte_other_wifi:
                mLockManager.filterPackage(mActivity.getPackageName(), 1000);
                Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                try {
                    startActivity(wifiIntent);
                } catch (Exception e) {
                }
                SDKWrapper.addEvent(mActivity,
                        SDKWrapper.P1, "wifi_rst", "wifi_rst_risk_other");
                break;
            case R.id.swifty_resulte_sure:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "wifi_rst", "swifty");
                lockManager.filterSelfOneMinites();
                boolean installISwipe = ISwipUpdateRequestManager.isInstallIsiwpe(mActivity);
                if (installISwipe) {
                    Utilities.startISwipIntent(mActivity);
                } else {
                    Utilities.gotoGpOrBrowser(mActivity, Constants.IS_CLICK_SWIFTY, false);
                }
                break;
            case R.id.wifimaster_resulte_sure:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "wifi_rst", "master");
                lockManager.filterSelfOneMinites();
                boolean installWifiMaster = AppUtil.isInstallPkgName(
                        mActivity, Constants.WIFIMASTER_PKG_NAME);
                if (installWifiMaster) {
                    Utilities.openAPPByPkgName(mActivity, Constants.WIFIMASTER_PKG_NAME);
                } else {
                    Utilities.gotoGpOrBrowser(mActivity, Constants.IS_CLICK_WIFIMASTER, false);
                }
                break;
            case R.id.fb_resulte_sure:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "wifi_rst", "FB");
                lockManager.filterSelfOneMinites();
                Utilities.goFaceBook(mActivity, false);
                break;
            case R.id.grade_resulte_sure:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "wifi_rst", "GP");
                lockManager.filterSelfOneMinites();
                Utilities.goFiveStar(mActivity, true, false);
                break;
        }
    }

    /**
     * 新需求：当广告大图加载完成之后再展示广告
     */
    public static class AdPreviewLoaderListener implements ImageLoadingListener {
        WeakReference<WifiResultFrangment> mFragment;
        Campaign mCampaign;

        public AdPreviewLoaderListener(WifiResultFrangment fragment, final Campaign campaign) {
            mFragment = new WeakReference<WifiResultFrangment>(fragment);
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
            WifiResultFrangment fragment = mFragment.get();
            if (loadedImage != null && fragment != null) {
                LeoLog.d("MobvistaEngine", "onLoadingComplete -> " + imageUri);
                fragment.initAdLayout(fragment.mRootView, mCampaign, Constants.UNIT_ID_60, loadedImage);
                SDKWrapper.addEvent(fragment.mActivity, SDKWrapper.P1, "ad_act", "adv_shws_wifi");
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

    private static AdPreviewLoaderListener sAdImageListener;

    private void loadAd(final View rootView) {
        AppMasterPreference amp = AppMasterPreference.getInstance(mActivity);
        if (amp.getADWifiScan() == 1) {
            MobvistaEngine.getInstance(mActivity).loadMobvista(Constants.UNIT_ID_60, new MobvistaListener() {

                @Override
                public void onMobvistaFinished(int code, final Campaign campaign, String msg) {
                    if (code == MobvistaEngine.ERR_OK) {
                        sAdImageListener = new AdPreviewLoaderListener(WifiResultFrangment.this, campaign);
                        ImageLoader.getInstance().loadImage(campaign.getImageUrl(), sAdImageListener);
                    }
                }

                @Override
                public void onMobvistaClick(Campaign campaign) {
                    LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                    lm.filterSelfOneMinites();

                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_cli", "adv_cnts_wifi");
                }
            });
        }
    }


    private void initAdLayout(View rootView, Campaign campaign, String unitId, Bitmap previewImage) {
        View adView = rootView.findViewById(R.id.ad_content);
        TextView tvTitle = (TextView) adView.findViewById(R.id.item_title);
        tvTitle.setText(campaign.getAppName());
        ImageView preview = (ImageView) adView.findViewById(R.id.item_ad_preview);
        TextView summary = (TextView) adView.findViewById(R.id.item_summary);
        summary.setText(campaign.getAppDesc());
        Button btnCTA = (Button) adView.findViewById(R.id.ad_result_cta);
        btnCTA.setText(campaign.getAdCall());
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setImageBitmap(previewImage);
        adView.setVisibility(View.VISIBLE);
        MobvistaEngine.getInstance(mActivity).registerView(Constants.UNIT_ID_60, adView);
    }

    public void showTab(boolean isWifiSafe, boolean isWifiConnect) {
        down2Up(isWifiSafe, isWifiConnect);
    }

    public void showTab(boolean isWifiSafe, boolean isWifiConnect, boolean mPingOk,
                        boolean mOneState, boolean mTwoState,
                        boolean mThridState, boolean mFourState) {
        down2Up(isWifiSafe, isWifiConnect);
        mOneLoadState = mOneState;
        mTwoLoadState = mTwoState;
        mThridLoadState = mThridState;
        mFourLoadState = mFourState;
    }

    public void down2Up(final boolean isWifiSafe, boolean isWifiConnect) {
        if (mRootView.getVisibility() == View.VISIBLE) return;
        mRootView.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_down_to_up);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (isWifiSafe) {
                    mSafeView.setVisibility(View.VISIBLE);
                } else {
                    mUnsafeView.setVisibility(View.VISIBLE);
                    mUnsafeViewBottom.setVisibility(View.VISIBLE);
                    mBottomView.setVisibility(View.VISIBLE);
                    setUnsafePageIcon();
                }

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRootView.startAnimation(animation);
    }

    private void setUnsafePageIcon() {
        setImgIcon(mOneImg, mOneLoadState);
        setImgIcon(mTwoImg, mTwoLoadState);
        setImgIcon(mThreeImg, mThridLoadState);
        setImgIcon(mFourImg, mFourLoadState);
    }

    private void setImgIcon(ImageView view, boolean mFlag) {
        if (mFlag) {
            view.setImageResource(R.drawable.wifi_complete);
        } else {
            view.setImageResource(R.drawable.wifi_error);
        }
    }

    private void initSwiftyLayout(View view) {
        mSwiftyViewStub = (ViewStub) view.findViewById(R.id.swifty_wifi_stub);
        if(mSwiftyViewStub == null) {
            return;
        }

        boolean isContentEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_SWIFTY_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_SWIFTY_IMG_URL));

        boolean isTypeEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_SWIFTY_TYPE));

        boolean isGpUrlEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_SWIFTY_GP_URL));

        boolean isBrowserUrlEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_SWIFTY_URL));

        boolean isUrlEmpty = isGpUrlEmpty && isBrowserUrlEmpty; //判断两个地址是否都为空

        if (!isContentEmpty && !isImgUrlEmpty && !isTypeEmpty && !isUrlEmpty) {
            if (mActivity == null) {
                return;
            }
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long ttt = System.currentTimeMillis();
                    View include = mSwiftyViewStub.inflate();
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "wifi_rst", "swifty_shw");
                    mSwiftyTitle = (TextView) include.findViewById(R.id.swifty_text);
                    mSwiftyImg = (ImageView) include.findViewById(R.id.swifty_img);
                    mSwiftyContent = (TextView) include.findViewById(R.id.swifty_txt);
                    mSwiftyBtnLt = (RippleView) include.findViewById(R.id.swifty_resulte_sure);
                    mSwiftyBtnLt.setOnClickListener(WifiResultFrangment.this);
                    mSwiftyContent.setText(mPt.getString(PrefConst.KEY_WIFI_SWIFTY_CONTENT));
                    String imgUrl = mPt.getString(PrefConst.KEY_WIFI_SWIFTY_IMG_URL);
                    mImageLoader.displayImage(imgUrl, mSwiftyImg, getOptions(R.drawable.swifty_banner));
                    boolean isTitleEmpty = TextUtils.isEmpty(
                            mPt.getString(PrefConst.KEY_WIFI_SWIFTY_TITLE));
                    if (!isTitleEmpty) {
                        mSwiftyTitle.setText(mPt.getString(
                                PrefConst.KEY_WIFI_SWIFTY_TITLE));
                    }
                    LeoLog.i("testwifi", "swifty init = " + (System.currentTimeMillis() - ttt));
                }
            });
        }
        
        
//        if (!isContentEmpty && !isImgUrlEmpty && !isTypeEmpty && !isUrlEmpty) {
//            View include = viewStub.inflate();
//            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "wifi_rst", "swifty_shw");
//            mSwiftyTitle = (TextView) include.findViewById(R.id.swifty_text);
//            mSwiftyImg = (ImageView) include.findViewById(R.id.swifty_img);
//            mSwiftyContent = (TextView) include.findViewById(R.id.swifty_txt);
//            mSwiftyBtnLt = (RippleView) include.findViewById(R.id.swifty_resulte_sure);
//            mSwiftyBtnLt.setOnClickListener(this);
//            mSwiftyContent.setText(preferenceTable.getString(PrefConst.KEY_WIFI_SWIFTY_CONTENT));
//            String imgUrl = preferenceTable.getString(PrefConst.KEY_WIFI_SWIFTY_IMG_URL);
//            mImageLoader.displayImage(imgUrl, mSwiftyImg, getOptions(R.drawable.swifty_banner));
//            boolean isTitleEmpty = TextUtils.isEmpty(
//                    preferenceTable.getString(PrefConst.KEY_WIFI_SWIFTY_TITLE));
//            if (!isTitleEmpty) {
//                mSwiftyTitle.setText(preferenceTable.getString(
//                        PrefConst.KEY_WIFI_SWIFTY_TITLE));
//            }
//
//        }
    }

    private void initWifiMasterLayout(View view) {
        mWifiMasterViewStub = (ViewStub) view.findViewById(R.id.wifimaster_wifi_stub);
        if(mWifiMasterViewStub == null) {
            return;
        }
        boolean isContentEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_WIFIMASTER_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_WIFIMASTER_IMG_URL));

        boolean isTypeEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_WIFIMASTER_TYPE));

        boolean isGpUrlEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_WIFIMASTER_GP_URL));

        boolean isBrowserUrlEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_WIFIMASTER_URL));

        boolean isUrlEmpty = isGpUrlEmpty && isBrowserUrlEmpty; //判断两个地址是否都为空

        if (!isContentEmpty && !isImgUrlEmpty && !isTypeEmpty && !isUrlEmpty) {
            if (mActivity == null) {
                return;
            }
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long ttt = System.currentTimeMillis();
                    View include = mWifiMasterViewStub.inflate();
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "wifi_rst", "master_shw");
                    mWifiMasterTitle = (TextView) include.findViewById(R.id.wifimaster_text);
                    mWifiMasterImg = (ImageView) include.findViewById(R.id.wifimaster_img);
                    mWifiMasterContent = (TextView) include.findViewById(R.id.wifimaster_txt);
                    mWifiMasterBtnLt = (RippleView) include.findViewById(R.id.wifimaster_resulte_sure);
                    mWifiMasterBtnLt.setOnClickListener(WifiResultFrangment.this);
                    mWifiMasterContent.setText(mPt.getString(PrefConst.KEY_WIFI_WIFIMASTER_CONTENT));
                    String imgUrl = mPt.getString(PrefConst.KEY_WIFI_WIFIMASTER_IMG_URL);
                    mImageLoader.displayImage(imgUrl, mWifiMasterImg, getOptions(R.drawable.swifty_banner));
                    boolean isTitleEmpty = TextUtils.isEmpty(
                            mPt.getString(PrefConst.KEY_WIFI_WIFIMASTER_TITLE));
                    if (!isTitleEmpty) {
                        mWifiMasterTitle.setText(mPt.getString(
                                PrefConst.KEY_WIFI_WIFIMASTER_TITLE));
                    }
                    LeoLog.i("testwifi", "wifimaster init = " + (System.currentTimeMillis() - ttt));
                }
            });
        }
    }


    private void initFbLayout(View view) {
        mFbViewStub = (ViewStub) view.findViewById(R.id.fb_wifi_stub);
        if(mFbViewStub == null) {
            return;
        }

        boolean isContentEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_FB_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_FB_IMG_URL));

        boolean isURLEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_FB_URL));

        if (!isContentEmpty && !isImgUrlEmpty && !isURLEmpty) {
            if (mActivity == null) {
                return;
            }
            mActivity.runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    long ttt = System.currentTimeMillis();
                    View include = mFbViewStub.inflate();
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "wifi_rst", "FB_shw");
                    mFbTitle = (TextView) include.findViewById(R.id.fb_text);
                    mFbImg = (ImageView) include.findViewById(R.id.fb_img);
                    mFbContent = (TextView) include.findViewById(R.id.fb_txt);
                    mFbBtnLt = (RippleView) include.findViewById(R.id.fb_resulte_sure);
                    mFbBtnLt.setOnClickListener(WifiResultFrangment.this);
                    mFbContent.setText(mPt.getString(PrefConst.KEY_WIFI_FB_CONTENT));
                    String imgUrl = mPt.getString(PrefConst.KEY_WIFI_FB_IMG_URL);
                    mImageLoader.displayImage(imgUrl, mFbImg, getOptions(R.drawable.fb_banner));
                    boolean isTitleEmpty = TextUtils.isEmpty(
                            mPt.getString(PrefConst.KEY_WIFI_FB_TITLE));
                    if (!isTitleEmpty) {
                        mFbTitle.setText(mPt.getString(
                                PrefConst.KEY_WIFI_FB_TITLE));
                    }
                    LeoLog.i("testwifi", "fb init = " + (System.currentTimeMillis() - ttt));
                }
            });
        }
    }

    private void initGradeLayout(View view) {
         mGradeViewStub = (ViewStub) view.findViewById(R.id.grade_wifi_stub);
        if(mGradeViewStub == null) {
            return;
        }

        boolean isContentEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_GRADE_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_GRADE_IMG_URL));

        boolean isURLEmpty = TextUtils.isEmpty(
                mPt.getString(PrefConst.KEY_WIFI_GRADE_URL));

        if (!isContentEmpty && !isImgUrlEmpty && !isURLEmpty) {
            if (mActivity == null) {
                return;
            }
            mActivity.runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    long ttt = System.currentTimeMillis();
                    View include = mGradeViewStub.inflate();
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "wifi_rst", "GP_shw");
                    FiveStarsLayout fiveStarsLayout = (FiveStarsLayout)
                                    include.findViewById(R.id.fsl_fivestars);
                    fiveStarsLayout.setBackgroundNull();
                    mGradeTitle = (TextView) include.findViewById(R.id.grade_text);
                    mGradeImg = (ImageView) include.findViewById(R.id.grade_img);
                    mGradeContent = (TextView) include.findViewById(R.id.grade_txt);
                    mGradeBtnLt = (RippleView) include.findViewById(R.id.grade_resulte_sure);
                    mGradeBtnLt.setOnClickListener(WifiResultFrangment.this);
                    mGradeContent.setText(mPt.getString(PrefConst.KEY_WIFI_GRADE_CONTENT));
                    String imgUrl = mPt.getString(PrefConst.KEY_WIFI_GRADE_IMG_URL);
                    mImageLoader.displayImage(imgUrl, mGradeImg, getOptions(R.drawable.grade_bg));
                    boolean isTitleEmpty = TextUtils.isEmpty(
                            mPt.getString(PrefConst.KEY_WIFI_GRADE_TITLE));
                    if (!isTitleEmpty) {
                        mGradeTitle.setText(mPt.getString(
                                PrefConst.KEY_WIFI_GRADE_TITLE));
                    }
                    LeoLog.i("testwifi", "grade init = " + (System.currentTimeMillis() - ttt));
                }
            });

        }
    }

    public DisplayImageOptions getOptions(int drawble) {  //需要提供默认图
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(drawble)
                .showImageForEmptyUri(drawble)
                .showImageOnFail(drawble)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();

        return options;
    }

}
