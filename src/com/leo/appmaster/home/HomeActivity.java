package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.PasswdLockScreenActivity;
import com.leo.appmaster.applocker.PasswdSettingActivity;
import com.leo.appmaster.applocker.gesture.GestureLockScreenActivity;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.appmanage.AppManagerActivity;
import com.leo.appmaster.backup.AppBackupRestoreActivity;
import com.leo.appmaster.cleanmemory.CleanMemActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leoers.leoanalytics.LeoStat;

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
	        LeoStat.addEvent(LeoStat.P2, "tasksCompleted", "click the one key clear button");
			startTaskView(mCurrentProgress - 10);
			break;
		case R.id.tv_app_manage:
	         LeoStat.addEvent(LeoStat.P2, "app_manage", "click the app manage button");
			intent = new Intent(this, AppManagerActivity.class);
			this.startActivity(intent);
			break;
		case R.id.tv_app_lock:
	          LeoStat.addEvent(LeoStat.P2, "app lock", "click the app lock button");
			if (AppLockerPreference.getInstance(this).getLockType() != AppLockerPreference.LOCK_TYPE_NONE) {
				enterLockPage();
			} else {
				startPswdSetting();
			}
			break;
		case R.id.tv_app_backup:
            LeoStat.addEvent(LeoStat.P2, "app backup", "click the app backup button");
			intent = new Intent(this, AppBackupRestoreActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_clean_memory:
	         LeoStat.addEvent(LeoStat.P2, "tasksCompleted", "click the one key clear button");
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
		if (lockType == AppLockerPreference.LOCK_TYPE_PASSWD) {
			intent = new Intent(this, PasswdLockScreenActivity.class);
			intent.putExtra(PasswdLockScreenActivity.ERTRA_UNLOCK_TYPE,
					PasswdLockScreenActivity.TYPE_SELF);
		} else {
			intent = new Intent(this, GestureLockScreenActivity.class);
			intent.putExtra(PasswdLockScreenActivity.ERTRA_UNLOCK_TYPE,
					PasswdLockScreenActivity.TYPE_SELF);
		}
		startActivity(intent);
	}

	private void startPswdSetting() {
		Intent intent = new Intent(this, PasswdSettingActivity.class);
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
