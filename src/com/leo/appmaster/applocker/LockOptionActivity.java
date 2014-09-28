package com.leo.appmaster.applocker;

import android.app.Activity;
import android.os.Bundle;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;

public class LockOptionActivity extends Activity {

	private CommonTitleBar mTtileBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_option);
		initUI();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_lock);
	}
}
