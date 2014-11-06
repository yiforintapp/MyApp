package com.leo.appmaster.applocker;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PasswdProtectActivity extends BaseActivity implements OnClickListener {

	private CommonTitleBar mTtileBar;

	private EditText mQuestion, mAnwser;
	private TextView mSave;
	private ScrollView mScrollView;
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passwd_protect);
		initUI();
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.passwd_protect_setting);
		mTtileBar.openBackView();
		mQuestion = (EditText) findViewById(R.id.et_question);
		mQuestion.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean isFucus) {
				if (isFucus) {
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							mScrollView.fullScroll(View.FOCUS_UP);
						}
					}, 100);
				}
			}
		});
		mAnwser = (EditText) findViewById(R.id.et_anwser);
		mAnwser.setOnClickListener(this);
		mAnwser.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean isFucus) {
				if (isFucus) {
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							mScrollView.fullScroll(View.FOCUS_DOWN);
						}
					}, 100);
				}
			}
		});
		mSave = (TextView) findViewById(R.id.tv_save);
		mSave.setOnClickListener(this);

		mScrollView = (ScrollView) findViewById(R.id.scroll);
		String question = AppMasterPreference.getInstance(this).getPpQuestion();
		if (question != null) {
			mQuestion.setText(question);
			mQuestion.selectAll();
		}
		String answer = AppMasterPreference.getInstance(this).getPpAnwser();
		if (question != null) {
			mAnwser.setText(answer);
		}

	}

	@Override
	public void onClick(View v) {
		String qusetion = mQuestion.getText().toString();
		String answer = mAnwser.getText().toString();
		String passwdHint = AppMasterPreference.getInstance(this)
				.getPasswdTip();
		if (v == mSave) {
			boolean noQuestion = qusetion == null || qusetion.trim().equals("");
			boolean noAnswer = answer == null || answer.equals("");
			if (noQuestion && noAnswer) {
				qusetion = answer = "";
			} else if (noQuestion && !noAnswer) {
				Toast.makeText(this, R.string.qusetion_cant_null,
						Toast.LENGTH_SHORT).show();
				return;
			} else if (!noQuestion && noAnswer) {
				Toast.makeText(this, R.string.aneser_cant_null,
						Toast.LENGTH_SHORT).show();
				return;
			} else {
				if (qusetion.length() > 40) {
					Toast.makeText(this, R.string.question_charsize_tip,
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (answer.length() > 40) {
					Toast.makeText(this, R.string.anwser_charsize_tip,
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
			if (qusetion == null || qusetion.trim().equals("")) {
				qusetion = answer = "";
			}
			AppMasterPreference.getInstance(this).savePasswdProtect(qusetion,
					answer, passwdHint);
			Toast.makeText(this, R.string.pp_success, Toast.LENGTH_SHORT)
					.show();
			finish();
		} else if (v == mAnwser) {
			if (mAnwser.isFocused()) {
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						mScrollView.fullScroll(View.FOCUS_DOWN);
					}
				}, 100);
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}
