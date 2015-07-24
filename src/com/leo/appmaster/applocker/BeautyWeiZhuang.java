
package com.leo.appmaster.applocker;

import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.ZipperView.OnGestureSuccessListener;
import com.leo.appmaster.applocker.ZipperView.OnGestureTooFastListener;
import com.leo.appmaster.applocker.ZipperView.OnGestureTooSlowListener;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;

public class BeautyWeiZhuang extends BaseActivity
{
    private final static int BEAUTYWEIZHUANG = 4;
    private LEOAlarmDialog mAlarmDialog;
    private AppMasterPreference mAppMasterSP;
    private ZipperView mZipperView;
    private Vibrator vib;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_beauty);
        init();
    }

    private void init() {
        mAppMasterSP=AppMasterPreference.getInstance(this);
        mZipperView=(ZipperView) findViewById(R.id.zipperview_beauty_guide);
        mZipperView.setOnGestureSuccessListener(new OnGestureSuccessListener() {
       
            @Override
            public void OnGestureSuccess() {
                // TODO Auto-generated method stub
                showAlarmDialog(getString(R.string.open_weizhuang_dialog_title),
                        getString(R.string.open_weizhuang_dialog_content),
                        getString(R.string.open_weizhuang_dialog_sure));
            }
        });
        
        
        
        
        vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
    }
        
    private void showAlarmDialog(String title, String content, String sureText)
    {
        if (mAlarmDialog == null)
        {
            mAlarmDialog = new LEOAlarmDialog(this);
            mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {
                @Override
                public void onClick(int which) {
                    // ok
                    if (which == 1)
                    {               
                        mAppMasterSP.setPretendLock(BEAUTYWEIZHUANG);       
                        vib.vibrate(150);
                        BeautyWeiZhuang.this.finish();
//                     Toast.makeText(this, getString(R.string.beauty_mode_ok), 0).show();
                    }

                }
            });
        }
        mAlarmDialog.setSureButtonText(sureText);
        mAlarmDialog.setTitle(title);
        mAlarmDialog.setContent(content);
        mAlarmDialog.show();
    }
    protected void onDestroy() {
        if (mAlarmDialog != null) {
            mAlarmDialog.dismiss();
            mAlarmDialog = null;
        }
        super.onDestroy();
    }
}
