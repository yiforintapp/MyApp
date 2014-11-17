package com.leo.appmaster.lockertheme;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.constants.Constants;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.lockertheme.LockerThemeChanageDialog.OnDiaogClickListener;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.model.AppLockerThemeBean;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.LeoLog;

public class LockerTheme extends Activity {
	private ListView listTheme;
	private List<AppLockerThemeBean> mThemes;
	private List<String> localThemes;
	private List<String> onlineThemes;// 在线包名
	private boolean flagGp = false;// 判断是否存在GP
	private AppLockerThemeBean itemTheme;
	private SharedPreferences sharedPreferences;
	private int number = 0;
	private String sharedPackageName;
	private LockerThemeAdapter mLockerThemeAdapter;
	private LockerThemeChanageDialog dialog ;
	private boolean mShouldLockOnRestart = false;
	private LayoutInflater mLayoutInflater;
	private Button mApply;
	private Button  mUninstall;
	private Button  mCancel;
	private TextView mText;

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
		mLayoutInflater=LayoutInflater.from(this);
		View dialog=mLayoutInflater.inflate(R.layout. dialog_theme_alarm, null);
		mApply=(Button)dialog. findViewById(R.id.apply);
		mUninstall=(Button) dialog. findViewById(R.id.uninstall);
		mCancel=(Button) dialog. findViewById(R.id.cancel);
		mText=(TextView) dialog.findViewById(R.id.dialogTV);
		initUI();
		onlineThemes = new ArrayList<String>();
		localThemes = new ArrayList<String>();
		mThemes = new ArrayList<AppLockerThemeBean>();
		mThemes.add(getDefaultData());
		getData();
		mLockerThemeAdapter = new LockerThemeAdapter(this, mThemes);
		listTheme.setAdapter(mLockerThemeAdapter);
		// 定向主题
		Intent intent = this.getIntent();
		String temp = intent.getStringExtra("theme_package");
		if (temp != null && !temp.equals("")) {
			for (int i = 0; i < mThemes.size(); i++) {
				if (mThemes.get(i).getPackageName().equals(temp)) {
					number = i;
				}
			}
		} else {
			number = 0;
		}
		listTheme.setSelection(number);// Item定向跳转
		listTheme.setOnItemClickListener(item);
		getTeme();
	}

	/**
	 * AlarmDialog
	 * 
	 * @param title
	 * @param content
	 */

	public void showAlarmDialog(String packageName,int visible) {
		dialog = new LockerThemeChanageDialog(this);
		dialog.setText(packageName);
		dialog.setVisibleUninstal(visible);
	dialog.show();
	
			dialog.setOnClickListener(new OnDiaogClickListener() {
				@Override
				public void onClick(int which) {
					if(which==0){					
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
						} else {
							itemTheme.setIsVisibility(Constants.GONE);
						}

						mLockerThemeAdapter.notifyDataSetChanged();
						dialog.cancel();
					}else if(which==1){
				dialog.cancel();
					}else if(which==2){
						// 卸载主题
						Uri uri = Uri.fromParts("package", itemTheme.getPackageName(),null);
						Intent intent = new Intent(Intent.ACTION_DELETE, uri);
						startActivity(intent);
						getTheme();
						for (int i = 0; i < mThemes.size(); i++) {
							if (mThemes.get(i).getPackageName().equals(itemTheme.getPackageName())&& !onlineThemes.contains(itemTheme.getPackageName())) {
								mThemes.remove(i);
							} else if ((mThemes.get(i).getPackageName().equals(itemTheme.getPackageName()))) {
								mThemes.get(i).setFlagName((String) LockerTheme.this.getResources().getText(R.string.onlinetheme));
							}
						}
						mLockerThemeAdapter.notifyDataSetChanged();
						dialog.cancel();
					}
				}
			});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LeoLog.e("LockOptionActivity", "onActivityResault: requestCode = "
				+ requestCode + "    resultCode = " + resultCode);
		mShouldLockOnRestart = false;
		super.onActivityResult(requestCode, resultCode, data);
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
	protected void onRestart() {
		super.onRestart();
		if (mShouldLockOnRestart) {
			showLockPage();
		} else {
			mShouldLockOnRestart = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * getTeme
	 */
	public void getTeme() {
		boolean flagPackge = false;
		// 获取本地已安装包信息
		List<AppDetailInfo> pkgInfos = AppLoadEngine.getInstance(
				LockerTheme.this).getAllPkgInfo();// 获取本地安装的所有包信息
		// 本地包名
		for (int i = 0; i < pkgInfos.size(); i++) {
			if (pkgInfos.get(i).getPkg().equals(Constants.GPPACKAGE)) {
				flagGp = true;
			}
			// flagPackge=mThemes.contains(pkgInfos.get(i).getPkg());
			flagPackge = pkgInfos.get(i).getPkg().startsWith("com.leo.theme");
			if (flagPackge) {
				localThemes.add(pkgInfos.get(i).getPkg());
			}
		}
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
							// mThemes.get(j).setThemeImage(saveContext.getResources().getDrawable(R.drawable.moonnight_theme));
							mThemes.get(j).setThemeImage(
									this.getResources().getDrawable(
											R.drawable.moonnight_theme));
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
		for (int a = 0; a < mThemes.size(); a++) {
			onlineThemes.add(mThemes.get(a).getPackageName());
		}
	}

	public OnItemClickListener item = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			itemTheme = (AppLockerThemeBean) arg0.getItemAtPosition(arg2);
			String[] urls = itemTheme.getUrl();
			if (mThemes
					.get(arg2)
					.getFlagName()
					.equals((String) getResources().getText(
							R.string.onlinetheme))) {
				for (int i = 0; i < urls.length; i++) {
					boolean flag = AppwallHttpUtil.isHttpUrl(urls[i]);
					if (flagGp) {
						if (!flag) {
							AppwallHttpUtil.requestGp(LockerTheme.this, urls[i]);
							break;
						}
					} else if (flag) {
						AppwallHttpUtil.requestUrl(LockerTheme.this, urls[i]);
						break;
					}
				}
			} else if ((mThemes.get(arg2).getFlagName().equals((String) getResources().getText(R.string.localtheme)) || mThemes.get(arg2).getFlagName().equals((String) getResources().getText(R.string.defaultTheme)))&& !mThemes.get(arg2).getPackageName().equals(AppMasterApplication.sharedPackage)) {
			if(mThemes.get(arg2).getFlagName().equals((String) getResources().getText(R.string.defaultTheme))){
				showAlarmDialog(itemTheme.getThemeName(),View.GONE);
			}else{
				showAlarmDialog(itemTheme.getThemeName(),View.VISIBLE);
			}
			}
		}
	};

	private void getData() {
		/*
		 * ------------------------------------------构造数据------------------------
		 * 
		 */
		// Theme1
		AppLockerThemeBean moonnightTheme = new AppLockerThemeBean();
		moonnightTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.moonnight_theme));
		moonnightTheme.setThemeName((String) this.getResources().getText(
				R.string.moonightTheme));
		String[] moonnightUrl = new String[2];
		moonnightUrl[1] = "http://download.leoers.com/am/Theme1.apk";
		moonnightUrl[0] = "com.leo.theme.moonight";
		moonnightTheme.setUrl(moonnightUrl);
		moonnightTheme.setPackageName("com.leo.theme");
		moonnightTheme.setFlagName((String) this.getResources().getText(
				R.string.onlinetheme));
		moonnightTheme.setIsVisibility(Constants.VISIBLE);
		mThemes.add(moonnightTheme);
		// Theme2
		AppLockerThemeBean orangeTheme = new AppLockerThemeBean();
		orangeTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.orange_theme));
		orangeTheme.setThemeName((String) this.getResources().getText(
				R.string.orangeTheme));
		String[] orangeUrl = new String[2];
		orangeUrl[1] = "http://download.leoers.com/am/Theme1.apk";
		orangeUrl[0] = "com.mah.calldetailscreen";
		orangeTheme.setUrl(orangeUrl);
		orangeTheme.setPackageName("com.mah.calldetailscreen");
		orangeTheme.setFlagName((String) this.getResources().getText(
				R.string.onlinetheme));
		orangeTheme.setIsVisibility(Constants.GONE);
		mThemes.add(orangeTheme);
		// Theme3
		AppLockerThemeBean paradoxTheme = new AppLockerThemeBean();
		paradoxTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.paradox_theme));
		paradoxTheme.setThemeName((String) this.getResources().getText(
				R.string.ParadoxTheme));
		String[] paradoxUrl = new String[2];
		paradoxUrl[1] = "http://download.leoers.com/am/Theme1.apk";
		paradoxUrl[0] = "com.mah.calldetailscreen";
		paradoxTheme.setUrl(orangeUrl);
		paradoxTheme.setPackageName("com.mah.calldetailscreen");
		paradoxTheme.setFlagName((String) this.getResources().getText(
				R.string.onlinetheme));
		paradoxTheme.setIsVisibility(Constants.GONE);
		mThemes.add(paradoxTheme);
		getOnlineThemePackage();
		/*
		 * ----------------------------------------------------------------------
		 *
		 */

	}

	/**
	 * 默认主题
	 */
	private AppLockerThemeBean getDefaultData() {
		AppLockerThemeBean defaultTheme = new AppLockerThemeBean();
		defaultTheme.setThemeImage(this.getResources().getDrawable(
				R.drawable.app_list_bg));
		defaultTheme.setThemeName((String) this.getResources().getText(
				R.string.ParadoxTheme));
		String[] defaultUrl = new String[2];
		defaultUrl[1] = "";
		defaultUrl[0] = "com.leo.appmaster";
		defaultTheme.setUrl(defaultUrl);
		defaultTheme.setPackageName("com.leo.appmaster");
		defaultTheme.setFlagName((String) this.getResources().getText(
				R.string.defaultTheme));
		defaultTheme.setThemeName((String)this.getResources().getText(R.string.defaultTheme));
		defaultTheme.setIsVisibility(Constants.VISIBLE);
		return defaultTheme;
	}
}
