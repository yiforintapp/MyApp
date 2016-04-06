
package com.leo.appmaster.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;


public class CommonTitleBar extends FrameLayout implements OnClickListener {

    private ImageView mIvBackArrow;
    private TextView mTvTitle;
    private RelativeLayout mTvTitleContainer;
    private TextView mTvOptionText;
    private View mTvOptionImageClick;
    private ImageView mTvOptionImage;

    private ImageView mTvLogo;
    private ImageView mHelpSetting;
    private RelativeLayout mHelpSettingParent;
    private View  mLayoutSpiner;
    private View viewOldLeft, viewNewLeft;
    private View mNewClickArea;
    private TextView mNewText;
    private View newBackView;
    private OnClickListener mListener;

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


        mIvBackArrow = (ImageView) findViewById(R.id.iv_back_arrow);
        newBackView = findViewById(R.id.layout_title_back_arraow);
        newBackView.setOnClickListener(this);
//        if (newBackView instanceof RippleView) {
//            ((RippleView) newBackView).setOnRippleCompleteListener(this);
//        } else {
//            newBackView.setOnClickListener(this);
//        }

        mTvTitleContainer = (RelativeLayout) findViewById(R.id.title_container);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
//        mTvSpinner = (TextView) findViewById(R.id.tv_layout_right);
//        mImgSpinner = (ImageView) findViewById(R.id.img_layout_right);
        mTvOptionText = (TextView) findViewById(R.id.tv_option_text);

        mTvOptionImage = (ImageView) findViewById(R.id.tv_option_image);
        mTvOptionImageClick = findViewById(R.id.tv_option_image_content);

        mLayoutSpiner = findViewById(R.id.layout_right);
        mTvLogo = (ImageView) findViewById(R.id.iv_logo);
        mHelpSetting = (ImageView) findViewById(R.id.setting_help_iv);
        mHelpSettingParent = (RelativeLayout) findViewById(R.id.setting_help_tip);

        viewOldLeft = findViewById(R.id.left_content);
        viewNewLeft = findViewById(R.id.new_style_content);

        mNewClickArea = findViewById(R.id.ct_back_rl);
        mNewClickArea.setOnClickListener(this);
//        if (mNewClickArea instanceof RippleView) {
//            ((RippleView) mNewClickArea).setOnRippleCompleteListener(this);
//        } else {
//            mNewClickArea.setOnClickListener(this);
//        }

        mNewText = (TextView) findViewById(R.id.ct_title_tv);

        setBackgroundResource(R.color.ctc);
        super.onFinishInflate();
    }

    public void setNewStyle() {
        viewNewLeft.setVisibility(View.VISIBLE);
        viewOldLeft.setVisibility(View.GONE);
    }

    public void setTitleBarColorResource(int resource) {
        setBackgroundResource(resource);
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    public void setTitle(int resid) {
        mTvTitle.setText(resid);
    }

    public TextView getTitleView() {
        return mTvTitle;
    }

    public RelativeLayout getTitleContainer() {
        return mTvTitleContainer;
    }

    public void setOptionImagePadding(int padding) {
//        mTvOptionImage.setPadding(padding, padding, padding, padding);
        mTvOptionImageClick.setPadding(padding, padding, padding, padding);
    }

    public void setOptionImagePadding(int left, int top, int right, int bottom) {
//        mTvOptionImage.setPadding(left, top, right, bottom);
        mTvOptionImageClick.setPadding(left, top, right, bottom);
    }


    public void setSpinerListener(OnClickListener listener) {
        mLayoutSpiner.setOnClickListener(listener);
    }

    public void setSpinerVibility(int visibility) {
        mLayoutSpiner.setVisibility(visibility);
    }

    public void setBackArrowVisibility(int visibility) {
//        mIvBackArrow.setVisibility(visibility);
        newBackView.setVisibility(visibility);
    }

    public void setBackArrowImg(int imgID) {
        mIvBackArrow.setImageResource(imgID);
    }

    public void setBackArrawImgSize(int size) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mIvBackArrow.getLayoutParams();
        params.width = size;
        params.height = size;
        mIvBackArrow.setLayoutParams(params);
    }

    public void setOptionTextVisibility(int visibility) {
        mTvOptionText.setVisibility(visibility);
    }

    public void setOptionImageVisibility(int visibility) {
//        mTvOptionImage.setVisibility(visibility);
        mTvOptionImageClick.setVisibility(visibility);
    }

    public void setOptionBackground(int resId) {
        mTvOptionText.setBackgroundResource(resId);
    }

    public void setOptionImageBackground(int resId) {
        mTvOptionImage.setBackgroundResource(resId);
    }

    public void setOptionText(String text) {
        mTvOptionText.setText(text);
    }

    public void setOptionListener(OnClickListener listener) {
        mTvOptionText.setOnClickListener(listener);

//        mTvOptionImage.setOnClickListener(listener);
        mTvOptionImageClick.setOnClickListener(listener);
    }

    public void setOptionAnimation(Animation animation) {
        mTvOptionImage.startAnimation(animation);
    }

    private OnClickListener mBackViewListener;

    public void setSelfBackPressListener(OnClickListener listener) {
        mBackViewListener = listener;
    }

    public void openBackView() {
//        mLayoutBackView.setOnClickListener(this);
        newBackView.setOnClickListener(this);
    }

    public void setNewStyleBackViewListrener(OnClickListener listener) {
        mListener = listener;
//        mNewClickArea.setOnClickListener(listener);
    }

    public void setNewStyleText(int name) {
        mNewText.setText(name);
    }

    public void setBackViewListener(OnClickListener listener) {
//        mLayoutBackView.setOnClickListener(listener);
//        newBackView.setOnClickListener(this);
        mNewClickArea.setOnClickListener(this);
    }

    public void setOptionImage(int resID) {
        mTvOptionImage.setImageResource(resID);
    }

    public void setOptionIamgeEnabled(boolean disable) {
        mTvOptionImage.setEnabled(disable);
    }

    public void setHelpSettingImage(int resId) {
        mHelpSetting.setImageResource(resId);
    }

    public void setHelpSettingVisiblity(int visibility) {
        mHelpSettingParent.setVisibility(visibility);
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

    public View getOptionImageView() {
//        return mTvOptionImage;
        return mTvOptionImageClick;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ct_back_rl:
                if (mListener != null) {
                    mListener.onClick(v);
                } else {
                    Context context = getContext();
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }
                break;
            case R.id.layout_title_back_arraow:
                if (mBackViewListener != null) {
                    mBackViewListener.onClick(v);
                } else {
                    Context context = getContext();
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }
                break;
        }

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {

        }
    }

}
