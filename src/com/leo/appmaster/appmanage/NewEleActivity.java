
package com.leo.appmaster.appmanage;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.fragment.PretendAppBeautyFragment;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.wifiSecurity.WifiTabFragment;

public class NewEleActivity extends BaseFragmentActivity implements OnClickListener {
    private CommonToolbar mCtbMain;
    private RelativeLayout mContent;
    private Fragment mFrgmResult;
    private boolean mIsShowingResult = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_manage);
        initUI();
    }

    private void initUI() {
        mCtbMain = (CommonToolbar) findViewById(R.id.ctb_battery);
        mCtbMain.setToolbarTitle(R.string.app_elec_aca);
        mCtbMain.setToolbarColorResource(R.color.cb);
        mCtbMain.setOptionClickListener(this);
        mCtbMain.setNavigationClickListener(this);
        mCtbMain.setOptionImageResource(R.drawable.setup_icon);
        mCtbMain.setOptionMenuVisible(true);
        
        mContent = (RelativeLayout) findViewById(R.id.rl_content);
        
        ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
            
            @Override
            public void run() {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();  
                transaction.setCustomAnimations(R.anim.anim_down_to_up, R.anim.anim_up_to_down);
                mFrgmResult = new PretendAppBeautyFragment();  
                transaction.replace(mContent.getId(), mFrgmResult);
                transaction.commit();  
                mIsShowingResult = true;
            }
        }, 3000);
        
    }

    @Override
    public void onBackPressed() {
        if (mIsShowingResult) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();  
            transaction.setCustomAnimations(R.anim.anim_down_to_up, R.anim.anim_up_to_down);
            transaction.remove(mFrgmResult);
            mIsShowingResult = false;
            transaction.commit();
        } else {
            super.onBackPressed();
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
         
    }



}
