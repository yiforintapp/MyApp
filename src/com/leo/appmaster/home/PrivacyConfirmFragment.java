package com.leo.appmaster.home;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.LostSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyContactManager;
import com.leo.appmaster.mgr.WifiSecurityManager;
import com.leo.appmaster.mgr.impl.CallFilterManagerImpl;
import com.leo.appmaster.phoneSecurity.PhoneSecurityGuideActivity;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.MessageCallLogBean;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.ResizableImageView;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmaster.videohide.VideoHideMainActivity;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageScaleType;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.mobvista.sdk.m.core.entity.Campaign;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jasper on 2015/10/18.
 */
public class PrivacyConfirmFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "PrivacyConfirmFragment";

    private static final String KEY_SHOW_CONTACT = "SHOW_CONTACT";
    private View mPanelView;

    private View mRootView;
    private TextView mHeadView;

    private TextView mLostSummary;
    private TextView mLostBtnTv;
    private RippleView mLostBtnLt;
    private View mLostBtnDiv;
    private View mLostMiddleLt;
    private View mLostFixedLt;
    private TextView mLostFixedTv;

    private TextView mIntruderSummary;
    private TextView mIntruderBtnTv;
    private View mIntruderBtnDiv;
    private RippleView mIntruderBtnLt;
    private View mIntruderMiddleLt;
    private View mIntruderFixedLt;
    private TextView mIntruderFixedTv;

    private TextView mWifiSummary;
    private TextView mWifiSubSummary;
    private TextView mWifiBtnTv;
    private View mWifiBtnDiv;
    private RippleView mWifiBtnLt;
    private View mWifiFixedLt;
    private View mWifiMiddleLt;
    private TextView mWifiFixedTitle;
    private View mWifiFixedSummaryLt;

    private TextView mContactBtnTv;
    private RippleView mContactBtnLt;
    private View mContactBtnDiv;
    private LinearLayout mContactContainor;
    private ImageView mContactArrowIv;

    /**
     * 评分星星
     */

    private ImageView mHighOneStar;
    private ImageView mHighTwoStar;
    private ImageView mHighThreeStar;
    private ImageView mHighFourStar;
    private ImageView mHighFiveStar;
    private ImageView mHighGradeGesture;
    private RippleView mHighGradeBtnLt;
    private ImageView mHighFiveEmptyStar;
    private TextView mHighGradeTitle;
    private ImageView mHighGradeImg;
    private TextView mHighGradeContent;
    private RelativeLayout mHighGradeLayout;


    private ImageView mOneStar;
    private ImageView mTwoStar;
    private ImageView mThreeStar;
    private ImageView mFourStar;
    private ImageView mFiveStar;
    private ImageView mGradeGesture;
    private RippleView mGradeBtnLt;
    private ImageView mFiveEmptyStar;
    private TextView mGradeTitle;
    private ImageView mGradeImg;
    private TextView mGradeContent;
    private RelativeLayout mGradeLayout;

    private AnimatorSet mAnimatorSet;

    /**
     * 前往FaceBook
     */
    private TextView mFbTitle;
    private ImageView mFbImg;
    private TextView mFbContent;
    private RippleView mFbBtnLt;

    /**
     * Swifty
     */
    private TextView mSwiftyTitle;
    private ImageView mSwiftyImg;
    private TextView mSwiftyContent;
    private RippleView mSwiftyBtnLt;

    /**
     * WifiMaster
     */
    private TextView mWifiMasteTitle;
    private ImageView mWifiMasteImg;
    private TextView mWifiMasteContent;
    private RippleView mWifiMasteBtnLt;

    private CheckBox mSelectAllCb;
    private List<View> mContactViews;
    private List<ContactBean> mSelectData;
    private List<ContactBean> mAddedData;
    private HashMap<CheckBox, ContactBean> mDataMap;

    private View mIgnoreBtn;
    private MaterialRippleLayout mProcessBtn;
    private View mProcessClick;
    private TextView mProcessTv;

    private List<ContactBean> mContactList;

    private ImageLoader mImageLoader;

    private HomeActivity mActivity;

    private LEOAlarmDialog mImportRecordDlg;
    private MessageCallLogBean mMessageCallBean;

    private boolean mShowContact;
    private TextView mImpTv;
    private View mContactContainorDisable;
    private View mLostInclude;
    private View mResultInclude;
    private View mIntruderInclude;
    private View mWIfiInclude;
    private View mContactInclude;
    private boolean mIsNeedContractVisible = true;
    private View mBoxOne;
    private View mBoxTwo;
    private View mBoxThree;

    private RelativeLayout mBottomLayout;
    private ScrollView mScrollView;
    private LeoPreference mPt;

    // 初始化时的占位View，避免一开始显示空白页面
//    private View mDisplayProxyView;

    public static PrivacyConfirmFragment newInstance(boolean showContact) {
        PrivacyConfirmFragment fragment = new PrivacyConfirmFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_SHOW_CONTACT, showContact);
        fragment.setArguments(args);
        return fragment;
    }

    public void setDataList(List<ContactBean> contactList) {
        if (contactList == null) return;

        mContactList = new ArrayList<ContactBean>(contactList.size());
        mContactList.addAll(contactList);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (HomeActivity) activity;
        if (AppMasterPreference.getInstance(mActivity).getIsNeedCutBackupUninstallAndPrivacyContact()) {
            mIsNeedContractVisible = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "proposals_cnts");
        mImageLoader = ImageLoader.getInstance();
        mSelectData = new ArrayList<ContactBean>();
        mAddedData = new ArrayList<ContactBean>();
        mDataMap = new HashMap<CheckBox, ContactBean>();
        mPt = LeoPreference.getInstance();

        Bundle args = getArguments();
        if (args != null) {
            mShowContact = args.getBoolean(KEY_SHOW_CONTACT);
        }

        int score = PrivacyHelper.getInstance(mActivity).getSecurityScore();
        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "points", "points_sug_" + score);

        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "result");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView = view;

        mHeadView = (TextView) view.findViewById(R.id.pri_con_header);
