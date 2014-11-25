package com.leo.appmaster.lockertheme;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.SDKWrapper;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.lockertheme.LockerThemeChanageDialog.OnDiaogClickListener;
import com.leo.appmaster.lockertheme.XListView.IXListViewListener;
import com.leo.appmaster.model.AppLockerThemeBean;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;

public class LockerTheme extends BaseActivity implements OnClickListener,
		IXListViewListener, OnPageChangeListener {

	private ViewPager mViewPager;
	private ListView localThemeList;
	private View onlineTheme;
	private View mOnlineThemeHolder;
	private TextView mTvRetry, mTvLocal, mTvOnline;
	private ProgressBar mProgressBar;
	private LockerThemeChanageDialog dialog;

	private List<AppLockerThemeBean> mLocalTheme;
	private List<AppLockerThemeBean> mOnlineTheme;
	private LockerThemeAdapter mLocalThemeAdapter;
	private LockerThemeAdapter mOnlineThemeAdapter;
	private List<String> localThemes;
	private ThemeItemClickListener itemListener;
	public AppLockerThemeBean lastSelectedItem;

	private boolean mShouldLockOnRestart = true;
	private LockerThemeReceive mLockerThemeReceive;
	private boolean mNeedLock = false;
	private int number = 0;
	private AppMasterPreference mPreference;

	private static final int MSG_LOAD_INIT_FAILED = 0;
	private static final int MSG_LOAD_INIT_SUCCESSED = 1;
	private static final int MSG_LOAD_PAGE_DATA_FAILED = 3;
	private static final int MSG_LOAD_PAGE_DATA_SUCCESS = 4;

	private EventHandler mHandler;

	private static class EventHandler extends Handler {
		WeakReference<LockerTheme> lockerTheme;

		public EventHandler(LockerTheme lockerTheme) {
			super();
			this.lockerTheme = new WeakReference<LockerTheme>(lockerTheme);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOAD_INIT_FAILED:
				if (lockerTheme.get() != null) {
					lockerTheme.get().onLoadInitThemeFinish(false, null);
				}
				break;
			case MSG_LOAD_INIT_SUCCESSED:
				if (lockerTheme.get() != null) {
					lockerTheme.get().onLoadInitThemeFinish(true, msg.obj);
				}
				break;
			case MSG_LOAD_PAGE_DATA_FAILED:

				break;
			case MSG_LOAD_PAGE_DATA_SUCCESS:

				break;

			default:
				break;
			}
		}

	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_locker_theme);
		mHandler = new EventHandler(this);
		loadData();
		initUI();

		Intent intent = this.getIntent();
		String temp = intent.getStringExtra("theme_package");
		mNeedLock = intent.getBooleanExtra("need_lock", false);
		if (temp != null && !temp.equals("")) {
			tryHideThemeApk(temp);
			for (int i = 0; i < mLocalTheme.size(); i++) {
				if (mLocalTheme.get(i).packageName.equals(temp)) {
					number = i;
					showAlarmDialog(mLocalTheme.get(i).themeName, View.VISIBLE);
					lastSelectedItem = mLocalTheme.get(i);
				}
			}
		} else {
			number = 0;
		}
		localThemeList.setSelection(number);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addDataScheme("package");
		mLockerThemeReceive = new LockerThemeReceive();
		registerReceiver(mLockerThemeReceive, intentFilter);
	}

	private void loadData() {
		mLocalTheme = new ArrayList<AppLockerThemeBean>();
		mOnlineTheme = new ArrayList<AppLockerThemeBean>();
		mPreference = AppMasterPreference.getInstance(LockerTheme.this);

		// load local theme
		localThemes = mPreference.getHideThemeList();
		mLocalTheme.add(getDefaultTheme());
		Context themeContext = null;
		for (String thmemPackage : localThemes) {
			AppLockerThemeBean bean = new AppLockerThemeBean();
			bean.themeType = Constants.THEME_TYPE_LOCAL;
			try {
				themeContext = createPackageContext(thmemPackage,
						Context.CONTEXT_IGNORE_SECURITY);
				String str = (String) themeContext.getResources().getText(
						themeContext.getResources().getIdentifier("app_name",
								"string", themeContext.getPackageName()));
				bean.themeName = str;
				int themePreview = themeContext.getResources().getIdentifier(
						"lockertheme", "drawable",
						themeContext.getPackageName());
				if (themePreview > 0) {
					bean.themeImage = themeContext.getResources().getDrawable(
							themePreview);
				} else {
					bean.themeImage = this.getResources().getDrawable(
							R.drawable.app_list_bg);
				}
				bean.label = (String) this.getResources().getText(
						R.string.localtheme);
				bean.packageName = thmemPackage;

				if (AppMasterApplication.sharedPackage.equals(thmemPackage)) {
					bean.curUsedTheme = true;
				} else {
					bean.curUsedTheme = false;
				}
				mLocalTheme.add(bean);
			} catch (NameNotFoundException e1) {
				LeoLog.i("Context", "getContext error");
			}
		}

		// load online theme
		 loadInitOnlineTheme();
	}

	private void initUI() {
		mTvLocal = (TextView) findViewById(R.id.local);
		mTvOnline = (TextView) findViewById(R.id.online);
		mTvLocal.setOnClickListener(this);
		mTvOnline.setOnClickListener(this);

		LayoutInflater inflater = LayoutInflater.from(this);
		CommonTitleBar title = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		title.setTitle(R.string.lockerTheme);
		title.openBackView();
		mViewPager = (ViewPager) findViewById(R.id.pager);

		// inflate local theme
		localThemeList = (ListView) inflater.inflate(R.layout.theme_local_list,
				null);
		mLocalThemeAdapter = new LockerThemeAdapter(this, mLocalTheme);
		localThemeList.setAdapter(mLocalThemeAdapter);
		itemListener = new ThemeItemClickListener();
		localThemeList.setOnItemClickListener(itemListener);

		// inflate online theme
		onlineTheme = inflater.inflate(R.layout.theme_online_list, null);
		mProgressBar = (ProgressBar) onlineTheme
				.findViewById(R.id.progressbar_loading);
		mTvRetry = (TextView) onlineTheme.findViewById(R.id.tv_reload);
		mTvRetry.setOnClickListener(this);
		mOnlineThemeHolder = onlineTheme.findViewById(R.id.content_holder);
		XListView pullLoadListView = (XListView) onlineTheme
				.findViewById(android.R.id.list);
		pullLoadListView.setPullLoadEnable(true);
		mOnlineThemeAdapter = new LockerThemeAdapter(this, mOnlineTheme);
		pullLoadListView.setAdapter(mOnlineThemeAdapter);
		pullLoadListView.setPullLoadEnable(true);
		pullLoadListView.setPullRefreshEnable(false);
		pullLoadListView.setXListViewListener(this);
		pullLoadListView.setOnItemClickListener(itemListener);

		// fill viewpager
		mViewPager.setAdapter(new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return 2;
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				View view = null;
				if (position == 0) {
					view = localThemeList;
				} else {
					view = onlineTheme;
				}
				container.addView(view, LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);
				return view;
			}
		});
		mViewPager.setOnPageChangeListener(this);
		setCurrentPage(0);

	}

	private void onLoadInitThemeFinish(boolean succeed, Object object) {
		if (succeed) {
			mOnlineTheme.clear();
			if (object != null) {
				mOnlineTheme = (List<AppLockerThemeBean>) object;
			}
			mOnlineThemeAdapter.notifyDataSetChanged();
		}

		mProgressBar.setVisibility(View.INVISIBLE);
		mOnlineThemeHolder.setVisibility(View.VISIBLE);
	}

	private void loadInitOnlineTheme() {
		HttpRequestAgent.getInstance(this).loadOnlineTheme(0,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						List<AppLockerThemeBean> list = ThemeJsonObjectParser
								.parserJsonObject(response);
						Message msg = mHandler.obtainMessage(
								MSG_LOAD_INIT_SUCCESSED, list);
						mHandler.sendMessage(msg);
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						mHandler.sendEmptyMessage(MSG_LOAD_INIT_FAILED);
					}
				});
	}

	private void tryHideThemeApk(String pkg) {
		if (pkg == null)
			return;
		if (pkg.startsWith("com.leo.theme")) {
			String action = "disable_theme_" + pkg;
			LeoLog.e("sendHideThemeBroadcast", action);
			Intent intent = new Intent(action);
			this.sendBroadcast(intent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mLockerThemeReceive);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	/**
	 * AlarmDialog
	 * 
	 * @param title
	 * @param content
	 */

	public void showAlarmDialog(String packageName, int visible) {
		dialog = new LockerThemeChanageDialog(this);
		dialog.setText(packageName);
		dialog.setVisibleUninstal(visible);
		dialog.show();

		dialog.setOnClickListener(new OnDiaogClickListener() {
			@Override
			public void onClick(int which) {
				if (which == 0) {
					for (int i = 0; i < mLocalTheme.size(); i++) {
						mLocalTheme.get(i).curUsedTheme = false;
					}
					AppMasterApplication
							.setSharedPreferencesValue(lastSelectedItem.packageName);
					lastSelectedItem.curUsedTheme = true;
					lastSelectedItem.label = (String) LockerTheme.this
							.getResources().getText(R.string.localtheme);
					// loadThemeData();
					// mLockerThemeAdapter.notifyDataSetChanged();

					SDKWrapper.addEvent(LockerTheme.this, LeoStat.P1,
							"theme_apply", lastSelectedItem.packageName);
					dialog.cancel();
				} else if (which == 1) {
					dialog.cancel();
				} else if (which == 2) {
					Uri uri = Uri.fromParts("package",
							lastSelectedItem.packageName, null);
					Intent intent = new Intent(Intent.ACTION_DELETE, uri);
					startActivity(intent);
					// mLockerThemeAdapter.notifyDataSetChanged();

					dialog.cancel();
				}
			}
		});
	}

	@Override
	public void onActivityRestart() {

		if (mNeedLock) {
			if (mShouldLockOnRestart) {
				showLockPage();
			} else {
				mShouldLockOnRestart = true;
			}
		}
	}

	private void showLockPage() {
		Intent intent = new Intent(this, LockScreenActivity.class);
		int lockType = AppMasterPreference.getInstance(this).getLockType();
		if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_PASSWD);
		} else {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_GESTURE);
		}
		intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
				LockFragment.FROM_SELF);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		startActivityForResult(intent, 1000);
	}

	@Override
	public void onActivityResault(int requestCode, int resultCode) {
		mShouldLockOnRestart = false;
	}

	private class ThemeItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			lastSelectedItem = (AppLockerThemeBean) arg0
					.getItemAtPosition(arg2);
			SDKWrapper.addEvent(LockerTheme.this, LeoStat.P1, "theme_choice",
					lastSelectedItem.packageName);
			if (lastSelectedItem.themeType == Constants.THEME_TYPE_ONLINE) {
				if (AppUtil
						.appInstalled(LockerTheme.this, Constants.GP_PACKAGE)) {
					AppwallHttpUtil.requestGp(LockerTheme.this,
							lastSelectedItem.packageName);
				} else {
					AppwallHttpUtil.requestUrl(LockerTheme.this,
							lastSelectedItem.downloadUrl);
				}
			} else if (!lastSelectedItem.packageName
					.equals(AppMasterApplication.sharedPackage)) {
				if (mLocalTheme.get(arg2).packageName
						.equals("com.leo.theme.default")) {
					showAlarmDialog(lastSelectedItem.themeName, View.GONE);
				} else {
					showAlarmDialog(lastSelectedItem.themeName, View.VISIBLE);
				}
			}
		}
	}

	/**
	 * Default Theme
	 */
	private AppLockerThemeBean getDefaultTheme() {
		AppLockerThemeBean defaultTheme = new AppLockerThemeBean();
		defaultTheme.themeImage = getResources().getDrawable(
				R.drawable.default_theme);
		defaultTheme.themeName = (String) this.getResources().getText(
				R.string.ParadoxTheme);
		defaultTheme.packageName = "com.leo.theme.default";
		defaultTheme.label = (String) this.getResources().getText(
				R.string.defaultTheme);
		defaultTheme.themeName = (String) this.getResources().getText(
				R.string.defaultTheme);
		defaultTheme.curUsedTheme = true;

		if (AppMasterApplication.sharedPackage.equals("com.leo.theme.default")) {
			defaultTheme.curUsedTheme = true;
		} else {
			defaultTheme.curUsedTheme = false;
		}

		return defaultTheme;
	}

	private void setCurrentPage(int page) {
		if (page == 0) {
			mTvLocal.setTextColor(getResources().getColor(
					R.color.tab_select_text));
			mTvLocal.setTextColor(getResources().getColor(R.color.white));
			mTvLocal.setBackgroundResource(R.drawable.stacked_tabs_l);
		} else {
			mTvOnline.setTextColor(getResources().getColor(R.color.white));
			mTvOnline.setTextColor(getResources().getColor(
					R.color.tab_select_text));
			mTvOnline.setBackgroundResource(R.drawable.stacked_tabs_r);
		}
	}

	private class LockerThemeReceive extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				if (packageName != null && packageName.equals("com.leo.theme")) {
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							loadData();
							mLocalThemeAdapter.notifyDataSetChanged();
						}
					}, 2000);
				}
			}
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				if (packageName != null && packageName.equals("com.leo.theme")) {
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							loadData();
							mLocalThemeAdapter.notifyDataSetChanged();
						}
					}, 2000);
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.tv_reload:
			onReload();
			break;
		case R.id.local:
			mViewPager.setCurrentItem(0, true);
			break;
		case R.id.online:
			mViewPager.setCurrentItem(1, true);
			break;
		default:
			break;
		}

	}

	private void onReload() {

	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		setCurrentPage(arg0);
	}
}
