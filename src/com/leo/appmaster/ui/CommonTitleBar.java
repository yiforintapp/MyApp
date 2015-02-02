
package com.leo.appmaster.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.home.HomeActivity;

public class CommonTitleBar extends FrameLayout implements OnClickListener {

    private ImageView mIvBackArrow;
    private TextView mTvTitle;
    private TextView mTvLayoutRight;
    private TextView mTvOptionText;
    private ImageView mTvOptionImage;
    private ImageView mTvLogo;
    private ImageView mHelpSetting;
    private RelativeLayout mHelpSettingParent;
    private View mLayoutBackView, mLayoutSpiner;

    public CommonTitleBar(Context context) {
        this(context, null);
    }

    public CommonTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.common_title_bar, this, true);
        mLayoutBackView = findViewById(R.id.layout_title_back);
        mIvBackArrow = (ImageView) findViewById(R.id.iv_back_arrow);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvLayoutRight = (TextView) findViewById(R.id.tv_layout_right);
        mTvOptionText = (TextView) findViewById(R.id.tv_option_text);
        mTvOptionImage = (ImageView) findViewById(R.id.tv_option_image);
        mLayoutSpiner = findViewById(R.id.layout_right);
        mTvLogo = (ImageView) findViewById(R.id.iv_logo);
        mHelpSetting = (ImageView) findViewById(R.id.setting_help_iv);
        mHelpSettingParent = (RelativeLayout) findViewById(R.id.setting_help_tip);
        super.onFinishInflate();
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    public void setTitle(int resid) {
        mTvTitle.setText(resid);
    }

    public void setSpinerText(String text) {
        mTvLayoutRight.setText(text);
    }

    public void setSpinerText(int resid) {
        mTvLayoutRight.setText(resid);
    }

    public void setSpinerListener(OnClickListener listener) {
        mLayoutSpiner.setOnClickListener(listener);
    }

    public void setSpinerVibility(int visibility) {
        mLayoutSpiner.setVisibility(visibility);
    }

    public void setBackArrowVisibility(int visibility) {
        mIvBackArrow.setVisibility(visibility);
    }

    public void setOptionTextVisibility(int visibility) {
        mTvOptionText.setVisibility(visibility);
    }

    public void setOptionImageVisibility(int visibility) {
        mTvOptionImage.setVisibility(visibility);
    }

    public void setOptionBackground(int resId) {
        mTvOptionText.setBackgroundResource(resId);
    }

    public void setOptionText(String text) {
        mTvOptionText.setText(text);
    }

    public void setOptionListener(OnClickListener listener) {
        mTvOptionText.setOnClickListener(listener);
        mTvOptionImage.setOnClickListener(listener);
    }

    public void openBackView() {
        mLayoutBackView.setOnClickListener(this);
    }

    public void toHomeView(final Context context) {
        mLayoutBackView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                try {
                    context.startActivity(intent);
                    ((Activity) getContext()).finish();
                } catch (Exception e) {
                }

            }
        });
    }

    public void setBackViewListener(OnClickListener listener) {
        mLayoutBackView.setOnClickListener(listener);
    }

    public void setOptionImage(int resID) {
        mTvOptionImage.setImageResource(resID);
    }

    public void setHelpSettingImage(int resId) {
        mHelpSetting.setImageResource(resId);
    }

    public void setHelpSettingVisiblity(int visibility) {
        mHelpSetting.setVisibility(visibility);
    }

    public void setHelpSettingListener(OnClickListener listener) {
        mHelpSettingParent.setOnClickListener(listener);
    }

    public void setHelpSettingParentListener(OnClickListener listener) {
        mHelpSettingParent.setOnClickListener(listener);
    }

    public void showLogo() {
        mTvLogo.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (mLayoutBackView == v) {
            ((Activity) getContext()).finish();
        }

    }
}