//        mDisplayProxyView = view.findViewById(R.id.list_parent_layout_proxy);
        mActivity.resetToolbarColor();

        if (PrivacyHelper.getInstance(mActivity).getSecurityScore() == 100) {
            mHeadView.setText(R.string.pri_pro_summary_confirm);
        } else {
            mHeadView.setText(R.string.pri_pro_summary_not_confirm);
        }

        updateBottomPanel();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDidLoadAd) {
            MobvistaEngine.getInstance(mActivity).release(Constants.UNIT_ID_67);
        }
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet = null;
        }

        HomeActivity mMianActivity = mActivity;
        mMianActivity.clearAllNum();

        mMianActivity.setHidePicFinish(true);

    }

    /**
     * 新需求：当广告大图加载完成之后再展示广告
     */
    public static class AdPreviewLoaderListener implements ImageLoadingListener {
        WeakReference<PrivacyConfirmFragment> mFragment;
        Campaign mCampaign;

        public AdPreviewLoaderListener(PrivacyConfirmFragment fragment, final Campaign campaign) {
            mFragment = new WeakReference<PrivacyConfirmFragment>(fragment);
            mCampaign = campaign;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

        }

        @Override
        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
            final PrivacyConfirmFragment fragment = mFragment.get();
            LeoLog.d("MobvistaEngine", "[PrivacyConfirmFragment] onLoadingComplete -> "
                    + imageUri + ";  fragment=" + fragment);
            if (loadedImage != null && fragment != null) {
                ThreadManager.executeOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.initAdLayout(mCampaign, fragment.mPanelView, Constants.UNIT_ID_67, loadedImage);
                    }
                });
                SDKWrapper.addEvent(fragment.mActivity, SDKWrapper.P1, "ad_act", "adv_shws_scanRST");
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

    private static AdPreviewLoaderListener sAdImageListener;
    private boolean mDidLoadAd = false;
    private void loadAd(final View view) {
        LeoLog.d(TAG, "loadAD with thread " + Thread.currentThread().getName());
        AppMasterPreference amp = AppMasterPreference.getInstance(mActivity);
        if (amp.getIsADAfterPrivacyProtectionOpen() == 1) {
            mDidLoadAd = true;
            ThreadManager.executeOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MobvistaEngine.getInstance(mActivity).loadMobvista(
                            Constants.UNIT_ID_67, new MobvistaListener() {

                                @Override
                                public void onMobvistaFinished(int code, final Campaign campaign, String msg) {
                                    if (code == MobvistaEngine.ERR_OK) {
                                        sAdImageListener = new AdPreviewLoaderListener(
                                                PrivacyConfirmFragment.this, campaign);
                                        ImageLoader.getInstance().loadImage(campaign.getImageUrl(), sAdImageListener);
                                    }
                                }

                                @Override
                                public void onMobvistaClick(Campaign campaign, String unitID) {
                                    LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                                    lm.filterSelfOneMinites();

                                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_cli", "adv_cnts_scanRST");
                                }
                            });
                }
            });

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LeoLog.d(TAG, "onResume...");

        try {
            if (mPanelView == null) {
                mPanelView = mRootView.findViewById(R.id.list_parent_layout);
                // AM-3143: add animation for advertise cell
                LeoLog.i("tempp", android.os.Build.MODEL);
                if (!("ZTE U817".equals(android.os.Build.MODEL))) {
                    ((ViewGroup) mPanelView).setLayoutTransition(new LayoutTransition());
                }

                initWifiLayout(mPanelView);
                initLostLayout(mPanelView);
                initScanResultLayout(mPanelView);
                initIntruderLayout(mPanelView);
                updateIntruderAndLost();

                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        loadAd(mPanelView);
                    }
                });
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initContactLayout(mPanelView);
                        initGradeLayout(mPanelView);
                        initFbLayout(mPanelView);
                        initSwiftyLayout(mPanelView);
                        initWifiMasterLayout(mPanelView);

                        updateStubPanelVisibility();
                    }
                }, 700);
            } else {
                updateIntruderAndLost();
                updateStubPanelVisibility();
            }
        } catch (Exception e) {
        }

    }


    private void updateBottomPanel() {
//        ViewStub viewStub = (ViewStub) mRootView.findViewById(R.id.pri_pro_bottom_stub);
//        View view = viewStub.inflate();
        View view = mRootView;

        mProcessBtn = (MaterialRippleLayout) view.findViewById(R.id.pp_process_rv);
        mProcessBtn.setRippleOverlay(true);
        mProcessClick = view.findViewById(R.id.pp_process_rv_click);
        mProcessClick.setOnClickListener(this);
        mIgnoreBtn = view.findViewById(R.id.pp_process_ignore_rv);
        mProcessTv = (TextView) view.findViewById(R.id.pp_process_tv);

        mIgnoreBtn.setVisibility(View.GONE);
        mProcessTv.setText(R.string.pri_pro_complete);
        mProcessBtn.setBackgroundResource(R.drawable.green_radius_btn_shape);

        mBottomLayout = (RelativeLayout) view.findViewById(R.id.bottom_layout);
        mScrollView = (ScrollView) view.findViewById(R.id.pri_confirm_sv);
        LeoPreference leoPreference = LeoPreference.getInstance();
        if (leoPreference.getBoolean(PrefConst.KEY_IS_OLD_USER, true)) {
            mBottomLayout.setVisibility(View.VISIBLE);
        } else {
            mBottomLayout.setVisibility(View.GONE);
            mScrollView.setPadding(0, 0, 0, 0);
        }
    }

    private void updateIntruderAndLost() {
        View panelView = mPanelView;
        IntrudeSecurityManager ism = (IntrudeSecurityManager)
                MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        if (!ism.getIsIntruderSecurityAvailable()) {
            View include = panelView.findViewById(R.id.intruder_security);
            include.setVisibility(View.GONE);
        } else if (ism.getIntruderMode()) {
            mIntruderBtnLt.setVisibility(View.GONE);
            mIntruderBtnDiv.setVisibility(View.GONE);
            mIntruderMiddleLt.setVisibility(View.GONE);
            mIntruderFixedLt.setVisibility(View.VISIBLE);

            mIntruderFixedTv.setText(mActivity.getString(R.string.pri_intruder_fixed_pattern, ism.getCatchTimes()));
        }

        LostSecurityManager lsm = (LostSecurityManager)
                MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        if (lsm.isUsePhoneSecurity()) {
            int[] times = lsm.getPhoneProtectTime();

            mLostBtnLt.setVisibility(View.GONE);
            mLostBtnDiv.setVisibility(View.GONE);
            mLostMiddleLt.setVisibility(View.GONE);
            mLostFixedLt.setVisibility(View.VISIBLE);
            mLostFixedTv.setText(mActivity.getString(R.string.pri_pro_lost_fixed_pattern, times[0], times[1]));
        }
    }

    private void updateStubPanelVisibility() {


        View include = mWIfiInclude;
        WifiSecurityManager wsm = (WifiSecurityManager) MgrContext.getManager(MgrContext.MGR_WIFI_SECURITY);
        boolean isScnnedEver = wsm.getLastScanState();
        if (wsm.getWifiState() == WifiSecurityManager.NO_WIFI) {
            include.setVisibility(View.GONE);
        } else {
            mWifiSummary.setText(mActivity.getString(R.string.pri_pro_wifi_connect, wsm.getWifiName()));
            if (isScnnedEver) {
                if (wsm.getWifiState() == WifiSecurityManager.SAFE_WIFI) {
                    mWifiFixedLt.setVisibility(View.VISIBLE);
                    mWifiFixedTitle.setText(mActivity.getString(R.string.pri_pro_wifi_connect, wsm.getWifiName()));
                    mWifiFixedSummaryLt.setVisibility(View.VISIBLE);
                    mWifiMiddleLt.setVisibility(View.GONE);
                    mWifiBtnLt.setVisibility(View.GONE);
                    mWifiBtnDiv.setVisibility(View.GONE);
                } else if (wsm.getWifiState() == WifiSecurityManager.NOT_SAFE) {
                    mWifiMiddleLt.setVisibility(View.VISIBLE);
                    mWifiFixedLt.setVisibility(View.GONE);
                    mWifiSubSummary.setText(R.string.pri_pro_wifi_warn);
                    mWifiFixedSummaryLt.setVisibility(View.GONE);
                    mWifiBtnLt.setVisibility(View.VISIBLE);
                    mWifiBtnDiv.setVisibility(View.VISIBLE);
                }
            } else {
                mWifiFixedLt.setVisibility(View.GONE);
                mWifiMiddleLt.setVisibility(View.VISIBLE);
                mWifiSubSummary.setText(R.string.pri_pro_wifi_not_scan);
                mWifiFixedSummaryLt.setVisibility(View.GONE);
                mWifiBtnLt.setVisibility(View.VISIBLE);
                mWifiBtnDiv.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initContactLayout(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.contact_security_stub);
//        View include = view.findViewById(R.id.contact_security);
        if (viewStub == null) {
            return;
        }
        mContactInclude = viewStub.inflate();
        if (mContactList == null || mContactList.isEmpty() || !mShowContact || !mIsNeedContractVisible) {
            mContactInclude.setVisibility(View.GONE);
            return;
        }

        mContactArrowIv = (ImageView) mContactInclude.findViewById(R.id.contact_arrow_iv);
        mContactArrowIv.setOnClickListener(this);
        mSelectAllCb = (CheckBox) mContactInclude.findViewById(R.id.contact_all_cb);
        mSelectAllCb.setOnClickListener(this);
        mContactContainor = (LinearLayout) mContactInclude.findViewById(R.id.contact_containor);

        if (mContactList.size() > 1) {
            mContactArrowIv.setImageResource(R.drawable.ic_pri_arrow_down);
        } else {
            mContactArrowIv.setImageResource(R.drawable.ic_pri_arrow_up);
        }

        if (mContactList.size() == 1) {
            mContactArrowIv.setVisibility(View.GONE);
            mSelectAllCb.setVisibility(View.GONE);
        }
        mContactViews = new ArrayList<View>();
        for (int i = 0; i < mContactList.size(); i++) {
            ContactBean contactBean = mContactList.get(i);
            Context context = AppMasterApplication.getInstance();
            View v = View.inflate(context, R.layout.pri_contact_item, null);
            ImageView imageView = (ImageView) v.findViewById(R.id.contact_head_iv);
            imageView.setImageBitmap(contactBean.getContactIcon());

            TextView nameTv = (TextView) v.findViewById(R.id.contact_name_tv);
            nameTv.setText(contactBean.getContactName());

            TextView numberTv = (TextView) v.findViewById(R.id.contact_phone_tv);
            numberTv.setText(contactBean.getContactNumber());

            TextView callNumTv = (TextView) v.findViewById(R.id.contact_call_count_tv);
            callNumTv.setText(contactBean.getCallCount() + "");

            TextView smsNumTv = (TextView) v.findViewById(R.id.contact_sms_count_tv);
            smsNumTv.setText(contactBean.getMessageCount() + "");
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.contact_single_cb);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectData.size() + mAddedData.size() == mContactViews.size()) {
                        mSelectAllCb.setChecked(true);
                    } else {
                        mSelectAllCb.setChecked(false);
                    }
                }
            });
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ContactBean cb = mDataMap.get(buttonView);
                    if (mAddedData.contains(cb)) return;

                    if (isChecked) {
                        if (!mSelectData.contains(cb)) {
                            mSelectData.add(cb);
                        }
                    } else {
                        if (mSelectData.contains(cb)) {
                            mSelectData.remove(cb);
                        }
                    }
                    ChangeContactColor();
                }
            });
            if (i == 0) {
                mContactContainor.addView(v);
            }
            mContactViews.add(v);
            mDataMap.put(checkBox, contactBean);
        }

        mContactBtnTv = (TextView) mContactInclude.findViewById(R.id.item_btn_tv);
        ChangeContactColor();
        mContactBtnLt = (RippleView) mContactInclude.findViewById(R.id.item_btn_rv);
        mContactBtnDiv = mContactInclude.findViewById(R.id.item_btn_divider);
        mContactBtnLt.setOnClickListener(this);
