package com.leo.appmaster.applocker;

import com.leo.appmaster.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PasswdTipActivity extends Activity implements OnClickListener {

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
		
		String tip = AppLockerPreference.getInstance(this).getPasswdTip();
		if(tip != null) {
			mEtTip.setHint(tip);
		}
	}

	@Override
	public void onClick(View v) {
		if(v == mTvMakesure) {
			String tip = mEtTip.getText().toString();
			if(tip != null && !tip.equals("")) {
				AppLockerPreference.getInstance(this).savePasswdProtect(null, null, tip);
				Toast.makeText(this, "设置成功", 0).show();
			} else {
				Toast.makeText(this, "密码提示不能为空", 0).show();
			}
		}
	}

}
