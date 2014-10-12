
package com.leo.appmaster.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;

public class CommonTitleBar extends FrameLayout implements OnClickListener {

    private ImageView mIvBackArrow;
    private TextView mTvTitle;

    private TextView mTvOptionText;
    private ImageView mTvOptionImage;

    private View mLayoutBackView;

    public CommonTitleBar(Context context) {
        super(context);
    }

    public CommonTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.common_title_bar, this, true);
        mLayoutBackView = findViewById(R.id.layout_title_back);
        mIvBackArrow = (ImageView) findViewById(R.id.iv_back_arrow);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvOptionText = (TextView) findViewById(R.id.tv_option_text);
        mTvOptionImage = (ImageView) findViewById(R.id.tv_option_image);

        super.onFinishInflate();
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    public void setTitle(int resid) {
        mTvTitle.setText(resid);
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

    public void setOptionImage(int resID) {
        mTvOptionText.setBackgroundResource(resID);
    }

    @Override
    public void onClick(View v) {
        if (mLayoutBackView == v) {
            ((Activity) getContext()).finish();
        }

    }

}
