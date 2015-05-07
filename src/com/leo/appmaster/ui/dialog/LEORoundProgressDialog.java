package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RoundProgressBar;


public class LEORoundProgressDialog extends LEOBaseDialog {
	private Context mContext;

	private int mMax;
	private int mCurrent;
	private TextView mTitle;
	private TextView mMessage;
	private RoundProgressBar mProgressBar;
	private View bottomLayout;

	public LEORoundProgressDialog(Context context) {
		super(context, R.style.bt_dialog);
		mContext = context.getApplicationContext();
		initUI();
	}

	public void setMessage(String message) {
		if (message != null) {
		    mMessage.setText(message);
		}
	}
	
	   public void setTitle(String title) {
	        if (title != null) {
	            mTitle.setText(title);
	        }
	    }

	public void setMax(int maxValue) {
		mMax = maxValue;
		mProgressBar.setMax(maxValue);
	}
	
	public void setButtonVisiable(boolean visiable) {
	    bottomLayout.setVisibility(visiable ? View.VISIBLE : View.GONE);
	}
	
	
	public void setProgressTextVisiable(boolean visiable) {
	    mProgressBar.setTextIsDisplayable(visiable);
	}

	public void setProgress(int currentValue) {
	    mCurrent = currentValue;
	    if(mProgressBar.ifTextIsDisplayable()){
	        mProgressBar.setProgressText(mCurrent  + "/" + mMax);
	    }
        mProgressBar.setProgress(mCurrent);
	}

	private void initUI() {
		View dlgView = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress_round, null);

		mMessage = (TextView) dlgView.findViewById(R.id.dlg_content);
		mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
		mProgressBar = (RoundProgressBar) dlgView.findViewById(R.id.dlg_pro);
		bottomLayout = dlgView.findViewById(R.id.dlg_bottom_btn);
		
		bottomLayout.setOnClickListener(new View.OnClickListener() {           
            @Override
            public void onClick(View v) {
               cancel();
            }
        });

		setContentView(dlgView);
	}

}
