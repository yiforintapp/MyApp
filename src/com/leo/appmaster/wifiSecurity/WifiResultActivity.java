package com.leo.appmaster.wifiSecurity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.home.HomeColor;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView1;
import com.leo.appmaster.ui.dialog.OneButtonDialog;

/**
 * Created by qili on 15-10-21.
 */
public class WifiResultActivity extends BaseActivity implements View.OnClickListener {
    private TextView bigText, smallText;
    private boolean isConnect, oneState, twoState, threeState, fourState, isSafe;
    private CommonToolbar mTitleBar;
    private ImageView bigViewIcon;
    private View safeView, unsafeView, mTopViewBackground, unsafeBottomView;
    private ImageView mOneLoad, mTwoLoad, mThreeLoad, mFourLoad;
    private OneButtonDialog selectWifiDialog;
    private RippleView1 mProcessBtn, mOtherWifiBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_result);

        handleIntent();
        initUI();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        isConnect = intent.getBooleanExtra("wifi_state", false);
        isSafe = intent.getBooleanExtra("isSafe", false);
        if (!isSafe) {
            oneState = intent.getBooleanExtra("one", false);
            twoState = intent.getBooleanExtra("two", false);
            threeState = intent.getBooleanExtra("three", false);
            fourState = intent.getBooleanExtra("four", false);
        }

        if (!isConnect) {
            showSelectWifiDialog(getString(R.string.can_not_connect_wifi));
        }
    }

    private void showSelectWifiDialog(String text) {
        if (selectWifiDialog == null) {
            selectWifiDialog = new OneButtonDialog(this);
        }

        if (!selectWifiDialog.isShowing()) {
            selectWifiDialog.setOnClickListener(new OneButtonDialog.OnWifiDiaogClickListener() {
                @Override
                public void onClick() {
                    mLockManager.filterSelfOneMinites();
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                }
            });
            selectWifiDialog.setText(text);
            selectWifiDialog.show();
        }
    }

    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.wifi_result_title_bar);
        mTitleBar.setToolbarTitle(R.string.wifi_titlebar_name);
        mTitleBar.setOptionMenuVisible(false);

        mTopViewBackground = findViewById(R.id.top_content);
        unsafeBottomView = findViewById(R.id.wifi_below_view_recomment);

        bigText = (TextView) findViewById(R.id.wifi_big_text);
        smallText = (TextView) findViewById(R.id.wifi_sma_text);
        bigViewIcon = (ImageView) findViewById(R.id.wifi_big_icon);

        safeView = findViewById(R.id.bottom_content);
        unsafeView = findViewById(R.id.unsafe_result_content);

        if (!isSafe) {
            bigText.setText(getString(R.string.wifi_scan_result_no_safe));
            smallText.setText(getString(R.string.wifi_scan_result_no_safe_advice));
            smallText.setVisibility(View.VISIBLE);
            bigViewIcon.setImageResource(R.drawable.wifiunsafety);

            mTopViewBackground.setBackgroundColor(getResources().getColor(R.color.wifi_error_red));
            mTitleBar.setToolbarColorResource(R.color.wifi_error_red);

            safeView.setVisibility(View.GONE);
            unsafeView.setVisibility(View.VISIBLE);
            unsafeBottomView.setVisibility(View.VISIBLE);

            mOneLoad = (ImageView) unsafeView.findViewById(R.id.unsafe_wifi_is_connect_icon);
            mTwoLoad = (ImageView) unsafeView.findViewById(R.id.unsafe_wifi_second_connect_icon);
            mThreeLoad = (ImageView) unsafeView.findViewById(R.id.unsafe_wifi_ssl_icon);
            mFourLoad = (ImageView) unsafeView.findViewById(R.id.unsafe_wifi_pas_type_icon);

            setImageState();
        } else {

            mTopViewBackground.setBackgroundColor(getResources().getColor(R.color.cb));
            mTitleBar.setToolbarColorResource(R.color.cb);

            bigText.setText(getString(R.string.wifi_scan_result_safe));
            bigViewIcon.setImageResource(R.drawable.wifisafety);
            safeView.setVisibility(View.VISIBLE);
            unsafeView.setVisibility(View.GONE);
        }


        mProcessBtn = (RippleView1) findViewById(R.id.wifi_resulte_sure);
        mProcessBtn.setOnClickListener(this);
        mOtherWifiBtn = (RippleView1) findViewById(R.id.wifi_resulte_other_wifi);
        mOtherWifiBtn.setOnClickListener(this);
    }

    public Pair<Integer, Integer> getColorPairByScore(int score) {
        Pair<Integer, Integer> pair = null;
        if (score == 1) {
            pair = new Pair<Integer, Integer>(HomeColor.UNSAFE_PAGE_START, HomeColor.UNSAFE_PAGE_END);
        } else {
            pair = new Pair<Integer, Integer>(HomeColor.SAFE_PAGE_START, HomeColor.SAFE_PAGE_END);
        }
        return pair;
    }

    private void setImageState() {
        setImage(mOneLoad, oneState);
        setImage(mTwoLoad, twoState);
        setImage(mThreeLoad, threeState);
        setImage(mFourLoad, fourState);
    }

    private void setImage(ImageView view, boolean flag) {
        if (flag) {
            view.setImageResource(R.drawable.wifi_complete);
        } else {
            view.setImageResource(R.drawable.wifi_error);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wifi_result_sure_text:
                mLockManager.filterSelfOneMinites();
                Intent intent = new Intent(WifiResultActivity.this, FlowActivity.class);
                WifiResultActivity.this.startActivity(intent);
                break;
            case R.id.wifi_recomment_lock:
                mLockManager.filterSelfOneMinites();
                Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                try {
                    startActivity(wifiIntent);
                } catch (Exception e) {
                }
                break;
        }
    }
}
