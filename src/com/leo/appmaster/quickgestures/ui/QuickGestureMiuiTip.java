
package com.leo.appmaster.quickgestures.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;

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
    }

}
