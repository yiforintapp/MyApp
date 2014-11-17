/*package com.leo.appmaster.lockertheme;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

public class LockerThemeChanageDialog extends LEOBaseDialog {
	public static final String TAG = "XLAlarmDialog";

	private Context mContext;

	private TextView mTitle;
	private TextView mContent;
	private ImageView mLeftIcon;
	private TextView mLeftBtn;
	private TextView mRightBtn;
	private TextView mMidBtn;
	private Object mUserData;
	private Button mApply;
	private Button mUninstall;
	private Button  mCancel;

	private OnDiaogClickListener mListener;

	public interface OnDiaogClickListener {
		public void onClick(int which);
	}

	public LockerThemeChanageDialog(Context context) {
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

	public void setTitle(String titleStr) {
		if (titleStr != null) {
			mTitle.setText(titleStr);
		} else {
			mTitle.setText(R.string.tips);
		}
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

	public void setIcon(Drawable drawable) {
		if (drawable != null) {
			mLeftIcon.setVisibility(View.VISIBLE);
			mLeftIcon.setImageDrawable(drawable);
		} else {
			mLeftIcon.setVisibility(View.GONE);
		}
	}

	public void setLeftBtnStr(String lStr) {
		if (lStr != null) {
			mLeftBtn.setText(lStr);
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

	public void setApplyListener(DialogInterface.OnClickListener lListener) {
		mApply.setTag(lListener);
		mApply.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mApply
						.getTag();
				lListener.onClick(LockerThemeChanageDialog.this, 0);
			}
		});
	}

	public void setUninstallListener(DialogInterface.OnClickListener rListener) {
		mUninstall.setTag(rListener);
		mUninstall.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mUninstall
						.getTag();
				lListener.onClick(LockerThemeChanageDialog.this, 2);
			}
		});
	}

	public void setCancelListener(DialogInterface.OnClickListener rListener) {
		mCancel.setTag(rListener);
		mCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mCancel
						.getTag();
				lListener.onClick(LockerThemeChanageDialog.this, 1);
			}
		});
	}

	private void initUI() {
		View dlgView = LayoutInflater.from(mContext).inflate(
				R.layout. dialog_theme_alarm, null);

		mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
		mContent = (TextView) dlgView.findViewById(R.id.dlg_content);

		mLeftIcon = (ImageView) dlgView.findViewById(R.id.dlg_left_icon);
		mLeftBtn = (TextView) dlgView.findViewById(R.id.dlg_left_btn);
		mRightBtn = (TextView) dlgView.findViewById(R.id.dlg_right_btn);
		mMidBtn = (TextView) dlgView.findViewById(R.id.dlg_mid_btn);
		mApply=(Button) findViewById(R.id.apply);
		mUninstall=(Button) findViewById(R.id.uninstall);
		mCancel=(Button) findViewById(R.id.cancel);
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				if (mListener != null) {
					mListener.onClick(arg1);
				}
				dialog.dismiss();
			}
		};
		setApplyListener(listener);
		setUninstallListener(listener);
		setCancelListener(listener);
		setContentView(dlgView);
		setCanceledOnTouchOutside(true);
	}

	public void setContentGravity(int gravity) {
		mContent.setGravity(gravity);
	}

	public void setOnClickListener(OnDiaogClickListener listener) {
		mListener = listener;
	}

}
*/