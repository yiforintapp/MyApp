package com.leo.appmaster.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.service.LockService;

public class PasswdSettingFragment extends BaseFragment implements
		OnClickListener, android.content.DialogInterface.OnClickListener {

	private TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv0;

	private ImageView iv_delete, iv_makesure;
	private TextView mTvPasswd1, mTvPasswd2, mTvPasswd3, mTvPasswd4;
	private TextView mInputTip;

	private int mInputCount = 1;
	private String mTempFirstPasswd;
	private String mTempSecondPasswd;

	@Override
	protected int layoutResourceId() {
		return R.layout.fragment_passwd_setting;
	}

	@Override
	protected void onInitUI() {
		tv1 = (TextView) findViewById(R.id.tv_1);
		tv2 = (TextView) findViewById(R.id.tv_2);
		tv3 = (TextView) findViewById(R.id.tv_3);
		tv4 = (TextView) findViewById(R.id.tv_4);
		tv5 = (TextView) findViewById(R.id.tv_5);
		tv6 = (TextView) findViewById(R.id.tv_6);
		tv7 = (TextView) findViewById(R.id.tv_7);
		tv8 = (TextView) findViewById(R.id.tv_8);
		tv9 = (TextView) findViewById(R.id.tv_9);
		tv0 = (TextView) findViewById(R.id.tv_0);
		iv_delete = (ImageView) findViewById(R.id.tv_delete);
		iv_makesure = (ImageView) findViewById(R.id.tv_ok);

		tv1.setOnClickListener(this);
		tv2.setOnClickListener(this);
		tv3.setOnClickListener(this);
		tv4.setOnClickListener(this);
		tv5.setOnClickListener(this);
		tv6.setOnClickListener(this);
		tv7.setOnClickListener(this);
		tv8.setOnClickListener(this);
		tv9.setOnClickListener(this);
		tv0.setOnClickListener(this);
		iv_delete.setOnClickListener(this);
		iv_makesure.setOnClickListener(this);
		iv_makesure.setEnabled(false);
		iv_delete.setEnabled(false);

		mTvPasswd1 = (TextView) findViewById(R.id.tv_passwd_1);
		mTvPasswd2 = (TextView) findViewById(R.id.tv_passwd_2);
		mTvPasswd3 = (TextView) findViewById(R.id.tv_passwd_3);
		mTvPasswd4 = (TextView) findViewById(R.id.tv_passwd_4);

		mInputTip = (TextView) findViewById(R.id.tv_passwd_tip);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_1:
			inputPasswd(1 + "");
			break;
		case R.id.tv_2:
			inputPasswd(2 + "");
			break;
		case R.id.tv_3:
			inputPasswd(3 + "");
			break;
		case R.id.tv_4:
			inputPasswd(4 + "");
			break;
		case R.id.tv_5:
			inputPasswd(5 + "");
			break;
		case R.id.tv_6:
			inputPasswd(6 + "");
			break;
		case R.id.tv_7:
			inputPasswd(7 + "");
			break;
		case R.id.tv_8:
			inputPasswd(8 + "");
			break;
		case R.id.tv_9:
			inputPasswd(9 + "");
			break;
		case R.id.tv_0:
			inputPasswd(0 + "");
			break;
		case R.id.tv_delete:
			deletePasswd();
			break;
		case R.id.tv_ok:
			makesurePasswd();
			break;

		default:
			break;
		}
	}

	private void resetPasswd() {
		clearPasswd();
		mInputCount = 1;
		iv_delete.setEnabled(false);
		iv_makesure.setEnabled(false);
		mInputTip.setText(R.string.passwd_hint);
		mTempFirstPasswd = mTempSecondPasswd = "";

	}

	private void makesurePasswd() {
		if (mInputCount == 1) {
			mTempFirstPasswd = mTvPasswd1.getText().toString()
					+ mTvPasswd2.getText().toString()
					+ mTvPasswd3.getText().toString()
					+ mTvPasswd4.getText().toString();
			mInputCount++;
			clearPasswd();
			mInputTip.setText(R.string.please_input_pswd_again);
			iv_makesure.setEnabled(false);
			iv_delete.setEnabled(false);
		} else if (mInputCount == 2) {
			mTempSecondPasswd = mTvPasswd1.getText().toString()
					+ mTvPasswd2.getText().toString()
					+ mTvPasswd3.getText().toString()
					+ mTvPasswd4.getText().toString();

			if (mTempFirstPasswd.equals(mTempSecondPasswd)) {
				// todo save passwd
				Intent intent = null;
				// now we can start lock service
				intent = new Intent(mActivity, LockService.class);
				mActivity.startService(intent);
				// save password
				AppLockerPreference.getInstance(mActivity).savePassword(
						mTempFirstPasswd);
				// todo set passwd protect
				if (!AppLockerPreference.getInstance(mActivity)
						.hasPswdProtect()) {
					setPasswdProtect();
				} else {
					intent = new Intent(mActivity, AppLockListActivity.class);
					startActivity(intent);
				}
			} else {
				// todo re-input
				Toast.makeText(mActivity, R.string.tip_no_the_same_pswd, 1)
						.show();
				clearPasswd();
				mInputTip.setText(R.string.please_input_pswd_again);
			}
		}
	}

	private void clearPasswd() {
		mTvPasswd1.setText("");
		mTvPasswd2.setText("");
		mTvPasswd3.setText("");
		mTvPasswd4.setText("");
	}

	private void setPasswdProtect() {

		Dialog dialog = new AlertDialog.Builder(mActivity)
				.setTitle("是否设置密保问题?")
				.setMessage("为了避免忘记密码而无法进入应用锁，建议设置密保问题，是否设置？")
				.setNegativeButton(R.string.cancel, this)
				.setPositiveButton(R.string.makesure, this).create();
		dialog.show();
	}

	private void deletePasswd() {
		if (!mTvPasswd4.getText().equals("")) {
			mTvPasswd4.setText("");
			iv_makesure.setEnabled(false);
		} else if (!mTvPasswd3.getText().equals("")) {
			mTvPasswd3.setText("");
		} else if (!mTvPasswd2.getText().equals("")) {
			mTvPasswd2.setText("");
		} else if (!mTvPasswd1.getText().equals("")) {
			mTvPasswd1.setText("");
			iv_delete.setEnabled(false);
		}

	}

	private void inputPasswd(String s) {
		if (mTvPasswd1.getText().equals("")) {
			mTvPasswd1.setText(s);
			iv_delete.setEnabled(true);
		} else if (mTvPasswd2.getText().equals("")) {
			mTvPasswd2.setText(s);
		} else if (mTvPasswd3.getText().equals("")) {
			mTvPasswd3.setText(s);
		} else if (mTvPasswd4.getText().equals("")) {
			mTvPasswd4.setText(s);
			iv_makesure.setEnabled(true);
		}

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Intent intent;
		if (which == DialogInterface.BUTTON_POSITIVE) {
			intent = new Intent(mActivity, PasswdProtectActivity.class);
			this.startActivity(intent);
		} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			intent = new Intent(mActivity, AppLockListActivity.class);
			this.startActivity(intent);
		}
		mActivity.finish();

	}
}
