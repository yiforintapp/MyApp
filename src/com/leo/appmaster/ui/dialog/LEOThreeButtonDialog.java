package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.leo.appmaster.R;

public class LEOThreeButtonDialog extends LEOBaseDialog {
	public static final String TAG = "XLAlarmDialog";

	private Context mContext;

	private TextView mTitle;
	private TextView mContent;
	private TextView mLeftBtn;
	private TextView mRightBtn;
	private TextView mMidBtn;
	private Object mUserData;

	private OnDiaogClickListener mListener;

	public interface OnDiaogClickListener {
		public void onClick(int which);
	}

	public LEOThreeButtonDialog(Context context) {
		super(context, R.style.bt_dialog);
		mContext = context.getApplicationContext();
		initUI();
	}

	public void setUserData(Object userData) {
		mUserData = userData;
	}

	public Object getUserData() {
		return mUserData;
	}
	
	public void setTitleVisiable(boolean visiable) {
	    mTitle.setVisibility(visiable ? View.VISIBLE : View.GONE);
	}

	public void setTitle(String titleStr) {
		if (titleStr != null) {
			mTitle.setText(titleStr);
		} else {
			mTitle.setText(R.string.tips);
		}
	}

	   
    public void setContentVisiable(boolean visiable) {
        mContent.setVisibility(visiable ? View.VISIBLE : View.GONE);
    }
	
	public void setContent(String titleStr) {
		if (titleStr != null) {
			mContent.setText(titleStr);
		}
	}

	public void setContentLineSpacing(int lineSpace) {
		mContent.setLineSpacing(lineSpace, 1);
	}

	public void setContent(SpannableString text) {
		if (text != null) {
			mContent.setText(text);
		}
	}

	public void setSpanContent(SpannableString titleStr) {
		if (titleStr != null) {
			mContent.setText(titleStr);
		}
	}


	public void setLeftBtnStr(String lStr) {
		if (lStr != null) {
			mLeftBtn.setText(lStr);
		}
	}
	
	   public void setMiddleBtnStr(String lStr) {
	        if (lStr != null) {
	            mMidBtn.setText(lStr);
	        }
	    }

	public void setRightBtnStr(String rStr) {
		if (rStr != null) {
			mRightBtn.setText(rStr);
		}
	}
	
	public void setRightBtnBackground(Drawable drawable) {
		if (drawable != null) {
			mRightBtn.setBackgroundDrawable(drawable);
		}
	}

	public void setRightBtnBackground(int resid) {
		mRightBtn.setBackgroundResource(resid);
	}

	public void setRightBtnTextColor(int color) {
		mRightBtn.setTextColor(color);
	}

    public void setRightBtnVisiable(int visiable) {
        mRightBtn.setVisibility(visiable);
        if(visiable != View.VISIBLE){
            setSecondBtnBackground(R.drawable.manager_right_contact_button_selecter);
        }
    }


	public void setLeftBtnListener(DialogInterface.OnClickListener lListener) {
		mLeftBtn.setTag(lListener);
		mLeftBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mLeftBtn
						.getTag();
				lListener.onClick(LEOThreeButtonDialog.this, 0);
			}
		});
	}

	public void setRightBtnListener(DialogInterface.OnClickListener rListener) {
		mRightBtn.setTag(rListener);
		mRightBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRightBtn
						.getTag();
				lListener.onClick(LEOThreeButtonDialog.this, 2);
			}
		});
	}

	public void setMidBtnListener(DialogInterface.OnClickListener rListener) {
		mMidBtn.setTag(rListener);
		mMidBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mMidBtn
						.getTag();
				lListener.onClick(LEOThreeButtonDialog.this, 1);
			}
		});
	}

	private void initUI() {
		View dlgView = LayoutInflater.from(mContext).inflate(
				R.layout.dialog_alarm_three_button, null);

		mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
		mContent = (TextView) dlgView.findViewById(R.id.dlg_content);

		mLeftBtn = (TextView) dlgView.findViewById(R.id.dlg_left_btn);
		mRightBtn = (TextView) dlgView.findViewById(R.id.dlg_right_btn);
		mMidBtn = (TextView) dlgView.findViewById(R.id.dlg_mid_btn);

		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				if (mListener != null) {
					mListener.onClick(arg1);
				}
				dialog.dismiss();
			}
		};
		setLeftBtnListener(listener);
		setRightBtnListener(listener);
		setMidBtnListener(listener);
		setContentView(dlgView);
		setCanceledOnTouchOutside(true);
	}

	public void setContentGravity(int gravity) {
		mContent.setGravity(gravity);
	}

	public void setOnClickListener(OnDiaogClickListener listener) {
		mListener = listener;
	}

	public void setSecondBtnBackground(int background){
	    mMidBtn.setBackgroundResource(background);
	}
}
