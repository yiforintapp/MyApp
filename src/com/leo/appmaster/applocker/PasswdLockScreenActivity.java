package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.AppUtil;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PasswdLockScreenActivity extends FragmentActivity implements
		OnClickListener {

	public static String ERTRA_UNLOCK_TYPE = "extra_unlock_type";

	private final int mMaxInput = 5;

	private TextView mInputTip, mInputHeader, mFindPasswd;;
	private EditText mPswdEdit;
	private CommonTitleBar mTtileBar;
	private ImageView mAppIcon;

	private int mInputCount;
	private String mInputPasswd;

	public static final int TYPE_SELF = 0;
	public static final int TYPE_OTHER = 1;

	private int mType = TYPE_SELF;

	private String mPkg;

	Dialog mPpDialog;
	private EditText mEtQuestion, mEtAnwser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passwd_lock_screen);

		handleIntent();

		initUI();
		mInputCount = 0;
	}

	private void handleIntent() {
		Intent intent = getIntent();
		mType = intent.getIntExtra(ERTRA_UNLOCK_TYPE, TYPE_SELF);
		mPkg = intent.getStringExtra(LockHandler.EXTRA_LOCKED_APP_PKG);
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_lock);

		mInputHeader = (TextView) findViewById(R.id.tv_input_header);
		mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);

		mInputTip = (TextView) findViewById(R.id.tv_input_tip);
		
		if (mType == TYPE_SELF) {
			mInputHeader.setVisibility(View.VISIBLE);
			mAppIcon.setVisibility(View.INVISIBLE);
			mTtileBar.openBackView();
		} else {
			mAppIcon.setVisibility(View.VISIBLE);
			mAppIcon.setImageDrawable(AppUtil.getDrawable(getPackageManager(),
					mPkg));
			mInputHeader.setVisibility(View.INVISIBLE);
		}

		mFindPasswd = (TextView) findViewById(R.id.tv_find_passwd);
		mPswdEdit = (EditText) findViewById(R.id.et_passwd_input);
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
			checkPasswd();
			break;
		case R.id.tv_find_passwd:
			// goto find passwd
			findPasswd();
			break;
		default:
			break;
		}
	}

	private void findPasswd() {
		// TODO Auto-generated method stub
		if (mPpDialog == null) {
			ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(
					R.layout.dialog_passwd_protect, null);
			mEtQuestion = (EditText) viewGroup.findViewById(R.id.et_question);
			mEtAnwser = (EditText) viewGroup.findViewById(R.id.et_anwser);
			mPpDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.pleas_input_anwser)
					.setNegativeButton(R.string.makesure, this)
					.setPositiveButton(R.string.cancel, this)
					.setView(viewGroup).create();
			mEtQuestion.setText(AppLockerPreference.getInstance(this).getPpQuestion());
		}

		mPpDialog.show();
	}

	private void checkPasswd() {
		mInputCount++;
		mInputPasswd = mPswdEdit.getText().toString();
		AppLockerPreference pref = AppLockerPreference.getInstance(this);

		if (pref.getPassword().equals(mInputPasswd)) { // 密码输入正确
			if (mType == TYPE_SELF) {
				Intent intent = null;
				// try start lock service
				intent = new Intent(this, LockService.class);
				this.startService(intent);

				intent = new Intent(this, AppLockListActivity.class);
				this.startActivity(intent);
			} else if (mType == TYPE_OTHER) {

			}
			finish();
		} else {
			if (mInputCount >= mMaxInput) {
				Intent intent = new Intent(this, WaitActivity.class);
				this.startActivity(intent);
				mInputTip.setText("");
				mInputCount = 0;
			} else {
				if (pref.hasPswdProtect()
						&& mFindPasswd.getVisibility() != View.VISIBLE) {
					mFindPasswd.setVisibility(View.VISIBLE);
				}
				mInputTip.setText("您已输错" + mInputCount + "次" + "，还剩"
						+ (mMaxInput - mInputCount) + "次机会");
			}
			mPswdEdit.setText("");
		}
	}

	@Override
	public void onBackPressed() {
		if (mType == TYPE_SELF) {
			super.onBackPressed();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(which == DialogInterface.BUTTON_NEGATIVE) {//make sure
			String anwser = AppLockerPreference.getInstance(this).getPpAnwser();
			if(anwser.equals(mEtAnwser.getText().toString())) {
				//goto reset passwd
				
				
			} else {
				Toast.makeText(this, "答案不正确，请重新输入", 0).show();
			}
		} else if(which == DialogInterface.BUTTON_POSITIVE) { //cancel
			dialog.dismiss();
		}

	}

}
