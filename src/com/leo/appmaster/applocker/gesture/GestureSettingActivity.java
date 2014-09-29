package com.leo.appmaster.applocker.gesture;

import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.PasswdSettingActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.LockPatternUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

public class GestureSettingActivity extends FragmentActivity implements
		OnClickListener, OnPatternListener {

	private TextView mTvButtom;
	private TextView mInputText;
	private TextView mGestureTip;
	private CommonTitleBar mTtileBar;

	private LockPatternView mLockPatternView;
	private int mInputCount;

	private String mTempGesture1, mTempGesture2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_gesture_setting);
		initUI();
		mInputCount = 1;
	}

	private void initUI() {
		mTvButtom = (TextView) findViewById(R.id.tv_buttom);
		mGestureTip = (TextView) findViewById(R.id.tv_gesture_tip);
		mInputText = (TextView) findViewById(R.id.tv_input_header);
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_lock);
		mTtileBar.openBackView();

		mLockPatternView = (LockPatternView) findViewById(R.id.gesture_lockview);

		mLockPatternView.setOnPatternListener(this);

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_buttom:
			if (mInputCount == 1) {
				switchPasswd();
			} else {
				mInputCount = 1;
				mInputText.setText(R.string.please_input_pswd);
				mTvButtom.setText(R.string.use_passwd);
			}
			break;
		default:
			break;
		}
	}

	private void switchPasswd() {
		Intent intent = new Intent(this, PasswdSettingActivity.class);
		startActivity(intent);
		finish();
	}

	private void setGestureProtect() {
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
			startActivity(intent);
		} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			intent = new Intent(this, AppLockListActivity.class);
			startActivity(intent);
		}
		finish();
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
		if (mInputCount == 1) {
			if (patternSize < 4) {
				shakeGestureTip();
				mLockPatternView.clearPattern();
				return;
			}
			mTempGesture1 = LockPatternUtils.patternToString(pattern);
			mInputText.setText(R.string.please_input_gesture_again);
			mTvButtom.setText(R.string.set_gesture_again);
			mLockPatternView.clearPattern();
			mInputCount++;
		} else {
			mTempGesture2 = LockPatternUtils.patternToString(pattern);
			if (mTempGesture2.equals(mTempGesture1)) {
				Toast.makeText(this, "手势设置成功", 1).show();
				Intent intent = null;
				// now we can start lock service
				intent = new Intent(this, LockService.class);
				startService(intent);
				AppLockerPreference.getInstance(this)
						.saveGesture(mTempGesture2);
				if (!AppLockerPreference.getInstance(this).hasPswdProtect()) {
					setGestureProtect();
				} else {
					intent = new Intent(this, AppLockListActivity.class);
					startActivity(intent);
				}
			} else {
				Toast.makeText(this, R.string.tip_no_the_same_pswd, 1).show();
				mLockPatternView.clearPattern();
			}
		}
	}

	private void shakeGestureTip() {
		Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		mGestureTip.startAnimation(shake);
	}

}
