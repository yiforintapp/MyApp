package com.leo.appmaster.fragment;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.util.Log;
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
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;

public class PasswdSettingFragment extends BaseFragment implements
		OnDismissListener, OnDiaogClickListener, OnClickListener {

	private TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv0;

	private ImageView iv_delete;
	private TextView mTvPasswd1, mTvPasswd2, mTvPasswd3, mTvPasswd4;
	private TextView mInputTip, mPasswdHint;

	private int mInputCount = 1;
	private String mTempFirstPasswd = "";
	private String mTempSecondPasswd = "";

	private boolean mGotoPasswdProtect;

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
		mPasswdHint = (TextView) findViewById(R.id.tv_passwd_hint);

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

		mInputTip = (TextView) findViewById(R.id.tv_passwd_input_tip);

		String passwdtip = AppLockerPreference.getInstance(mActivity)
				.getPasswdTip();

		mPasswdHint.setVisibility(View.INVISIBLE);
		// if (passwdtip == null || passwdtip.equals("")) {
		// mPasswdHint.setVisibility(View.INVISIBLE);
		// } else {
		// mPasswdHint.setText(passwdtip);
		// }
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

	private void makesurePasswd() {
		if (mInputCount == 1) {
			mInputCount++;
			clearPasswd();
			mInputTip.setText(R.string.please_input_pswd_again);
			iv_delete.setEnabled(false);
		} else if (mInputCount == 2) {
			if (mTempFirstPasswd.equals(mTempSecondPasswd)) {
				Toast.makeText(mActivity, R.string.set_passwd_suc, 1).show();
				Intent intent = null;
				intent = new Intent(mActivity, LockService.class);
				mActivity.startService(intent);
				AppLockerPreference.getInstance(mActivity).savePassword(
						mTempFirstPasswd);
				if (!AppLockerPreference.getInstance(mActivity)
						.hasPswdProtect()) {
					setPasswdProtect();
				} else {
					intent = new Intent(mActivity, AppLockListActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			} else {
				Toast.makeText(mActivity, R.string.tip_no_the_same_pswd, 1)
						.show();
				clearPasswd();
				mInputCount = 1;
				mTempFirstPasswd = "";
				mTempSecondPasswd = "";
				mInputTip.setText(R.string.passwd_hint);
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
		LEOAlarmDialog d = new LEOAlarmDialog(mActivity);
		d.setTitle(getString(R.string.set_protect_or_not));
		d.setContent(getString(R.string.set_protect_message));
		d.setLeftBtnStr(getString(R.string.cancel));
		d.setRightBtnStr(getString(R.string.makesure));
		d.setOnClickListener(this);
		d.setOnDismissListener(this);
		d.show();
	}

	private void deletePasswd() {
		if (!mTvPasswd4.getText().equals("")) {
			mTvPasswd4.setText("");
		} else if (!mTvPasswd3.getText().equals("")) {
			mTvPasswd3.setText("");
		} else if (!mTvPasswd2.getText().equals("")) {
			mTvPasswd2.setText("");
		} else if (!mTvPasswd1.getText().equals("")) {
			mTvPasswd1.setText("");
			iv_delete.setEnabled(false);
		}

		if (mInputCount == 1) {
			if (mTempFirstPasswd.length() > 0) {
				mTempFirstPasswd = mTempFirstPasswd.substring(0,
						mTempFirstPasswd.length());
			}
		} else {
			if (mTempFirstPasswd.length() > 0) {
				mTempSecondPasswd = mTempSecondPasswd.substring(0,
						mTempSecondPasswd.length());
			}
		}

	}

	private void inputPasswd(String s) {
		if (mTvPasswd1.getText().equals("")) {
			mTvPasswd1.setText("*");
			iv_delete.setEnabled(true);
			if (mInputCount == 1) {
				mTempFirstPasswd = mTempFirstPasswd + s;
			} else {
				mTempSecondPasswd = mTempSecondPasswd + s;
			}
		} else if (mTvPasswd2.getText().equals("")) {
			mTvPasswd2.setText("*");
			if (mInputCount == 1) {
				mTempFirstPasswd = mTempFirstPasswd + s;
			} else {
				mTempSecondPasswd = mTempSecondPasswd + s;
			}
		} else if (mTvPasswd3.getText().equals("")) {
			mTvPasswd3.setText("*");
			if (mInputCount == 1) {
				mTempFirstPasswd = mTempFirstPasswd + s;
			} else {
				mTempSecondPasswd = mTempSecondPasswd + s;
			}
		} else if (mTvPasswd4.getText().equals("")) {
			mTvPasswd4.setText("*");
			if (mInputCount == 1) {
				mTempFirstPasswd = mTempFirstPasswd + s;
			} else {
				mTempSecondPasswd = mTempSecondPasswd + s;
			}
			makesurePasswd();
		}

	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		Intent intent;
		if (mGotoPasswdProtect) {
			intent = new Intent(mActivity, PasswdProtectActivity.class);
			startActivity(intent);
		} else {
			intent = new Intent(mActivity, AppLockListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		mActivity.finish();
	}

	@Override
	public void onClick(int which) {
		if (which == 0) {
		} else if (which == 1) {
			mGotoPasswdProtect = true;
		}

	}
}
