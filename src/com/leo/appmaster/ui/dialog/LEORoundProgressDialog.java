package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView1;
import com.leo.appmaster.ui.RoundProgressBar;


public class LEORoundProgressDialog extends LEOBaseDialog {
	private Context mContext;

	private int mMax;
	private int mCurrent;
	private TextView mTitle;
	private TextView mMessage;
	private RoundProgressBar mProgressBar;
	private View bottomLayout;
	private View mCustomProTextView;
	private TextView mCustomProDownText;
	private RippleView1 mRvBlue;
	private TextView mCustomProTotalText;

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
	    mProgressBar.setProgress(mCurrent);
	    if(mProgressBar.ifTextIsDisplayable()){
	        mProgressBar.setProgressText(mCurrent  + "/" + mMax);
	    }else if(mCustomProTextView.getVisibility() == View.VISIBLE){
	        mCustomProDownText.setText(String.valueOf(mCurrent));
	        mCustomProTotalText.setText(String.valueOf(mMax));
	    }
	}
	
	/**
	 * set custom progress text visiable
	 * @param visiable
	 */
	public void setCustomProgressTextVisiable(boolean visiable){
	    mCustomProTextView.setVisibility(visiable?View.VISIBLE:View.GONE);
	}

	private void initUI() {
		View dlgView = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress_round, null);
		mRvBlue = (RippleView1) dlgView.findViewById(R.id.rv_dialog_blue_button);
		mMessage = (TextView) dlgView.findViewById(R.id.dlg_content);
		mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
		mProgressBar = (RoundProgressBar) dlgView.findViewById(R.id.dlg_pro);
		mCustomProTextView = dlgView.findViewById(R.id.dlg_custom_pro_text);
		mCustomProDownText = (TextView) mCustomProTextView.findViewById(R.id.dlg_pro_text_down);
		mCustomProTotalText = (TextView) mCustomProTextView.findViewById(R.id.dlg_pro_text_total);
		bottomLayout = dlgView.findViewById(R.id.dlg_bottom_btn);
		mRvBlue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				cancel();
			}
		});
//		mRvBlue.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//            @Override
//            public void onRippleComplete(RippleView rippleView) {
//                cancel();
//            }
//        });

		setContentView(dlgView);
	}

}
