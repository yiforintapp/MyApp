package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.gesture.GestureSettingActivity;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.ui.CommonTitleBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PasswdSettingActivity extends Activity implements
		OnClickListener {

	private TextView mTvButtom;
	private TextView mInputText;
	EditText mPswdEdit;
	private CommonTitleBar mTtileBar;

	private int mInputCount;
	private String mTempFirstPasswd;
	private String mTempSecondPasswd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_passwd_setting);
		initUI();
		mInputCount = 1;
	}

	private void initUI() {
		mTvButtom = (TextView) findViewById(R.id.tv_buttom);
		mInputText = (TextView) findViewById(R.id.tv_input_header);
		mPswdEdit = (EditText) findViewById(R.id.tv_passwd_tip);
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_lock);
		mTtileBar.openBackView();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_0:
			mPswdEdit.setText(mPswdEdit.getText().toString() + "0");
			break;
		case R.id.tv_1:
			mPswdEdit.setText(mPswdEdit.getText().toString() + "1");
			break;
		case R.id.tv_2:
			mPswdEdit.setText(mPswdEdit.getText().toString() + "2");
			break;
		case R.id.tv_3:
			mPswdEdit.setText(mPswdEdit.getText().toString() + "3");
			break;
		case R.id.tv_4:
			mPswdEdit.setText(mPswdEdit.getText().toString() + "4");
			break;
		case R.id.tv_5:
			mPswdEdit.setText(mPswdEdit.getText().toString() + "5");
			break;
		case R.id.tv_6:
			mPswdEdit.setText(mPswdEdit.getText().toString() + "6");
			break;
		case R.id.tv_7:
			mPswdEdit.setText(mPswdEdit.getText().toString() + "7");
			break;
		case R.id.tv_8:
			mPswdEdit.setText(mPswdEdit.getText().toString() + "8");
			break;
		case R.id.tv_9:
			mPswdEdit.setText(mPswdEdit.getText().toString() + "9");
			break;
		case R.id.tv_delete:
			String str = mPswdEdit.getText().toString();
			if (str != null && !str.equals("")) {
				mPswdEdit.setText(str.subSequence(0, str.length() - 1));
			}
			break;
		case R.id.tv_ok:
			setPasswd();
			break;
		case R.id.tv_buttom:
			if (mInputCount == 1) {
				switchGesture();
			} else {
				mInputCount = 1;
				mInputText.setText(R.string.please_input_pswd);
				mPswdEdit.setText("");
				mTvButtom.setText(R.string.use_gesture);
			}
			break;
		default:
			break;
		}
	}

	private void switchGesture() {
		Intent intent = new Intent(this, GestureSettingActivity.class);
		startActivity(intent);
		finish();
	}

	private void setPasswd() {
		mTempFirstPasswd = mPswdEdit.getText().toString();
		AppLockerPreference pref = AppLockerPreference.getInstance(this);
		if (mInputCount == 1) {
			if (mTempFirstPasswd.equals("")) {
				Toast.makeText(this, "密码不能为空", 1).show();
			} else if (mTempFirstPasswd.length() < 6) {
				Toast.makeText(this, "密码不能小于六位数", 1).show();
			} else {
				mTvButtom.setText(R.string.reset_passwd);
				mPswdEdit.setText("");
				mInputText.setText(R.string.please_input_pswd_again);
				mInputCount = 2;
			}
		} else {
			mTempSecondPasswd = mPswdEdit.getText().toString();
			if (mTempSecondPasswd.equals(mTempFirstPasswd)) {
				Intent intent = null;
				// now we can start lock service
				intent = new Intent(this, LockService.class);
				this.startService(intent);
				// save password
				AppLockerPreference.getInstance(this).savePassword(
						mTempFirstPasswd);
				// todo set passwd protect
				setPasswdProtect();

			} else {
				Toast.makeText(this, R.string.tip_no_the_same_pswd, 1).show();
			}
		}
	}

	private void setPasswdProtect() {

		Dialog dialog = new AlertDialog.Builder(this).setTitle("是否设置密保问题?")
				.setMessage("为了避免忘记密码而无法进入应用锁，建议设置密保问题，是否设置？")
				.setNegativeButton(R.string.cancel, this)
				.setPositiveButton(R.string.makesure, this).create();
		dialog.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Intent intent;
		if (which == DialogInterface.BUTTON_POSITIVE) {
			intent = new Intent(this, PasswdProtectActivity.class);
			this.startActivity(intent);
		} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			intent = new Intent(this, AppLockListActivity.class);
			this.startActivity(intent);
		}
		finish();
	}
}
