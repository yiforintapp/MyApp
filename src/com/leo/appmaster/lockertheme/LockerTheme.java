package com.leo.appmaster.lockertheme;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.R;
import com.leo.appmaster.SDKWrapper;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.constants.Constants;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.lockertheme.LockerThemeChanageDialog.OnDiaogClickListener;
import com.leo.appmaster.model.AppLockerThemeBean;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;

public class LockerTheme extends BaseActivity {
	private ListView listTheme;
	private List<AppLockerThemeBean> mThemes;
	private List<String> localThemes;
	private boolean mFlagGp = false;
	public AppLockerThemeBean itemTheme;
	private int number = 0;
	private LockerThemeAdapter mLockerThemeAdapter;
	private LockerThemeChanageDialog dialog;
	private boolean mShouldLockOnRestart = true;
	private LockerThemeReceive mLockerThemeReceive;
	private boolean mNeedLock = false;
	private AppMasterPreference mPreference;

	private void initUI() {
		CommonTitleBar title = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		title.setTitle(R.string.lockerTheme);
		title.openBackView();
		listTheme = (ListView) findViewById(R.id.themeLV);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_locker_theme);

		/*
		 * Uninstall Listener
		 */
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addDataScheme("package");
		mLockerThemeReceive = new LockerThemeReceive();
		registerReceiver(mLockerThemeReceive, intentFilter);
		initUI();
		mThemes = new ArrayList<AppLockerThemeBean>();