//        mContactBtnLt.setOnRippleCompleteListener(this);

        mContactBtnTv.setText(R.string.pri_pro_contact_btn);
        mImpTv = (TextView) mContactInclude.findViewById(R.id.imp_item_btn_rv);
        mContactContainorDisable = (View) mContactInclude.findViewById(R.id.contact_containor_disab);
    }

    private void ChangeContactColor() {
        if (mSelectData.size() > 0) {
            mContactBtnTv.setTextColor(mActivity.getResources().getColor(R.color.cgn));
            Drawable drawable = mActivity.getResources().getDrawable(R.drawable.arrow_button_right);
            mContactBtnTv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        } else {
            mContactBtnTv.setTextColor(mActivity.getResources().getColor(R.color.contact_no_select));
            Drawable drawable = mActivity.getResources().getDrawable(R.drawable.arrow_button_right_normal);
            mContactBtnTv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privacy_confirm, container, false);
    }


    private void onAddContactFinish() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                ChangeContactColor();
                Toast.makeText(mActivity, mActivity.getString(R.string.pri_contact_succ), Toast.LENGTH_SHORT).show();
//                for (View contactView : mContactViews) {
//                    CheckBox checkBox = (CheckBox) contactView.findViewById(R.id.contact_single_cb);
//                    ContactBean cb = mDataMap.get(checkBox);
//                    if (mAddedData.contains(cb)) {
//                        checkBox.setVisibility(View.GONE);
//
//                        TextView textView = (TextView) contactView.findViewById(R.id.contact_added);
//                        textView.setVisibility(View.VISIBLE);
//                    } else {
//                        checkBox.setChecked(false);
//                    }
//                }
//                if (mAddedData.size() == mContactViews.size()) {
//                    mContactBtnTv.setText(R.string.pri_contact_over);
//                    mContactBtnLt.setClickable(false);
//                    mSelectAllCb.setVisibility(View.GONE);
//                    mContactBtnLt.setVisibility(View.GONE);
//                    mContactBtnDiv.setVisibility(View.GONE);
//                    mImpTv.setVisibility(View.GONE);
//                } else {
//                    setAddPrivacyText(false);
//                    mSelectAllCb.setChecked(false);
//                }
//                resetContact();
                resetCheckState();
                if (mMessageCallBean != null) {
                    showImportDlg();
                }
            }
        });
    }

    public void resetCheckState() {
        ChangeContactColor();
        for (View contactView : mContactViews) {
            CheckBox checkBox = (CheckBox) contactView.findViewById(R.id.contact_single_cb);
            ContactBean cb = mDataMap.get(checkBox);
            if (mAddedData.contains(cb)) {
                checkBox.setVisibility(View.GONE);

                TextView textView = (TextView) contactView.findViewById(R.id.contact_added);
                textView.setVisibility(View.VISIBLE);
            } else {
                checkBox.setChecked(false);
            }
        }
        if (mAddedData.size() == mContactViews.size()) {
            mContactBtnTv.setText(R.string.pri_contact_over);
            mContactBtnLt.setClickable(false);
            mSelectAllCb.setVisibility(View.GONE);
            mContactBtnLt.setVisibility(View.GONE);
            mContactBtnDiv.setVisibility(View.GONE);
            mImpTv.setVisibility(View.GONE);
        } else {
            setAddPrivacyText(false);
            mSelectAllCb.setChecked(false);
        }
        resetContact();

    }


    /*设置在隐私清理页面添加联系人时按钮显示状态*/
    private void setAddPrivacyText(boolean isImport) {
        if (isImport) {
            mContactContainorDisable.setVisibility(View.VISIBLE);
            mImpTv.setVisibility(View.VISIBLE);
            mContactBtnLt.setVisibility(View.GONE);
            mImpTv.setClickable(false);
        } else {
            mContactContainorDisable.setVisibility(View.GONE);
            mContactBtnLt.setVisibility(View.VISIBLE);
            mImpTv.setVisibility(View.GONE);
        }
    }

    private void initAdLayout(Campaign campaign, View view, String unitId, Bitmap previewBitmap) {
        if (previewBitmap == null || previewBitmap.isRecycled()) {
            return;
        }

        if (view != null) {
            ViewStub viewStub = (ViewStub) view.findViewById(R.id.advertise_security_stub);
//            View include = view.findViewById(R.id.advertise_security);
            if (viewStub == null) {
                return;
            }
            View include = viewStub.inflate();
            TextView title = (TextView) include.findViewById(R.id.item_title);
            title.setText(campaign.getAppName());
            TextView btnCTA = (TextView) include.findViewById(R.id.item_btn_tv);
            btnCTA.setText(campaign.getAdCall());
            ResizableImageView preview = (ResizableImageView) include.findViewById(R.id.item_ad_preview);
            preview.setImageBitmap(previewBitmap);
            TextView tvSummary = (TextView) include.findViewById(R.id.item_summary);
            tvSummary.setText(campaign.getAppDesc());
            include.setVisibility(View.VISIBLE);
            MobvistaEngine.getInstance(mActivity).registerView(unitId, include);
        }
    }

    private void initScanResultLayout(View mPanelView) {
        if (mPanelView != null) {
            mResultInclude = mPanelView.findViewById(R.id.scan_result);
//            ViewStub viewStub = (ViewStub) mPanelView.findViewById(R.id.scan_result);
//            if (viewStub == null) {
//                return;
//            }

            int appnum = mActivity.getLockAppNum();
            int picnum = mActivity.getHidePicNum();
            int vidnum = mActivity.getHideVidNum();

//            mResultInclude = viewStub.inflate();

            mBoxOne = mResultInclude.findViewById(R.id.box_one);
            mBoxOne.setOnClickListener(this);
            mBoxTwo = mResultInclude.findViewById(R.id.box_two);
            mBoxTwo.setOnClickListener(this);
            mBoxThree = mResultInclude.findViewById(R.id.box_three);
            mBoxThree.setOnClickListener(this);

            TextView item_one_num = (TextView) mResultInclude.findViewById(R.id.item_one_num);
            ImageView item_iv_one = (ImageView) mResultInclude.findViewById(R.id.item_iv_one);

            TextView item_two_num = (TextView) mResultInclude.findViewById(R.id.item_two_num);
            ImageView item_iv_two = (ImageView) mResultInclude.findViewById(R.id.item_iv_two);

            TextView item_three_num = (TextView) mResultInclude.findViewById(R.id.item_three_num);
            ImageView item_three_bottom = (ImageView) mResultInclude.findViewById(R.id.item_iv_three);

            if (appnum < 0) {
                item_one_num.setVisibility(View.GONE);
                item_iv_one.setVisibility(View.VISIBLE);
                LeoLog.d("testConfrim", "appnum == 0");
            } else {
                LeoLog.d("testConfrim", "appnum != 0");
                item_one_num.setVisibility(View.VISIBLE);
                item_one_num.setText("+" + appnum);
                item_iv_one.setVisibility(View.GONE);
            }

            if (picnum < 0) {
                item_two_num.setVisibility(View.GONE);
                item_iv_two.setVisibility(View.VISIBLE);
            } else {
                item_two_num.setVisibility(View.VISIBLE);
                item_two_num.setText("+"+picnum);
                item_iv_two.setVisibility(View.GONE);
            }

            if (vidnum < 0) {
                item_three_num.setVisibility(View.GONE);
                item_three_bottom.setVisibility(View.VISIBLE);
            } else {
                item_three_num.setVisibility(View.VISIBLE);
                item_three_num.setText("+"+vidnum);
                item_three_bottom.setVisibility(View.GONE);
            }

//            LeoLog.d("testNum", "appnum : " + appnum + "picnum : " + picnum + "vidnum : " + vidnum);
//            if (appnum > 0 || picnum > 0 || vidnum > 0) {
//                mResultInclude = viewStub.inflate();
//                View appLayout = mResultInclude.findViewById(R.id.result_one);
//                if (appnum == 0) {
//                    appLayout.setVisibility(View.GONE);
//                } else {
//                    TextView tv_app = (TextView) mResultInclude.findViewById(R.id.tv_applock_item);
//                    String text = mActivity.getString(R.string.scan_done_app, appnum);
//                    tv_app.setText(Html.fromHtml(text));
//                    mResultClickApp = mResultInclude.findViewById(R.id.item_btn_rv_one);
//                    mResultClickApp.setOnClickListener(this);
//                }
//
//                View picLayout = mResultInclude.findViewById(R.id.result_two);
//                if (picnum == 0) {
//                    picLayout.setVisibility(View.GONE);
//                } else {
//                    TextView tv_pic = (TextView) mResultInclude.findViewById(R.id.tv_hidepic_item);
//                    String text = mActivity.getString(R.string.scan_done_pic, picnum);
//                    tv_pic.setText(Html.fromHtml(text));
//                    mResultClickPic = mResultInclude.findViewById(R.id.item_btn_rv_two);
//                    mResultClickPic.setOnClickListener(this);
//                }
//
//                View vidLayout = mResultInclude.findViewById(R.id.result_three);
//                if (vidnum == 0) {
//                    vidLayout.setVisibility(View.GONE);
//                } else {
//                    TextView tv_vid = (TextView) mResultInclude.findViewById(R.id.tv_hidevid_item);
//                    String text = mActivity.getString(R.string.scan_done_vid, vidnum);
//                    tv_vid.setText(Html.fromHtml(text));
//                    mResultClickVid = mResultInclude.findViewById(R.id.item_btn_rv_three);
//                    mResultClickVid.setOnClickListener(this);
//                }
//            }
        }
    }

    private void initLostLayout(View view) {
        if (view != null) {
            mLostInclude = view.findViewById(R.id.lost_security);
            LostSecurityManager manager = (LostSecurityManager) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            mLostFixedLt = mLostInclude.findViewById(R.id.item_middle_fixed_rl);
            mLostMiddleLt = mLostInclude.findViewById(R.id.item_middle_ll);
            mLostFixedTv = (TextView) mLostInclude.findViewById(R.id.fixed_summary);
            mLostSummary = (TextView) mLostInclude.findViewById(R.id.item_summary);
            mLostBtnTv = (TextView) mLostInclude.findViewById(R.id.item_btn_tv);
            mLostBtnDiv = mLostInclude.findViewById(R.id.item_btn_divider);
            mLostBtnLt = (RippleView) mLostInclude.findViewById(R.id.item_btn_rv);
            mLostBtnLt.setOnClickListener(this);
//            mLostBtnLt.setOnRippleCompleteListener(this);

            String text = mActivity.getString(R.string.pri_pro_btn, manager.getMaxScore());
            mLostBtnTv.setText(text);

            mLostSummary.setText(R.string.pri_pro_lost_summary);
        }
    }

    private void initIntruderLayout(View view) {
        IntrudeSecurityManager manager = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        mIntruderInclude = view.findViewById(R.id.intruder_security);
        mIntruderFixedLt = mIntruderInclude.findViewById(R.id.item_middle_fixed_rl);
        mIntruderMiddleLt = mIntruderInclude.findViewById(R.id.item_middle_ll);
        mIntruderFixedTv = (TextView) mIntruderInclude.findViewById(R.id.fixed_summary);
        mIntruderSummary = (TextView) mIntruderInclude.findViewById(R.id.item_summary);
        mIntruderBtnTv = (TextView) mIntruderInclude.findViewById(R.id.item_btn_tv);
        mIntruderBtnDiv = mIntruderInclude.findViewById(R.id.item_btn_divider);
        mIntruderBtnLt = (RippleView) mIntruderInclude.findViewById(R.id.item_btn_rv);
        mIntruderBtnLt.setOnClickListener(this);
//        mIntruderBtnLt.setOnRippleCompleteListener(this);

        String text = mActivity.getString(R.string.pri_pro_btn, manager.getMaxScore());
        mIntruderBtnTv.setText(text);

        mIntruderSummary.setText(R.string.pri_pro_intruder_summary);
    }

    private void initWifiLayout(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.wifi_security_stub);
