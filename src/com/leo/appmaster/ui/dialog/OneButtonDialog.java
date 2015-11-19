
package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView1;

public class OneButtonDialog extends LEOBaseDialog {
    private Context mContext;
    private OnWifiDiaogClickListener mListener;
    private TextView mNoWifi, mSelectOther,mTitle;
    private RippleView1 mRvBlue;
    
    public interface OnWifiDiaogClickListener {
        public void onClick();
    }

    public OneButtonDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_single_done, null);
        Resources resources = AppMasterApplication.getInstance().getResources();
        mNoWifi = (TextView) dlgView.findViewById(R.id.dlg_content);
        mSelectOther = (TextView) dlgView.findViewById(R.id.dlg_bottom_btn);
        mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
        mSelectOther.setText(resources.getString(R.string.select_other_wifi));
        mRvBlue = (RippleView1) dlgView.findViewById(R.id.rv_dialog_blue_button);
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                if (mListener != null) {
                    mListener.onClick();
                }
                dialog.dismiss();
            }
        };

        setRightBtnListener(listener);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setTitle(String text) {
        mTitle.setText(text);
    }
    
    public void setText(String text) {
        mNoWifi.setText(text);
    }

    public void setBtnText(String text) {
        mSelectOther.setText(text);
    }

    public void setRightBtnListener(DialogInterface.OnClickListener rListener) {
        mRvBlue.setTag(rListener);
        mRvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvBlue
                        .getTag();
                try {
                    lListener.onClick(OneButtonDialog.this, 1);
                } catch (Exception e) {
                }
            }
        });
//        mRvBlue.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//            @Override
//            public void onRippleComplete(RippleView arg0) {
//                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvBlue
//                        .getTag();
//                try {
//                    lListener.onClick(OneButtonDialog.this, 1);
//                } catch (Exception e) {
//                }
//            }
//        });
    }

    public void setOnClickListener(OnWifiDiaogClickListener listener) {
        mListener = listener;
    }


}
