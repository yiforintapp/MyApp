package com.leo.appmaster.home;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.LostSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyContactManager;
import com.leo.appmaster.mgr.WifiSecurityManager;
import com.leo.appmaster.phoneSecurity.PhoneSecurityGuideActivity;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.MessageCallLogBean;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.RippleView1;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
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

    private static final String KEY_SHOW_CONTACT = "SHOW_CONTACT";
    private View mRootView;
    private TextView mHeadView;

    private TextView mLostSummary;
    private TextView mLostBtnTv;
    private RippleView1 mLostBtnLt;
    private View mLostBtnDiv;
    private View mLostMiddleLt;
    private View mLostFixedLt;
    private TextView mLostFixedTv;

    private TextView mIntruderSummary;
    private TextView mIntruderBtnTv;
    private View mIntruderBtnDiv;
    private RippleView1 mIntruderBtnLt;
    private View mIntruderMiddleLt;
    private View mIntruderFixedLt;
    private TextView mIntruderFixedTv;

    private TextView mWifiSummary;
    private TextView mWifiSubSummary;
    private TextView mWifiBtnTv;
    private View mWifiBtnDiv;
    private RippleView1 mWifiBtnLt;
    private ImageView mWifiSummaryImg;
    private View mWifiFixedLt;
    private View mWifiMiddleLt;
    private TextView mWifiFixedTitle;
    private View mWifiFixedSummaryLt;

    private TextView mContactBtnTv;
    private RippleView1 mContactBtnLt;
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
    private RippleView1 mHighGradeBtnLt;
    private ImageView mHighFiveEmptyStar;
    private RelativeLayout mHighGrageFrame;

    private ImageView mOneStar;
    private ImageView mTwoStar;
    private ImageView mThreeStar;
    private ImageView mFourStar;
    private ImageView mFiveStar;
    private ImageView mGradeGesture;
    private RippleView1 mGradeBtnLt;
    private ImageView mFiveEmptyStar;
    private RelativeLayout mGrageFrame;

    private AnimatorSet mAnimatorSet;

    /**
     * 前往FaceBook
     */
    private RippleView1 mFbBtnLt;

    /**
     * Swifty
     */
    private ImageView mSwiftyImg;
    private TextView mSwiftyContent;
    private RippleView1 mSwiftyBtnLt;

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "proposals_cnts");
        mImageLoader = ImageLoader.getInstance();
        mSelectData = new ArrayList<ContactBean>();
        mAddedData = new ArrayList<ContactBean>();
        mDataMap = new HashMap<CheckBox, ContactBean>();

        Bundle args = getArguments();
        if (args != null) {
            mShowContact = args.getBoolean(KEY_SHOW_CONTACT);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView = view;

        // AM-3143: add animation for advertise cell
        ((ViewGroup) mRootView.findViewById(R.id.list_parent_layout)).setLayoutTransition(new LayoutTransition());

        mProcessBtn = (MaterialRippleLayout) view.findViewById(R.id.pp_process_rv);
        mProcessBtn.setRippleOverlay(true);
        mProcessClick = view.findViewById(R.id.pp_process_rv_click);
        mProcessClick.setOnClickListener(this);
        mIgnoreBtn = view.findViewById(R.id.pp_process_ignore_rv);
        mProcessTv = (TextView) view.findViewById(R.id.pp_process_tv);

        mIgnoreBtn.setVisibility(View.GONE);
        mProcessTv.setText(R.string.pri_pro_complete);
        mProcessBtn.setBackgroundResource(R.drawable.green_radius_btn_shape);

        mHeadView = (TextView) view.findViewById(R.id.pri_con_header);

        initLostLayout(view);
        initIntruderLayout(view);
        initWifiLayout(view);

        initContactLayout(view);
        initGradeLayout(view);
        initFbLayout(view);
        initSwiftyLayout(view);

        loadAd(view);
        mActivity.resetToolbarColor();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        MobvistaEngine.getInstance(mActivity).release(Constants.UNIT_ID_67);
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
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            PrivacyConfirmFragment fragment = mFragment.get();
            if (loadedImage != null && fragment != null) {
                LeoLog.d("MobvistaEngine", "[PrivacyConfirmFragment] onLoadingComplete -> " + imageUri);
                fragment.initAdLayout(mCampaign, fragment.mRootView, Constants.UNIT_ID_67, loadedImage);
                SDKWrapper.addEvent(fragment.mActivity, SDKWrapper.P1, "ad_act", "adv_shws_scanRST");
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

    private static AdPreviewLoaderListener sAdImageListener;

    private void loadAd(final View view) {
        AppMasterPreference amp = AppMasterPreference.getInstance(mActivity);
        if (amp.getIsADAfterPrivacyProtectionOpen() == 1) {
            MobvistaEngine.getInstance(mActivity).loadMobvista(Constants.UNIT_ID_67, new MobvistaListener() {

                @Override
                public void onMobvistaFinished(int code, Campaign campaign, String msg) {
                    if (code == MobvistaEngine.ERR_OK) {
                        sAdImageListener = new AdPreviewLoaderListener(PrivacyConfirmFragment.this, campaign);
                        ImageLoader.getInstance().loadImage(campaign.getImageUrl(), sAdImageListener);
                    }
                }

                @Override
                public void onMobvistaClick(Campaign campaign) {
                    LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                    lm.filterSelfOneMinites();

                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_cli", "adv_cnts_scanRST");
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntrudeSecurityManager ism = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        if (!ism.getIsIntruderSecurityAvailable()) {
            View include = mRootView.findViewById(R.id.intruder_security);
            include.setVisibility(View.GONE);
        } else if (ism.getIntruderMode()) {
            mIntruderBtnLt.setVisibility(View.GONE);
            mIntruderBtnDiv.setVisibility(View.GONE);
            mIntruderMiddleLt.setVisibility(View.GONE);
            mIntruderFixedLt.setVisibility(View.VISIBLE);

            mIntruderFixedTv.setText(getString(R.string.pri_intruder_fixed_pattern, ism.getCatchTimes()));
        }

        LostSecurityManager lsm = (LostSecurityManager) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        if (lsm.isUsePhoneSecurity()) {
            int[] times = lsm.getPhoneProtectTime();

            mLostBtnLt.setVisibility(View.GONE);
            mLostBtnDiv.setVisibility(View.GONE);
            mLostMiddleLt.setVisibility(View.GONE);
            mLostFixedLt.setVisibility(View.VISIBLE);
            mLostFixedTv.setText(getString(R.string.pri_pro_lost_fixed_pattern, times[0], times[1]));
        }

        if (PrivacyHelper.getInstance(getActivity()).getSecurityScore() == 100) {
            mHeadView.setText(R.string.pri_pro_summary_confirm);
        } else {
            mHeadView.setText(R.string.pri_pro_summary_not_confirm);
        }

        View include = mRootView.findViewById(R.id.wifi_security);
        WifiSecurityManager wsm = (WifiSecurityManager) MgrContext.getManager(MgrContext.MGR_WIFI_SECURITY);
        boolean isScnnedEver = wsm.getLastScanState();
        if (wsm.getWifiState() == WifiSecurityManager.NO_WIFI) {
            include.setVisibility(View.GONE);
        } else {
            mWifiSummary.setText(getString(R.string.pri_pro_wifi_connect, wsm.getWifiName()));
            if (isScnnedEver) {
                if (wsm.getWifiState() == WifiSecurityManager.SAFE_WIFI) {
                    mWifiFixedLt.setVisibility(View.VISIBLE);
                    mWifiFixedTitle.setText(getString(R.string.pri_pro_wifi_connect, wsm.getWifiName()));
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
                mWifiSubSummary.setText(R.string.pri_pro_wifi_warn);
                mWifiFixedSummaryLt.setVisibility(View.GONE);
                mWifiBtnLt.setVisibility(View.VISIBLE);
                mWifiBtnDiv.setVisibility(View.VISIBLE);
            }
        }

    }

    private void initContactLayout(View view) {
        View include = view.findViewById(R.id.contact_security);
        if (mContactList == null || mContactList.isEmpty() || !mShowContact) {
            include.setVisibility(View.GONE);
            return;
        }

        mContactArrowIv = (ImageView) include.findViewById(R.id.contact_arrow_iv);
        mContactArrowIv.setOnClickListener(this);
        mSelectAllCb = (CheckBox) include.findViewById(R.id.contact_all_cb);
        mSelectAllCb.setOnClickListener(this);
        mContactContainor = (LinearLayout) include.findViewById(R.id.contact_containor);

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
            View v = View.inflate(getActivity(), R.layout.pri_contact_item, null);
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

        mContactBtnTv = (TextView) include.findViewById(R.id.item_btn_tv);
        mContactBtnLt = (RippleView1) include.findViewById(R.id.item_btn_rv);
        mContactBtnDiv = include.findViewById(R.id.item_btn_divider);
        mContactBtnLt.setOnClickListener(this);
//        mContactBtnLt.setOnRippleCompleteListener(this);

        mContactBtnTv.setText(R.string.pri_pro_contact_btn);
        mImpTv = (TextView) include.findViewById(R.id.imp_item_btn_rv);
        mContactContainorDisable = (View) include.findViewById(R.id.contact_containor_disab);
    }

    private void ChangeContactColor() {
        if (mSelectData.size() > 0) {
            mContactBtnTv.setTextColor(mActivity.getResources().getColor(R.color.cgn));
        } else {
            mContactBtnTv.setTextColor(mActivity.getResources().getColor(R.color.contact_no_select));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privacy_confirm, container, false);
    }

    /**
     * 前往Gp或亚马逊云
     */
    private void gotoGpOrBrowser() {
        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        String gpUrl;
        String browserUrl;
        if ("1".equals(preferenceTable.getString(PrefConst.KEY_SWIFTY_TYPE))) { // 使用浏览器
            if (preferenceTable.getString(PrefConst.KEY_SWIFTY_URL) != null &&
                    preferenceTable.getString(PrefConst.KEY_SWIFTY_URL).length() > 0) {

                browserUrl = preferenceTable.getString(PrefConst.KEY_SWIFTY_URL);
                gotoBrowser(browserUrl);
            }
        } else {  // 使用gp
            if (preferenceTable.getString(PrefConst.KEY_SWIFTY_GP_URL) != null &&
                    preferenceTable.getString(PrefConst.KEY_SWIFTY_GP_URL).length() > 0) {
                gpUrl = preferenceTable.getString(PrefConst.KEY_SWIFTY_GP_URL);

                if (preferenceTable.getString(PrefConst.KEY_SWIFTY_URL) != null &&
                        preferenceTable.getString(PrefConst.KEY_SWIFTY_URL).length() > 0) {

                    browserUrl = preferenceTable.getString(PrefConst.KEY_SWIFTY_URL);
                } else {
                    browserUrl = "";
                }
                gotoGp(gpUrl, browserUrl);
            } else {
                if (preferenceTable.getString(PrefConst.KEY_SWIFTY_URL) != null &&
                        preferenceTable.getString(PrefConst.KEY_SWIFTY_URL).length() > 0) {

                    browserUrl = preferenceTable.getString(PrefConst.KEY_SWIFTY_URL);
                    gotoBrowser(browserUrl);
                }
            }
        }
    }

    /**
     * 使用Gp,没有Gp用浏览器
     */
    private void gotoGp(String gpUrl, String browserUrl) {
        Intent intent = null;
        if (AppUtil.appInstalled(mActivity,
                "com.android.vending")) {
            intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(gpUrl);
            intent.setData(uri);
            intent.setPackage("com.android.vending");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mActivity.startActivity(intent);
            } catch (Exception e) {
                if (!"".equals(browserUrl)) {
                    gotoBrowser(browserUrl);
                }
            }
        } else {
            if (!"".equals(browserUrl)) {
                gotoBrowser(browserUrl);
            }
        }
    }

    /**
     * 是使用浏览器
     */
    private void gotoBrowser(String url) {
        Intent intent;
        intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mActivity.startActivity(intent);
        } catch (Exception e) {
        }
    }

    /**
     * 前往FaceBook
     */
    private void goFaceBook() {
        Intent intentLikeUs = null;
        if (AppUtil.appInstalled(mActivity.getApplicationContext(),
                Constants.FACEBOOK_PKG_NAME)) {
            intentLikeUs = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(Constants.FACEBOOK_URL);
            intentLikeUs.setData(uri);
            ComponentName cn = new ComponentName(Constants.FACEBOOK_PKG_NAME,
                    Constants.FACEBOOK_CLASS);
            intentLikeUs.setComponent(cn);
            intentLikeUs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intentLikeUs);
            } catch (Exception e) {
            }
        } else {
            intentLikeUs = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(Constants.FACEBOOK_PG_URL);
            intentLikeUs.setData(uri);
            intentLikeUs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intentLikeUs);
            } catch (Exception e) {
            }
        }
    }

    private void onAddContactFinish() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), getActivity().getString(R.string.pri_contact_succ), Toast.LENGTH_SHORT).show();
                for (View contactView : mContactViews) {
                    CheckBox checkBox = (CheckBox) contactView.findViewById(R.id.contact_single_cb);
                    ContactBean cb = mDataMap.get(checkBox);
                    if (mAddedData.contains(cb)) {
                        checkBox.setVisibility(View.GONE);

                        TextView textView = (TextView) contactView.findViewById(R.id.contact_added);
                        textView.setVisibility(View.VISIBLE);
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
                }
                resetContact();
                if (mMessageCallBean != null) {
                    showImportDlg();
                }
            }
        });
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
        if (view != null) {
            View include = view.findViewById(R.id.advertise_security);
            TextView title = (TextView) include.findViewById(R.id.item_title);
            title.setText(campaign.getAppName());
            TextView btnCTA = (TextView) include.findViewById(R.id.item_btn_tv);
            btnCTA.setText(campaign.getAdCall());
            ImageView preview = (ImageView) include.findViewById(R.id.item_ad_preview);
            preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            preview.setImageBitmap(previewBitmap);
            TextView tvSummary = (TextView) include.findViewById(R.id.item_summary);
            tvSummary.setText(campaign.getAppDesc());
            include.setVisibility(View.VISIBLE);
            MobvistaEngine.getInstance(mActivity).registerView(unitId, include);
        }
    }

    private void displayImage(ImageView view, String uri) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true).build();
        ImageLoader.getInstance().displayImage(uri, view, options);
    }

    private void initLostLayout(View view) {
        if (view != null) {
            View include = view.findViewById(R.id.lost_security);
            LostSecurityManager manager = (LostSecurityManager) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            mLostFixedLt = include.findViewById(R.id.item_middle_fixed_rl);
            mLostMiddleLt = include.findViewById(R.id.item_middle_ll);
            mLostFixedTv = (TextView) include.findViewById(R.id.fixed_summary);
            mLostSummary = (TextView) include.findViewById(R.id.item_summary);
            mLostBtnTv = (TextView) include.findViewById(R.id.item_btn_tv);
            mLostBtnDiv = include.findViewById(R.id.item_btn_divider);
            mLostBtnLt = (RippleView1) include.findViewById(R.id.item_btn_rv);
            mLostBtnLt.setOnClickListener(this);
//            mLostBtnLt.setOnRippleCompleteListener(this);

            String text = getActivity().getString(R.string.pri_pro_btn, manager.getMaxScore());
            mLostBtnTv.setText(text);

            mLostSummary.setText(R.string.pri_pro_lost_summary);
        }
    }

    private void initIntruderLayout(View view) {
        IntrudeSecurityManager manager = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        View include = view.findViewById(R.id.intruder_security);
        mIntruderFixedLt = include.findViewById(R.id.item_middle_fixed_rl);
        mIntruderMiddleLt = include.findViewById(R.id.item_middle_ll);
        mIntruderFixedTv = (TextView) include.findViewById(R.id.fixed_summary);
        mIntruderSummary = (TextView) include.findViewById(R.id.item_summary);
        mIntruderBtnTv = (TextView) include.findViewById(R.id.item_btn_tv);
        mIntruderBtnDiv = include.findViewById(R.id.item_btn_divider);
        mIntruderBtnLt = (RippleView1) include.findViewById(R.id.item_btn_rv);
        mIntruderBtnLt.setOnClickListener(this);
//        mIntruderBtnLt.setOnRippleCompleteListener(this);

        String text = getActivity().getString(R.string.pri_pro_btn, manager.getMaxScore());
        mIntruderBtnTv.setText(text);

        mIntruderSummary.setText(R.string.pri_pro_intruder_summary);
    }

    private void initWifiLayout(View view) {
        View include = view.findViewById(R.id.wifi_security);
        View warn = include.findViewById(R.id.item_summay_sub_ll);
        warn.setVisibility(View.VISIBLE);
        mWifiSummary = (TextView) include.findViewById(R.id.item_summary);
        mWifiSubSummary = (TextView) include.findViewById(R.id.item_summary_subject);
        mWifiBtnTv = (TextView) include.findViewById(R.id.item_btn_tv);
        mWifiBtnDiv = include.findViewById(R.id.item_btn_divider);
        mWifiBtnLt = (RippleView1) include.findViewById(R.id.item_btn_rv);
        mWifiBtnLt.setOnClickListener(this);
//        mWifiBtnLt.setOnRippleCompleteListener(this);

        mWifiSummaryImg = (ImageView) include.findViewById(R.id.item_summary_iv);
        mWifiMiddleLt = include.findViewById(R.id.item_middle_ll);
        mWifiFixedLt = include.findViewById(R.id.item_middle_fixed_rl);

        mWifiFixedTitle = (TextView) include.findViewById(R.id.fixed_title);
        mWifiFixedSummaryLt = include.findViewById(R.id.fixed_summary_ll);

        String text = getActivity().getString(R.string.pri_pro_wifi_btn);
        mWifiBtnTv.setText(text);
    }

    private void initSwiftyLayout(View view) {
        View include = view.findViewById(R.id.swifty_security);
        mSwiftyImg = (ImageView) include.findViewById(R.id.swifty_img);
        mSwiftyContent = (TextView) include.findViewById(R.id.swifty_content);
        mSwiftyBtnLt = (RippleView1) include.findViewById(R.id.item_btn_rv);
        mSwiftyBtnLt.setOnClickListener(this);
//        mSwiftyBtnLt.setOnRippleCompleteListener(this);

        PreferenceTable preferenceTable = PreferenceTable.getInstance();

        if ((preferenceTable.getString(PrefConst.KEY_SWIFTY_CONTENT) != null &&
                preferenceTable.getString(PrefConst.KEY_SWIFTY_CONTENT).length() > 0) &&
                (preferenceTable.getString(PrefConst.KEY_SWIFTY_IMG_URL) != null &&
                        preferenceTable.getString(PrefConst.KEY_SWIFTY_IMG_URL).length() > 0) &&
                (preferenceTable.getString(PrefConst.KEY_SWIFTY_TYPE) != null &&
                        preferenceTable.getString(PrefConst.KEY_SWIFTY_TYPE).length() > 0) &&
                NetWorkUtil.isNetworkAvailable(AppMasterApplication.getInstance())) {

            mSwiftyContent.setText(preferenceTable.getString(PrefConst.KEY_SWIFTY_CONTENT));
            String imgUrl = preferenceTable.getString(PrefConst.KEY_SWIFTY_IMG_URL);
            mImageLoader.displayImage(imgUrl, mSwiftyImg, getSwiftyOptions());
            include.setVisibility(View.VISIBLE);

        } else {
            include.setVisibility(View.GONE);
        }

    }

    public DisplayImageOptions getSwiftyOptions() {  //需要提供默认图
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.swifty_banner)
                .showImageForEmptyUri(R.drawable.swifty_banner)
                .showImageOnFail(R.drawable.swifty_banner)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();

        return options;
    }

    private void initFbLayout(View view) {
        View include = view.findViewById(R.id.fb_security);
        mFbBtnLt = (RippleView1) include.findViewById(R.id.item_btn_rv);
        mFbBtnLt.setOnClickListener(this);
//        mFbBtnLt.setOnRippleCompleteListener(this);
    }

    private void initGradeLayout(View view) {
        int score = PrivacyHelper.getInstance(mActivity).getSecurityScore();
        LeoLog.i("loadSwiftySecurity", "score：" + score);
        View highInclude = view.findViewById(R.id.grade_high_security);
        View include = view.findViewById(R.id.grade_security);
        if (score == 100) {  // 等于100分

            mHighOneStar = (ImageView) highInclude.findViewById(R.id.one_star);
            mHighTwoStar = (ImageView) highInclude.findViewById(R.id.two_star);
            mHighThreeStar = (ImageView) highInclude.findViewById(R.id.three_star);
            mHighFourStar = (ImageView) highInclude.findViewById(R.id.four_star);
            mHighFiveStar = (ImageView) highInclude.findViewById(R.id.five_star);
            mHighGradeGesture = (ImageView) highInclude.findViewById(R.id.grade_gesture);
            mHighGrageFrame = (RelativeLayout) highInclude.findViewById(R.id.grade_frame);
            mHighGradeBtnLt = (RippleView1) highInclude.findViewById(R.id.item_btn_rv);
            mHighFiveEmptyStar = (ImageView) highInclude.findViewById(R.id.five_star_empty);
            mHighGradeBtnLt.setOnClickListener(this);
//            mHighGradeBtnLt.setOnRippleCompleteListener(this);

//            FrameLayout.LayoutParams highFrameParams =
//                    (FrameLayout.LayoutParams)mHighGrageFrame.getLayoutParams();
//
//            highFrameParams.height = (int)getLayoutHeight();

            highInclude.setVisibility(View.VISIBLE);
            include.setVisibility(View.GONE);

            showStarAnimation(mHighOneStar, mHighTwoStar, mHighThreeStar,
                    mHighFourStar, mHighFiveStar ,mHighFiveEmptyStar, mHighGradeGesture);

        } else {

            mOneStar = (ImageView) include.findViewById(R.id.one_star);
            mTwoStar = (ImageView) include.findViewById(R.id.two_star);
            mThreeStar = (ImageView) include.findViewById(R.id.three_star);
            mFourStar = (ImageView) include.findViewById(R.id.four_star);
            mFiveStar = (ImageView) include.findViewById(R.id.five_star);
            mGradeGesture = (ImageView) include.findViewById(R.id.grade_gesture);
            mGrageFrame = (RelativeLayout) include.findViewById(R.id.grade_frame);
            mGradeBtnLt = (RippleView1) include.findViewById(R.id.item_btn_rv);
            mFiveEmptyStar = (ImageView) include.findViewById(R.id.five_star_empty);
            mGradeBtnLt.setOnClickListener(this);
//            mGradeBtnLt.setOnRippleCompleteListener(this);

//            FrameLayout.LayoutParams frameParams =
//                    (FrameLayout.LayoutParams)mGrageFrame.getLayoutParams();
//
//            frameParams.height = (int)getLayoutHeight();

            highInclude.setVisibility(View.GONE);
            include.setVisibility(View.VISIBLE);

            showStarAnimation(mOneStar, mTwoStar, mThreeStar, mFourStar,
                    mFiveStar ,mFiveEmptyStar, mGradeGesture);
        }

    }

    private double getLayoutHeight() {
        double height = (Utilities.getScreenSize(mActivity)[0] -
                DipPixelUtil.dip2px(mActivity, 38)) / 0.52;
        LeoLog.i("loadSwiftySecurity", "setLayoutHeight:" + height);
        return height;


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
                mAnimatorSet.start();
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
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "finish");
            mActivity.onBackPressed();
        } else if (mContactBtnLt == v) {
            final PrivacyContactManager pcm = (PrivacyContactManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
            if (!mSelectData.isEmpty()) {
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "contact_enable");
                setAddPrivacyText(true);
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        mMessageCallBean = pcm.addPrivacyContact(mSelectData);
                        mAddedData.addAll(mSelectData);
                        mSelectData.clear();
                        onAddContactFinish();
                    }
                });
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.pri_contact_empty), Toast.LENGTH_SHORT).show();
            }
            ChangeContactColor();
        } else if (mWifiBtnLt == v) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "wifi_scan");
            Intent intent = new Intent(getActivity(), WifiSecurityActivity.class);
            startActivity(intent);
        } else if (mIntruderBtnLt == v) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "intruder_enable");
            Intent intent = new Intent(getActivity(), IntruderprotectionActivity.class);
            startActivity(intent);
        } else if (mFbBtnLt == v) {  // FaceBook分享
            lockManager.filterSelfOneMinites();
            goFaceBook();
        } else if (mHighGradeBtnLt == v) {
            lockManager.filterSelfOneMinites();
            Utilities.goFiveStar(mActivity);
        } else if (mLostBtnLt == v) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "theft_enable");
            Intent intent = new Intent(getActivity(), PhoneSecurityGuideActivity.class);
            startActivity(intent);
        } else if (mGradeBtnLt == v) { // 五星好评
            lockManager.filterSelfOneMinites();
            Utilities.goFiveStar(mActivity);
        } else if (mSwiftyBtnLt == v) {
            lockManager.filterSelfOneMinites();
            gotoGpOrBrowser();
        }
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

        String title = getResources().getString(R.string.privacy_contact_add_log_dialog_title);
        String content = getResources().getString(R.string.privacy_contact_add_log_dialog_dialog_content);
        mImportRecordDlg = new LEOAlarmDialog(getActivity());
        mImportRecordDlg.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "contact_import");
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            PrivacyContactManager pcm = (PrivacyContactManager)
                                    MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                            pcm.importCallAndMsms(mMessageCallBean);
                        }
                    });
                } else if (which == 0) {
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "contact_cancel");
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
