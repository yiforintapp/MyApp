package com.leo.appmaster.appmanage;

import com.leo.appmaster.R;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.utils.ProcessUtils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class AppInfoBaseLayout extends LinearLayout implements OnClickListener {

	ImageView mIvIcon;
	TextView mBtnStop;
	TextView mBtnUninstall;
	TextView mTvAppName;

	AppDetailInfo mAppInfo;

	public AppInfoBaseLayout(Context context) {
		super(context);
	}

	public AppInfoBaseLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		LayoutInflater.from(getContext()).inflate(R.layout.app_use_info, this,
				true);
		initUI();
		super.onFinishInflate();
	}

	private void initUI() {
		mTvAppName = (TextView) findViewById(R.id.tv_app_name);
		mIvIcon = (ImageView) findViewById(R.id.iv_app_icon);
		mBtnStop = (TextView) findViewById(R.id.btn_stop);
		mBtnUninstall = (TextView) findViewById(R.id.btn_uninstall);
		mBtnStop.setOnClickListener(this);
		mBtnUninstall.setOnClickListener(this);
	}

	public void setAppDetailInfo(AppDetailInfo info) {
		mAppInfo = info;
		inflateUI();
	}

	private void inflateUI() {
		mIvIcon.setImageDrawable(mAppInfo.icon);
		mTvAppName.setText(mAppInfo.label);

		if (ProcessUtils.isAppRunning((ActivityManager) getContext()
				.getSystemService(Context.ACTIVITY_SERVICE), mAppInfo.packageName)) {
			mBtnStop.setEnabled(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_stop:

			break;
		case R.id.btn_uninstall:

			break;

		default:
			break;
		}
	}

}
