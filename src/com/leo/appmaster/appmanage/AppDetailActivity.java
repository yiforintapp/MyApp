package com.leo.appmaster.appmanage;

import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.ui.CommonTitleBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class AppDetailActivity extends Activity implements
		OnPageChangeListener, OnClickListener {

	public static final String EXTRA_LOAD_PKG = "load_pkg_name";

	TextView mTvUseInfo;
	TextView mTvPermissionInfo;
	ViewPager mViewPager;
	ImageView mIvIcon;

	AppInfoBaseLayout mPager1;
	AppInfoBaseLayout mPager2;

	private AppDetailInfo mAppInfo;
	
	private CommonTitleBar mTtileBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_detail);
		handleIntent();
		initUI();
	}

	private void handleIntent() {
		Intent intent = this.getIntent();
		String pkg = intent.getStringExtra(EXTRA_LOAD_PKG);
		mAppInfo = AppLoadEngine.getInstance().loadAppDetailInfo(pkg);
	}

	private void initUI() {
		mTvUseInfo = (TextView) findViewById(R.id.tv_app_use_info);
		mTvPermissionInfo = (TextView) findViewById(R.id.tv_app_permission_info);
		mViewPager = (ViewPager) findViewById(R.id.pager_app_detail);

		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_manage);
		mTtileBar.setOptionVisibility(View.INVISIBLE);
		mTtileBar.setBackViewListener(this);
		
		mPager1 = (AppInfoBaseLayout) this.getLayoutInflater().inflate(
				R.layout.pager_app_use_info, null);
		mPager2 = (AppInfoBaseLayout) this.getLayoutInflater().inflate(
				R.layout.pager_app_use_info, null);
		mPager1.setAppDetailInfo(mAppInfo);
		mPager2.setAppDetailInfo(mAppInfo);
		mViewPager.setAdapter(new AppdetailAdapter());
		mViewPager.setOnPageChangeListener(this);

	}

	private class AppdetailAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;
			if (position == 0) {
				view = mPager1;
			} else {
				view = mPager2;
			}
			container.addView(view, 0);
			return view;
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_title_bar:
			finish();
			break;

		default:
			break;
		}
		
	}

}
