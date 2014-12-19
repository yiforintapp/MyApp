package com.leo.appmaster.videohide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

public class VideoHideDialog extends LEOBaseDialog {
	private Context mContext;

	private TextView mTitle;
	private TextView mProHint;
	private ProgressBar mProgressBar;
	private View bottomLayout;

	public VideoHideDialog(Context context) {
		super(context, R.style.bt_dialog);
		mContext = context.getApplicationContext();
		initUI();
	}

	public void setMessage(String hintString) {
		if (hintString != null) {
			mProHint.setText(hintString);
		}
	}

	public void setTitle(String title) {
		if (title != null) {
			mTitle.setText(title);
		}
	}

	public void setButtonVisiable(boolean visiable) {
		bottomLayout.setVisibility(visiable ? View.VISIBLE : View.GONE);
	}

	public void setIndeterminate(boolean indeterminate) {
		mProgressBar.setIndeterminate(indeterminate);
	}

	private void initUI() {
		View dlgView = LayoutInflater.from(mContext).inflate(
				R.layout.dialog_videohide, null);
		mProgressBar = (ProgressBar) dlgView.findViewById(R.id.dlg_pro);

		setContentView(dlgView);
	}

}
