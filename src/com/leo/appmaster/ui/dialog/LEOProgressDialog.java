package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.RippleView1;


public class LEOProgressDialog extends LEOBaseDialog {
    private Context mContext;

    private int mMax;
    private int mCurrent;
    private TextView mTitle;
    private TextView mProHint;
    private ProgressBar mProgressBar;
    private RippleView1 mRvBlue;
    private TextView mProgressText;
    private View bottomLayout;

    public LEOProgressDialog(Context context) {
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

    public void setMax(int maxValue) {
        mMax = maxValue;
        mProgressBar.setMax(maxValue);
    }

    public void setButtonVisiable(boolean visiable) {
        bottomLayout.setVisibility(visiable ? View.VISIBLE : View.GONE);
    }

    public void setIndeterminate(boolean indeterminate) {
        mProgressBar.setIndeterminate(indeterminate);
        mProgressText.setVisibility(indeterminate ? View.GONE : View.VISIBLE);
    }

    public void setStateTextVisiable(boolean visiable) {
        mProgressText.setVisibility(visiable ? View.VISIBLE : View.GONE);
    }

    public void setProgress(int currentValue) {
        mCurrent = currentValue;
        mProgressText.setText("(" + mCurrent + "/" + mMax + ")");
        mProgressBar.setProgress(mCurrent);
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress, null);

        mProHint = (TextView) dlgView.findViewById(R.id.dlg_pro_hint);
        mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
        mProgressText = (TextView) dlgView.findViewById(R.id.dlg_pro_progress_text);
        mProgressBar = (ProgressBar) dlgView.findViewById(R.id.dlg_pro);

        mRvBlue = (RippleView1) dlgView.findViewById(R.id.rv_blue);

        bottomLayout = dlgView.findViewById(R.id.progress_dlg_bottom_btn);

        mRvBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
//		mRvBlue.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//
//            @Override
//            public void onRippleComplete(RippleView rippleView) {
//                cancel();
//            }
//        });

        setContentView(dlgView);
    }

}
