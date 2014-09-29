package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PasswdProtectActivity extends Activity {

	private CommonTitleBar mTtileBar;

	private EditText mQuestion, mAnwser, mPasswdTip;
	private TextView mSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passwd_protect);
		initUI();
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.passwd_protect_setting);
		mTtileBar.openBackView();

		mQuestion = (EditText) findViewById(R.id.et_qusetion);
		mAnwser = (EditText) findViewById(R.id.et_anwser);
		mPasswdTip = (EditText) findViewById(R.id.et_passwd_tip);

		mSave = (TextView) findViewById(R.id.tv_szve);
	}

	public void onClick(View v) {
		String qusetion = mQuestion.getText().toString();
		String answer = mAnwser.getText().toString();
		String tip = mPasswdTip.getText().toString();
		if (v == mSave) {
			if (qusetion == null || qusetion.equals("")) {
				Toast.makeText(this, R.string.qusetion_cant_null, 1).show();
				return;
			}
			if (answer == null || answer.equals("")) {
				Toast.makeText(this, R.string.aneser_cant_null, 1).show();
				return;
			}

			AppLockerPreference.getInstance(this).savePasswdProtect(qusetion,
					answer, tip);

			// show app lock list
			Intent intent = new Intent(this, AppLockListActivity.class);
			startActivity(intent);
			Toast.makeText(this, R.string.pp_success, 1).show();
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		// show app lock list
//		Intent intent = new Intent(this, AppLockListActivity.class);
//		startActivity(intent);
		super.onBackPressed();
	}

}
