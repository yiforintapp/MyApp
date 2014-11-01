package com.leo.appmaster.fragment;

import java.util.List;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.LockOptionActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.WaitActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LockPatternUtils;

public class GestureLockFragment extends LockFragment implements OnPatternListener {
	private LockPatternView mLockPatternView;
	private TextView mGestureTip;
	private ImageView mAppIcon;

	private LeoDoubleLinesInputDialog mDialog;

	@Override
	protected int layoutResourceId() {
		return R.layout.fragment_lock_gesture;
	}

	@Override
	protected void onInitUI() {
		mLockPatternView = (LockPatternView) findViewById(R.id.gesture_lockview);
		mLockPatternView.setOnPatternListener(this);

		mGestureTip = (TextView) findViewById(R.id.tv_gesture_tip);

		if (mPackage != null) {
			mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
			mAppIcon.setImageDrawable(AppUtil.getDrawable(
					mActivity.getPackageManager(), mPackage));
			mAppIcon.setVisibility(View.VISIBLE);
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
		final String gesture = LockPatternUtils.patternToString(pattern);
		mLockPatternView.postDelayed(new Runnable() {
			@Override
			public void run() {
				checkGesture(gesture);
			}
		}, 200);

	}

	private void checkGesture(String gesture) {
		mInputCount++;
		AppLockerPreference pref = AppLockerPreference.getInstance(mActivity);
		if (pref.getGesture().equals(gesture)) { // 密码输入正确
			if (mFrom == FROM_SELF) {
				Intent intent = null;
				// try start lock service
				intent = new Intent(mActivity, LockService.class);
				mActivity.startService(intent);
				if (LockOptionActivity.class.getName().equals(mActivityName)) {
					intent = new Intent(mActivity, LockOptionActivity.class);
				} else {
					intent = new Intent(mActivity, AppLockListActivity.class);
				}
				mActivity.startActivity(intent);
			} else if (mFrom == FROM_OTHER) {
				// input right gesture, just finish self
				unlockSucceed(mPackage);
			}
			mActivity.finish();
		} else {
			if (mInputCount >= mMaxInput) {
				Intent intent = new Intent(mActivity, WaitActivity.class);
				intent.putExtra(LockHandler.EXTRA_LOCKED_APP_PKG, mPackage);
				mActivity.startActivity(intent);
				// mGestureTip.setText(R.string.please_input_gesture);
				// mInputCount = 0;
				mActivity.finish();
			} else {
				mGestureTip.setText(String.format(
						mActivity.getString(R.string.input_error_tip),
						mInputCount + "", (mMaxInput - mInputCount) + ""));
			}
			mLockPatternView.clearPattern();
		}
	}

	private void shakeGestureTip() {
		Animation shake = AnimationUtils.loadAnimation(mActivity, R.anim.up_down_shake);
		mGestureTip.startAnimation(shake);
	}

	@Override
	public void onNewIntent(Intent intent) {

	}
}
