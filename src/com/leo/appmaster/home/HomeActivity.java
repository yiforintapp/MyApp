package com.leo.appmaster.home;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.appmanage.AppManagerActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class HomeActivity extends Activity implements OnClickListener {

	private TasksCompletedView mTaskProgessView;
	private TextView mTvAppManage;
	private TextView mTvAppLock;
	private TextView mTvAppBackup;
	private TextView mTvCleanMem;
	
	
	private int mCurrentProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		// startLockService();
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
	}

	private void startLockService() {
		Intent serviceIntent = new Intent(this, LockService.class);
		serviceIntent.putExtra(LockService.EXTRA_STARTUP_FROM, "main activity");
		startService(serviceIntent);
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

			break;
		case R.id.tv_app_backup:

			break;
		case R.id.tv_clean_memory:

			break;

		default:
			break;
		}
	}

	class ProgressRunable implements Runnable {
		
		private int targetProgress;
		
		public ProgressRunable(int targetProgress) {
			this.targetProgress = targetProgress;
		}

		@Override
		public void run() {
			while (mCurrentProgress > 0) {
				mCurrentProgress -= 1;
				mTaskProgessView.setProgress(mCurrentProgress);
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			while(mCurrentProgress < targetProgress) {
				mCurrentProgress += 1;
				mTaskProgessView.setProgress(mCurrentProgress);
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mCurrentProgress = targetProgress;
		}

	}
}
