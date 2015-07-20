
package com.leo.appmaster.applocker;

import android.os.Bundle;
import android.view.View;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.ZipperView.OnGestureSuccessListener;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;

public class BeautyWeiZhuang extends BaseActivity
{
    private final static int BEAUTYWEIZHUANG = 1;
    private LEOAlarmDialog mAlarmDialog;
    private AppMasterPreference mAppMasterSP;
    private ZipperView mZipperView;
    
    
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
                showAlarmDialog("标题啦","呵呵","确定");
            }
        });
        
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
                       
                        BeautyWeiZhuang.this.finish();
                    }

                }
            });
        }
        mAlarmDialog.setSureButtonText(sureText);
        mAlarmDialog.setTitle(title);
        mAlarmDialog.setContent(content);
        mAlarmDialog.show();
    }
}
