package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.handmark.pulltorefresh.library.xlistview.CircularProgressView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ui.dialog.LEOBaseDialog;

/**
 * Created by Administrator on 2016/8/26.
 */
public class LoginProgressDialog extends LEOBaseDialog {

    public LoginProgressDialog(Context context) {
        super(context, R.style.LoginProgressDialog);
        View dlgView = LayoutInflater.from(context.getApplicationContext()).inflate(
                R.layout.progress_login, null);
        setContentView(dlgView);
        CircularProgressView progressBar = (CircularProgressView)
                findViewById(R.id.xlistview_footer_progressbar);
        progressBar.setVisibility(View.VISIBLE);
    }
}

