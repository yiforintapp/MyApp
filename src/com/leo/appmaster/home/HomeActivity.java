package com.leo.appmaster.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.appmanage.AppListActivity;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.appsetting.AboutActivity;
import com.leo.appmaster.appwall.AppWallActivity;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.feedback.FeedbackHelper;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.sdk.MainViewActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.RootChecker;
import com.leo.appmaster.videohide.VideoHideMainActivity;
import com.leo.imageloader.utils.HideFileUtils;
import com.leoers.leoanalytics.LeoStat;

public class HomeActivity extends MainViewActivity implements OnClickListener,
		OnTouchListener {

	private final static String KEY_ROOT_CHECK = "root_check";
	public final static String KEY_PLAY_ANIM = "play_anim";
	private View mPictureHide;
	private View mAppLock;
	private View mAppBackup;
	private View mVideoHide;
	private ImageView mBackup;
	private View mLockTheme;
	private View mFileTransfer;

	private View mOptionImage;

	private View mPressedEffect1;
	private View mPressedEffect2;

	private CommonTitleBar mTtileBar;

	private LeoPopMenu mLeoPopMenu;
	private ImageView spiner;
	private String themeHome;
	private SharedPreferences mySharedPreferences;
	private boolean mNewTheme;

	private TextView mHidePic;
	private TextView mHideVideo;
	private TextView mHidePicText;
	private TextView mHideVideoText;
	private CircleAnimView mAnimView;

	private Handler mHandler = new Handler();

	private boolean mIsUpdating = false;

	private int mLastHiddenPicCount = -1;
	private int mLastHiddenVideoCount = -1;
	private boolean mNeedLock = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		spiner = (ImageView) findViewById(R.id.image1);
		initUI();
		Intent intent = getIntent();
		if (intent.getBooleanExtra(KEY_PLAY_ANIM, false)) {
			mAnimView.invalidateDraw(false);
			prepareToAnim();
		} else {
			mAnimView.invalidateDraw(true);
		}
		// commit feedbacks if any
		FeedbackHelper.getInstance().tryCommit();
		installShortcut();

		// Root chack
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (sp.getBoolean(KEY_ROOT_CHECK, true)) {
			boolean root = RootChecker.isRoot();
			if (root) {
				SDKWrapper.addEvent(getApplicationContext(), LeoStat.P1,
						KEY_ROOT_CHECK, "root");
			}
			sp.edit().putBoolean(KEY_ROOT_CHECK, false).commit();
		}
	}

	private void prepareToAnim() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mAnimView.palyAnim();
			}
		}, 500);
	}

	private void judgeShowGradeTip() {
		mHandler.postDelayed(new Runnable() {
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
						mNeedLock= false;
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
	protected void onStop() {
		super.onStop();
		if (mAnimView != null) {
			mAnimView.cancelAnim();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initUI() {
		mHidePic = (TextView) findViewById(R.id.hide_pic_icon);
		mHidePic.setOnClickListener(this);
		mHideVideo = (TextView) findViewById(R.id.hide_video_icon);
		mHideVideo.setOnClickListener(this);
		mHidePicText = (TextView) findViewById(R.id.hide_pic_text);
		mHidePicText.setOnClickListener(this);
		mHideVideoText = (TextView) findViewById(R.id.hide_video_text);
		mHideVideoText.setOnClickListener(this);
		mBackup = (ImageView) findViewById(R.id.backup_icon);

		mAnimView = (CircleAnimView) findViewById(R.id.lock_circle_view);
		mAnimView.setOnClickListener(this);
		mPictureHide = findViewById(R.id.tv_picture_hide);
		mAppLock = findViewById(R.id.tv_app_lock);
		mAppBackup = findViewById(R.id.tv_app_backup);
		mVideoHide = findViewById(R.id.tv_video_hide);
		mPictureHide.setOnClickListener(this);
		mAppLock.setOnClickListener(this);
		mVideoHide.setOnTouchListener(this);
		mAppLock.setOnTouchListener(this);
		mAppBackup.setOnClickListener(this);
		mVideoHide.setOnClickListener(this);

		mPressedEffect1 = findViewById(R.id.pressed_effect1);
		mPressedEffect2 = findViewById(R.id.pressed_effect2);

		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mySharedPreferences = getSharedPreferences("LockerThemeHome",
				HomeActivity.this.MODE_WORLD_WRITEABLE);

		mOptionImage = mTtileBar.findViewById(R.id.image1);
		mOptionImage.setVisibility(View.INVISIBLE);
		mTtileBar.setTitle(R.string.app_name);
		mTtileBar.setBackArrowVisibility(View.GONE);
		mTtileBar.setOptionImageVisibility(View.VISIBLE);
		mTtileBar.setOptionText("");
		mTtileBar.setOptionImage(R.drawable.setting_btn);
		mTtileBar.setOptionListener(this);
		mTtileBar.setSpinerVibility(View.VISIBLE);
		mTtileBar.setSpinerListener(this);
		mTtileBar.showLogo();
		spiner = (ImageView) findViewById(R.id.image1);
		spiner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(HomeActivity.this, LockerTheme.class);
				mNeedLock= false;
				startActivityForResult(intent, 0);
				SDKWrapper.addEvent(HomeActivity.this, LeoStat.P1,
						"theme_enter", "home");
			}
		});
	}

	@Override
	protected void onResume() {
		AppMasterPreference pref = AppMasterPreference.getInstance(this);

		mNewTheme = !pref.getLocalThemeSerialNumber().equals(
				pref.getOnlineThemeSerialNumber());
		if (mNewTheme) {
			spiner.setImageDrawable(this.getResources().getDrawable(
					R.drawable.themetip_spiner_press));
		} else {
			spiner.setImageDrawable(this.getResources().getDrawable(
					R.drawable.theme_spiner_press));
		}

		updateSettingIcon();
		judgeShowGradeTip();
		updateBackupIcon();
		super.onResume();

		SDKWrapper.addEvent(this, LeoStat.P1, "home", "enter");

		updatePrivacyData();

		if (pref.getHomeLocked() && mNeedLock) {
			int lockType = AppMasterPreference.getInstance(this).getLockType();
			Intent intent = new Intent(this, LockScreenActivity.class);
			if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
				intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
						LockFragment.LOCK_TYPE_PASSWD);
			} else if (lockType == AppMasterPreference.LOCK_TYPE_GESTURE) {
				intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
						LockFragment.LOCK_TYPE_GESTURE);
			}
			intent.putExtra(LockHandler.EXTRA_LOCKED_APP_PKG, getPackageName());
			intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
					LockFragment.FROM_OTHER);
			mNeedLock = false;
			startActivity(intent);
		} else {
			mNeedLock = true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 10000) {
//			mNeedLock = false;
		}
	}

	private void updateBackupIcon() {
		AppMasterPreference pref = AppMasterPreference.getInstance(this);
		boolean click = pref.getHomeBusinessTipClick();
		String online = pref.getOnlineBusinessSerialNumber();
		String local = pref.getLocalBusinessSerialNumber();
		if (online != null && !online.equals(local) && !click) {
			mBackup.setImageResource(R.drawable.home_backup_icon_new);
		} else {
			mBackup.setImageResource(R.drawable.home_backup_icon);
		}
	}

	private void updatePrivacyData() {
		if (!mIsUpdating) {
			mIsUpdating = true;
			AppMasterPreference pref = AppMasterPreference
					.getInstance(HomeActivity.this);
			List<String> list = pref.getLockedAppList();
			if (mAnimView != null) {
				mAnimView.setLockedCount(list == null ? 0 : list.size());
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					final int picSize = HideFileUtils
							.getHidePhotoCount(getApplicationContext());
					final int videoSize = HideFileUtils
							.getVideoInfo(getApplicationContext());
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							updateHidePicCount(picSize);
							updateHideVideoCount(videoSize);
							mIsUpdating = false;
						}
					});
				}
			}).start();
		}
	}

	private void updateHidePicCount(int count) {
		if (mHidePic != null && mHidePicText != null) {
			if (mLastHiddenPicCount != count) {
				mLastHiddenPicCount = count;
				if (count > 0) {
					FontMetrics fm = mHidePic.getPaint().getFontMetrics();
					int textH = (int) Math.ceil(fm.descent - fm.ascent) * 2 / 3;
					Drawable iconPic = getResources().getDrawable(
							R.drawable.home_photo_icon);
					int width = (int) (iconPic.getIntrinsicWidth() * (((float) textH) / iconPic
							.getIntrinsicHeight()));

					iconPic.setBounds(0, 0, width, textH);
					mHidePic.setText(String.valueOf(count));
					mHidePic.setCompoundDrawables(null, null, iconPic, null);
					mHidePicText.setText(R.string.hide_pic_text);
				} else {
					Drawable iconPic = getResources().getDrawable(
							R.drawable.home_photo_empty_icon);
					int padding = getResources().getDimensionPixelSize(
							R.dimen.hide_empty_icon_padding);
					int width = iconPic.getIntrinsicWidth();
					int height = iconPic.getIntrinsicHeight();
					int viewH = mHidePic.getHeight() - 2 * padding;
					if (viewH > 0 && height > viewH) {
						width = (int) (width * (((float) viewH) / height));
						height = viewH;
					}
					iconPic.setBounds(0, 0, width, height);
					mHidePic.setText("");
					mHidePic.setCompoundDrawables(null, null, iconPic, null);
					mHidePicText.setText(R.string.hide_pic_empty_text);
				}
			}

		}
	}

	private void updateHideVideoCount(int count) {
		if (mHideVideo != null && mHideVideoText != null) {
			if (mLastHiddenVideoCount != count) {
				mLastHiddenVideoCount = count;
				if (count > 0) {
					FontMetrics fm = mHideVideo.getPaint().getFontMetrics();
					int textH = (int) Math.ceil(fm.descent - fm.ascent) * 2 / 3;
					Drawable iconVideo = getResources().getDrawable(
							R.drawable.home_video_icon);
					int width = (int) (iconVideo.getIntrinsicWidth() * (((float) textH) / iconVideo
							.getIntrinsicHeight()));
					iconVideo.setBounds(0, 0, width, textH);
					mHideVideo.setText(String.valueOf(count));
					mHideVideo
							.setCompoundDrawables(null, null, iconVideo, null);
					mHideVideoText.setText(R.string.hide_video_text);
				} else {
					Drawable iconVideo = getResources().getDrawable(
							R.drawable.home_video_empty_icon);
					int padding = getResources().getDimensionPixelSize(
							R.dimen.hide_empty_icon_padding);
					int width = iconVideo.getIntrinsicWidth();
					int height = iconVideo.getIntrinsicHeight();
					int viewH = mHideVideo.getHeight() - 2 * padding;
					if (viewH > 0 && height > viewH) {
						width = (int) (width * (((float) viewH) / height));
						height = viewH;
					}
					iconVideo.setBounds(0, 0, width, height);
					mHideVideo.setText("");
					mHideVideo
							.setCompoundDrawables(null, null, iconVideo, null);
					mHideVideoText.setText(R.string.hide_video_empty_text);
				}
			}
		}
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
		LeoLog.d("homepage", "onOptionsMenuClosed");
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.top_layout:
			break;
		case R.id.tv_picture_hide:
			gotoHidePic();
			break;
		case R.id.tv_app_lock:
			gotoAppLock();
			break;
		case R.id.tv_app_backup:
			SDKWrapper.addEvent(this, LeoStat.P1, "home", "backup");

			Vector<BusinessItemInfo> list = AppBusinessManager
					.getInstance(this).getBusinessData();
			AppMasterPreference pref = AppMasterPreference.getInstance(this);
			pref.setHomeBusinessTipClick(true);
			// intent = new Intent(this, AppBackupRestoreActivity.class);
			intent = new Intent(this, AppListActivity.class);
			mNeedLock= false;
			startActivity(intent);
			break;
		case R.id.tv_video_hide:
			gotoHideVideo();
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
								mHandler.postDelayed(new Runnable() {
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
						} else if (position == 1) {
							Intent intent = new Intent(HomeActivity.this,
									FeedbackActivity.class);
							mNeedLock= false;
							startActivity(intent);
						} else if (position == 2) {
							Intent intent = new Intent(HomeActivity.this,
									AppWallActivity.class);
							mNeedLock= false;
							startActivity(intent);
						} else if (position == 3) {
							mNeedLock= false;
							SDKWrapper.addEvent(HomeActivity.this, LeoStat.P1,
									"setting", "check_update");
							LeoStat.checkUpdate();
						} else if (position == 4) {
							Intent intent = new Intent(HomeActivity.this,
									AboutActivity.class);
							mNeedLock= false;
							startActivity(intent);
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

		case R.id.hide_pic_icon:
		case R.id.hide_pic_text:
			gotoHidePic();
			break;
		case R.id.hide_video_icon:
		case R.id.hide_video_text:
			gotoHideVideo();
			break;
		case R.id.lock_circle_view:
			gotoAppLock();
			break;
		default:
			break;
		}
	}

	private void gotoHidePic() {
		// track: home - enter hide picture activity
		SDKWrapper.addEvent(this, LeoStat.P1, "home", "hidpic");
		if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
			enterHidePicture();
		} else {
			startPictureLockSetting();
		}
	}

	private void gotoHideVideo() {
		// track: home - enter system boost activity
		SDKWrapper.addEvent(this, LeoStat.P1, "home", "hidvideo");
		if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
			enterHideVideo();
		} else {
			startVideoLockSetting();
		}
	}

	private void gotoAppLock() {
		// track: home - enter lock application activity
		SDKWrapper.addEvent(this, LeoStat.P1, "home", "lock");
		if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
			enterLockPage();
		} else {
			startLockSetting();
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
		listItems.add(resources.getString(R.string.grade));
		listItems.add(resources.getString(R.string.feedback));
		listItems.add(resources.getString(R.string.app_wall));
		if (LeoStat.isUpdateAvailable()) {
			listItems.add(resources.getString(R.string.app_setting_has_update));
		} else {
			listItems.add(resources.getString(R.string.app_setting_update));
		}
		listItems.add(resources.getString(R.string.app_setting_about));

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
		mNeedLock= false;
		startActivity(intent);

	}

	private void startLockSetting() {
		Intent intent = new Intent(this, RecommentAppLockListActivity.class);
		intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
				AppLockListActivity.class.getName());
		mNeedLock= false;
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
		mNeedLock = false;
		startActivity(intent);
	}

	private void startVideoLockSetting() {
		Intent intent = new Intent(this, LockSettingActivity.class);
		intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
				VideoHideMainActivity.class.getName());
		mNeedLock= false;
		startActivity(intent);
	}

	private void enterHideVideo() {
		Intent intent = null;
		int lockType = AppMasterPreference.getInstance(this).getLockType();
		intent = new Intent(this, LockScreenActivity.class);
		intent.putExtra(LockScreenActivity.EXTRA_LOCK_TITLE,
				getString(R.string.app_video_hide));
		intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
				LockFragment.FROM_SELF_HOME);
		intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
				VideoHideMainActivity.class.getName());
		if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_PASSWD);
		} else {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_GESTURE);
		}
		mNeedLock= false;
		startActivity(intent);
	}

	private void startPictureLockSetting() {
		Intent intent = new Intent(this, LockSettingActivity.class);
		intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
				ImageHideMainActivity.class.getName());
		mNeedLock= false;
		startActivity(intent);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (v.getId() == R.id.tv_app_lock) {
				mPressedEffect1.setBackgroundResource(R.drawable.home_sel);
				mAppLock.setBackgroundResource(R.drawable.home_sel);
			} else if (v.getId() == R.id.tv_video_hide) {
				mPressedEffect2.setBackgroundResource(R.drawable.home_sel);
				mVideoHide.setBackgroundResource(R.drawable.home_sel);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			if (v.getId() == R.id.tv_app_lock) {
				mPressedEffect1.setBackgroundColor(Color.WHITE);
				mAppLock.setBackgroundColor(Color.WHITE);
			} else if (v.getId() == R.id.tv_video_hide) {
				mPressedEffect2.setBackgroundColor(Color.WHITE);
				mVideoHide.setBackgroundColor(Color.WHITE);
			}
			break;
		default:
			break;
		}
		return false;
	}
}
