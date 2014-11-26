package com.leo.appmaster.appsetting;
import java.util.Locale;

import com.leo.appmaster.R;
import com.leo.appmaster.home.ProtocolActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leoers.leoanalytics.LeoStat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends BaseActivity implements OnClickListener{

    private CommonTitleBar mTtileBar;
    private TextView mAppVersion;
    
    private Button mShowProtocol;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_about_layout);
        mTtileBar = (CommonTitleBar) findViewById(R.id.about_title_bar);
        mTtileBar.setTitle(R.string.app_setting_about);
        mTtileBar.setOptionImageVisibility(View.VISIBLE);
        mTtileBar.openBackView();
        mShowProtocol = (Button) findViewById(R.id.check_update_button);
        mShowProtocol.setOnClickListener(this);
        
        mAppVersion = (TextView)findViewById(R.id.app_version);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
            mAppVersion.setText("V"+versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        
        if (mShowProtocol != null) {
            mShowProtocol.setText(R.string.protocolBar);
        }
        
        super.onResume();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.check_update_button:
              Intent intent=new Intent();
              intent.setClass(AboutActivity.this,ProtocolActivity.class);
              startActivity(intent);
                break;

            default:
                break;
        }
        
    }

}
