
package com.leo.appmaster.privacycontact;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView1;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

public class PrivacyContactDeleteAlarmDialog extends LEOBaseDialog {

    private Context mContext;
    private TextView mTitle, mContentOne, mContentTwo;
    private CheckBox mCheckBox;
    private TextView mLeftBtn;
    private TextView mRightBtn;
    private Object mUserData;
    private TextView mCheckText;

    private OnDiaogClickListener mListener;

    public void setCheckText(int string) {
        mCheckText.setText(string);
    }

    public void setChecked(boolean check) {
        mCheckBox.setChecked(check);
    }

    public boolean getChecked() {
        return mCheckBox.isChecked();
    }

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }

    public PrivacyContactDeleteAlarmDialog(Context context) {
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

    public void setContentOne(String titleStr) {
        if (titleStr != null) {
            mContentOne.setText(titleStr);
        }
    }

    public void setContentTwo(String titleStr) {
        if (titleStr != null) {
            mContentTwo.setText(titleStr);
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

    public void setLeftBtnListener(View.OnClickListener listener) {
//        mLeftBtn.setOnClickListener(listener);
        rippleView2.setOnClickListener(listener);
    }

    public void setRightBtnListener(View.OnClickListener listener) {
//        mRightBtn.setOnClickListener(listener);
        rippleView1.setOnClickListener(listener);
    }

    private RippleView1 rippleView1;
    private RippleView1 rippleView2;
    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.privacy_contact_delete_dialog_alarm, null);
        mContentOne = (TextView) dlgView.findViewById(R.id.dlg_content_title_tv);
        mContentTwo = (TextView) dlgView.findViewById(R.id.dlg_content_tv);
        mLeftBtn = (TextView) dlgView.findViewById(R.id.dlg_left_btn);
        rippleView2 = (RippleView1) dlgView.findViewById(R.id.rv_dialog_whitle_button);
        mRightBtn = (TextView) dlgView.findViewById(R.id.dlg_right_btn);
        rippleView1 = (RippleView1) dlgView.findViewById(R.id.rv_dialog_blue_button);
        mCheckBox = (CheckBox) dlgView.findViewById(R.id.dlg_check);
        mCheckText = (TextView) dlgView.findViewById(R.id.dlg_check_tv);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                if (mListener != null) {
                    mListener.onClick(arg1);
                }
                dialog.dismiss();
            }
        };
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setOnClickListener(OnDiaogClickListener listener) {
        mListener = listener;
    }

    public void setLeftBtnVisibility(boolean flag) {
        if (!flag) {
            mLeftBtn.setVisibility(View.GONE);
        }
    }

    public void setRightBtnParam(float width, float height) {
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mRightBtn
                .getLayoutParams();
        linearParams.height = (int) height;
        linearParams.width = (int) width;
        mRightBtn.setLayoutParams(linearParams);
    }
}
