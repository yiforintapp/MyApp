package com.leo.appmaster.appsetting;
import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leoers.leoanalytics.LeoStat;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnClickListener{

    private CommonTitleBar mTtileBar;
    private TextView mAppVersion;
    
    private Button mCheckUpdate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_about_layout);
        mTtileBar = (CommonTitleBar) findViewById(R.id.about_title_bar);
        mTtileBar.setTitle(R.string.app_setting_about);
        mTtileBar.setOptionImageVisibility(View.GONE);
        mTtileBar.openBackView();
        mCheckUpdate = (Button) findViewById(R.id.check_update_button);
        mCheckUpdate.setOnClickListener(this);
        
        mAppVersion = (TextView)findViewById(R.id.app_version);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
            mAppVersion.setText(versionName);
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
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.check_update_button:
                LeoStat.checkUpdate();
                break;

            default:
                break;
        }
        
    }

}
