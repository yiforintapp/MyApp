package com.leo.appmaster.fragment;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
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
import com.leo.appmaster.applocker.WaitActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LockPatternUtils;

public class GestureLockFragment extends LockFragment implements
		OnClickListener, OnPatternListener,
		android.content.DialogInterface.OnClickListener {
	private LockPatternView mLockPatternView;
	private TextView mGestureTip, mFindGesture;
	private ImageView mAppIcon;


	private EditText mEtQuestion, mEtAnwser;
	private AlertDialog mDialog;

	@Override
	protected int layoutResourceId() {
		return R.layout.fragment_lock_gesture;
	}

	@Override
	protected void onInitUI() {
		mLockPatternView = (LockPatternView) findViewById(R.id.gesture_lockview);
		mLockPatternView.setOnPatternListener(this);

		mGestureTip = (TextView) findViewById(R.id.tv_gesture_tip);
		mFindGesture = (TextView) findViewById(R.id.tv_find_gesture);
		mFindGesture.setOnClickListener(this);
		
		if(mPackage != null) {
			mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
			mAppIcon.setImageDrawable(AppUtil.getDrawable(
					mActivity.getPackageManager(), mPackage));
			mAppIcon.setVisibility(View.VISIBLE);
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_find_gesture:
			findGesture();
			break;

		default:
			break;
		}
	}

	private void findGesture() {
		ViewGroup viewGroup = (ViewGroup) mActivity.getLayoutInflater().inflate(
				R.layout.dialog_passwd_protect, null);
		mEtQuestion = (EditText) viewGroup.findViewById(R.id.et_question);
		mEtAnwser = (EditText) viewGroup.findViewById(R.id.et_anwser);
		mDialog = new AlertDialog.Builder(mActivity)
				.setTitle(R.string.pleas_input_anwser)
				.setNegativeButton(R.string.makesure, this)
				.setPositiveButton(R.string.cancel, this).setView(viewGroup)
				.create();
		mEtQuestion.setText(AppLockerPreference.getInstance(mActivity)
				.getPpQuestion());
		mDialog.show();		
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
		String gesture = LockPatternUtils.patternToString(pattern);
		checkGesture(gesture);
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

				intent = new Intent(mActivity, AppLockListActivity.class);
				this.startActivity(intent);
			} else if (mFrom == FROM_OTHER) {
				//input right gesture, just finish self
			}
			mActivity.finish();
		} else {
			if (mInputCount >= mMaxInput) {
				Intent intent = new Intent(mActivity, WaitActivity.class);
				this.startActivity(intent);
				mGestureTip.setText(R.string.please_input_gesture);
				mInputCount = 0;
			} else {
				if (pref.hasPswdProtect()
						&& mFindGesture.getVisibility() != View.VISIBLE) {
					mFindGesture.setVisibility(View.VISIBLE);
				}
				mGestureTip.setText("您已输错" + mInputCount + "次" + "，还剩"
						+ (mMaxInput - mInputCount) + "次机会");
			}
			mLockPatternView.clearPattern();
		}
	}


	private void shakeGestureTip() {
		Animation shake = AnimationUtils.loadAnimation(mActivity, R.anim.shake);
		mGestureTip.startAnimation(shake);
	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == DialogInterface.BUTTON_NEGATIVE) {// make sure
			String anwser = AppLockerPreference.getInstance(mActivity).getPpAnwser();
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
}
