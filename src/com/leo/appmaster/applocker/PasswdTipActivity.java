package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PasswdTipActivity extends Activity implements OnClickListener {
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
		mTitleBar.openBackView();
		String tip = AppLockerPreference.getInstance(this).getPasswdTip();
		if (tip != null) {
			mEtTip.setText(tip);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mTvMakesure) {
			String tip = mEtTip.getText().toString();
			AppLockerPreference ap = AppLockerPreference.getInstance(this);
			String q = ap.getPpQuestion();
			String a = ap.getPpAnwser();
			if (tip != null && !tip.equals("")) {
				AppLockerPreference.getInstance(this).savePasswdProtect(q,
						a, tip);
				Toast.makeText(this, R.string.set_success, 0).show();
				finish();
			} else {
				Toast.makeText(this, R.string.passwd_hint_cant_null, 0).show();
			}
		}
	}

}
