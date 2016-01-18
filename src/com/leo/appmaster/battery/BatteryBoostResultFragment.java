package com.leo.appmaster.battery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.callfilter.CallFilterMainActivity;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.WifiSecurityManager;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.ImageScaleType;

public class BatteryBoostResultFragment extends Fragment implements View.OnClickListener {
    private Activity mActivity;

    private ImageLoader mImageLoader;

    private TextView mSwiftyTitle;
    private ImageView mSwiftyImg;
    private TextView mSwiftyContent;
    private RippleView mSwiftyBtnLt;

    private TextView mWifiSummary;
    private TextView mWifiSubSummary;
    private TextView mWifiBtnTv;
    private View mWifiBtnDiv;
    private RippleView mWifiBtnLt;
    private View mWifiFixedLt;
    private View mWifiMiddleLt;
    private TextView mWifiFixedTitle;
    private View mWifiFixedSummaryLt;
    private View mWIfiInclude;

    private RippleView mCallFilterBtn;

    @Override
    public void onViewCreated(final View view, @Nullable
    Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initSwiftyLayout(view);
        initWifiLayout(view);
        initCallFilterLayout(view);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
    ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        return inflater.inflate(R.layout.battery_result_fragment, container, false);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
           updateStubPanelVisibility();
        } catch (Exception e) {
        }
    }

    @Override
    public void onClick(View v) {
        LockManager lockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        if (mWifiBtnLt == v) {
            Intent intent = new Intent(mActivity, WifiSecurityActivity.class);
            startActivity(intent);
        } else if (mSwiftyBtnLt == v) {
            lockManager.filterSelfOneMinites();
            boolean installISwipe = ISwipUpdateRequestManager.isInstallIsiwpe(mActivity);
            if (installISwipe) {
                Utilities.startISwipIntent(mActivity);
            } else {
                PreferenceTable preferenceTable = PreferenceTable.getInstance();
                Utilities.selectType(preferenceTable, PrefConst.KEY_CLEAN_SWIFTY_TYPE,
                        PrefConst.KEY_CLEAN_SWIFTY_GP_URL, PrefConst.KEY_CLEAN_SWIFTY_URL,
                        Constants.ISWIPE_PACKAGE, mActivity);
            }
        } else if (mCallFilterBtn == v) {
            Intent intent = new Intent(mActivity, CallFilterMainActivity.class);
            startActivity(intent);
        }

    }

    private void initSwiftyLayout(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.swifty_security_stub);
        if (viewStub == null) {
            return;
        }

        PreferenceTable preferenceTable = PreferenceTable.getInstance();

        boolean isContentEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CLEAN_SWIFTY_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CLEAN_SWIFTY_IMG_URL));

        boolean isTypeEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CLEAN_SWIFTY_TYPE));

        boolean isGpUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CLEAN_SWIFTY_GP_URL));

        boolean isBrowserUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CLEAN_SWIFTY_URL));

        boolean isUrlEmpty = isGpUrlEmpty && isBrowserUrlEmpty; //判断两个地址是否都为空

        if (!isContentEmpty && !isImgUrlEmpty && !isTypeEmpty && !isUrlEmpty) {
            View include = viewStub.inflate();
            mSwiftyTitle = (TextView) include.findViewById(R.id.item_title);
            mSwiftyImg = (ImageView) include.findViewById(R.id.swifty_img);
            mSwiftyContent = (TextView) include.findViewById(R.id.swifty_content);
            mSwiftyBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
            mSwiftyBtnLt.setOnClickListener(this);
            mSwiftyContent.setText(preferenceTable.getString(PrefConst.KEY_CLEAN_SWIFTY_CONTENT));
            String imgUrl = preferenceTable.getString(PrefConst.KEY_CLEAN_SWIFTY_IMG_URL);
            mImageLoader.displayImage(imgUrl, mSwiftyImg, getOptions(R.drawable.swifty_banner));
            boolean isTitleEmpty = TextUtils.isEmpty(
                    preferenceTable.getString(PrefConst.KEY_CLEAN_SWIFTY_TITLE));
            if (!isTitleEmpty) {
                mSwiftyTitle.setText(preferenceTable.getString(
                        PrefConst.KEY_CLEAN_SWIFTY_TITLE));
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

    private void initWifiLayout(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.wifi_security_stub);
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

        mWifiMiddleLt = mWIfiInclude.findViewById(R.id.item_middle_ll);
        mWifiFixedLt = mWIfiInclude.findViewById(R.id.item_middle_fixed_rl);

        mWifiFixedTitle = (TextView) mWIfiInclude.findViewById(R.id.fixed_title);
        mWifiFixedSummaryLt = mWIfiInclude.findViewById(R.id.fixed_summary_ll);

        String text = mActivity.getString(R.string.pri_pro_wifi_btn);
        mWifiBtnTv.setText(text);
    }

    private void updateStubPanelVisibility() {

        if (mWIfiInclude == null) {
            return;
        }
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
                mWifiSubSummary.setText(R.string.pri_pro_wifi_warn);
                mWifiFixedSummaryLt.setVisibility(View.GONE);
                mWifiBtnLt.setVisibility(View.VISIBLE);
                mWifiBtnDiv.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initCallFilterLayout(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.wifi_call_filter_stub);
        if (viewStub == null) {
            return;
        }
        View include = viewStub.inflate();
        mCallFilterBtn = (RippleView) include.findViewById(R.id.item_btn_rv);
        mCallFilterBtn.setOnClickListener(this);
    }
}
