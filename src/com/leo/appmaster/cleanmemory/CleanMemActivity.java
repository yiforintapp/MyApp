package com.leo.appmaster.cleanmemory;

import com.leo.appmaster.R;
import com.leo.appmaster.home.TasksCompletedView;
import com.leo.appmaster.ui.CommonTitleBar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class CleanMemActivity extends Activity implements OnClickListener {
	private CommonTitleBar mTtileBar;
	private TasksCompletedView mTaskProgessView;
	private int mCurrentProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_mem);
		initUI();
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTaskProgessView = (TasksCompletedView) findViewById(R.id.tasksCompletedView);
		mCurrentProgress = 80;
		mTaskProgessView.setProgress(mCurrentProgress);
		
		mTtileBar.setTitle(R.string.clean_memory);
		mTtileBar.openBackView();
		
		mTaskProgessView.setOnClickListener(this);
	}

	private void startTaskView(int tatget) {
		new Thread(new ProgressRunable(tatget)).start();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tasksCompletedView:
			startTaskView(mCurrentProgress - 5);
			break;
		case R.id.layout_title_back:
			finish();
			break;
		default:
			break;
		}
		
	}

}
