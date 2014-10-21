package com.leo.appmaster.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.LockOptionActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.WaitActivity;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;

public class PasswdLockFragment extends LockFragment implements
		OnClickListener, OnDiaogClickListener {

	private ImageView mAppicon;

	private TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv0;
	private ImageView iv_delete;
	private TextView mTvPasswd1, mTvPasswd2, mTvPasswd3, mTvPasswd4;
	private TextView mPasswdTip, mPasswdHint;
	private TextView mFindPasswd;

	private EditText mEtQuestion, mEtAnwser;
	private LeoDoubleLinesInputDialog mDialog;

	private String mTempPasswd = "";

	@Override
	protected int layoutResourceId() {
		return R.layout.fragment_lock_passwd;
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
		iv_delete.setEnabled(false);

		mTvPasswd1 = (TextView) findViewById(R.id.tv_passwd_1);
		mTvPasswd2 = (TextView) findViewById(R.id.tv_passwd_2);
		mTvPasswd3 = (TextView) findViewById(R.id.tv_passwd_3);
		mTvPasswd4 = (TextView) findViewById(R.id.tv_passwd_4);

		mPasswdHint = (TextView) findViewById(R.id.tv_passwd_hint);
		mPasswdTip = (TextView) findViewById(R.id.tv_passwd_input_tip);
		mAppicon = (ImageView) findViewById(R.id.iv_app_icon);

		mFindPasswd = (TextView) findViewById(R.id.tv_find_passwd);
		mFindPasswd.setOnClickListener(this);

		String passwdtip = AppLockerPreference.getInstance(mActivity)
				.getPasswdTip();
		if (passwdtip == null || passwdtip.trim().equals("")) {
			mPasswdHint.setVisibility(View.INVISIBLE);
		} else {
			mPasswdHint.setText(mActivity.getString(R.string.passwd_hint_tip)
					+ passwdtip);
		}
		if (AppLockerPreference.getInstance(mActivity).hasPswdProtect()) {
			mFindPasswd.setVisibility(View.VISIBLE);
		}

		if (mPackage != null) {
			mAppicon = (ImageView) findViewById(R.id.iv_app_icon);
			mAppicon.setImageDrawable(AppUtil.getDrawable(
					mActivity.getPackageManager(), mPackage));
			mAppicon.setVisibility(View.VISIBLE);
		}

		clearPasswd();

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
			checkPasswd();
			break;

		case R.id.tv_find_passwd:
			findPasswd();
			break;

		default:
			break;
		}
	}

	private void findPasswd() {
		mDialog = new LeoDoubleLinesInputDialog(mActivity);
		mDialog.setTitle(R.string.pleas_input_anwser);
		mDialog.setFirstHead(R.string.passwd_question);
		mDialog.setSecondHead(R.string.passwd_anwser);
		mDialog.setOnClickListener(this);
		mEtQuestion = mDialog.getFirstEditText();
		mEtAnwser = mDialog.getSecondEditText();
		mEtQuestion.setFocusable(false);
		mEtQuestion.setText(AppLockerPreference.getInstance(mActivity)
				.getPpQuestion());
		mDialog.show();
	}

	private void checkPasswd() {
		mInputCount++;
		AppLockerPreference pref = AppLockerPreference.getInstance(mActivity);

		if (pref.getPassword().equals(mTempPasswd)) { // 密码输入正确
			if (mFrom == FROM_SELF) {
				Intent intent = null;
				// try start lock service
				intent = new Intent(mActivity, LockService.class);
				mActivity.startService(intent);
				if (mPackage.equals(mActivity.getPackageName())) {

				}
				intent = new Intent(mActivity, AppLockListActivity.class);
				mActivity.startActivity(intent);
			} else if (mFrom == LockFragment.FROM_OTHER) {
				unlockSucceed(mPackage);
			}
			mActivity.finish();
		} else {
			if (mInputCount >= mMaxInput) {
				Intent intent = new Intent(mActivity, WaitActivity.class);
				intent.putExtra(LockHandler.EXTRA_LOCKED_APP_PKG, mPackage);
				mActivity.startActivity(intent);
				mActivity.finish();
			} else {
				mPasswdTip.setText(String.format(
						mActivity.getString(R.string.input_error_tip),
						mInputCount + "", (mMaxInput - mInputCount) + ""));
			}
			clearPasswd();
		}
	}

	private void clearPasswd() {
		mTvPasswd1.postDelayed(new Runnable() {
			@Override
			public void run() {
				mTvPasswd1.setText("");
				mTvPasswd2.setText("");
				mTvPasswd3.setText("");
				mTvPasswd4.setText("");
			}
		}, 300);
	}

	private void deletePasswd() {
		if (!mTvPasswd4.getText().equals("")) {
			mTvPasswd4.setText("");
			mTempPasswd = mTempPasswd.substring(0, mTempPasswd.length() - 1);
		} else if (!mTvPasswd3.getText().equals("")) {
			mTvPasswd3.setText("");
			mTempPasswd = mTempPasswd.substring(0, mTempPasswd.length() - 1);
		} else if (!mTvPasswd2.getText().equals("")) {
			mTvPasswd2.setText("");
			mTempPasswd = mTempPasswd.substring(0, mTempPasswd.length() - 1);
		} else if (!mTvPasswd1.getText().equals("")) {
			mTvPasswd1.setText("");
			iv_delete.setEnabled(false);
			mTempPasswd = "";
		}

	}

	private void inputPasswd(String s) {
		if (mTvPasswd1.getText().equals("")) {
			mTvPasswd1.setText("*");
			iv_delete.setEnabled(true);
			mTempPasswd = s;
		} else if (mTvPasswd2.getText().equals("")) {
			mTvPasswd2.setText("*");
			mTempPasswd = mTempPasswd + s;
		} else if (mTvPasswd3.getText().equals("")) {
			mTvPasswd3.setText("*");
			mTempPasswd = mTempPasswd + s;
		} else if (mTvPasswd4.getText().equals("")) {
			mTvPasswd4.setText("*");
			mTempPasswd = mTempPasswd + s;

			checkPasswd();
		}

	}

	@Override
	public void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(int which) {
		if (which == 1) {// make sure
			String anwser = AppLockerPreference.getInstance(mActivity)
					.getPpAnwser();
			if (anwser.equals(mEtAnwser.getText().toString())) {
				// goto reset passwd
				Intent intent = new Intent(mActivity, LockSettingActivity.class);
				intent.putExtra(LockSettingActivity.RESET_PASSWD_FLAG, true);
				mActivity.startActivity(intent);

			} else {
				Toast.makeText(mActivity, R.string.reinput_anwser, 0).show();
			}
		} else if (which == 0) { // cancel
			mDialog.dismiss();
		}

	}
}
