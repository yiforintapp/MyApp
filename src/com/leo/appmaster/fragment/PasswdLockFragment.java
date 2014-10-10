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
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.WaitActivity;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.utils.AppUtil;

public class PasswdLockFragment extends LockFragment implements
		OnClickListener, android.content.DialogInterface.OnClickListener {

	private ImageView mAppicon;

	private TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv0;
	private ImageView iv_delete, iv_makesure;
	private TextView mTvPasswd1, mTvPasswd2, mTvPasswd3, mTvPasswd4;
	private TextView mPasswdTip;
	private TextView mFindPasswd;

	private EditText mEtQuestion, mEtAnwser;
	private AlertDialog mDialog;

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
		iv_delete = (ImageView) findViewById(R.id.tv_ok);

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
		iv_delete.setOnClickListener(this);
		iv_delete.setEnabled(false);
		iv_delete.setEnabled(false);

		mTvPasswd1 = (TextView) findViewById(R.id.tv_passwd_1);
		mTvPasswd2 = (TextView) findViewById(R.id.tv_passwd_2);
		mTvPasswd3 = (TextView) findViewById(R.id.tv_passwd_3);
		mTvPasswd4 = (TextView) findViewById(R.id.tv_passwd_4);

		mPasswdTip = (TextView) findViewById(R.id.tv_passwd_tip);
		mAppicon = (ImageView) findViewById(R.id.iv_app_icon);

		mFindPasswd = (TextView) findViewById(R.id.tv_find_passwd);
		mFindPasswd.setOnClickListener(this);

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

		case R.id.tv_find_gesture:
			findPasswd();
			break;

		default:
			break;
		}
	}

	private void findPasswd() {
		// TODO Auto-generated method stub
		if (mDialog == null) {
			ViewGroup viewGroup = (ViewGroup) mActivity.getLayoutInflater()
					.inflate(R.layout.dialog_passwd_protect, null);
			mEtQuestion = (EditText) viewGroup.findViewById(R.id.et_question);
			mEtAnwser = (EditText) viewGroup.findViewById(R.id.et_anwser);
			mDialog = new AlertDialog.Builder(mActivity)
					.setTitle(R.string.pleas_input_anwser)
					.setNegativeButton(R.string.makesure, this)
					.setPositiveButton(R.string.cancel, this)
					.setView(viewGroup).create();
			mEtQuestion.setText(AppLockerPreference.getInstance(mActivity)
					.getPpQuestion());
		}

		mDialog.show();
	}

	private void checkPasswd() {
		mInputCount++;
		String passwd = getInputPasswd();
		AppLockerPreference pref = AppLockerPreference.getInstance(mActivity);

		if (pref.getPassword().equals(passwd)) { // 密码输入正确
			if (mFrom == FROM_SELF) {
				Intent intent = null;
				// try start lock service
				intent = new Intent(mActivity, LockService.class);
				mActivity.startService(intent);

				intent = new Intent(mActivity, AppLockListActivity.class);
				mActivity.startActivity(intent);
			} else if (mFrom == LockFragment.FROM_OTHER) {

			}
			mActivity.finish();
		} else {
			if (mInputCount >= mMaxInput) {
				Intent intent = new Intent(mActivity, WaitActivity.class);
				this.startActivity(intent);
				mPasswdTip.setText(R.string.please_input_pswd);
				mInputCount = 0;
			} else {
				if (pref.hasPswdProtect()
						&& mFindPasswd.getVisibility() != View.VISIBLE) {
					mFindPasswd.setVisibility(View.VISIBLE);
				}
				mPasswdTip.setText("您已输错" + mInputCount + "次" + "，还剩"
						+ (mMaxInput - mInputCount) + "次机会");
			}
			clearPasswd();
		}
	}

	private String getInputPasswd() {
		return mTvPasswd1.getText().toString()
				+ mTvPasswd2.getText().toString()
				+ mTvPasswd3.getText().toString()
				+ mTvPasswd4.getText().toString();
	}

	private void clearPasswd() {
		mTvPasswd1.setText("");
		mTvPasswd2.setText("");
		mTvPasswd3.setText("");
		mTvPasswd4.setText("");
	}

	private void deletePasswd() {
		if (!mTvPasswd4.getText().equals("")) {
			mTvPasswd4.setText("");
			iv_delete.setEnabled(false);
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
			iv_delete.setEnabled(true);
		}

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_NEGATIVE) {// make sure
			String anwser = AppLockerPreference.getInstance(mActivity)
					.getPpAnwser();
			if (anwser.equals(mEtAnwser.getText().toString())) {
				// goto reset passwd
				Intent intent = new Intent(mActivity, LockOptionActivity.class);
				mActivity.startActivity(intent);

			} else {
				Toast.makeText(mActivity, "答案不正确，请重新输入", 0).show();
			}
		} else if (which == DialogInterface.BUTTON_POSITIVE) { // cancel
			mDialog.dismiss();
		}

	}

	@Override
	public void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub

	}
}
