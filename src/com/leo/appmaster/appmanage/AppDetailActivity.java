package com.leo.appmaster.appmanage;

import java.util.ArrayList;
import java.util.Date;

import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.view.GestureLayout.IGestureListener;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.CustomViewPager;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.FastBlur;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AppDetailActivity extends BaseActivity implements
		OnPageChangeListener, OnClickListener, IGestureListener {

	public static final String EXTRA_LOAD_PKG = "load_pkg_name";

	private TextView mTvUseInfo;
	private TextView mTvPermissionInfo;
	private CustomViewPager mViewPager;
	private ImageView mIvIcon;

	private RelativeLayout mBatteryInfoLayout;
	// private BatteryChartsView mBatteryCharts;

	private CommonTitleBar mTitle;
	private LinearLayout mAppBaseInfoLayout;
	private ImageView mAppIcon;
	private TextView mAppName;
	private TextView mAppVersion;
	private TextView mAppMemory;
	private TextView mAppUsedFlow;
	// private GestureLayout mGestureLayout;

	private Button mUninstall;
	private Button mStop;

	private LinearLayout mPager1;
	private ScrollView mPager2;

	private Bitmap mAppBaseInfoLayoutbg;

	private AppItemInfo mAppInfo;
	private DeleteReceiver mDeleteReceiver;
	private Date mCurrdate;

	// private Calendar mCalendar = Calendar.getInstance();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_detail);
		handleIntent();
		resisterDeleteReceiver();
		initUI();
	}

	private void handleIntent() {
		Intent intent = this.getIntent();
		String pkg = intent.getStringExtra(EXTRA_LOAD_PKG);
		mAppInfo = AppLoadEngine.getInstance(this).loadAppDetailInfo(pkg);
	}

	private void resisterDeleteReceiver() {
		mDeleteReceiver = new DeleteReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addDataScheme("package");

		registerReceiver(mDeleteReceiver, filter);
	}

	private void unResisterDeleteReceiver() {
		unregisterReceiver(mDeleteReceiver);
	}

	private void initUI() {
		mTvUseInfo = (TextView) findViewById(R.id.tv_app_use_info);
		mTvPermissionInfo = (TextView) findViewById(R.id.tv_app_permission_info);
		mTvUseInfo.setOnClickListener(this);
		mTvPermissionInfo.setOnClickListener(this);

		mViewPager = (CustomViewPager) findViewById(R.id.pager_app_detail);
		mAppBaseInfoLayout = (LinearLayout) findViewById(R.id.app_base_info);
		mTitle = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTitle.setTitle(R.string.app_details);
		mTitle.openBackView();
		mTitle.setOptionTextVisibility(View.INVISIBLE);
		mAppIcon = (ImageView) findViewById(R.id.app_info_icon);
		mAppIcon.setImageDrawable(mAppInfo.icon);
		mAppName = (TextView) findViewById(R.id.app_name);
		mAppName.setText(mAppInfo.label);
		mAppVersion = (TextView) findViewById(R.id.version_name);
		mAppVersion.setText(mAppInfo.versionName);
		mAppMemory = (TextView) findViewById(R.id.app_memory);
		mAppMemory.setText(String.format(getString(R.string.app_memory),
				mAppInfo.cacheInfo.codeSize));
		mAppUsedFlow = (TextView) findViewById(R.id.app_cache_size);
		mAppUsedFlow.setText(String.format(getString(R.string.app_cache_size),
				mAppInfo.cacheInfo.cacheSize));

		mUninstall = (Button) findViewById(R.id.uninstall_app);
		mUninstall.setOnClickListener(this);
		mStop = (Button) findViewById(R.id.stop_app);
		mStop.setOnClickListener(this);

		mPager1 = (LinearLayout) this.getLayoutInflater().inflate(
				R.layout.activity_user_details, null);
		mPager2 = (ScrollView) this.getLayoutInflater().inflate(
				R.layout.app_permission_info, null);
		// mBatteryCharts = (BatteryChartsView)
		// mPager1.findViewById(R.id.battery_charts);
		// mBatteryCharts.setPowerPercent((float)mAppInfo.getPowerComsuPercent()
		// / 100);
		mViewPager.setAdapter(new AppdetailAdapter());
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setScanScroll(true);
		mViewPager.requestDisallowInterceptTouchEvent(true);
		mViewPager.setCurrentItem(0);
		setSelecteTab(0);
		// mGestureLayout = (GestureLayout)
		// mPager1.findViewById(R.id.gesture_layout);
		// mGestureLayout.setListener(this);
		setAppInfoBackground(mAppInfo.icon);
		//
		// mCurrdate = mCalendar.getTime();
		// setWeekString(mCurrdate);

		initPermisionInfo();
	}

	private void setAppInfoBackground(Drawable drawable) {
		float d = (float) 3 / 2/*
								 * mAppBaseInfoLayout.getWidth() /
								 * mAppBaseInfoLayout.getHeight()
								 */;

		int w = drawable.getIntrinsicWidth() * 9 / 10;
		int h = w * 2 / 3;
		mAppBaseInfoLayoutbg = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		// Bitmap tmp = Bitmap.createBitmap(
		// w,
		// h,
		// Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(mAppBaseInfoLayoutbg);
		canvas.drawColor(Color.WHITE);
		drawable.setBounds(-(drawable.getIntrinsicWidth() - w) / 2,
				-(drawable.getIntrinsicHeight() - h) / 2,
				(drawable.getIntrinsicWidth() - w) / 2 + w,
				(drawable.getIntrinsicHeight() - h) / 2 + h);
		drawable.draw(canvas);
		canvas.drawColor(Color.argb(60, 0, 0, 0));
		// NativeBlurMethod.doConvertBlur(tmp, mAppBaseInfoLayoutbg);
		mAppBaseInfoLayoutbg = FastBlur.doBlur(mAppBaseInfoLayoutbg, 5, true);
		mAppBaseInfoLayout.setBackgroundDrawable(new BitmapDrawable(
				mAppBaseInfoLayoutbg));
		// tmp.recycle();
		// tmp = null;

	}

	/*
	 * private void setWeekString(Date currDate) { String newMessageInfo;
	 * 
	 * TextView sun = (TextView) mPager1.findViewById(R.id.sunday); TextView mon
	 * = (TextView) mPager1.findViewById(R.id.monday); TextView tue = (TextView)
	 * mPager1.findViewById(R.id.tuesday); TextView wed = (TextView)
	 * mPager1.findViewById(R.id.wednesday); TextView thu = (TextView)
	 * mPager1.findViewById(R.id.thursday); TextView fri = (TextView)
	 * mPager1.findViewById(R.id.friday); TextView sat = (TextView)
	 * mPager1.findViewById(R.id.saturday);
	 * 
	 * SimpleDateFormat format = new SimpleDateFormat("dd/MM"); Calendar
	 * calendar = mCalendar; calendar.setTime(currDate); int dayIndex =
	 * calendar.get(Calendar.DAY_OF_WEEK); calendar.add(Calendar.DATE,
	 * -(dayIndex - 1)); Date date = calendar.getTime(); String text=
	 * format.format(date); dayIndex = calendar.get(Calendar.DAY_OF_WEEK); int
	 * count = 7; while (count > 0) { count--; switch (dayIndex) { case 1:
	 * newMessageInfo = "S " + "<font color='#cccccc'><b>" + format.format(date)
	 * + "</b></font>"; sun.setText(Html.fromHtml(newMessageInfo)); break; case
	 * 2: newMessageInfo = "M " + "<font color='#cccccc'><b>" +
	 * format.format(date) + "</b></font>";
	 * mon.setText(Html.fromHtml(newMessageInfo)); break; case 3: newMessageInfo
	 * = "T " + "<font color='#cccccc'><b>" + format.format(date) +
	 * "</b></font>"; tue.setText(Html.fromHtml(newMessageInfo)); break; case 4:
	 * newMessageInfo = "W " + "<font color='#cccccc'><b>" + format.format(date)
	 * + "</b></font>"; wed.setText(Html.fromHtml(newMessageInfo)); break; case
	 * 5: newMessageInfo = "T " + "<font color='#cccccc'><b>" +
	 * format.format(date) + "</b></font>";
	 * thu.setText(Html.fromHtml(newMessageInfo)); break; case 6: newMessageInfo
	 * = "F " + "<font color='#cccccc'><b>" + format.format(date) +
	 * "</b></font>"; fri.setText(Html.fromHtml(newMessageInfo)); break; case 7:
	 * newMessageInfo = "S " + "<font color='#cccccc'><b>" + format.format(date)
	 * + "</b></font>"; sat.setText(Html.fromHtml(newMessageInfo)); break;
	 * default: break; } calendar.add(Calendar.DATE, 1); dayIndex =
	 * calendar.get(Calendar.DAY_OF_WEEK); date = calendar.getTime(); }
	 * calendar.setTime(currDate); }
	 */

	private void initPermisionInfo() {

		boolean isSetBgTag = false;
		LinearLayout permissionContent = (LinearLayout) mPager2
				.findViewById(R.id.permission_content);
		PackageManager packageManager = this.getPackageManager();
		String[] sharedPkgList = mAppInfo.permissionInfo.mPermissionList;
		if (sharedPkgList == null)
			return;
		ArrayList<PermissionInfo> nomalPermissions = new ArrayList<PermissionInfo>();
		ArrayList<PermissionInfo> dangerousPermissions = new ArrayList<PermissionInfo>();
		ArrayList<PermissionInfo> sinatruePermissions = new ArrayList<PermissionInfo>();

		for (int i = 0; i < sharedPkgList.length; i++) {
			PermissionInfo tmpPermInfo;
			try {
				tmpPermInfo = packageManager.getPermissionInfo(
						sharedPkgList[i], 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				continue;
			}
			if (tmpPermInfo.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
				dangerousPermissions.add(tmpPermInfo);
			} else if (tmpPermInfo.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS
					|| tmpPermInfo.protectionLevel == PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM) {
				sinatruePermissions.add(tmpPermInfo);
			} else {
				nomalPermissions.add(tmpPermInfo);
			}
		}
		if (dangerousPermissions.size() > 0) {
			TextView title = new TextView(this);
			title.setText(R.string.app_permission1);
			title.setTextColor(Color.rgb(148, 148, 148));
			title.setBackgroundColor(Color.rgb(214, 214, 214));
			title.setGravity(Gravity.CENTER_VERTICAL);
			title.setPadding(DipPixelUtil.dip2px(this, 20), 0, 0, 0);
			title.setTextSize(15);
			title.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, DipPixelUtil.dip2px(this, 40)));
			permissionContent.addView(title);
			for (int i = 0; i < dangerousPermissions.size(); i++) {
				RelativeLayout permissionItem = (RelativeLayout) this
						.getLayoutInflater().inflate(
								R.layout.permission_item_layout, null);
				if (isSetBgTag) {
					permissionItem.setBackgroundColor(Color.rgb(247, 247, 247));
					isSetBgTag = false;
				} else {
					isSetBgTag = true;
				}
				TextView permissionName = (TextView) permissionItem
						.findViewById(R.id.permission_name);
				CharSequence pName = dangerousPermissions.get(i).loadLabel(
						packageManager);
				if (TextUtils.isEmpty(pName)) {
					continue;
				}
				permissionName.setText(pName);

				TextView permissionDiscr = (TextView) permissionItem
						.findViewById(R.id.permission_discription);
				permissionDiscr.setText(dangerousPermissions.get(i)
						.loadDescription(packageManager));
				permissionContent.addView(permissionItem);
			}
		}
		if (sinatruePermissions.size() > 0) {
			TextView title = new TextView(this);
			title.setText(R.string.app_permission3);
			title.setTextColor(Color.rgb(148, 148, 148));
			title.setBackgroundColor(Color.rgb(214, 214, 214));
			title.setGravity(Gravity.CENTER_VERTICAL);
			title.setPadding(DipPixelUtil.dip2px(this, 20), 0, 0, 0);
			title.setTextSize(15);
			title.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, DipPixelUtil.dip2px(this, 40)));
			permissionContent.addView(title);
			for (int i = 0; i < sinatruePermissions.size(); i++) {
				RelativeLayout permissionItem = (RelativeLayout) this
						.getLayoutInflater().inflate(
								R.layout.permission_item_layout, null);
				if (isSetBgTag) {
					permissionItem.setBackgroundColor(Color.rgb(247, 247, 247));
					isSetBgTag = false;
				} else {
					isSetBgTag = true;
				}
				TextView permissionName = (TextView) permissionItem
						.findViewById(R.id.permission_name);
				CharSequence pName = sinatruePermissions.get(i).loadLabel(
						packageManager);
				if (TextUtils.isEmpty(pName)) {
					continue;
				}
				permissionName.setText(pName);

				TextView permissionDiscr = (TextView) permissionItem
						.findViewById(R.id.permission_discription);
				permissionDiscr.setText(sinatruePermissions.get(i)
						.loadDescription(packageManager));
				permissionContent.addView(permissionItem);
			}
		}
		if (nomalPermissions.size() > 0) {
			TextView title = new TextView(this);
			title.setText(R.string.app_permission2);
			title.setTextColor(Color.rgb(148, 148, 148));
			title.setBackgroundColor(Color.rgb(214, 214, 214));
			title.setGravity(Gravity.CENTER_VERTICAL);
			title.setPadding(DipPixelUtil.dip2px(this, 20), 0, 0, 0);
			title.setTextSize(15);
			title.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, DipPixelUtil.dip2px(this, 40)));
			permissionContent.addView(title);
			for (int i = 0; i < nomalPermissions.size(); i++) {
				RelativeLayout permissionItem = (RelativeLayout) this
						.getLayoutInflater().inflate(
								R.layout.permission_item_layout, null);
				if (isSetBgTag) {
					permissionItem.setBackgroundColor(Color.rgb(247, 247, 247));
					isSetBgTag = false;
				} else {
					isSetBgTag = true;
				}
				TextView permissionName = (TextView) permissionItem
						.findViewById(R.id.permission_name);
				CharSequence pName = nomalPermissions.get(i).loadLabel(
						packageManager);
				if (TextUtils.isEmpty(pName)) {
					continue;
				}
				permissionName.setText(pName);

				TextView permissionDiscr = (TextView) permissionItem
						.findViewById(R.id.permission_discription);
				permissionDiscr.setText(nomalPermissions.get(i)
						.loadDescription(packageManager));
				permissionContent.addView(permissionItem);
			}
		}

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
			container.addView(view, position);
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
	public void onPageSelected(int pageIndex) {
		setSelecteTab(pageIndex);
	}

	private void setSelecteTab(int index) {
		if (index == 0) {
			mTvUseInfo.setBackgroundResource(R.drawable.tab_selected);
			mTvUseInfo.setTextColor(Color.rgb(0, 127, 255));
			mTvPermissionInfo.setBackgroundColor(Color.TRANSPARENT);
			mTvPermissionInfo.setTextColor(Color.rgb(80, 80, 80));
		} else if (index == 1) {
			mTvUseInfo.setBackgroundColor(Color.TRANSPARENT);
			mTvUseInfo.setTextColor(Color.rgb(80, 80, 80));
			mTvPermissionInfo.setBackgroundResource(R.drawable.tab_selected);
			mTvPermissionInfo.setTextColor(Color.rgb(0, 127, 255));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.uninstall_app:
			RemoveApp(mAppInfo.packageName);
			break;
		case R.id.stop_app:
			Intent intent = new Intent(
					Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts("package", mAppInfo.packageName, null);
			intent.setData(uri);
			startActivity(intent);
			break;
		case R.id.tv_app_use_info:
			mViewPager.setCurrentItem(0);
			break;
		case R.id.tv_app_permission_info:
			mViewPager.setCurrentItem(1);
			break;

		default:
			break;
		}

	}

	private void RemoveApp(String packageName) {
		Uri uri = Uri.fromParts("package", packageName, null);
		Intent intentdel = new Intent(Intent.ACTION_DELETE, uri);
		startActivity(intentdel);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mAppBaseInfoLayoutbg != null) {
			mAppBaseInfoLayoutbg.recycle();
			mAppBaseInfoLayoutbg = null;
		}
		unResisterDeleteReceiver();
	}

	@Override
	public void onScroll(int direction) {
		/*
		 * if (direction > 0) { //left if (mCurrdate.equals(
		 * mCalendar.getTime())) { return; }
		 * mCalendar.add(Calendar.WEEK_OF_YEAR, 1);
		 * setWeekString(mCalendar.getTime()); } else if (direction < 0) {
		 * //right mCalendar.add(Calendar.WEEK_OF_YEAR, -1);
		 * setWeekString(mCalendar.getTime()); }
		 */

	}

	private class DeleteReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				if (mAppInfo != null) {
					if (mAppInfo.packageName.equals(packageName)) {
						finish();
					}
				}
			}
		}
	}
}
