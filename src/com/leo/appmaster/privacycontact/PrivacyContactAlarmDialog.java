
package com.leo.appmaster.privacycontact;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

public class PrivacyContactAlarmDialog extends LEOBaseDialog {

    private Context mContext;
    private TextView mTitle, mContentOne, mContentTwo;
    private ImageView mTitleEditIcon, mTitleDeleteIcon;
    private View mLeftBtn;
    private View mRightBtn;
    private Object mUserData;
    private CircleImageView mContactIcon;
    private RippleView mRvBlue;
    private RippleView mRvWhite;

    private OnDiaogClickListener mListener;

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }

    public PrivacyContactAlarmDialog(Context context) {
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

    public void setTitleIcon(Drawable editIcon, Drawable deleteIcon) {
        if (editIcon != null && deleteIcon != null) {
            mTitleEditIcon.setVisibility(View.VISIBLE);
            mTitleDeleteIcon.setVisibility(View.VISIBLE);
            mTitleEditIcon.setImageDrawable(editIcon);
            mTitleDeleteIcon.setImageDrawable(deleteIcon);

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

    public void setLeftBtnBackground(int resid) {
        mLeftBtn.setBackgroundResource(resid);
    }


    public void setLeftBtnListener(View.OnClickListener listener) {
        mRvWhite.setOnClickListener(listener);
    }

    public void setRightBtnListener(View.OnClickListener listener) {
        mRvBlue.setOnClickListener(listener);
    }

//    public void setLeftBtnListener(OnRippleCompleteListener listener) {
//        mRvWhite.setOnRippleCompleteListener(listener);
//    }
//    public void setRightBtnListener(OnRippleCompleteListener listener) {
//        mRvBlue.setOnRippleCompleteListener(listener);
//    }

    public void setTitleEditIconListener(View.OnClickListener listener) {
        mTitleEditIcon.setOnClickListener(listener);
    }

    public void setTitleDeleteIconListener(View.OnClickListener listener) {
        mTitleDeleteIcon.setOnClickListener(listener);
    }

    public void setContactIcon(Bitmap icon) {
        mContactIcon.setImageBitmap(icon);
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.privacy_contact_dialog_alarm, null);

        mContentOne = (TextView) dlgView.findViewById(R.id.dlg_content_number);
        mContentTwo = (TextView) dlgView.findViewById(R.id.dlg_content_answer_type);
        mTitleEditIcon = (ImageView) dlgView.findViewById(R.id.dlg_title_edit);
        mTitleDeleteIcon = (ImageView) dlgView.findViewById(R.id.dlg_title_delete);
        mLeftBtn = dlgView.findViewById(R.id.dlg_left_btn);
        mRightBtn = dlgView.findViewById(R.id.dlg_right_btn);
        mContactIcon = (CircleImageView) dlgView.findViewById(R.id.contactIV);
        mRvBlue = (RippleView) dlgView.findViewById(R.id.rv_blue);
        mRvWhite = (RippleView) dlgView.findViewById(R.id.rv_white);

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
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mRvBlue
                .getLayoutParams();
        linearParams.height = (int) height;
        linearParams.width = (int) width;
        mRvBlue.setLayoutParams(linearParams);
    }
}
