package com.zlf.appmaster.ui.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.utils.TimeUtil;

/**
 * Created by Administrator on 2016/11/4.
 */
public class AdviceDialog extends LEOBaseDialog {

    private TextView mTitle;
    private TextView mContent;
    private ImageView mClose;
    private TextView mTime;

    public AdviceDialog(Context context) {
        super(context, R.style.LoginProgressDialog);
        View dlgView = LayoutInflater.from(context.getApplicationContext()).inflate(
                R.layout.dialog_word_advice, null);
        setContentView(dlgView);
        mTitle = (TextView) findViewById(R.id.advice_title);
        mContent = (TextView) findViewById(R.id.advice_content);
        mClose = (ImageView) findViewById(R.id.advice_close);
        mTime = (TextView) findViewById(R.id.advice_time);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setTitle(String s) {
        if (!TextUtils.isEmpty(s)) {
            mTitle.setText(s);
        }
    }

    public void setTime(String s) {
        if (!TextUtils.isEmpty(s)) {
            mTime.setText(TimeUtil.getTime(Long.parseLong(s)));
        }
    }

    public void setContent(String s) {
        if (!TextUtils.isEmpty(s)) {
            mContent.setText(s);
        }
    }
}
