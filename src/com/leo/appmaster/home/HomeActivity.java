package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.appmanage.AppManagerActivity;
import com.leo.appmaster.backup.AppBackupRestoreActivity;
import com.leo.appmaster.cleanmemory.CleanMemActivity;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.ui.CommonTitleBar;

public class HomeActivity extends Activity implements OnClickListener {

	private TasksCompletedView mTaskProgessView;
	private TextView mTvAppManage;
	private TextView mTvAppLock;
	private TextView mTvAppBackup;
	private TextView mTvCleanMem;

	private CommonTitleBar mTtileBar;

	private int mCurrentProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		judgeLockService();
		initUI();
	}

	private void initUI() {
		mTaskProgessView = (TasksCompletedView) findViewById(R.id.tasksCompletedView);
		mCurrentProgress = 80;
		mTaskProgessView.setProgress(mCurrentProgress);
		mTaskProgessView.setOnClickListener(this);

		mTvAppManage = (TextView) findViewById(R.id.tv_app_manage);
		mTvAppLock = (TextView) findViewById(R.id.tv_app_lock);
		mTvAppBackup = (TextView) findViewById(R.id.tv_app_backup);
		mTvCleanMem = (TextView) findViewById(R.id.tv_clean_memory);
		mTvAppManage.setOnClickListener(this);
		mTvAppLock.setOnClickListener(this);
		mTvAppBackup.setOnClickListener(this);
		mTvCleanMem.setOnClickListener(this);

		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_name);
		mTtileBar.setBackArrowVisibility(View.GONE);
		mTtileBar.setOptionVisibility(View.VISIBLE);

	}

	private void judgeLockService() {
		if (AppLockerPreference.getInstance(this).getLockType() != AppLockerPreference.LOCK_TYPE_NONE) {
			Intent serviceIntent = new Intent(this, LockService.class);
			serviceIntent.putExtra(LockService.EXTRA_STARTUP_FROM,
					"main activity");

			startService(serviceIntent);
		}
	}

	private void startTaskView(int tatget) {
		new Thread(new ProgressRunable(tatget)).start();
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.tasksCompletedView:
			startTaskView(mCurrentProgress - 10);
			break;
		case R.id.tv_app_manage:
			intent = new Intent(this, AppManagerActivity.class);
			this.startActivity(intent);
			break;
		case R.id.tv_app_lock:
			if (AppLockerPreference.getInstance(this).getLockType() != AppLockerPreference.LOCK_TYPE_NONE) {
				enterLockPage();
			} else {
				startLockSetting();
			}
			break;
		case R.id.tv_app_backup:
			intent = new Intent(this, AppBackupRestoreActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_clean_memory:
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

	class ProgressRunable implements Runnable {

		private int targetProgress;

		public ProgressRunable(int targetProgress) {
			this.targetProgress = targetProgress;
		}

		@Override
		public void run() {
			while (mCurrentProgress > 1) {
				mCurrentProgress -= 1;
				mTaskProgessView.setProgress(mCurrentProgress);
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			while (mCurrentProgress < targetProgress) {
				mCurrentProgress += 1;
				mTaskProgessView.setProgress(mCurrentProgress);
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mCurrentProgress = targetProgress;
		}

	}
}
