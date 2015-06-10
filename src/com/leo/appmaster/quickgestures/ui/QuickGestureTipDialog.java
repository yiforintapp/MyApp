
package com.leo.appmaster.quickgestures.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

public class QuickGestureTipDialog extends LEOBaseDialog {
    private Context mContext;

    private TextView mTitle;
    private TextView mContent;
    private TextView mLeftBtn;
    private TextView mRightBtn;

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }

    public QuickGestureTipDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.quick_gesture_tip_dialog_alarm, null);
        mTitle = (TextView) dlgView.findViewById(R.id.quick_dlg_title_tv);
        mContent = (TextView) dlgView.findViewById(R.id.quick_dlg_content);

        mLeftBtn = (TextView) dlgView.findViewById(R.id.quick_dlg_left_btn);
        mRightBtn = (TextView) dlgView.findViewById(R.id.quick_dlg_right_btn);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
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

    public void setLeftBtnVisibility(boolean flag) {
        if (!flag) {
            mLeftBtn.setVisibility(View.GONE);
        }
    }

    public void setLeftOnClickListener(android.view.View.OnClickListener listener) {
        mLeftBtn.setOnClickListener(listener);
    }

    public void setRightOnClickListener(android.view.View.OnClickListener listener) {
        mRightBtn.setOnClickListener(listener);
    }
}
