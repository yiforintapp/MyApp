package com.leo.appmaster.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.SDKWrapper;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.appsetting.AboutActivity;
import com.leo.appmaster.backup.AppBackupRestoreActivity;
import com.leo.appmaster.cleanmemory.CleanMemActivity;
import com.leo.appmaster.cleanmemory.ProcessCleaner;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.CricleView;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.TextFormater;
import com.leoers.leoanalytics.LeoStat;

public class HomeActivity extends Activity implements OnClickListener,
		OnTouchListener, AppChangeListener {

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
	private CommonTitleBar mTtileBar;

	private LeoPopMenu mLeoPopMenu;
	private CricleView mCricleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		initUI();
		AppLoadEngine.getInstance(this).registerAppChangeListener(this);
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
		mTtileBar.setTitle(R.string.app_name);
		mTtileBar.setBackArrowVisibility(View.GONE);
		mTtileBar.setOptionImageVisibility(View.VISIBLE);
		mTtileBar.setOptionText("");
		mTtileBar.setOptionImage(R.drawable.setting_btn);
		mTtileBar.setOptionListener(this);

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

		if (LeoStat.isUpdateAvailable()) {
			if (mSettingIcon != null) {
				mSettingIcon.setImageResource(R.drawable.setting_icon_new);
			}
		} else {
			if (mSettingIcon != null) {
				mSettingIcon.setImageResource(R.drawable.setting_icon);
			}
		}
		super.onResume();
	}

	private void calculateAppCount() {
		int appCount = AppLoadEngine.getInstance(HomeActivity.this)
				.getAllPkgInfo().size();
		setAppCount(appCount);
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
			// goto picture hide
			// intent = new Intent(this, AboutActivity.class);
			// this.startActivity(intent);
			break;
		case R.id.tv_app_lock:
			SDKWrapper.addEvent(LeoStat.P2, "main page",
					"click the app lock button");
			if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
				enterLockPage();
			} else {
				startLockSetting();
			}
			break;
		case R.id.tv_app_backup:
			SDKWrapper.addEvent(LeoStat.P2, "main page",
					"click the app backup button");
			intent = new Intent(this, AppBackupRestoreActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_clean_memory:
			SDKWrapper.addEvent(LeoStat.P2, "main page",
					"click the one key clear button");
			intent = new Intent(this, CleanMemActivity.class);
			this.startActivity(intent);
			break;
		case R.id.tv_option_image:
			if (mLeoPopMenu == null) {
				mLeoPopMenu = new LeoPopMenu();
				mLeoPopMenu.setAnimation(R.style.RightEnterAnim);
				mLeoPopMenu.setPopMenuItems(getPopMenuItems());
				mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if (position == 0) {
							// goto user feedback

						} else if (position == 1) {
							// goto app recommend
						} else if (position == 2) {
							LeoStat.checkUpdate();
						} else if (position == 3) {
							// goto about pager
							Intent about = new Intent(HomeActivity.this,
									AboutActivity.class);
							HomeActivity.this.startActivity(about);
						}
						mLeoPopMenu.dismissSnapshotList();
					}
				});
			}
			mLeoPopMenu.showPopMenu(this,
					mTtileBar.findViewById(R.id.tv_option_image));
			break;
		default:
			break;
		}
	}

	private List<String> getPopMenuItems() {
		List<String> listItems = new ArrayList<String>();
		Resources resources = AppMasterApplication.getInstance().getResources();
		listItems.add(resources.getString(R.string.feedback));
		listItems.add(resources.getString(R.string.app_recomend));
		listItems.add(resources.getString(R.string.app_setting_update));
		listItems.add(resources.getString(R.string.app_setting_about));
		return listItems;
	}

	private void enterLockPage() {
		Intent intent = null;
		int lockType = AppMasterPreference.getInstance(this).getLockType();
		intent = new Intent(this, LockScreenActivity.class);
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
