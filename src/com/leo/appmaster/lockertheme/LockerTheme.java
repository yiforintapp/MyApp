package com.leo.appmaster.lockertheme;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.lockertheme.LockerThemeChanageDialog.OnDiaogClickListener;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.model.ThemeItemInfo;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.ImageLoader;
import com.leoers.leoanalytics.LeoStat;

public class LockerTheme extends BaseActivity implements OnClickListener,
		OnPageChangeListener, OnRefreshListener2<ListView> {

	private View mTabContainer;
	private ViewPager mViewPager;
	private PullToRefreshListView localThemeList;
	private PullToRefreshListView mOnlineThemeList;
	private View mOnlineThemeView;
	private View mOnlineThemeHolder;
	private View mErrorView, mLayoutEmptyTip;
	private TextView mTvRetry, mTvLocal, mTvOnline;
	private ProgressBar mProgressBar;
	private LockerThemeChanageDialog dialog;

	private List<ThemeItemInfo> mLocalThemes;
	private List<ThemeItemInfo> mOnlineThemes;
	private LockerThemeAdapter mLocalThemeAdapter;
	private LockerThemeAdapter mOnlineThemeAdapter;
	private List<String> mHideThemes;
	private ThemeItemClickListener itemListener;
	public ThemeItemInfo lastSelectedItem;

	private boolean mShouldLockOnRestart = true;
	private LockerThemeReceive mLockerThemeReceive;
	private boolean mNeedLock = false;
	private int number = 0;
	private String mFrom;
	private String mGuideFlag;
	// private int mCurrentShowPage = 0;
	// private int mNextLoadPage = 0;

	private static final int MSG_LOAD_INIT_FAILED = 0;
	private static final int MSG_LOAD_INIT_SUCCESSED = 1;
	private static final int MSG_LOAD_PAGE_DATA_FAILED = 3;
	private static final int MSG_LOAD_PAGE_DATA_SUCCESS = 4;

	private EventHandler mHandler;
	private String mFromTheme;
	

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
					AppMasterPreference pref = AppMasterPreference
							.getInstance(lockerTheme.get());
					pref.setLocalThemeSerialNumber(pref.getOnlineThemeSerialNumber());
					lockerTheme.get().onLoadInitThemeFinish(true, msg.obj);
				}
				break;
			case MSG_LOAD_PAGE_DATA_FAILED:
				if (lockerTheme.get() != null) {
					lockerTheme.get().mOnlineThemeList.onRefreshComplete();

					lockerTheme.get().onLoadMoreThemeFinish(false, null);
				}
				break;
			case MSG_LOAD_PAGE_DATA_SUCCESS:
				if (lockerTheme.get() != null) {
					lockerTheme.get().mOnlineThemeList.onRefreshComplete();
					AppMasterPreference pref = AppMasterPreference
							.getInstance(lockerTheme.get());
					pref.setLocalThemeSerialNumber(pref.getOnlineThemeSerialNumber());
					List<ThemeItemInfo> loadList = (List<ThemeItemInfo>) msg.obj;
					lockerTheme.get().onLoadMoreThemeFinish(true, loadList);
				}
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
		handleIntent();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addDataScheme("package");
		mLockerThemeReceive = new LockerThemeReceive();
		registerReceiver(mLockerThemeReceive, intentFilter);
	}

	private void handleIntent() {
		AppMasterPreference pref = AppMasterPreference.getInstance(this);
		Intent intent = this.getIntent();
		mFromTheme = intent.getStringExtra("theme_package");
		mNeedLock = intent.getBooleanExtra("need_lock", false);
		mFrom = intent.getStringExtra("from");
		if (mFromTheme != null && !mFromTheme.equals("")) {
			tryHideThemeApk(mFromTheme);
			for (int i = 0; i < mLocalThemes.size(); i++) {
				if (mLocalThemes.get(i).packageName.equals(mFromTheme)) {
					number = i;
					showAlarmDialog(mLocalThemes.get(i).themeName, View.VISIBLE);
					lastSelectedItem = mLocalThemes.get(i);
				}
			}
		} else {
			number = 0;
		}
		// localThemeList.setSelection(number);
		boolean newTheme = !pref.getLocalThemeSerialNumber().equals(
				pref.getOnlineThemeSerialNumber());
		if (newTheme) {
			pref.setLocalThemeSerialNumber(pref.getOnlineThemeSerialNumber());
			mViewPager.setCurrentItem(1);
		}

		// form statusbar
		if (mFrom != null && mFrom.equals("new_theme_tip")) {
			/* SDK event mark */
			SDKWrapper.addEvent(this, LeoStat.P1, "theme_enter", "statusbar");
			mViewPager.setCurrentItem(1);
		}

		// but if from theme app, select page 0
		if (mFromTheme != null && !mFromTheme.equals("")) {
			mViewPager.setCurrentItem(0);
		}

		localThemeList.getRefreshableView().setSelection(number);
	}

	private void loadData() {
		mLocalThemes = new ArrayList<ThemeItemInfo>();
		mOnlineThemes = new ArrayList<ThemeItemInfo>();
		loadLocalTheme();
		loadInitOnlineTheme();
	}

	private void loadLocalTheme() {
		AppMasterPreference pref = AppMasterPreference
				.getInstance(LockerTheme.this);
		mLocalThemes.clear();
		mHideThemes = pref.getHideThemeList();
		mLocalThemes.add(getDefaultTheme());
		Context themeContext = null;
		for (String themePackage : mHideThemes) {
			ThemeItemInfo bean = new ThemeItemInfo();
			bean.themeType = Constants.THEME_TYPE_LOCAL;
			try {
				themeContext = createPackageContext(themePackage,
						Context.CONTEXT_IGNORE_SECURITY);
				String str = (String) themeContext.getResources().getText(
						themeContext.getResources().getIdentifier("app_name",
								"string", themeContext.getPackageName()));
				bean.themeName = str;
				bean.packageName = themePackage;
				int themePreview = themeContext.getResources().getIdentifier(
						"lockertheme", "drawable",
						themeContext.getPackageName());
				if (themePreview > 0) {
					ImageLoader imageLoader = ImageLoader.getInstance();
					File file = null;
					boolean loaded = false;
					if (Constants.THEME_PACKAGE_NIGHT.equals(bean.packageName)) {
						loaded = false;
						file = imageLoader.getDiskCache().get(
								Constants.THEME_MOONNIGHT_URL);
						if (file != null && file.exists()) {
							BitmapDrawable bd = new BitmapDrawable(
									this.getResources(), file.getAbsolutePath());
							if (bd != null) {
								bean.themeImage = bd;
								loaded = true;
							}
						}
						if (!loaded) {
							bean.themeImage = themeContext.getResources()
									.getDrawable(themePreview);
						}
					} else if (Constants.THEME_PACKAGE_CHRITMAS
							.equals(bean.packageName)) {
						loaded = false;
						file = imageLoader.getDiskCache().get(
								Constants.THEME_CHRISTMAS_URL);
						if (file != null && file.exists()) {
							BitmapDrawable bd = new BitmapDrawable(
									this.getResources(), file.getAbsolutePath());
							if (bd != null) {
								bean.themeImage = bd;
								loaded = true;
							}
						}
						if (!loaded) {
							bean.themeImage = themeContext.getResources()
									.getDrawable(themePreview);
						}
					} else if (Constants.THEME_PACKAGE_FRUIT
							.equals(bean.packageName)) {
						loaded = false;
						file = imageLoader.getDiskCache().get(
								Constants.THEME_FRUIT_URL);
						if (file != null && file.exists()) {
							BitmapDrawable bd = new BitmapDrawable(
									this.getResources(), file.getAbsolutePath());
							if (bd != null) {
								bean.themeImage = bd;
								loaded = true;
							}
						}
						if (!loaded) {
							bean.themeImage = themeContext.getResources()
									.getDrawable(themePreview);
						}
					} else if (Constants.THEME_PACKAGE_SPATIAL
							.equals(bean.packageName)) {
						loaded = false;
						file = imageLoader.getDiskCache().get(
								Constants.THEME_SPATIAL_URL);
						if (file != null && file.exists()) {
							BitmapDrawable bd = new BitmapDrawable(
									this.getResources(), file.getAbsolutePath());
							if (bd != null) {
								bean.themeImage = bd;
								loaded = true;
							}
						}
						if (!loaded) {
							bean.themeImage = themeContext.getResources()
									.getDrawable(themePreview);
						}
					} else {
						bean.themeImage = themeContext.getResources()
								.getDrawable(themePreview);
					}

				} else {
					bean.themeImage = this.getResources().getDrawable(
							R.drawable.app_list_bg);
				}
				bean.label = (String) this.getResources().getText(
						R.string.localtheme);

				if (AppMasterApplication.usedThemePackage.equals(themePackage)) {
					bean.curUsedTheme = true;
				} else {
					bean.curUsedTheme = false;
				}
				mLocalThemes.add(bean);
			} catch (NameNotFoundException e1) {
				LeoLog.e("Context", "getContext error");
			}
		}
	}

	private void loadInitOnlineTheme() {
		HttpRequestAgent.getInstance(this).loadOnlineTheme(mHideThemes,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response, boolean noModify) {
						LeoLog.d("response", response.toString());
						List<ThemeItemInfo> list = ThemeJsonObjectParser
								.parserJsonObject(LockerTheme.this, response);
						Message msg = mHandler.obtainMessage(
								MSG_LOAD_INIT_SUCCESSED, list);
						mHandler.sendMessage(msg);
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						mHandler.sendEmptyMessage(MSG_LOAD_INIT_FAILED);
						SDKWrapper.addEvent(LockerTheme.this,
								LeoStat.P1, "load_failed", "theme");
					}
				});
	}

	private void initUI() {
		mTabContainer = findViewById(R.id.tab_container);
		mTvLocal = (TextView) findViewById(R.id.local);
		mTvOnline = (TextView) findViewById(R.id.online);
		mTvLocal.setOnClickListener(this);
		mTvOnline.setOnClickListener(this);

		LayoutInflater inflater = LayoutInflater.from(this);
		CommonTitleBar title = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		title.setTitle(R.string.lockerTheme);
		title.setBackViewListener(this);
		mViewPager = (ViewPager) findViewById(R.id.pager);

		// inflate local theme
		localThemeList = (PullToRefreshListView) inflater.inflate(
				R.layout.theme_local_list, null);
		localThemeList.setMode(Mode.DISABLED);
		mLocalThemeAdapter = new LockerThemeAdapter(this, mLocalThemes);
		localThemeList.setAdapter(mLocalThemeAdapter);
		itemListener = new ThemeItemClickListener();
		localThemeList.setOnItemClickListener(itemListener);

		// inflate online theme
		mOnlineThemeView = inflater.inflate(R.layout.theme_online_list, null);
		mProgressBar = (ProgressBar) mOnlineThemeView
				.findViewById(R.id.progressbar_loading);
		mErrorView = mOnlineThemeView.findViewById(R.id.layout_load_error);
		mLayoutEmptyTip = mOnlineThemeView.findViewById(R.id.layout_empty);
		mTvRetry = (TextView) mOnlineThemeView.findViewById(R.id.tv_reload);
		mTvRetry.setOnClickListener(this);
		mOnlineThemeHolder = mOnlineThemeView.findViewById(R.id.content_holder);
		mOnlineThemeList = (PullToRefreshListView) mOnlineThemeView
				.findViewById(R.id.list_online);
		mOnlineThemeList.setMode(Mode.PULL_FROM_END);
		mOnlineThemeAdapter = new LockerThemeAdapter(this, mOnlineThemes);
		mOnlineThemeList.setAdapter(mOnlineThemeAdapter);
		mOnlineThemeList.setOnRefreshListener(this);
		mOnlineThemeList.setOnItemClickListener(itemListener);

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
					view = mOnlineThemeView;
				}
				container.addView(view, LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);
				return view;
			}
		});
		mViewPager.setOnPageChangeListener(this);
		setCurrentPage(0);

	}

	private void onLoadMoreThemeFinish(boolean succeed, Object object) {
		if (succeed) {
			List<ThemeItemInfo> list = (List<ThemeItemInfo>) object;
			if (list == null || list.isEmpty()) {
				Toast.makeText(this, R.string.no_more_theme, 0).show();
			} else {
				addMoreOnlineTheme(list);
			}
		} else {
			Toast.makeText(this, R.string.network_error_msg, 0).show();
		}

	}

	private void onLoadInitThemeFinish(boolean succeed, Object object) {
		mOnlineThemeHolder.setVisibility(View.VISIBLE);
		if (succeed) {
			mOnlineThemeHolder.setVisibility(View.VISIBLE);
			mErrorView.setVisibility(View.INVISIBLE);
			mOnlineThemes.clear();
			if (object != null) {
				mOnlineThemes.addAll((List<ThemeItemInfo>) object);
			}

			// filter local theme
			if (!mOnlineThemes.isEmpty()) {
				List<ThemeItemInfo> removeList = new ArrayList<ThemeItemInfo>();
				for (ThemeItemInfo themeInfo : mOnlineThemes) {
					for (ThemeItemInfo localInfo : mLocalThemes) {
						if (themeInfo.packageName.equals(localInfo.packageName)) {
							removeList.add(themeInfo);
							break;
						}
					}
				}
				mOnlineThemes.removeAll(removeList);
			}
			mOnlineThemeAdapter.notifyDataSetChanged();
			if (mOnlineThemes.isEmpty()) {
				mLayoutEmptyTip.setVisibility(View.VISIBLE);
				mOnlineThemeList.setVisibility(View.VISIBLE);
			} else {
				mOnlineThemeList.setVisibility(View.VISIBLE);
				mLayoutEmptyTip.setVisibility(View.INVISIBLE);
			}
		} else {
			mOnlineThemeList.setVisibility(View.INVISIBLE);
			mErrorView.setVisibility(View.VISIBLE);
			mLayoutEmptyTip.setVisibility(View.INVISIBLE);
		}
		mProgressBar.setVisibility(View.INVISIBLE);
	}

	private void loadMoreOnlineTheme() {
		// mNextLoadPage = mCurrentShowPage + 1;
		List<String> loadedTheme = new ArrayList<String>();
		loadedTheme.addAll(mHideThemes);
		for (ThemeItemInfo info : mOnlineThemes) {
			loadedTheme.add(info.packageName);
		}

		HttpRequestAgent.getInstance(this).loadOnlineTheme(loadedTheme,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response, boolean noModify) {
						List<ThemeItemInfo> list = ThemeJsonObjectParser
								.parserJsonObject(LockerTheme.this, response);
						Message msg = mHandler.obtainMessage(
								MSG_LOAD_PAGE_DATA_SUCCESS, list);
						mHandler.sendMessage(msg);
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						mHandler.sendEmptyMessage(MSG_LOAD_PAGE_DATA_FAILED);
						SDKWrapper.addEvent(LockerTheme.this,
								LeoStat.P1, "load_failed", "theme");
					}
				});
	}

	public void addMoreOnlineTheme(List<ThemeItemInfo> loadList) {
		boolean add;
		boolean newTheme = false;
		for (ThemeItemInfo appLockerThemeBean : loadList) {
			add = true;
			for (ThemeItemInfo themeInfo : mLocalThemes) {
				if (themeInfo.packageName
						.equals(appLockerThemeBean.packageName)) {
					add = false;
					break;
				}
			}
			if (add) {
				for (ThemeItemInfo themeInfo : mOnlineThemes) {
					if (themeInfo.packageName
							.equals(appLockerThemeBean.packageName)) {
						add = false;
						break;
					}
				}
			}
			if (add) {
				newTheme = true;
				mOnlineThemes.add(appLockerThemeBean);
			}
		}
		if (newTheme) {
			mLayoutEmptyTip.setVisibility(View.INVISIBLE);
			mOnlineThemeAdapter.notifyDataSetChanged();
		} else {
			Toast.makeText(this, R.string.no_more_theme, 0).show();
		}
	}

	private void tryHideThemeApk(String pkg) {
		if (pkg == null)
			return;
		if (pkg.startsWith("com.leo.theme")) {
			String action = "disable_theme_" + pkg;
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
		if ((mFromTheme != null && !mFromTheme.equals(""))
				|| (mFrom != null && mFrom.equals("new_theme_tip"))) {
			Intent intent = new Intent(this, HomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		super.onBackPressed();
	}

	public void showAlarmDialog(String packageName, int visible) {
		dialog = new LockerThemeChanageDialog(this);
		dialog.setText(packageName);
		dialog.setVisibleUninstal(visible);
		dialog.show();

		dialog.setOnClickListener(new OnDiaogClickListener() {
			@Override
			public void onClick(int which) {
				if (which == 0) {
					for (int i = 0; i < mLocalThemes.size(); i++) {
						mLocalThemes.get(i).curUsedTheme = false;
					}
					AppMasterApplication
							.setSharedPreferencesValue(lastSelectedItem.packageName);
					lastSelectedItem.curUsedTheme = true;
					lastSelectedItem.label = (String) LockerTheme.this
							.getResources().getText(R.string.localtheme);
					mLocalThemeAdapter.notifyDataSetChanged();

					SDKWrapper.addEvent(LockerTheme.this, LeoStat.P1,
							"theme_apply", lastSelectedItem.packageName);
					dialog.cancel();
					/**
					 * LockerTheme first use Guide
					 */
					SharedPreferences mLockerGuideShared = getSharedPreferences(
							"LockerThemeGuide",
							LockerTheme.this.MODE_WORLD_WRITEABLE);
					mGuideFlag = mLockerGuideShared.getString("guideFlag", "0");
					if (mGuideFlag.equals("0")) {
						setLockerGuideShare();
					}
				} else if (which == 1) {
					dialog.cancel();
				} else if (which == 2) {
					Uri uri = Uri.fromParts("package",
							lastSelectedItem.packageName, null);
					Intent intent = new Intent(Intent.ACTION_DELETE, uri);
					startActivity(intent);
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

	private ThemeItemInfo getDefaultTheme() {
		ThemeItemInfo defaultTheme = new ThemeItemInfo();
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

		if (AppMasterApplication.usedThemePackage
				.equals(Constants.DEFAULT_THEME)) {
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
			mTvOnline.setTextColor(getResources().getColor(R.color.white));
			mTabContainer.setBackgroundResource(R.drawable.stacked_tabs_l);
		} else {
			mTvLocal.setTextColor(getResources().getColor(R.color.white));
			mTvOnline.setTextColor(getResources().getColor(
					R.color.tab_select_text));
			mTabContainer.setBackgroundResource(R.drawable.stacked_tabs_r);
		}
	}

	/**
	 * setLockerGuideShare
	 */
	private void setLockerGuideShare() {
		Intent intent = new Intent(LockerTheme.this,
				LockerThemeGuideActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			startActivity(intent);
		} catch (Exception e) {
		}
		SharedPreferences mLockerGuideShared = getSharedPreferences(
				"LockerThemeGuide", LockerTheme.this.MODE_WORLD_WRITEABLE);
		Editor editor = mLockerGuideShared.edit();
		editor.putString("guideFlag", "1");
		mGuideFlag = "1";
		editor.commit();
	}

	private void doReload() {
		mOnlineThemeHolder.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.VISIBLE);
		loadInitOnlineTheme();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.tv_reload:
			doReload();
			break;
		case R.id.local:
			mViewPager.setCurrentItem(0, true);
			break;
		case R.id.layout_title_back:
			if ((mFromTheme != null && !mFromTheme.equals(""))
					|| (mFrom != null && mFrom.equals("new_theme_tip"))) {
				Intent intent = new Intent(this, HomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			finish();
			break;
		case R.id.online:
			mViewPager.setCurrentItem(1, true);
			break;
		default:
			break;
		}

	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		loadMoreOnlineTheme();
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

	private class LockerThemeReceive extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
				final String packageName = intent.getData()
						.getSchemeSpecificPart();
				if (packageName != null
						&& packageName.startsWith("com.leo.theme")) {
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							
							// if need to load online theme
							ThemeItemInfo addThemeOnline = null;
							int number=mOnlineThemes.size();
	                            for (ThemeItemInfo info : mLocalThemes) {
	                                if (info.packageName.equals(packageName)) {
//	                                    addThemeOnline = info;
	                                    
	                                    
	                                    String remove = null;
	                                    for (String hide : mHideThemes) {
	                                        if(info.packageName.equals(hide))  {
	                                            remove = hide;
	                                            break;
	                                        }
                                        }
	                                    mHideThemes.remove(remove);
	                                    loadMoreOnlineTheme();    
	                                    
	                                    break;
	                                }
	                            }
/*	                            
	                            if (addThemeOnline != null) {
	                                
	                                addThemeOnline.themeType=Constants.THEME_TYPE_ONLINE;
	                                addThemeOnline.tag=Constants.THEME_TAG_NONE;
	                                mOnlineThemes.add(addThemeOnline);	                               
	                                mOnlineThemeAdapter.notifyDataSetChanged();

	                                if (mOnlineThemes.isEmpty()) {
	                                    mLayoutEmptyTip.setVisibility(View.VISIBLE);
	                                }
	                            }*/
	                            loadLocalTheme();
	                            mLocalThemeAdapter.notifyDataSetChanged();
						}
					}, 1000);
				}
			}
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
				final String packageName = intent.getData()
						.getSchemeSpecificPart();
				if (packageName != null
						&& packageName.startsWith("com.leo.theme")) {
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							loadLocalTheme();
							mLocalThemeAdapter.notifyDataSetChanged();
							// if need to load online theme
							ThemeItemInfo remove = null;
							for (ThemeItemInfo info : mOnlineThemes) {
								if (info.packageName.equals(packageName)) {
									remove = info;									
								}
							}
							if (remove != null) {
								mOnlineThemes.remove(remove);
								mOnlineThemeAdapter.notifyDataSetChanged();								
								if (mOnlineThemes.isEmpty()) {
									mLayoutEmptyTip.setVisibility(View.VISIBLE);
								}
							}
						}
					}, 1000);
				}
			}
		}
	}

	private class ThemeItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			lastSelectedItem = (ThemeItemInfo) arg0.getItemAtPosition(arg2);
			/* SDK mark user click theme - begin */
			if (lastSelectedItem.themeType == Constants.THEME_TYPE_ONLINE) {
				SDKWrapper.addEvent(LockerTheme.this, LeoStat.P1,
						"theme_choice_online", lastSelectedItem.packageName);
			} else if (lastSelectedItem.themeType == Constants.THEME_TYPE_LOCAL) {
				SDKWrapper.addEvent(LockerTheme.this, LeoStat.P1,
						"theme_choice_local", lastSelectedItem.packageName);
			}
			/* SDK mark user click theme - end */
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
					.equals(AppMasterApplication.usedThemePackage)) {
				if (lastSelectedItem.packageName
						.equals("com.leo.theme.default")) {
					showAlarmDialog(lastSelectedItem.themeName, View.GONE);
				} else {
					showAlarmDialog(lastSelectedItem.themeName, View.VISIBLE);
				}
			}
		}
	}
}
