
package com.leo.appmaster.quickgestures.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.utils.BuildProperties;

/**
 * QuickGestureMiuiTip
 * 
 * @author run
 */
public class QuickGestureMiuiTip extends BaseActivity implements OnClickListener {
    private RelativeLayout mMiuiTipRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miui_open_float_window_tip);
        mMiuiTipRL = (RelativeLayout) findViewById(R.id.miui_tipRL);
        mMiuiTipRL.setOnClickListener(this);
    }

    @Override
    public void onClick(View arg0) {
        QuickGestureMiuiTip.this.finish();
        mMiuiTipRL.postDelayed(new Runnable() {

            @Override
            public void run() {
                System.exit((int) (0));
            }
        }, 3000);
        AppMasterPreference.getInstance(QuickGestureMiuiTip.this)
                .setQuickGestureMiuiSettingFirstDialogTip(true);
    }

}
