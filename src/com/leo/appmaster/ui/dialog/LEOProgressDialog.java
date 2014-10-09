package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leo.appmaster.R;


public class LEOProgressDialog extends LEOBaseDialog {
	private Context mContext;

	private int mMax;
	private int mCurrent;
	private TextView mProHint;
	private ProgressBar mProgressBar;
	private TextView mState;

	public LEOProgressDialog(Context context) {
		super(context, R.style.bt_dialog);
		mContext = context.getApplicationContext();
		initUI();
	}

	public void setMessage(String hintString) {
		if (hintString != null) {
			mProHint.setText(hintString);
		}
	}

	public void setMax(int maxValue) {
		mMax = maxValue;
		mProgressBar.setMax(maxValue);
	}
	
	public void setIndeterminate(boolean indeterminate) {
	    mProgressBar.setIndeterminate(indeterminate);
	    mState.setVisibility(indeterminate ? View.GONE : View.VISIBLE);
	}

	public void setProgress(int currentValue) {
	    mCurrent = currentValue;
        mState.setText(mCurrent + " / " + mMax);
        mProgressBar.setProgress(mCurrent);
	}

	private void initUI() {
		View dlgView = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress, null);

		mProHint = (TextView) dlgView.findViewById(R.id.dlg_pro_hint);
		mState = (TextView) dlgView.findViewById(R.id.dlg_pro_state);
		mProgressBar = (ProgressBar) dlgView.findViewById(R.id.dlg_pro);

		setContentView(dlgView);
	}

}
