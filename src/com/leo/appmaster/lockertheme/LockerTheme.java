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
import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.SDKWrapper;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.lockertheme.LockerThemeChanageDialog.OnDiaogClickListener;
import com.leo.appmaster.model.ThemeInfo;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
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

	private List<ThemeInfo> mLocalThemes;
	private List<ThemeInfo> mOnlineThemes;
	private LockerThemeAdapter mLocalThemeAdapter;
	private LockerThemeAdapter mOnlineThemeAdapter;
	private List<String> mHideThemes;
	private ThemeItemClickListener itemListener;
	public ThemeInfo lastSelectedItem;

	private boolean mShouldLockOnRestart = true;
	private LockerThemeReceive mLockerThemeReceive;
	private boolean mNeedLock = false;
	private int number = 0;
	private String mFrom;
	// private int mCurrentShowPage = 0;
	// private int mNextLoadPage = 0;

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
				if (lockerTheme.get() != null) {
					lockerTheme.get().mOnlineThemeList.onRefreshComplete();

					lockerTheme.get().onLoadMoreThemeFinish(false, null);
				}
				break;
			case MSG_LOAD_PAGE_DATA_SUCCESS:
				if (lockerTheme.get() != null) {
					lockerTheme.get().mOnlineThemeList.onRefreshComplete();
					List<ThemeInfo> loadList = (List<ThemeInfo>) msg.obj;
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
		String temp = intent.getStringExtra("theme_package");
		mNeedLock = intent.getBooleanExtra("need_lock", false);
		mFrom = intent.getStringExtra("from");
		if (temp != null && !temp.equals("")) {
			tryHideThemeApk(temp);
			for (int i = 0; i < mLocalThemes.size(); i++) {
				if (mLocalThemes.get(i).packageName.equals(temp)) {
					number = i;
					showAlarmDialog(mLocalThemes.get(i).themeName, View.VISIBLE);
					lastSelectedItem = mLocalThemes.get(i);
				}
			}
		} else {
			number = 0;
		}
		// localThemeList.setSelection(number);
		boolean newTheme = !pref.getLocalSerialNumber().equals(
				pref.getOnlineSerialNumber());
		if (newTheme) {
			pref.setLocalSerialNumber(pref.getOnlineSerialNumber());
			mViewPager.setCurrentItem(1);
		}

		localThemeList.getRefreshableView().setSelection(number);
	}

	private void loadData() {
		mLocalThemes = new ArrayList<ThemeInfo>();
		mOnlineThemes = new ArrayList<ThemeInfo>();
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
		for (String thmemPackage : mHideThemes) {
			ThemeInfo bean = new ThemeInfo();
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
				mLocalThemes.add(bean);
			} catch (NameNotFoundException e1) {
				LeoLog.e("Context", "getContext error");
			}
		}
	}

	private void loadInitOnlineTheme() {
		mOnlineThemes.clear();
		HttpRequestAgent.getInstance(this).loadOnlineTheme(mHideThemes,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						LeoLog.d("response", response.toString());
						List<ThemeInfo> list = ThemeJsonObjectParser
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
			List<ThemeInfo> list = (List<ThemeInfo>) object;
			if (list == null && list.isEmpty()) {
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
				mOnlineThemes.addAll((List<ThemeInfo>) object);
			}

			// filter local theme
			if (!mOnlineThemes.isEmpty()) {
				List<ThemeInfo> removeList = new ArrayList<ThemeInfo>();
				for (ThemeInfo themeInfo : mOnlineThemes) {
					for (ThemeInfo localInfo : mLocalThemes) {
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
				mOnlineThemeList.setVisibility(View.INVISIBLE);
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
		for (ThemeInfo info : mOnlineThemes) {
			loadedTheme.add(info.packageName);
		}

		HttpRequestAgent.getInstance(this).loadOnlineTheme(loadedTheme,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						List<ThemeInfo> list = ThemeJsonObjectParser
								.parserJsonObject(response);
						Message msg = mHandler.obtainMessage(
								MSG_LOAD_PAGE_DATA_SUCCESS, list);
						mHandler.sendMessage(msg);
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						mHandler.sendEmptyMessage(MSG_LOAD_PAGE_DATA_FAILED);
					}
				});
	}

	public void addMoreOnlineTheme(List<ThemeInfo> loadList) {
		boolean add;
		boolean newTheme = false;
		for (ThemeInfo appLockerThemeBean : loadList) {
			add = true;
			for (ThemeInfo themeInfo : mLocalThemes) {
				if (themeInfo.packageName
						.equals(appLockerThemeBean.packageName)) {
					add = false;
					break;
				}
			}
			if (add) {
				for (ThemeInfo themeInfo : mOnlineThemes) {
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
		if (mFrom != null && mFrom.equals("new_theme_tip")) {
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

	private ThemeInfo getDefaultTheme() {
		ThemeInfo defaultTheme = new ThemeInfo();
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
			mTvOnline.setTextColor(getResources().getColor(R.color.white));
			mTabContainer.setBackgroundResource(R.drawable.stacked_tabs_l);
		} else {
			mTvLocal.setTextColor(getResources().getColor(R.color.white));
			mTvOnline.setTextColor(getResources().getColor(
					R.color.tab_select_text));
			mTabContainer.setBackgroundResource(R.drawable.stacked_tabs_r);
		}
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
			if (mFrom != null && mFrom.equals("new_theme_tip")) {
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
							loadLocalTheme();
							mLocalThemeAdapter.notifyDataSetChanged();
							// if need to load online theme
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
							ThemeInfo remove = null;
							for (ThemeInfo info : mOnlineThemes) {
								if (info.packageName.equals(packageName)) {
									remove = info;
								}
							}
							if (remove != null) {
								mOnlineThemes.remove(remove);
								mOnlineThemeAdapter.notifyDataSetChanged();
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
			lastSelectedItem = (ThemeInfo) arg0.getItemAtPosition(arg2);
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
