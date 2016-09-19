package com.zlf.appmaster.ui.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.zlf.appmaster.R;

/**
 * Created by Administrator on 2016/8/26.
 */
public class LoginProgressDialog extends LEOBaseDialog {
    private TextView mLoadingContent;

    public LoginProgressDialog(Context context) {
        super(context, R.style.LoginProgressDialog);
        View dlgView = LayoutInflater.from(context.getApplicationContext()).inflate(
                R.layout.progress_login, null);
        setContentView(dlgView);
        CircularProgressView progressBar = (CircularProgressView)
                findViewById(R.id.xlistview_footer_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        mLoadingContent = (TextView) findViewById(R.id.loading_content);
    }

    public void setLoadingContent(String s) {
        if (!TextUtils.isEmpty(s)) {
            mLoadingContent.setText(s);
        }
    }
}