//        View include = view.findViewById(R.id.wifi_security);
        if (viewStub == null) {
            return;
        }
        mWIfiInclude = viewStub.inflate();
        View warn = mWIfiInclude.findViewById(R.id.item_summay_sub_ll);
        warn.setVisibility(View.VISIBLE);
        mWifiSummary = (TextView) mWIfiInclude.findViewById(R.id.item_summary);
        mWifiSubSummary = (TextView) mWIfiInclude.findViewById(R.id.item_summary_subject);
        mWifiBtnTv = (TextView) mWIfiInclude.findViewById(R.id.item_btn_tv);
        mWifiBtnDiv = mWIfiInclude.findViewById(R.id.item_btn_divider);
        mWifiBtnLt = (RippleView) mWIfiInclude.findViewById(R.id.item_btn_rv);
        mWifiBtnLt.setOnClickListener(this);
//        mWifiBtnLt.setOnRippleCompleteListener(this);

        mWifiMiddleLt = mWIfiInclude.findViewById(R.id.item_middle_ll);
        mWifiFixedLt = mWIfiInclude.findViewById(R.id.item_middle_fixed_rl);

        mWifiFixedTitle = (TextView) mWIfiInclude.findViewById(R.id.fixed_title);
        mWifiFixedSummaryLt = mWIfiInclude.findViewById(R.id.fixed_summary_ll);

        String text = mActivity.getString(R.string.pri_pro_wifi_btn);
        mWifiBtnTv.setText(text);
    }

    private void initWifiMasterLayout(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.wifi_master_stub);
        if (viewStub == null) {
            return;
        }

        LeoPreference leoPreference = LeoPreference.getInstance();

        boolean isContentEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_WIFIMASTER_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_WIFIMASTER_IMG_URL));

        boolean isTypeEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_WIFIMASTER_TYPE));

        boolean isGpUrlEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_WIFIMASTER_GP_URL));

        boolean isBrowserUrlEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_WIFIMASTER_URL));

        boolean isUrlEmpty = isGpUrlEmpty && isBrowserUrlEmpty; //判断两个地址是否都为空

        if (!isContentEmpty && !isImgUrlEmpty && !isTypeEmpty && !isUrlEmpty) {
            View include = viewStub.inflate();
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "master_shw");
            mWifiMasteTitle = (TextView) include.findViewById(R.id.item_title);
            mWifiMasteImg = (ImageView) include.findViewById(R.id.wifimaster_img);
            mWifiMasteContent = (TextView) include.findViewById(R.id.wifimaster_content);
            mWifiMasteBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
            mWifiMasteBtnLt.setOnClickListener(this);
            mWifiMasteContent.setText(leoPreference.getString(PrefConst.KEY_PRI_WIFIMASTER_CONTENT));
            String imgUrl = leoPreference.getString(PrefConst.KEY_PRI_WIFIMASTER_IMG_URL);
            mImageLoader.displayImage(imgUrl, mWifiMasteImg, getOptions(R.drawable.online_theme_loading));
            boolean isTitleEmpty = TextUtils.isEmpty(
                    leoPreference.getString(PrefConst.KEY_PRI_WIFIMASTER_TITLE));
            if (!isTitleEmpty) {
                mWifiMasteTitle.setText(leoPreference.getString(
                        PrefConst.KEY_PRI_WIFIMASTER_TITLE));
            }
        }
    }

    private void initSwiftyLayout(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.swifty_security_stub);
        if (viewStub == null) {
            return;
        }

        LeoPreference leoPreference = LeoPreference.getInstance();

        boolean isContentEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_SWIFTY_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_SWIFTY_IMG_URL));

        boolean isTypeEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_SWIFTY_TYPE));

        boolean isGpUrlEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_SWIFTY_GP_URL));

        boolean isBrowserUrlEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_SWIFTY_URL));

        boolean isUrlEmpty = isGpUrlEmpty && isBrowserUrlEmpty; //判断两个地址是否都为空

        if (!isContentEmpty && !isImgUrlEmpty && !isTypeEmpty && !isUrlEmpty) {
            View include = viewStub.inflate();
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "swifty_shw");
            mSwiftyTitle = (TextView) include.findViewById(R.id.item_title);
            mSwiftyImg = (ImageView) include.findViewById(R.id.swifty_img);
            mSwiftyContent = (TextView) include.findViewById(R.id.swifty_content);
            mSwiftyBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
            mSwiftyBtnLt.setOnClickListener(this);
            mSwiftyContent.setText(leoPreference.getString(PrefConst.KEY_SWIFTY_CONTENT));
            String imgUrl = leoPreference.getString(PrefConst.KEY_SWIFTY_IMG_URL);
            mImageLoader.displayImage(imgUrl, mSwiftyImg, getOptions(R.drawable.online_theme_loading));
            boolean isTitleEmpty = TextUtils.isEmpty(
                    leoPreference.getString(PrefConst.KEY_SWIFTY_TITLE));
            if (!isTitleEmpty) {
                mSwiftyTitle.setText(leoPreference.getString(
                        PrefConst.KEY_SWIFTY_TITLE));
            }
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

    private void initFbLayout(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.fb_security_stub);
        if (viewStub == null) {
            return;
        }

        LeoPreference leoPreference = LeoPreference.getInstance();

        boolean isContentEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_FB_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_FB_IMG_URL));

        boolean isURLEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_FB_URL));

        if (!isContentEmpty && !isImgUrlEmpty && !isURLEmpty) {
            View include = viewStub.inflate();
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "facebook_shw");
            mFbTitle = (TextView) include.findViewById(R.id.item_title);
            mFbImg = (ImageView) include.findViewById(R.id.fb_img);
            mFbContent = (TextView) include.findViewById(R.id.fb_content);
            mFbBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
            mFbBtnLt.setOnClickListener(this);
            mFbContent.setText(leoPreference.getString(PrefConst.KEY_PRI_FB_CONTENT));
            String imgUrl = leoPreference.getString(PrefConst.KEY_PRI_FB_IMG_URL);
            mImageLoader.displayImage(imgUrl, mFbImg, getOptions(R.drawable.fb_banner));
            boolean isTitleEmpty = TextUtils.isEmpty(
                    leoPreference.getString(PrefConst.KEY_PRI_FB_TITLE));
            if (!isTitleEmpty) {
                mFbTitle.setText(leoPreference.getString(
                        PrefConst.KEY_PRI_FB_TITLE));
            }
        }
    }

    private void initGradeLayout(View view) {
        int score = PrivacyHelper.getInstance(mActivity).getSecurityScore();
        LeoLog.i("loadSwiftySecurity", "score：" + score);

        LeoPreference leoPreference = LeoPreference.getInstance();

        boolean isContentEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_GRADE_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_GRADE_IMG_URL));

        boolean isURLEmpty = TextUtils.isEmpty(
                leoPreference.getString(PrefConst.KEY_PRI_GRADE_URL));

        if (!isContentEmpty && !isImgUrlEmpty && !isURLEmpty) {

            if (score == 100) {  // 等于100分
                ViewStub viewStub = (ViewStub) view.findViewById(R.id.grade_high_security_stub);
                if (viewStub == null) {
                    return;
                }
                View highInclude = viewStub.inflate();
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "rate_shw");
                mHighGradeLayout = (RelativeLayout) highInclude;
                mHighOneStar = (ImageView) highInclude.findViewById(R.id.one_star);
                mHighTwoStar = (ImageView) highInclude.findViewById(R.id.two_star);
                mHighThreeStar = (ImageView) highInclude.findViewById(R.id.three_star);
                mHighFourStar = (ImageView) highInclude.findViewById(R.id.four_star);
                mHighFiveStar = (ImageView) highInclude.findViewById(R.id.five_star);
                mHighGradeGesture = (ImageView) highInclude.findViewById(R.id.grade_gesture);
                mHighGradeBtnLt = (RippleView) highInclude.findViewById(R.id.item_btn_rv);
                mHighFiveEmptyStar = (ImageView) highInclude.findViewById(R.id.five_star_empty);
                mHighGradeTitle = (TextView) highInclude.findViewById(R.id.item_title);
                mHighGradeImg = (ImageView) highInclude.findViewById(R.id.grade_img);
                mHighGradeContent = (TextView) highInclude.findViewById(R.id.grade_content);
                mHighGradeBtnLt.setOnClickListener(this);
                mHighGradeLayout.setOnClickListener(this);

                mHighGradeContent.setText(leoPreference.getString(PrefConst.KEY_PRI_GRADE_CONTENT));
                String imgUrl = leoPreference.getString(PrefConst.KEY_PRI_GRADE_IMG_URL);
                mImageLoader.displayImage(imgUrl, mHighGradeImg, getOptions(R.drawable.grade_bg));
                boolean isTitleEmpty = TextUtils.isEmpty(
                        leoPreference.getString(PrefConst.KEY_PRI_GRADE_TITLE));
                if (!isTitleEmpty) {
                    mHighGradeTitle.setText(leoPreference.getString(
                            PrefConst.KEY_PRI_GRADE_TITLE));
                }

                showStarAnimation(mHighOneStar, mHighTwoStar, mHighThreeStar,
                        mHighFourStar, mHighFiveStar, mHighFiveEmptyStar, mHighGradeGesture);

            } else {
                ViewStub viewStub = (ViewStub) view.findViewById(R.id.grade_security_stub);
                if (viewStub == null) {
                    return;
                }
                View include = viewStub.inflate();
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "rate_shw");
                mGradeLayout = (RelativeLayout) include;
                mOneStar = (ImageView) include.findViewById(R.id.one_star);
                mTwoStar = (ImageView) include.findViewById(R.id.two_star);
                mThreeStar = (ImageView) include.findViewById(R.id.three_star);
                mFourStar = (ImageView) include.findViewById(R.id.four_star);
                mFiveStar = (ImageView) include.findViewById(R.id.five_star);
                mGradeGesture = (ImageView) include.findViewById(R.id.grade_gesture);
                mGradeBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
                mFiveEmptyStar = (ImageView) include.findViewById(R.id.five_star_empty);
                mGradeTitle = (TextView) include.findViewById(R.id.item_title);
                mGradeImg = (ImageView) include.findViewById(R.id.grade_img);
                mGradeContent = (TextView) include.findViewById(R.id.grade_content);
                mGradeBtnLt.setOnClickListener(this);
                mGradeLayout.setOnClickListener(this);

                mGradeContent.setText(leoPreference.getString(PrefConst.KEY_PRI_GRADE_CONTENT));
                String imgUrl = leoPreference.getString(PrefConst.KEY_PRI_GRADE_IMG_URL);
                mImageLoader.displayImage(imgUrl, mGradeImg, getOptions(R.drawable.grade_bg));
                boolean isTitleEmpty = TextUtils.isEmpty(
                        leoPreference.getString(PrefConst.KEY_PRI_GRADE_TITLE));
                if (!isTitleEmpty) {
                    mGradeTitle.setText(leoPreference.getString(
                            PrefConst.KEY_PRI_GRADE_TITLE));
                }

                showStarAnimation(mOneStar, mTwoStar, mThreeStar, mFourStar,
                        mFiveStar, mFiveEmptyStar, mGradeGesture);
            }
        }

    }


    /***
     * 开始动画
     */
    private void showStarAnimation(final ImageView theOne, final ImageView theTwo,
                                   final ImageView theThree, final ImageView theFour,
                                   final ImageView theFive, ImageView theEmptyFive,
                                   final ImageView gradeGesture) {
        ObjectAnimator oneStar = getObjectAnimator(theOne);
        ObjectAnimator twoStar = getObjectAnimator(theTwo);
        ObjectAnimator threeStar = getObjectAnimator(theThree);
        ObjectAnimator fourStar = getObjectAnimator(theFour);
        ObjectAnimator fiveStar = getObjectAnimator(theFive);

        /** 手势移入 */
        float currentY = gradeGesture.getTranslationY();
        ObjectAnimator gestureIn = ObjectAnimator.ofFloat(gradeGesture,
                "translationY", currentY + DipPixelUtil.dip2px(mActivity, 16), currentY);
        gestureIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                gradeGesture.setVisibility(View.VISIBLE);
            }
        });
        ObjectAnimator gestureShow = ObjectAnimator.ofFloat(gradeGesture, "alpha", 0f, 1f);

        AnimatorSet gestureMoveIn = new AnimatorSet();
        gestureMoveIn.playTogether(gestureIn, gestureShow);
        gestureMoveIn.setDuration(1000);

        /** 第五颗星星缩小放大 */
        ObjectAnimator starScaleX = ObjectAnimator.ofFloat(theEmptyFive, "scaleX", 1f, 0.7f, 1f);
        ObjectAnimator starScaleY = ObjectAnimator.ofFloat(theEmptyFive, "scaleY", 1f, 0.7f, 1f);

        AnimatorSet fiveStarScale = new AnimatorSet();
        fiveStarScale.playTogether(starScaleX, starScaleY);
        fiveStarScale.setDuration(500);

        /** 星星填充和手势移出 */
        ObjectAnimator gestureOut = ObjectAnimator.ofFloat(gradeGesture,
                "translationY", currentY, currentY + DipPixelUtil.dip2px(mActivity, 16));

        ObjectAnimator gestureHide = ObjectAnimator.ofFloat(gradeGesture, "alpha", 1f, 0f);

        gestureOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                gradeGesture.setVisibility(View.INVISIBLE);
            }
        });

        AnimatorSet gestureAnimator = new AnimatorSet();
        gestureAnimator.playTogether(gestureOut, gestureHide);
        gestureAnimator.setDuration(1000);


        AnimatorSet starAnimator = new AnimatorSet();
        starAnimator.playSequentially(oneStar, twoStar, threeStar, fourStar, fiveStar);

        AnimatorSet gestureMoveOut = new AnimatorSet();
        gestureMoveOut.playTogether(gestureAnimator, starAnimator);


        ObjectAnimator emptyAnimator = ObjectAnimator.ofFloat(theFive, "alpha", 1f, 1f);
        emptyAnimator.setDuration(1000);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(gestureMoveIn, fiveStarScale, gestureMoveOut, emptyAnimator);

        mAnimatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                theOne.setVisibility(View.INVISIBLE);
                theTwo.setVisibility(View.INVISIBLE);
                theThree.setVisibility(View.INVISIBLE);
                theFour.setVisibility(View.INVISIBLE);
                theFive.setVisibility(View.INVISIBLE);
                if (mAnimatorSet != null) {
                    mAnimatorSet.start();
                }
            }
        });

        mAnimatorSet.start();
    }

    /**
     * 每个星星动画开始监听
     */
    private class ObjectAnimStartListener extends AnimatorListenerAdapter {

        private ImageView theView;

        public ObjectAnimStartListener(ImageView imageView) {
            this.theView = imageView;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            theView.setVisibility(View.VISIBLE);
        }
    }


    private ObjectAnimator getObjectAnimator(ImageView img) {

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(img, "alpha", 0f, 1f);
        objectAnimator.setDuration(200);
        objectAnimator.addListener(new ObjectAnimStartListener(img));

        return objectAnimator;
    }

    @Override
    public void onClick(View v) {
        LockManager lockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        if (v == mContactArrowIv) {
            if (mContactContainor.getChildCount() > 1) {
                collapseContact();
            } else {
                expandContact();
            }
        } else if (v == mSelectAllCb) {
            for (View contactView : mContactViews) {
                CheckBox cb = (CheckBox) contactView.findViewById(R.id.contact_single_cb);
                if (mSelectAllCb.isChecked()) {
                    cb.setChecked(true);
                } else {
                    cb.setChecked(false);
                }
            }

            resetContact();
        } else if (v.getId() == R.id.pp_process_rv_click) {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "finish");
            mActivity.onBackPressed();
        } else if (mContactBtnLt == v) {
            final PrivacyContactManager pcm = (PrivacyContactManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
            if (!mSelectData.isEmpty()) {
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "contact_enable");
                setAddPrivacyText(true);
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        CallFilterManagerImpl cmp = (CallFilterManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                        if (mSelectData != null && mSelectData.size() == 1) {
                            boolean isHaveBlackNum = cmp.isExistBlackList(mSelectData.get(0).getContactNumber());
                            if (isHaveBlackNum) {
                                ThreadManager.getUiThreadHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String str = getResources().getString(R.string.call_filter_have_add_black_num);
                                        Toast.makeText(mActivity, str, Toast.LENGTH_SHORT).show();
//                                        setAddPrivacyText(false);
//                                        mAddedData.clear();
//                                        resetContact();
//                                        ChangeContactColor();
                                        resetCheckState();
                                    }
                                });
                            } else {
                                mMessageCallBean = pcm.addPrivacyContact(mSelectData);
                                mAddedData.addAll(mSelectData);
                                mSelectData.clear();
                                onAddContactFinish();
                            }
                        } else {
                            List<ContactBean> selectDat = (List<ContactBean>) ((ArrayList) mSelectData).clone();
                            for (ContactBean contact : selectDat) {
                                boolean isHaveBlackNum = cmp.isExistBlackList(contact.getContactNumber());
                                if (isHaveBlackNum) {
                                    mSelectData.remove(contact);
                                }
                            }
                            if (mSelectData == null || mSelectData.size() < 1) {
                                ThreadManager.getUiThreadHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String str = getResources().getString(R.string.call_filter_have_add_black_num);
                                        Toast.makeText(mActivity, str, Toast.LENGTH_SHORT).show();
                                        setAddPrivacyText(false);
                                        mAddedData.clear();
                                        resetContact();
                                        ChangeContactColor();
                                    }
                                });
                            }
                            mMessageCallBean = pcm.addPrivacyContact(mSelectData);
                            mAddedData.addAll(mSelectData);
                            mSelectData.clear();
                            onAddContactFinish();
                        }
                    }
                });
            } else {
//                Toast.makeText(mActivity(), mActivity().mActivity.getString(R.string.pri_contact_empty), Toast.LENGTH_SHORT).show();
            }
            ChangeContactColor();
        } else if (mWifiBtnLt == v) {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "wifi_scan");
            LeoPreference table = LeoPreference.getInstance();
            int count = table.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_WIFI_SECURITY, 0);
            table.putInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_WIFI_SECURITY, count+1);
            Intent intent = new Intent(mActivity, WifiSecurityActivity.class);
            startActivity(intent);
        } else if (mIntruderBtnLt == v) {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "intruder_enable");
            Intent intent = new Intent(mActivity, IntruderprotectionActivity.class);
            intent.putExtra(Constants.EXTRA_IS_FROM_SCAN, true);
            startActivity(intent);
        } else if (mFbBtnLt == v) {  // FaceBook分享
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "facebook");
            lockManager.filterSelfOneMinites();
            Utilities.goFaceBook(mActivity, true);
        } else if (mHighGradeBtnLt == v) {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "rate");
            mPt.putBoolean(PrefConst.KEY_HAS_GRADE, true);
            lockManager.filterSelfOneMinites();
            Utilities.goFiveStar(mActivity, true, true);
        } else if (mLostBtnLt == v) {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "theft_enable");
            Intent intent = new Intent(mActivity, PhoneSecurityGuideActivity.class);
            intent.putExtra(Constants.EXTRA_IS_FROM_SCAN, true);
            startActivity(intent);
        } else if (mGradeBtnLt == v) { // 五星好评
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "rate");
            lockManager.filterSelfOneMinites();
            mPt.putBoolean(PrefConst.KEY_HAS_GRADE, true);
            Utilities.goFiveStar(mActivity, true, true);
        } else if (mSwiftyBtnLt == v) {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "swifty");
            lockManager.filterSelfOneMinites();
            Utilities.gotoGpOrBrowser(mActivity, Constants.IS_CLICK_SWIFTY, true);
        } else if (mWifiMasteBtnLt == v) {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "master");
            lockManager.filterSelfOneMinites();
            Utilities.gotoGpOrBrowser(mActivity, Constants.IS_CLICK_WIFIMASTER, true);
        } else if (mGradeLayout == v) {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "rate");
            mPt.putBoolean(PrefConst.KEY_HAS_GRADE, true);
            lockManager.filterSelfOneMinites();
            Utilities.goFiveStar(mActivity, true, true);
        } else if (mHighGradeLayout == v) {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "rate");
            mPt.putBoolean(PrefConst.KEY_HAS_GRADE, true);
            lockManager.filterSelfOneMinites();
            Utilities.goFiveStar(mActivity, true, true);
        } else if (mBoxOne == v) {
            resultToAppLock();
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "result_app_cnts");
        } else if (mBoxTwo == v) {
            resultToPic();
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "result_pic_cnts");
        } else if (mBoxThree == v) {
            resultToVid();
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "result_vid_cnts");
        }

    }

    private void resultToAppLock() {
        Intent intent = new Intent(mActivity, AppLockListActivity.class);
        intent.putExtra(Constants.FROM_CONFIRM_FRAGMENT, true);
        mActivity.startActivity(intent);
    }

    private void resultToPic() {
        LeoPreference table = LeoPreference.getInstance();
        int count = table.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC, 0);
        table.putInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_PIC, count+1);
        Intent intent = new Intent(mActivity, ImageHideMainActivity.class);
        intent.putExtra("hidePicFinish", mActivity.getHidePicFinish());
        intent.putExtra(Constants.FROM_CONFIRM_FRAGMENT, true);
        mActivity.startActivity(intent);
    }

    private void resultToVid() {
        LeoPreference table = LeoPreference.getInstance();
        int count = table.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_VIDEO, 0);
        table.putInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_HIDE_VIDEO, count+1);
        Intent intent = new Intent(mActivity, VideoHideMainActivity.class);
        intent.putExtra(Constants.FROM_CONFIRM_FRAGMENT, true);
        mActivity.startActivity(intent);
    }

    private void collapseContact() {
        if (mContactContainor.getChildCount() <= 1) return;

        for (int i = mContactViews.size() - 1; i >= 1; i--) {
            mContactContainor.removeView(mContactViews.get(i));
        }
        mContactArrowIv.setImageResource(R.drawable.ic_pri_arrow_down);
    }

    private void expandContact() {
        if (mContactContainor.getChildCount() > 1) return;

        for (int i = 1; i < mContactViews.size(); i++) {
            mContactContainor.addView(mContactViews.get(i));
        }
        mContactArrowIv.setImageResource(R.drawable.ic_pri_arrow_up);
    }

    private void resetContact() {
        mContactContainor.removeAllViews();

        for (int i = 0; i < mContactViews.size(); i++) {
            mContactContainor.addView(mContactViews.get(i));
        }

        mSelectAllCb.setChecked(mSelectAllCb.isChecked());
        if (mContactList.size() > 1) {
            mContactArrowIv.setImageResource(R.drawable.ic_pri_arrow_up);
        } else {
            mContactArrowIv.setImageResource(R.drawable.ic_pri_arrow_down);
        }
    }

    private void showImportDlg() {
        if (mImportRecordDlg != null) {
            mImportRecordDlg.show();
            return;
        }

        String title = mActivity.getString(R.string.privacy_contact_add_log_dialog_title);
        String content = mActivity.getString(R.string.privacy_contact_add_log_dialog_dialog_content);
        mImportRecordDlg = new LEOAlarmDialog(mActivity);
        mImportRecordDlg.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "contact_import");
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            PrivacyContactManager pcm = (PrivacyContactManager)
                                    MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                            pcm.importCallAndMsms(mMessageCallBean);
                        }
                    });
                } else if (which == 0) {
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "proposals", "contact_cancel");
                    mImportRecordDlg.dismiss();
                }

            }
        });
        mImportRecordDlg.setCanceledOnTouchOutside(false);
        mImportRecordDlg.setTitle(title);
        mImportRecordDlg.setContent(content);
        mImportRecordDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ChangeContactColor();
            }
        });
        mImportRecordDlg.show();
    }
}
