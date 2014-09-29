package com.leo.appmaster.applocker.gesture;

import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.WaitActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LockPatternUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GestureLockScreenActivity extends Activity implements
		OnClickListener, OnPatternListener {

	public static String ERTRA_UNLOCK_TYPE = "extra_unlock_type";

	private final int mMaxInput = 5;

	private LockPatternView mLockPatternView;
	private TextView mInputTip, mInputHeader, mFindPasswd;;
	private CommonTitleBar mTtileBar;
	private ImageView mAppIcon;

	private int mInputCount;
	private String mInputGesture;

	public static final int TYPE_SELF = 0;
	public static final int TYPE_OTHER = 1;

	private int mType = TYPE_SELF;

	private String mPkg;

	private EditText mEtQuestion, mEtAnwser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture_lock_screen);
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
		mLockPatternView = (LockPatternView) findViewById(R.id.gesture_lockview);
		mLockPatternView.setOnPatternListener(this);
		mInputHeader = (TextView) findViewById(R.id.tv_input_header);
		mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
		mInputTip = (TextView) findViewById(R.id.tv_gesture_tip);
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
	}

	private void findPasswd() {
		// TODO Auto-generated method stub
		ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(
				R.layout.dialog_passwd_protect, null);
		mEtQuestion = (EditText) viewGroup.findViewById(R.id.et_question);
		mEtAnwser = (EditText) viewGroup.findViewById(R.id.et_anwser);
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.pleas_input_anwser)
				.setNegativeButton(R.string.makesure, this)
				.setPositiveButton(R.string.cancel, this).setView(viewGroup)
				.create();
		mEtQuestion.setText(AppLockerPreference.getInstance(this)
				.getPpQuestion());
		dialog.show();
	}

	public void onClick(View v) {
		if (v == mFindPasswd) {
			findPasswd();
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
		if (which == DialogInterface.BUTTON_NEGATIVE) {// make sure
			String anwser = AppLockerPreference.getInstance(this).getPpAnwser();
			if (anwser.equals(mEtAnwser.getText().toString())) {
				// goto reset passwd

			} else {
				Toast.makeText(this, "答案不正确，请重新输入", 0).show();
			}
		} else if (which == DialogInterface.BUTTON_POSITIVE) { // cancel
			dialog.dismiss();
		}
	}

	@Override
	public void onPatternStart() {
	}

	@Override
	public void onPatternCleared() {
	}

	@Override
	public void onPatternCellAdded(List<Cell> pattern) {
	}

	@Override
	public void onPatternDetected(List<Cell> pattern) {
		int patternSize = pattern.size();
//		if (patternSize < 4) {
//			shakeGestureTip();
//			mLockPatternView.clearPattern();
//			return;
//		}
		String gesture = LockPatternUtils.patternToString(pattern);
		checkGesture(gesture);

	}

	private void shakeGestureTip() {
		Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		mInputTip.startAnimation(shake);
	}

	private void checkGesture(String gesture) {
		mInputCount++;
		AppLockerPreference pref = AppLockerPreference.getInstance(this);
		if (pref.getGesture().equals(gesture)) { // 密码输入正确
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
			mLockPatternView.clearPattern();
		}
	}
}
