
package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView1;

public class LEOCircleProgressDialog extends LEOBaseDialog {
    private Context mContext;

    private TextView mTitle;
    private TextView mProHint;
    private ProgressBar mProgressBar;
    private View bottomLayout;
    private RippleView1 mRvBlue;

    public LEOCircleProgressDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    public void setMessage(String hintString) {
        if (hintString != null) {
            mProHint.setText(hintString);
        }
    }

    public void setTitle(String title) {
        if (title != null) {
            mTitle.setText(title);
        }
    }

    public void setButtonVisiable(boolean visiable) {
        bottomLayout.setVisibility(visiable ? View.VISIBLE : View.GONE);
    }

    public void setIndeterminate(boolean indeterminate) {
        mProgressBar.setIndeterminate(indeterminate);
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress_circle, null);

        mProHint = (TextView) dlgView.findViewById(R.id.dlg_content);
        mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
        mProgressBar = (ProgressBar) dlgView.findViewById(R.id.dlg_pro);
        bottomLayout = dlgView.findViewById(R.id.dlg_bottom_layout);

        mRvBlue = (RippleView1) dlgView.findViewById(R.id.rv_dialog_blue_button);
        mRvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
//        mRvBlue.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//            @Override
//            public void onRippleComplete(RippleView v) {
//                cancel();
//            }
//        });

        setContentView(dlgView);
    }

}
