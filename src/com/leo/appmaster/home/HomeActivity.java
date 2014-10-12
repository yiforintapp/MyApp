package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.appmanage.AppListActivity;
import com.leo.appmaster.backup.AppBackupRestoreActivity;
import com.leo.appmaster.cleanmemory.CleanMemActivity;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.CricleView;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.ProcessUtils;
import com.leo.appmaster.utils.TextFormater;
import com.leoers.leoanalytics.LeoStat;

public class HomeActivity extends Activity implements OnClickListener {

	private View mTopLayout;
	private View mTvAppManage;
	private View mTvAppLock;
	private View mTvAppBackup;
	private View mTvCleanMem;

	private TextView mTvMemoryInfo, mTvFlow;
	private ImageView mIvDigital_0, mIvDigital_1, mIvDigital_2;
	private CommonTitleBar mTtileBar;

	private CricleView mCricleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		judgeLockService();
		initUI();
	}

	private void initUI() {
		mTopLayout = findViewById(R.id.top_layout);
		mTopLayout.setOnClickListener(this);

		mIvDigital_0 = (ImageView) findViewById(R.id.digital_0);
		mIvDigital_1 = (ImageView) findViewById(R.id.digital_1);
		mIvDigital_2 = (ImageView) findViewById(R.id.digital_2);

		mTvMemoryInfo = (TextView) findViewById(R.id.tv_memory_info);
		mTvFlow = (TextView) findViewById(R.id.tv_flow);
		mCricleView = (CricleView) findViewById(R.id.cricle_view);
		
		mTvAppManage = findViewById(R.id.tv_app_manage);
		mTvAppLock = findViewById(R.id.tv_app_lock);
		mTvAppBackup = findViewById(R.id.tv_app_backup);
		mTvCleanMem = findViewById(R.id.tv_clean_memory);
		mTvAppManage.setOnClickListener(this);
		mTvAppLock.setOnClickListener(this);
		mTvAppBackup.setOnClickListener(this);
		mTvCleanMem.setOnClickListener(this);

		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_name);
		mTtileBar.setBackArrowVisibility(View.GONE);
		mTtileBar.setOptionVisibility(View.VISIBLE);

	}

	@Override
	protected void onResume() {
		int appCount = AppLoadEngine.getInstance(this).getAllPkgInfo().size();
		setAppCount(appCount);

		long total = ProcessUtils.getTotalMem();
		long used = total - ProcessUtils.getAvailableMem(this);

		mTvMemoryInfo.setText(TextFormater.dataSizeFormat(used) + "/"
				+ TextFormater.dataSizeFormat(total));

		mTvFlow.setText(TextFormater.dataSizeFormat(AppUtil.getTotalTriffic()));

		mCricleView.updateDegrees(360f / total * used);

		super.onResume();
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
		if (two == 0) {
			mIvDigital_1.setVisibility(View.GONE);
		} else {
			mIvDigital_1.setImageResource(index[two]);
		}

		mIvDigital_2.setImageResource(index[three]);
	}

	private void judgeLockService() {
		if (AppLockerPreference.getInstance(this).getLockType() != AppLockerPreference.LOCK_TYPE_NONE) {
			Intent serviceIntent = new Intent(this, LockService.class);
			serviceIntent.putExtra(LockService.EXTRA_STARTUP_FROM,
					"main activity");

			startService(serviceIntent);
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.top_layout:
		case R.id.tv_app_manage:
			LeoStat.addEvent(LeoStat.P2, "app_manage",
					"click the app manage button");
			intent = new Intent(this, AppListActivity.class);
			this.startActivity(intent);
			break;
		case R.id.tv_app_lock:
			LeoStat.addEvent(LeoStat.P2, "app lock",
					"click the app lock button");
			if (AppLockerPreference.getInstance(this).getLockType() != AppLockerPreference.LOCK_TYPE_NONE) {
				enterLockPage();
			} else {
				startLockSetting();
			}
			break;
		case R.id.tv_app_backup:
			LeoStat.addEvent(LeoStat.P2, "app backup",
					"click the app backup button");
			intent = new Intent(this, AppBackupRestoreActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_clean_memory:
			LeoStat.addEvent(LeoStat.P2, "tasksCompleted",
					"click the one key clear button");
			intent = new Intent(this, CleanMemActivity.class);
			this.startActivity(intent);
			break;

		default:
			break;
		}
	}

	private void enterLockPage() {
		Intent intent = null;
		int lockType = AppLockerPreference.getInstance(this).getLockType();
		intent = new Intent(this, LockScreenActivity.class);
		if (lockType == AppLockerPreference.LOCK_TYPE_PASSWD) {
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
}
