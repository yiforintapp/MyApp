package com.leo.appmaster.home;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
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
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.mobvista.sdk.m.core.entity.Campaign;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jasper on 2015/10/18.
 */
public class PrivacyConfirmFragment extends Fragment implements RippleView.OnRippleCompleteListener,
        View.OnClickListener {

    private static final String KEY_SHOW_CONTACT = "SHOW_CONTACT";
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
    private ImageView mWifiSummaryImg;
    private View mWifiFixedLt;
    private View mWifiMiddleLt;
    private TextView mWifiFixedTitle;
    private View mWifiFixedSummaryLt;

    private TextView mContactBtnTv;
    private RippleView mContactBtnLt;
    private View mContactBtnDiv;
    private LinearLayout mContactContainor;
    private ImageView mContactArrowIv;

    private CheckBox mSelectAllCb;
    private List<View> mContactViews;
    private List<ContactBean> mSelectData;
    private List<ContactBean> mAddedData;
    private HashMap<CheckBox, ContactBean> mDataMap;

    private RippleView mIgnoreBtn;
    private RippleView mProcessBtn;
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

        mProcessBtn = (RippleView) view.findViewById(R.id.pp_process_rv);
        mIgnoreBtn = (RippleView) view.findViewById(R.id.pp_process_ignore_rv);
        mProcessTv = (TextView) view.findViewById(R.id.pp_process_tv);

        mProcessBtn.setOnRippleCompleteListener(this);
        mIgnoreBtn.setVisibility(View.GONE);
        mProcessTv.setText(R.string.pri_pro_complete);
        mProcessBtn.setBackgroundResource(R.drawable.green_radius_btn_shape);

        mHeadView = (TextView) view.findViewById(R.id.pri_con_header);

        initLostLayout(view);
        initIntruderLayout(view);
        initWifiLayout(view);

        initContactLayout(view);

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

        public AdPreviewLoaderListener (PrivacyConfirmFragment fragment, final Campaign campaign) {
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
        if (ism.getIntruderMode()) {
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

//        if (ism.getIntruderMode() && lsm.isUsePhoneSecurity()) {
//            mHeadView.setText(R.string.pri_pro_summary_confirm);
//        } else {
//            mHeadView.setText(R.string.pri_pro_summary_not_confirm);
//        }
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
//                if (wsm.getWifiState() == WifiSecurityManager.SAFE_WIFI) {
//                    mWifiSummaryImg.setImageResource(R.drawable.ic_pri_wifi_safety);
//                    mWifiSubSummary.setText(R.string.pri_pro_wifi_safe);
//                } else if (wsm.getWifiState() == WifiSecurityManager.NOT_SAFE) {
                mWifiSubSummary.setText(R.string.pri_pro_wifi_warn);
                mWifiFixedSummaryLt.setVisibility(View.GONE);
                mWifiBtnLt.setVisibility(View.VISIBLE);
                mWifiBtnDiv.setVisibility(View.VISIBLE);
//                }
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
                    /*if (mSelectData.size() + mAddedData.size() == mContactViews.size()) {
                        mSelectAllCb.setChecked(true);
                    } else {
                        mSelectAllCb.setChecked(false);
                    }*/
                }
            });
            if (i == 0) {
                mContactContainor.addView(v);
            }
            mContactViews.add(v);
            mDataMap.put(checkBox, contactBean);
        }

        mContactBtnTv = (TextView) include.findViewById(R.id.item_btn_tv);
        mContactBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
        mContactBtnDiv = include.findViewById(R.id.item_btn_divider);
        mContactBtnLt.setOnRippleCompleteListener(this);

        mContactBtnTv.setText(R.string.pri_pro_contact_btn);
        mImpTv = (TextView) include.findViewById(R.id.imp_item_btn_rv);
        mContactContainorDisable = (View) include.findViewById(R.id.contact_containor_disab);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privacy_confirm, container, false);
    }

    @Override
    public void onRippleComplete(RippleView rippleView) {
        if (rippleView == mLostBtnLt) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "theft_enable");
            Intent intent = new Intent(getActivity(), PhoneSecurityGuideActivity.class);
            startActivity(intent);
        } else if (mIntruderBtnLt == rippleView) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "intruder_enable");
            Intent intent = new Intent(getActivity(), IntruderprotectionActivity.class);
            startActivity(intent);
        } else if (mWifiBtnLt == rippleView) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "wifi_scan");
            Intent intent = new Intent(getActivity(), WifiSecurityActivity.class);
            startActivity(intent);
        } else if (mContactBtnLt == rippleView) {
            final PrivacyContactManager pcm = (PrivacyContactManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
            if (!mSelectData.isEmpty()) {
                SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "contact_enable");
                setAddPrivacyText(true);
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
//                        for (ContactBean contactBean : mSelectData) {
//                            pcm.addPrivacyContact(contactBean);
//                        }
                        mMessageCallBean = pcm.addPrivacyContact(mSelectData);
                        mAddedData.addAll(mSelectData);
                        mSelectData.clear();
                        onAddContactFinish();
                    }
                });
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.pri_contact_empty), Toast.LENGTH_SHORT).show();
            }
        } else if (mProcessBtn == rippleView) {
            SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "proposals", "finish");
            mActivity.onBackPressed();
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
            mLostBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
            mLostBtnLt.setOnRippleCompleteListener(this);

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
        mIntruderBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
        mIntruderBtnLt.setOnRippleCompleteListener(this);

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
        mWifiBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
        mWifiBtnLt.setOnRippleCompleteListener(this);

        mWifiSummaryImg = (ImageView) include.findViewById(R.id.item_summary_iv);
        mWifiMiddleLt = include.findViewById(R.id.item_middle_ll);
        mWifiFixedLt = include.findViewById(R.id.item_middle_fixed_rl);

        mWifiFixedTitle = (TextView) include.findViewById(R.id.fixed_title);
        mWifiFixedSummaryLt = include.findViewById(R.id.fixed_summary_ll);

        String text = getActivity().getString(R.string.pri_pro_wifi_btn);
        mWifiBtnTv.setText(text);
    }

    @Override
    public void onClick(View v) {
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
        mImportRecordDlg.show();
    }
}
