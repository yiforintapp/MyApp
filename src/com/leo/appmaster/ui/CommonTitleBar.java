package com.leo.appmaster.ui;

import com.leo.appmaster.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonTitleBar extends FrameLayout {

	private View mVgTitleandBack;
	private ImageView mIvBackArrow;
	private TextView mTvTitle;

	private TextView mTvOption;

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

		mVgTitleandBack = findViewById(R.id.layout_title_back);
		mIvBackArrow = (ImageView) findViewById(R.id.iv_back_arrow);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mTvOption = (TextView) findViewById(R.id.tv_option);

		super.onFinishInflate();
	}

	public void setBackViewListener(OnClickListener listener) {
		mVgTitleandBack.setOnClickListener(listener);
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

	
	public void setOptionVisibility(int visibility) {
		mTvOption.setVisibility(visibility);
	}
	
	public void setOptionBackground(int resId) {
		mTvOption.setBackgroundResource(resId);
	}

	public void setOptionText(String text) {
		mTvOption.setText(text);
	}

	public void setOptionListener(OnClickListener listener) {
		mTvOption.setOnClickListener(listener);
	}

}
