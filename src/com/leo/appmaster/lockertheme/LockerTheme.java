package com.leo.appmaster.lockertheme;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
	private List<String> onlineThemes;// 在线包名
	private boolean flagGp = false;// 判断是否存在GP
	public AppLockerThemeBean itemTheme;
	private SharedPreferences sharedPreferences;
	private int number = 0;
	private String sharedPackageName;
	private LockerThemeAdapter mLockerThemeAdapter;
	private LockerThemeChanageDialog dialog;
	private boolean mShouldLockOnRestart = false;
	private LayoutInflater mLayoutInflater;
	private Button mApply;
	private Button mUninstall;
	private Button mCancel;
	private TextView mText;
	private LockerThemeReceive mLockerThemeReceive;
	private boolean mNeedLock = false;

	private void initUI() {
		CommonTitleBar title = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		title.setTitle(R.string.lockerTheme);
		title.openBackView();
		listTheme = (ListView) findViewById(R.id.themeLV);
		sharedPreferences = getSharedPreferences("lockerTheme",
				Context.MODE_WORLD_WRITEABLE);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		boolean flagPackge = false;
		setContentView(R.layout.activity_locker_theme);

		/*
		 * 注册卸载监听
		 */
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addDataScheme("package");
		mLockerThemeReceive = new LockerThemeReceive();
		registerReceiver(mLockerThemeReceive, intentFilter);

		mLayoutInflater = LayoutInflater.from(this);
		View dialog = mLayoutInflater
				.inflate(R.layout.dialog_theme_alarm, null);
		mApply = (Button) dialog.findViewById(R.id.apply);
		mUninstall = (Button) dialog.findViewById(R.id.uninstall);
		mCancel = (Button) dialog.findViewById(R.id.cancel);
		mText = (TextView) dialog.findViewById(R.id.dialogTV);
		initUI();
		onlineThemes = new ArrayList<String>();
		localThemes = new ArrayList<String>();
		AppMasterPreference preference = AppMasterPreference
				.getInstance(LockerTheme.this);
		localThemes = preference.getHideThemeList();
		mThemes = new ArrayList<AppLockerThemeBean>();
		mThemes.add(getDefaultData());
		getData();
		getOnlineThemePackage();
		mLockerThemeAdapter = new LockerThemeAdapter(this, mThemes);
		listTheme.setAdapter(mLockerThemeAdapter);
		// 定向主题
		Intent intent = this.getIntent();
		String temp = intent.getStringExtra("theme_package");
		mNeedLock = intent.getBooleanExtra("need_lock", false);
		if (temp != null && !temp.equals("")) {
			for (int i = 0; i < mThemes.size(); i++) {
				if (mThemes.get(i).getPackageName().equals(temp)) {
					number = i;
					showAlarmDialog(mThemes.get(i).getThemeName(), View.VISIBLE);
					itemTheme = mThemes.get(i);

				}
			}

			tryHideThemeApk(temp);
		} else {
			number = 0;
		}
		listTheme.setSelection(number);// Item定向跳转
		listTheme.setOnItemClickListener(item);
		getTeme();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mThemes = null;
		mThemes = new ArrayList<AppLockerThemeBean>();
		mThemes.add(getDefaultData());
		getData();
		getOnlineThemePackage();
		getTeme();
	}

	public void tryHideThemeApk(String pkg) {
		if (pkg.startsWith("com.leo.theme")) {
			PackageManager pm = this.getPackageManager();
			ComponentName name = new ComponentName(pkg,
					"com.leo.theme.ThemeActivity");
			int res = pm.getComponentEnabledSetting(name);
			if (res == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
					|| res == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
				LeoLog.d("tryHideThemeApk", "packageName = " + pkg);
				pm.setComponentEnabledSetting(name,
						PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
			}
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
						if (mThemes.get(i).getPackageName()
								.equals(AppMasterApplication.sharedPackage)) {
							mThemes.get(i).setIsVisibility(Constants.GONE);
						}
					}
					AppMasterApplication.setSharedPreferencesValue(itemTheme
							.getPackageName());

					String sharedPackageName = itemTheme.getPackageName();
					if (itemTheme.getPackageName().equals(sharedPackageName)) {
						itemTheme.setIsVisibility(Constants.VISIBLE);
						itemTheme.setFlagName((String) LockerTheme.this
								.getResources().getText(R.string.localtheme));
					} else {
						itemTheme.setIsVisibility(Constants.GONE);
					}

					SDKWrapper.addEvent(LockerTheme.this, LeoStat.P1,
							"theme_apply", itemTheme.getPackageName());
					mLockerThemeAdapter.notifyDataSetChanged();
					dialog.cancel();
				} else if (which == 1) {
					dialog.cancel();
				} else if (which == 2) {
					// 卸载主题
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
	 * getTeme
	 */
	public void getTeme() {
		boolean flagPackge = false;
		for (int i = 0; i < localThemes.size(); i++) {
			Context saveContext = null;
			try {
				saveContext = LockerTheme.this.createPackageContext(
						localThemes.get(i), Context.CONTEXT_IGNORE_SECURITY);
			} catch (NameNotFoundException e1) {
				LeoLog.i("Context", "getContext error");
			}

			boolean flag = onlineThemes.contains(localThemes.get(i));
			if (flag) {
				for (int j = 0; j < onlineThemes.size(); j++) {
					if (onlineThemes.get(j).equals("com.leo.appmaster")) {
						mThemes.get(0).setFlagName(
								(String) getResources().getText(
										R.string.defaultTheme));
					} else {
						if (onlineThemes.get(j).equals(localThemes.get(i))) {
							mThemes.get(j).setFlagName(
									(String) getResources().getText(
											R.string.localtheme));
							mThemes.get(j).setThemeImage(mThemes.get(j).getThemeImage());
							/*int themeres = saveContext.getResources()
									.getIdentifier("lockertheme", "drawable",
											saveContext.getPackageName());

							if (themeres > 0) {
								mThemes.get(j).setThemeImage(saveContext.getResources()
										.getDrawable(themeres));
							} else {
								mThemes.get(j).setThemeImage(this.getResources().getDrawable(
										R.drawable.app_list_bg));
							}*/
						} else {
							mThemes.get(j).setFlagName(
									(String) getResources().getText(
											R.string.onlinetheme));
						}
					}
				}
			} else {
				AppLockerThemeBean tempTheme = new AppLockerThemeBean();
				// tempTheme.setFlagName((String)saveContext.getResources().getText(R.string.localtheme));
				// tempTheme.setThemeImage(saveContext.getResources().getDrawable(R.drawable.splash_icon));
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
				tempTheme.setThemeName("");
				tempTheme.setPackageName(localThemes.get(i));
				mThemes.add(tempTheme);
			}
		}
		// 标记当前使用的主题
		sharedPackageName = AppMasterApplication.sharedPackage;
		for (int i = 0; i < mThemes.size(); i++) {
			if (mThemes.get(i).getPackageName().equals(sharedPackageName)) {
				mThemes.get(i).setIsVisibility(Constants.VISIBLE);
			} else {
				mThemes.get(i).setIsVisibility(Constants.GONE);
			}
		}

	}

	public void getOnlineThemePackage() {
		// 获取mThemes包名
		for (int i = 0; i < mThemes.size(); i++) {
			onlineThemes.add(mThemes.get(i).getPackageName());
		}
	}

	public OnItemClickListener item = new OnItemClickListener() {

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
					if (flagGp) {
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
			} else if ((mThemes
					.get(arg2)
					.getFlagName()
					.equals((String) getResources()
							.getText(R.string.localtheme)) || mThemes
					.get(arg2)
					.getFlagName()
					.equals((String) getResources().getText(
							R.string.defaultTheme)))
					&& !mThemes.get(arg2).getPackageName()
							.equals(AppMasterApplication.sharedPackage)) {
				if (mThemes
						.get(arg2)
						.getFlagName()
						.equals((String) getResources().getText(
								R.string.defaultTheme))) {
					showAlarmDialog(itemTheme.getThemeName(), View.GONE);
				} else {
					showAlarmDialog(itemTheme.getThemeName(), View.VISIBLE);
				}
			}
		}
	};

	private void getData() {
		/*
		 * ------------------------------------------构造数据------------------------
		 */
		AppLockerThemeBean christmasTheme = new AppLockerThemeBean();
		christmasTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.christmas_theme));
		christmasTheme.setThemeName((String) this.getResources().getText(
				R.string.christmas_theme));
		String[] christmasUrl = new String[2];
		christmasUrl[1] = "http://testd.leostat.com/am/christmas.apk";
		christmasUrl[0] = "com.leo.theme.christmas";
		christmasTheme.setUrl(christmasUrl);
		christmasTheme.setPackageName("com.leo.theme.christmas");
		christmasTheme.setFlagName((String) this.getResources().getText(
				R.string.onlinetheme));
		christmasTheme.setIsVisibility(Constants.VISIBLE);
		mThemes.add(christmasTheme);
		// Theme2
		AppLockerThemeBean moonnightTheme = new AppLockerThemeBean();
		moonnightTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.moonnight_theme));
		moonnightTheme.setThemeName((String) this.getResources().getText(
				R.string.moonightTheme));
		String[] moonnightUrl = new String[2];
		moonnightUrl[1] = "http://testd.leostat.com/am/master.apk";
		moonnightUrl[0] = "com.leo.theme.moonnight";
		moonnightTheme.setUrl(moonnightUrl);
		moonnightTheme.setPackageName("com.leo.theme.moonnight");
		moonnightTheme.setFlagName((String) this.getResources().getText(
				R.string.onlinetheme));
		moonnightTheme.setIsVisibility(Constants.GONE);
		mThemes.add(moonnightTheme);
		// Theme3
		AppLockerThemeBean orangeTheme = new AppLockerThemeBean();
		orangeTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.orange_theme));
		orangeTheme.setThemeName((String) this.getResources().getText(
				R.string.orangeTheme));
		String[] orangeUrl = new String[2];
		orangeUrl[1] = "http://testd.leostat.com/am/orange.apk";
		orangeUrl[0] = "com.leo.theme.orange";
		orangeTheme.setUrl(orangeUrl);
		orangeTheme.setPackageName("com.leo.theme.orange");
		orangeTheme.setFlagName((String) this.getResources().getText(
				R.string.onlinetheme));
		orangeTheme.setIsVisibility(Constants.GONE);
		mThemes.add(orangeTheme);
		
		// Theme4
		AppLockerThemeBean paradoxTheme = new AppLockerThemeBean();
		paradoxTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.paradox_theme));
		paradoxTheme.setThemeName((String) this.getResources().getText(
				R.string.ParadoxTheme));
		String[] paradoxUrl = new String[2];
		paradoxUrl[1] = "http://testd.leostat.com/am/contradict.apk";
		paradoxUrl[0] = "com.leo.theme.contradict";
		paradoxTheme.setUrl(orangeUrl);
		paradoxTheme.setPackageName("com.leo.theme.contradict");
		paradoxTheme.setFlagName((String) this.getResources().getText(
				R.string.onlinetheme));
		paradoxTheme.setIsVisibility(Constants.GONE);
		mThemes.add(paradoxTheme);
		 

		/*
		 * ----------------------------------------------------------------------
		 */

	}

	/**
	 * 默认主题
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
		return defaultTheme;
	}

	private class LockerThemeReceive extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				getTheme();
				if (packageName.equals(sharedPackageName)) {
					AppMasterApplication
							.setSharedPreferencesValue("com.leo.appmaster");
					mThemes.get(0).setIsVisibility(Constants.VISIBLE);
					mLockerThemeAdapter.notifyDataSetChanged();
				}
				for (int i = 0; i < mThemes.size(); i++) {
					if (mThemes.get(i).getPackageName().equals(packageName)
							&& !onlineThemes.contains(packageName)) {
						mThemes.remove(i);
					} else if ((mThemes.get(i).getPackageName()
							.equals(itemTheme.getPackageName()))) {
						/*
						 * if(packageName.equals(sharedPackageName)){
						 * AppMasterApplication
						 * .setSharedPreferencesValue("com.leo.appmaster");
						 * mThemes.get(0).setIsVisibility(Constants.VISIBLE); }
						 */
						mThemes.get(i).setFlagName(
								(String) LockerTheme.this.getResources()
										.getText(R.string.onlinetheme));
						mThemes.get(i).setIsVisibility(Constants.GONE);
						mLockerThemeAdapter.notifyDataSetChanged();
					}
				}
				itemTheme.setFlagName((String) LockerTheme.this.getResources()
						.getText(R.string.onlinetheme));
				mLockerThemeAdapter.notifyDataSetChanged();
			}
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
				String packageName = intent.getData().getSchemeSpecificPart();
/*				boolean installNameContainOnline = onlineThemes
						.contains(packageName);
				if (installNameContainOnline) {
					for (int i = 0; i < onlineThemes.size(); i++) {
						if (onlineThemes.get(i).equals(packageName)) {
							for (int j = 0; j < mThemes.size(); j++) {
								if (packageName.equals(mThemes.get(j)
										.getPackageName())) {
									mThemes.get(j)
											.setFlagName(
													(String) LockerTheme.this
															.getResources()
															.getText(
																	R.string.localtheme));
								}
							}
						} else {

						}
					}
				}
*/
			}
		}
	}
}
