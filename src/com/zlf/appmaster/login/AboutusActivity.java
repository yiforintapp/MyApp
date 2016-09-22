package com.zlf.appmaster.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.home.BaseFragmentActivity;
import com.zlf.appmaster.ui.CommonToolbar;

/**
 * Created by Administrator on 2016/7/19.
 */
public class AboutusActivity extends BaseFragmentActivity {


    private View mView;
    private TextView mVersionName;
    private CommonToolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);
        init();
    }

    private void init() {
        mToolBar = (CommonToolbar) findViewById(R.id.ab_toolbar);
        mToolBar.setToolbarTitle(getResources().getString(R.string.zlf_about_us));

        mVersionName = (TextView) findViewById(R.id.version_text);
        mVersionName.setText(getString(R.string.version_name));

        mView = findViewById(R.id.click_area);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://www.zlf1688.com");
                intent.setData(content_url);
                startActivity(intent);
            }
        });
    }


}
