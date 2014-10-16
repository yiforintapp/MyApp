package com.leo.appmaster.fragment;

import java.util.List;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.LockPatternUtils;

public class GestureSettingFragment extends BaseFragment implements
		OnClickListener, OnPatternListener, OnDismissListener,
		OnDiaogClickListener {

	private TextView mTvGestureTip;

	private LockPatternView mLockPatternView;
	private int mInputCount = 1;

	private String mTempGesture1, mTempGesture2;

	private boolean mGotoPasswdProtect;

	@Override
	protected int layoutResourceId() {
		return R.layout.fragment_gesture_setting;
	}

	@Override
	protected void onInitUI() {
		mLockPatternView = (LockPatternView) findViewById(R.id.gesture_lockview);
		mLockPatternView.setOnPatternListener(this);

		mTvGestureTip = (TextView) findViewById(R.id.tv_gesture_tip);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// case R.id.tv_reset_gesture:
		// resetGesture();
		// break;

		default:
			break;
		}
	}

	private void resetGesture() {
		mInputCount = 1;
		mTempGesture1 = mTempGesture2 = "";
		mTvGestureTip.setText(R.string.gesture_hint);
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
				// shakeGestureTip();
				mLockPatternView.clearPattern();
				return;
			}
			mTempGesture1 = LockPatternUtils.patternToString(pattern);
			mTvGestureTip.setText(R.string.please_input_gesture_again);
			mLockPatternView.clearPattern();
			mInputCount++;
		} else {
			mTempGesture2 = LockPatternUtils.patternToString(pattern);
			if (mTempGesture2.equals(mTempGesture1)) {
				Toast.makeText(mActivity, R.string.set_gesture_suc, 1).show();
				Intent intent = null;
				// now we can start lock service
				intent = new Intent(mActivity, LockService.class);
				mActivity.startService(intent);
				AppLockerPreference.getInstance(mActivity).saveGesture(
						mTempGesture2);
				if (!AppLockerPreference.getInstance(mActivity)
						.hasPswdProtect()) {
					setGestureProtect();
				} else {
					intent = new Intent(mActivity, AppLockListActivity.class);
					startActivity(intent);
				}
			} else {
				resetGesture();
				Toast.makeText(mActivity, R.string.tip_no_the_same_pswd, 1)
						.show();
				mLockPatternView.clearPattern();
			}
		}
	}

	private void setGestureProtect() {
		LEOAlarmDialog d = new LEOAlarmDialog(mActivity);
		d.setTitle(getString(R.string.set_protect_or_not));
		d.setContent(getString(R.string.set_protect_message));
		d.setLeftBtnStr(getString(R.string.cancel));
		d.setRightBtnStr(getString(R.string.makesure));
		d.setOnClickListener(this);
		d.setOnDismissListener(this);
		d.show();
	}

	private void shakeGestureTip() {
		Animation shake = AnimationUtils.loadAnimation(mActivity, R.anim.shake);
		mTvGestureTip.startAnimation(shake);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		Intent intent;
		if (mGotoPasswdProtect) {
			intent = new Intent(mActivity, PasswdProtectActivity.class);
			startActivity(intent);
		} else {
			intent = new Intent(mActivity, AppLockListActivity.class);
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
