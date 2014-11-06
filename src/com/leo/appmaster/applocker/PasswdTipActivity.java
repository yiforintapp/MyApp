package com.leo.appmaster.applocker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;

public class PasswdTipActivity extends BaseActivity implements OnClickListener {
	CommonTitleBar mTitleBar;
	EditText mEtTip;
	TextView mTvMakesure;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passwd_tip);
		initUI();
	}

	private void initUI() {
		mEtTip = (EditText) findViewById(R.id.et_passwd_tip);
		mTvMakesure = (TextView) findViewById(R.id.tv_make_sure);
		mTvMakesure.setOnClickListener(this);
		mTitleBar = (CommonTitleBar) findViewById(R.id.commonTitleBar1);
		mTitleBar.setTitle(R.string.passwd_notify);
		mTitleBar.openBackView();
		String tip = AppMasterPreference.getInstance(this).getPasswdTip();
		if (tip != null) {
			mEtTip.setText(tip);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mTvMakesure) {
			String tip = mEtTip.getText().toString().trim();
			AppMasterPreference ap = AppMasterPreference.getInstance(this);
			String q = ap.getPpQuestion();
			String a = ap.getPpAnwser();
			AppMasterPreference.getInstance(this).savePasswdProtect(q, a, tip);
			Toast.makeText(this, R.string.set_success, 0).show();
			finish();
		}
	}

}
