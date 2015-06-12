
package com.leo.appmaster.quickgestures.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.appmanage.view.HomeAppManagerFragment;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.sdk.BaseActivity;

/**
 * QuickGestureMiuiTip
 * 
 * @author run
 */
public class QuickGestureMiuiTip extends BaseActivity implements OnClickListener {
    private RelativeLayout mMiuiTipRL;
    private TextView mButton;
    private boolean checkHuaWeiSys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miui_open_float_window_tip);
        Intent intent = this.getIntent();
        String sysName = intent.getStringExtra("sys_name");
        if ("huawei".equals(sysName)) {
            checkHuaWeiSys = true;
        }
        mButton = (TextView) findViewById(R.id.miui_bt);
        mMiuiTipRL = (RelativeLayout) findViewById(R.id.miui_tipRL);
        mMiuiTipRL.setOnClickListener(this);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View arg0) {
        LockManager.getInstatnce().filterAllOneTime(2000);
        AppMasterPreference pref = AppMasterPreference.getInstance(QuickGestureMiuiTip.this);
        boolean miuiSetFirst = pref.getQuickGestureMiuiSettingFirstDialogTip();
        if (!miuiSetFirst) {
            AppMasterPreference.getInstance(QuickGestureMiuiTip.this)
                    .setQuickGestureMiuiSettingFirstDialogTip(true);
        }
        if (!checkHuaWeiSys) {
            mMiuiTipRL.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit((int) (0));
                }
            }, 3000);
        }
        QuickGestureMiuiTip.this.finish();
        LeoEventBus.getDefaultBus().post(
                new BackupEvent(HomeAppManagerFragment.FINISH_HOME_ACTIVITY_FALG));
        LockManager.getInstatnce().filterAllOneTime(1000);
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // QuickGestureMiuiTip.this.finish();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