		loadThemeData();
		mLockerThemeAdapter = new LockerThemeAdapter(this, mThemes);
		listTheme.setAdapter(mLockerThemeAdapter);
		// Directional Theme
		Intent intent = this.getIntent();
		String temp = intent.getStringExtra("theme_package");
		mNeedLock = intent.getBooleanExtra("need_lock", false);
		if (temp != null && !temp.equals("")) {
			checkSendHideThemeBroadcast(temp);
			for (int i = 0; i < mThemes.size(); i++) {
				if (mThemes.get(i).getPackageName().equals(temp)) {
					number = i;
					showAlarmDialog(mThemes.get(i).getThemeName(), View.VISIBLE);
					itemTheme = mThemes.get(i);
					mLockerThemeAdapter.notifyDataSetChanged();
				}
			}
		} else {
			number = 0;
		}
		listTheme.setSelection(number);
		listTheme.setOnItemClickListener(itemListener);
		mFlagGp = isInstallPackageName("com.android.vending");
	}

	@Override
	protected void onResume() {
		loadThemeData();
		mLockerThemeAdapter.notifyDataSetChanged();
		super.onResume();
	}

	private void loadThemeData() {
		mThemes.clear();
		mPreference = AppMasterPreference.getInstance(LockerTheme.this);
		localThemes = mPreference.getHideThemeList();
		mThemes.add(getDefaultData());
		getOriginalTheme();
		getThirdTheme();
	}

	public void checkSendHideThemeBroadcast(String pkg) {
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
					for (int i = 0; i < mThemes.size(); i++) {
						mThemes.get(i).setIsVisibility(Constants.GONE);
					}
					AppMasterApplication.setSharedPreferencesValue(itemTheme
							.getPackageName());
					itemTheme.setIsVisibility(Constants.VISIBLE);
					itemTheme.setFlagName((String) LockerTheme.this
							.getResources().getText(R.string.localtheme));
					LeoLog.e("xxxx", "itemTheme = " + itemTheme);
					loadThemeData();
					mLockerThemeAdapter.notifyDataSetChanged();
					SDKWrapper.addEvent(LockerTheme.this, LeoStat.P1,
							"theme_apply", itemTheme.getPackageName());
					dialog.cancel();
				} else if (which == 1) {
					dialog.cancel();
				} else if (which == 2) {
					// Uninstall Theme
					Uri uri = Uri.fromParts("package",
							itemTheme.getPackageName(), null);
					Intent intent = new Intent(Intent.ACTION_DELETE, uri);
					startActivity(intent);
					mLockerThemeAdapter.notifyDataSetChanged();
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

	/**
	 * getThirdTheme
	 */
	public void getThirdTheme() {
		Context saveContext = null;

		List<String> havedLoad = new ArrayList<String>();
		for (AppLockerThemeBean bean : mThemes) {
			havedLoad.add(bean.getPackageName());
		}

		for (int i = 0; i < localThemes.size(); i++) {
			if (!havedLoad.contains(localThemes.get(i))) {
				try {
					saveContext = LockerTheme.this
							.createPackageContext(localThemes.get(i),
									Context.CONTEXT_IGNORE_SECURITY);
				} catch (NameNotFoundException e1) {
					LeoLog.i("Context", "getContext error");
				}
				AppLockerThemeBean tempTheme = new AppLockerThemeBean();
				String str = (String) saveContext.getResources().getText(
						saveContext.getResources().getIdentifier("app_name",
								"string", saveContext.getPackageName()));
				tempTheme.setThemeName(str);
				int themeres = saveContext.getResources()
						.getIdentifier("lockertheme", "drawable",
								saveContext.getPackageName());

				if (themeres > 0) {
					tempTheme.setThemeImage(saveContext.getResources()
							.getDrawable(themeres));
				} else {
					tempTheme.setThemeImage(this.getResources().getDrawable(
							R.drawable.app_list_bg));
				}
				tempTheme.setFlagName((String) this.getResources().getText(
						R.string.localtheme));
				tempTheme.setPackageName(localThemes.get(i));

				if (AppMasterApplication.sharedPackage.equals(localThemes
						.get(i))) {
					tempTheme.setIsVisibility(Constants.VISIBLE);
				} else {
					tempTheme.setIsVisibility(Constants.GONE);
				}
				mThemes.add(tempTheme);
			}
		}
	}

	public OnItemClickListener itemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			itemTheme = (AppLockerThemeBean) arg0.getItemAtPosition(arg2);
			String[] urls = itemTheme.getUrl();
			/* user click the theme */
			SDKWrapper.addEvent(LockerTheme.this, LeoStat.P1, "theme_choice",
					itemTheme.getPackageName());
			if (mThemes
					.get(arg2)
					.getFlagName()
					.equals((String) getResources().getText(
							R.string.onlinetheme))) {
				for (int i = 0; i < urls.length; i++) {
					boolean flag = AppwallHttpUtil.isHttpUrl(urls[i]);
					if (mFlagGp) {
						if (!flag) {
							AppwallHttpUtil
									.requestGp(LockerTheme.this, urls[i]);
							break;
						}
					} else if (flag) {
						AppwallHttpUtil.requestUrl(LockerTheme.this, urls[i]);
						break;
					}
				}
			} else if (!mThemes.get(arg2).getPackageName()
					.equals(AppMasterApplication.sharedPackage)) {
				if (mThemes.get(arg2).getPackageName()
						.equals("com.leo.appmaster")) {
					showAlarmDialog(itemTheme.getThemeName(), View.GONE);
				} else {
					showAlarmDialog(itemTheme.getThemeName(), View.VISIBLE);
				}
			}
		}
	};

	private boolean isInstallPackageName(String packageName) {
		boolean installed = true;
		try {
			PackageInfo packageInfo = this.getPackageManager().getPackageInfo(
					packageName, 0);
			if (packageInfo == null) {
				installed = false;
			} else {
				installed = true;
			}
		} catch (Exception e) {
			installed = false;
		}
		return installed;
	}

	private void getOriginalTheme() {
		/*
		 * ------------------------------------------构造数据------------------------
		 */
		AppLockerThemeBean christmasTheme = new AppLockerThemeBean();
		christmasTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.christmas_theme));
		christmasTheme.setThemeName((String) this.getResources().getText(
				R.string.christmas_theme));
		String[] christmasUrl = new String[2];
		christmasUrl[1] = "http://dl.leomaster.com/files/christmas.apk";
		christmasUrl[0] = "com.leo.theme.christmas";
		christmasTheme.setUrl(christmasUrl);
		christmasTheme.setPackageName("com.leo.theme.christmas");
		if (localThemes.contains("com.leo.theme.christmas")) {
			christmasTheme.setFlagName((String) this.getResources().getText(
					R.string.localtheme));
		} else {
			christmasTheme.setFlagName((String) this.getResources().getText(
					R.string.onlinetheme));
		}
		if (AppMasterApplication.sharedPackage
				.equals("com.leo.theme.christmas")) {
			christmasTheme.setIsVisibility(Constants.VISIBLE);
		} else {
			christmasTheme.setIsVisibility(Constants.GONE);
		}

		mThemes.add(christmasTheme);

		// Theme2
		AppLockerThemeBean moonnightTheme = new AppLockerThemeBean();
		moonnightTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.moonnight_theme));
		moonnightTheme.setThemeName((String) this.getResources().getText(
				R.string.moonightTheme));
		String[] moonnightUrl = new String[2];
		moonnightUrl[1] = "http://dl.leomaster.com/files/moonnight.apk";
		moonnightUrl[0] = "com.leo.theme.moonnight";
		moonnightTheme.setUrl(moonnightUrl);
		moonnightTheme.setPackageName("com.leo.theme.moonnight");
		if (localThemes.contains("com.leo.theme.moonnight")) {
			moonnightTheme.setFlagName((String) this.getResources().getText(
					R.string.localtheme));
		} else {
			moonnightTheme.setFlagName((String) this.getResources().getText(
					R.string.onlinetheme));
		}
		if (AppMasterApplication.sharedPackage
				.equals("com.leo.theme.moonnight")) {
			moonnightTheme.setIsVisibility(Constants.VISIBLE);
		} else {
			moonnightTheme.setIsVisibility(Constants.GONE);
		}
		mThemes.add(moonnightTheme);

		// Theme3
		AppLockerThemeBean orangeTheme = new AppLockerThemeBean();
		orangeTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.orange_theme));
		orangeTheme.setThemeName((String) this.getResources().getText(
				R.string.orangeTheme));
		String[] orangeUrl = new String[2];
		orangeUrl[1] = "http://dl.leomaster.com/files/fruit.apk";
		orangeUrl[0] = "com.leo.theme.orange";
		orangeTheme.setUrl(orangeUrl);
		orangeTheme.setPackageName("com.leo.theme.orange");
		if (localThemes.contains("com.leo.theme.orange")) {
			orangeTheme.setFlagName((String) this.getResources().getText(
					R.string.localtheme));
		} else {
			orangeTheme.setFlagName((String) this.getResources().getText(
					R.string.onlinetheme));
		}

		if (AppMasterApplication.sharedPackage.equals("com.leo.theme.orange")) {
			orangeTheme.setIsVisibility(Constants.VISIBLE);
		} else {
			orangeTheme.setIsVisibility(Constants.GONE);
		}
		mThemes.add(orangeTheme);
		// Theme4
		AppLockerThemeBean paradoxTheme = new AppLockerThemeBean();
		paradoxTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.paradox_theme));
		paradoxTheme.setThemeName((String) this.getResources().getText(
				R.string.ParadoxTheme));
		String[] paradoxUrl = new String[2];
		paradoxUrl[1] = "http://dl.leomaster.com/files/spatial.apk";
		paradoxUrl[0] = "com.leo.theme.contradict";
		paradoxTheme.setUrl(paradoxUrl);
		paradoxTheme.setPackageName("com.leo.theme.contradict");

		if (localThemes.contains("com.leo.theme.contradict")) {
			paradoxTheme.setFlagName((String) this.getResources().getText(
					R.string.localtheme));
		} else {
			paradoxTheme.setFlagName((String) this.getResources().getText(
					R.string.onlinetheme));
		}
		if (AppMasterApplication.sharedPackage
				.equals("com.leo.theme.contradict")) {
			paradoxTheme.setIsVisibility(Constants.VISIBLE);
		} else {
			paradoxTheme.setIsVisibility(Constants.GONE);
		}
		mThemes.add(paradoxTheme);
		/*
		 * ----------------------------------------------------------------------
		 */

	}

	/**
	 * Default Theme
	 */
	private AppLockerThemeBean getDefaultData() {
		AppLockerThemeBean defaultTheme = new AppLockerThemeBean();
		defaultTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.default_theme));
		defaultTheme.setThemeName((String) this.getResources().getText(
				R.string.ParadoxTheme));
		String[] defaultUrl = new String[2];
		defaultUrl[1] = "";
		defaultUrl[0] = "com.leo.appmaster";
		defaultTheme.setUrl(defaultUrl);
		defaultTheme.setPackageName("com.leo.appmaster");
		defaultTheme.setFlagName((String) this.getResources().getText(
				R.string.defaultTheme));
		defaultTheme.setThemeName((String) this.getResources().getText(
				R.string.defaultTheme));
		defaultTheme.setIsVisibility(Constants.VISIBLE);

		if (AppMasterApplication.sharedPackage.equals("com.leo.appmaster")) {
			defaultTheme.setIsVisibility(Constants.VISIBLE);
		} else {
			defaultTheme.setIsVisibility(Constants.GONE);
		}

		return defaultTheme;
	}

	private class LockerThemeReceive extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				if (packageName != null && packageName.equals("com.leo.theme")) {
					listTheme.postDelayed(new Runnable() {
						@Override
						public void run() {
							loadThemeData();
							mLockerThemeAdapter.notifyDataSetChanged();
						}
					}, 2000);
				}
			}
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				if (packageName != null && packageName.equals("com.leo.theme")) {
					listTheme.postDelayed(new Runnable() {
						@Override
						public void run() {
							loadThemeData();
							mLockerThemeAdapter.notifyDataSetChanged();
						}
					}, 2000);
				}

			}
		}
	}
}
