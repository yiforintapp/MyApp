package com.leo.appmaster.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

public class showTrafficTip extends LEOBaseDialog {
    private Context mContext;
    private TextView mTitle;
    private TextView mContent;
    private TextView mLeftBtn;
    private TextView mRightBtn;
    
    public showTrafficTip(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    private OnDiaogClickListener mListener;

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }
    
    public void setTitle(String titleStr) {
//        if (titleStr != null) {
            mTitle.setText(titleStr);
//        } else {
//            mTitle.setText(R.string.tips);
//        }
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
    
    public void setLeftBtnListener(DialogInterface.OnClickListener lListener) {
        mLeftBtn.setTag(lListener);
        mLeftBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mLeftBtn
                        .getTag();
                lListener.onClick(showTrafficTip.this, 0);
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
                lListener.onClick(showTrafficTip.this, 1);
            }
        });
    }
    
    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.traffic_alot_dialog_alarm, null);

        mTitle = (TextView) dlgView.findViewById(R.id.show_traffic_title);
        mContent = (TextView) dlgView.findViewById(R.id.dlg_content);


        mLeftBtn = (TextView) dlgView.findViewById(R.id.dlg_left_btn);
        mRightBtn = (TextView) dlgView.findViewById(R.id.dlg_right_btn);

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
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setOnClickListener(OnDiaogClickListener listener) {
        mListener = listener;
    }
    
}
