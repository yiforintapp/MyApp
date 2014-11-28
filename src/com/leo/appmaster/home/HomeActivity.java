package com.leo.appmaster.home;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.appsetting.AboutActivity;
import com.leo.appmaster.appwall.AppWallActivity;
import com.leo.appmaster.backup.AppBackupRestoreActivity;
import com.leo.appmaster.cleanmemory.CleanMemActivity;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.feedback.FeedbackHelper;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.sdk.MainViewActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.CricleView;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.RootChecker;
import com.leo.appmaster.utils.TextFormater;
import com.leoers.leoanalytics.LeoStat;

public class HomeActivity extends MainViewActivity implements OnClickListener,
		OnTouchListener, AppChangeListener {

    private final static String KEY_ROOT_CHECK = "root_check";
    
	private View mPictureHide;
	private View mAppLock;
	private View mAppBackup;
	private View mCleanMem;
	private ImageView mSettingIcon;

	private View mPressedEffect1;
	private View mPressedEffect2;

	private TextView mMemoryPercent;
	private TextView mTvMemoryInfo;
	private TextView mTvFlow;
	private ImageView mIvDigital_0, mIvDigital_1, mIvDigital_2;
	private TextView mLockedApp;
	private CommonTitleBar mTtileBar;

	private LeoPopMenu mLeoPopMenu;
	private CricleView mCricleView;
	private ImageView spiner;
	private String themeHome;
	private SharedPreferences mySharedPreferences;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		 spiner=(ImageView) findViewById(R.id.image1);
		initUI();
		AppLoadEngine.getInstance(this).registerAppChangeListener(this);
		// commit feedbacks if any
		FeedbackHelper.getInstance().tryCommit();
		installShortcut();
		
		// Root chack
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sp.getBoolean(KEY_ROOT_CHECK, true)) {
            boolean root = RootChecker.isRoot();
            if(root) {
                SDKWrapper.addEvent(getApplicationContext(), LeoStat.P1, KEY_ROOT_CHECK, "root");
            }
            sp.edit().putBoolean(KEY_ROOT_CHECK, false).commit();
        }
	}

	private void judgeShowGradeTip() {
		mTtileBar.postDelayed(new Runnable() {
			@Override
			public void run() {
				ActivityManager mActivityManager = (ActivityManager) HomeActivity.this
						.getSystemService(Context.ACTIVITY_SERVICE);

				RunningTaskInfo topTaskInfo = mActivityManager.getRunningTasks(
						1).get(0);

				String pkg = HomeActivity.this.getPackageName();
				if (pkg.equals(topTaskInfo.baseActivity.getPackageName())) {
					long count = AppMasterPreference.getInstance(
							HomeActivity.this).getUnlockCount();
					boolean haveTip = AppMasterPreference.getInstance(
							HomeActivity.this).getGoogleTipShowed();
					if (count >= 50 && !haveTip) {
						Intent intent = new Intent(HomeActivity.this,
								GradeTipActivity.class);
						HomeActivity.this.startActivity(intent);
					}
				}
			}
		}, 5000);
	}

	private void installShortcut() {
		SharedPreferences prefernece = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean installed = prefernece.getBoolean("shortcut", false);
		if (!installed) {
			Intent shortcutIntent = new Intent(this, SplashActivity.class);
			shortcutIntent.setAction(Intent.ACTION_MAIN);
			shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			// shortcutIntent.setClassName("com.leo.appmaster",
			// "com.leo.appmaster.home.SplashActivity");

			Intent shortcut = new Intent(
					"com.android.launcher.action.INSTALL_SHORTCUT");
			shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
					getString(R.string.app_name));
			shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			ShortcutIconResource iconRes = Intent.ShortcutIconResource
					.fromContext(this, R.drawable.ic_launcher);
			shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
			shortcut.putExtra("duplicate", false);
			shortcut.putExtra("from_shortcut", true);
			sendBroadcast(shortcut);
			prefernece.edit().putBoolean("shortcut", true).commit();
		}
	}

	@Override
	protected void onDestroy() {
		AppLoadEngine.getInstance(this).unregisterAppChangeListener(this);
		super.onDestroy();
	}

	private void initUI() {
		mIvDigital_0 = (ImageView) findViewById(R.id.digital_0);
		mIvDigital_1 = (ImageView) findViewById(R.id.digital_1);
		mIvDigital_2 = (ImageView) findViewById(R.id.digital_2);
		mLockedApp = (TextView) findViewById(R.id.tv_lock_count);

		mTvMemoryInfo = (TextView) findViewById(R.id.tv_memory_info);
		mTvFlow = (TextView) findViewById(R.id.tv_flow);
		mMemoryPercent = (TextView) findViewById(R.id.tv_memory_percent);
		mCricleView = (CricleView) findViewById(R.id.cricle_view);

		mPictureHide = findViewById(R.id.tv_picture_hide);
		mAppLock = findViewById(R.id.tv_app_lock);
		mAppBackup = findViewById(R.id.tv_app_backup);
		mCleanMem = findViewById(R.id.tv_clean_memory);

		mSettingIcon = (ImageView) findViewById(R.id.setting_icon);

		mPictureHide.setOnClickListener(this);
		mAppLock.setOnClickListener(this);
		mAppBackup.setOnTouchListener(this);
		mAppLock.setOnTouchListener(this);
		mAppBackup.setOnClickListener(this);
		mCleanMem.setOnClickListener(this);

		mPressedEffect1 = findViewById(R.id.pressed_effect1);
		mPressedEffect2 = findViewById(R.id.pressed_effect2);
		
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		 mySharedPreferences= getSharedPreferences("LockerThemeHome",HomeActivity.this.MODE_WORLD_WRITEABLE);			
		themeHome=mySharedPreferences.getString("themeHome","0");
			if (themeHome.equals("0")) {
				spiner.setImageDrawable(this.getResources().getDrawable(R.drawable.themetip_spiner_press));
			} else {
				spiner.setImageDrawable(this.getResources().getDrawable(R.drawable.theme_spiner_press));
			}
		mTtileBar.setTitle(R.string.app_name);
		mTtileBar.setBackArrowVisibility(View.GONE);
		mTtileBar.setOptionImageVisibility(View.VISIBLE);
		mTtileBar.setOptionText("");
		mTtileBar.setOptionImage(R.drawable.setting_btn);
		mTtileBar.setOptionListener(this);
		mTtileBar.setSpinerVibility(View.VISIBLE);
		mTtileBar.setSpinerListener(this);
		spiner=(ImageView) findViewById(R.id.image1);  
		spiner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
	
				Editor editor=mySharedPreferences.edit();
				editor.putString("themeHome","1");
				editor.commit();
				Intent intent = new Intent(HomeActivity.this,
						LockerTheme.class);
				startActivityForResult(intent, 0);
				themeHome = "1";
				SDKWrapper.addEvent(HomeActivity.this, LeoStat.P1, "theme_enter", "home");
			}
		});

		calculateAppCount();
	}

	@Override
	protected void onResume() {
		ProcessCleaner pc = ProcessCleaner.getInstance(this);
		long total = pc.getTotalMem();
		long used = pc.getUsedMem();
		mMemoryPercent.setText(used * 100 / total + "%");
		mTvMemoryInfo.setText(TextFormater.dataSizeFormat(used));
		mTvFlow.setText(TextFormater.dataSizeFormat(AppUtil.getTotalTriffic()));
		mCricleView.updateDegrees(360f / total * used);

		if (themeHome.equals("0")) {
			spiner.setImageDrawable(this.getResources().getDrawable(
					R.drawable.themetip_spiner_press));
		} else {
			spiner.setImageDrawable(this.getResources().getDrawable(
					R.drawable.theme_spiner_press));
		}
		
		updateSettingIcon();
		setLockedAppCount();

		judgeShowGradeTip();
		super.onResume();
		
		SDKWrapper.addEvent(this, LeoStat.P1, "home", "enter");
	}

	private void calculateAppCount() {
		int appCount = AppLoadEngine.getInstance(HomeActivity.this)
				.getAllPkgInfo().size();
		setAppCount(appCount);
	}

	private void setLockedAppCount() {
		View v = findViewById(R.id.lock_count_layout);
		int lockedAppCount = AppMasterPreference.getInstance(this)
				.getLockedAppList().size();
		if (lockedAppCount == 0) {
			v.setVisibility(View.INVISIBLE);
		} else {
			v.setVisibility(View.VISIBLE);
			mLockedApp.setText(Integer.toString(lockedAppCount));
		}
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
		LeoLog.d("homepage", "onOptionsMenuClosed");
	}

	private void setAppCount(int count) {
		int one, two, three;
		one = count / 100;
		two = (count % 100) / 10;
		three = (count % 100) % 10;

		int[] index = new int[] { R.drawable.digital_0, R.drawable.digital_1,
				R.drawable.digital_2, R.drawable.digital_3,
				R.drawable.digital_4, R.drawable.digital_5,
				R.drawable.digital_6, R.drawable.digital_7,
				R.drawable.digital_8, R.drawable.digital_9 };

		if (one == 0) {
			mIvDigital_0.setVisibility(View.GONE);
		} else {
			mIvDigital_0.setImageResource(index[one]);
		}
		if (two == 0 && one == 0) {
			mIvDigital_1.setVisibility(View.GONE);
		} else {
			mIvDigital_1.setImageResource(index[two]);
		}

		mIvDigital_2.setImageResource(index[three]);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.top_layout:
			break;
		case R.id.tv_picture_hide:
			// track: home - enter hide picture activity
			SDKWrapper.addEvent(this, LeoStat.P1, "home", "hidpic");
			if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
				enterHidePicture();
			} else {
				startPictureLockSetting();
			}
			break;
		case R.id.tv_app_lock:
			// track: home - enter lock application activity
			SDKWrapper.addEvent(this, LeoStat.P1, "home", "lock");
			if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
				enterLockPage();
			} else {
				startLockSetting();
			}
			break;
		case R.id.tv_app_backup:
			SDKWrapper.addEvent(this, LeoStat.P1, "home", "backup");
			intent = new Intent(this, AppBackupRestoreActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_clean_memory:
			// track: home - enter system boost activity
			SDKWrapper.addEvent(this, LeoStat.P1, "home", "boost");
			intent = new Intent(this, CleanMemActivity.class);
			this.startActivity(intent);
			break;
		case R.id.tv_option_image:
			// track: home - show setting popup window
			SDKWrapper.addEvent(this, LeoStat.P1, "home", "setting");
			if (mLeoPopMenu == null) {
				mLeoPopMenu = new LeoPopMenu();
				mLeoPopMenu.setPopMenuItems(getPopMenuItems());
				mLeoPopMenu.setItemSpaned(true);
				mLeoPopMenu.setAnimation(R.style.RightEnterAnim);
				mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if (position == 0) {
							Intent intent = new Intent(HomeActivity.this,
									FeedbackActivity.class);
							startActivity(intent);
						} else if (position == 1) {
							Intent intent = new Intent(HomeActivity.this,
									AppWallActivity.class);
							startActivity(intent);
						} else if (position == 2) {
							SDKWrapper.addEvent(HomeActivity.this, LeoStat.P1,
									"setting", "check_update");
							LeoStat.checkUpdate();
						} else if (position == 3) {
							Intent intent = new Intent(HomeActivity.this,
									AboutActivity.class);
							startActivity(intent);
						} else if (position == 4) {

							if (AppUtil.appInstalled(getApplicationContext(),
									"com.android.vending")) {
								Intent intent = new Intent(Intent.ACTION_VIEW);
								Uri uri = Uri
										.parse("market://details?id=com.leo.appmaster&referrer=utm_source=AppMaster");
								intent.setData(uri);
								ComponentName cn = new ComponentName(
										"com.android.vending",
										"com.google.android.finsky.activities.MainActivity");
								intent.setComponent(cn);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
								mPictureHide.postDelayed(new Runnable() {
									@Override
									public void run() {
										Intent intent2 = new Intent(
												HomeActivity.this,
												GooglePlayGuideActivity.class);
										intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										startActivity(intent2);
									}
								}, 200);
							} else {
								Intent intent = new Intent(Intent.ACTION_VIEW);
								Uri uri = Uri
										.parse("https://play.google.com/store/apps/details?id=com.leo.appmaster&referrer=utm_source=AppMaster");
								intent.setData(uri);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							}

						}
						mLeoPopMenu.dismissSnapshotList();
					}
				});
			}
			mLeoPopMenu.setPopMenuItems(getPopMenuItems());
			mLeoPopMenu.showPopMenu(this,

			mTtileBar.findViewById(R.id.tv_option_image), null,
					new OnDismissListener() {
						@Override
						public void onDismiss() {
							updateSettingIcon();
						}
					});

			break;
		default:
			break;
		}
	}

	private void updateSettingIcon() {
		if (LeoStat.isUpdateAvailable()) {
			mTtileBar.setOptionImage(R.drawable.setting_updated_selector);
		} else {
			mTtileBar.setOptionImage(R.drawable.setting_selector);
		}
	}

	private List<String> getPopMenuItems() {
		List<String> listItems = new ArrayList<String>();
		Resources resources = AppMasterApplication.getInstance().getResources();
		listItems.add(resources.getString(R.string.feedback));
		listItems.add(resources.getString(R.string.app_wall));
		if (LeoStat.isUpdateAvailable()) {
			listItems.add(resources.getString(R.string.app_setting_has_update));
		} else {
			listItems.add(resources.getString(R.string.app_setting_update));
		}
		listItems.add(resources.getString(R.string.app_setting_about));
		listItems.add(resources.getString(R.string.grade));
		return listItems;
	}

	private void enterLockPage() {
		Intent intent = null;
		// intent = new Intent(this, AppLockListActivity.class);
		// startActivity(intent);

		int lockType = AppMasterPreference.getInstance(this).getLockType();
		intent = new Intent(this, LockScreenActivity.class);
		intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
				LockFragment.FROM_SELF_HOME);
		intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
				AppLockListActivity.class.getName());
		if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_PASSWD);
		} else {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_GESTURE);
		}
		startActivity(intent);

	}

	private void startLockSetting() {
		Intent intent = new Intent(this, LockSettingActivity.class);
		intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
				AppLockListActivity.class.getName());
		startActivity(intent);
	}

	private void enterHidePicture() {
		Intent intent = null;
		int lockType = AppMasterPreference.getInstance(this).getLockType();
		intent = new Intent(this, LockScreenActivity.class);
		intent.putExtra(LockScreenActivity.EXTRA_LOCK_TITLE,
				getString(R.string.app_image_hide));
		intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
				LockFragment.FROM_SELF_HOME);
		intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
				ImageHideMainActivity.class.getName());
		if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_PASSWD);
		} else {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_GESTURE);
		}
		startActivity(intent);
	}

	private void startPictureLockSetting() {
		Intent intent = new Intent(this, LockSettingActivity.class);
		intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
				ImageHideMainActivity.class.getName());
		startActivity(intent);
	}

	@Override
	public void onAppChanged(ArrayList<AppDetailInfo> changes, int type) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				calculateAppCount();
			}
		});
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (v.getId() == R.id.tv_app_lock) {
				mPressedEffect1.setBackgroundResource(R.drawable.home_sel);
				mAppLock.setBackgroundResource(R.drawable.home_sel);
			} else if (v.getId() == R.id.tv_app_backup) {
				mPressedEffect2.setBackgroundResource(R.drawable.home_sel);
				mAppBackup.setBackgroundResource(R.drawable.home_sel);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			if (v.getId() == R.id.tv_app_lock) {
				mPressedEffect1.setBackgroundColor(Color.WHITE);
				mAppLock.setBackgroundColor(Color.WHITE);
			} else if (v.getId() == R.id.tv_app_backup) {
				mPressedEffect2.setBackgroundColor(Color.WHITE);
				mAppBackup.setBackgroundColor(Color.WHITE);
			}
			break;
		default:
			break;
		}
		return false;
	}
}
