
package com.leo.appmaster.home;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.BuildProperties;

public class HomeTitleBar extends FrameLayout implements OnClickListener {

    private ImageView mMenuImgView;
    private ImageView mTvOptionImage;
    private ImageView mIvLogo;
    private RelativeLayout mIvHotApp;

    public HomeTitleBar(Context context) {
        this(context, null);
    }

    public HomeTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.home_title_bar, this, true);
        mMenuImgView = (ImageView) findViewById(R.id.iv_menu);
        mTvOptionImage = (ImageView) findViewById(R.id.iv_option_image);
        mIvLogo = (ImageView) findViewById(R.id.iv_logo);
        mIvHotApp = (RelativeLayout) findViewById(R.id.bg_show_hotapp);
        if (BuildProperties.isZTEAndApiLevel14()) {
            mIvHotApp.setVisibility(View.GONE);
        }
        super.onFinishInflate();
    }

    public void setMenuClickListener(OnClickListener listener) {
        mMenuImgView.setOnClickListener(listener);
    }

    public void setOptionClickListener(OnClickListener listener) {
        mTvOptionImage.setOnClickListener(listener);
    }

    public void setHotAppClickListener(OnClickListener listener) {
        mIvHotApp.setOnClickListener(listener);
    }

    public ImageView getLogoImgView() {
        return mIvLogo;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {

        }
    }

}
