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

import com.leo.appmaster.R;
import com.leo.appmaster.callfilter.CallFilterMainActivity;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
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

    private TextView mWifiTitle;
    private TextView mWifiContent;
    private RippleView mWifiBtn;
    private ImageView mWifiImg;
    private View mWifiView;

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
    public void onClick(View v) {
        LockManager lockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        if (mWifiBtn == v) {
            if (mActivity != null) {
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "promote_wifi");
            }
            Intent intent = new Intent(mActivity, WifiSecurityActivity.class);
            startActivity(intent);
        } else if (mSwiftyBtnLt == v) {
            if (mActivity != null) {
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "promote_product");
            }
            lockManager.filterSelfOneMinites();
            PreferenceTable preferenceTable = PreferenceTable.getInstance();
            Utilities.selectType(preferenceTable, PrefConst.KEY_CLEAN_SWIFTY_TYPE,
                       PrefConst.KEY_CLEAN_SWIFTY_GP_URL, PrefConst.KEY_CLEAN_SWIFTY_URL,
                       "", mActivity);
        } else if (mCallFilterBtn == v) {
            if (mActivity != null) {
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "promote_block");
            }
            Intent intent = new Intent(mActivity, CallFilterMainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("needToHomeWhenFinish", true);
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
            mImageLoader.displayImage(imgUrl, mSwiftyImg, getOptions(R.drawable.online_theme_loading));
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
        View include = viewStub.inflate();
        mWifiBtn = (RippleView) include.findViewById(R.id.item_btn_rv);
        mWifiBtn.setOnClickListener(this);
        mWifiTitle = (TextView) include.findViewById(R.id.item_title);
        mWifiTitle.setText(mActivity.getResources().getString(
                R.string.batterymanage_result_ad_wifi_title));
        mWifiContent = (TextView) include.findViewById(R.id.item_summary);
        mWifiContent.setText(mActivity.getResources().getString(
                R.string.batterymanage_result_ad_wifi_content));
        mWifiImg = (ImageView) include.findViewById(R.id.item_image);
        mWifiImg.setImageResource(R.drawable.ic_pri_wifi_security);
        mWifiView = (View) include.findViewById(R.id.item_label);
        mWifiView.setBackgroundColor(mActivity.getResources().getColor(R.color.security_wifi));

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
