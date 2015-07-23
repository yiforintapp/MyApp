
package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.leo.appmaster.R;

public class LeoSingleLinesInputDialog extends LEOBaseDialog {
    public static final String TAG = "XLAlarmDialog";

    private Context mContext;

    private TextView mTitle;
    private TextView mLeftBtn;
    private TextView mRightBtn;
    private TextView mFirstHead;
    private Object mUserData;

    private EditText mNameEdit;

    private OnDiaogClickListener mListener;

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }

    public LeoSingleLinesInputDialog(Context context) {
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
            mTitle.setText("");
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

    public void setLeftBtnListener(DialogInterface.OnClickListener lListener) {
        mLeftBtn.setTag(lListener);
        mLeftBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mLeftBtn
                        .getTag();
                if (lListener != null) {
                    lListener.onClick(LeoSingleLinesInputDialog.this, 0);
                }
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
                if (lListener != null) {
                    lListener.onClick(LeoSingleLinesInputDialog.this, 1);
                }
            }
        });
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_single_lines_input, null);

        mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);

        mLeftBtn = (TextView) dlgView.findViewById(R.id.dlg_left_btn);
        mRightBtn = (TextView) dlgView.findViewById(R.id.dlg_right_btn);
        mNameEdit = (EditText) dlgView.findViewById(R.id.et_second);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                if (mListener != null) {
                    mListener.onClick(arg1);
                }
                // dialog.dismiss();
            }
        };
        setLeftBtnListener(listener);
        setRightBtnListener(listener);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setOnClickListener(OnDiaogClickListener listener) {
        mListener = listener;
    }

    public void setFirstHead(int resID) {
        mFirstHead.setText(resID);
    }

    public EditText getEditText() {
        return mNameEdit;
    }

}
