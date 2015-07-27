
package com.leo.appmaster.fragment;

import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.ZipperView;
import com.leo.appmaster.applocker.ZipperView.OnGestureSuccessListener;
import com.leo.appmaster.applocker.ZipperView.OnGestureTooFastListener;
import com.leo.appmaster.applocker.ZipperView.OnGestureTooSlowListener;
import com.leo.appmaster.sdk.SDKWrapper;

public class PretendAppBeautyFragment extends PretendFragment {

    private ZipperView mZipperView;

    @Override
    protected int layoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.fragment_pretend_beauty;
    }

  
    
    
    
    
    
    
    
    
    @Override
    protected void onInitUI() {
        // TODO Auto-generated method stub
        
        SDKWrapper
        .addEvent(mActivity, SDKWrapper.P1, "appcover", "Beauty");
        
  
        mZipperView = (ZipperView) findViewById(R.id.zipperview_unlock);

        mZipperView.setOnGestureSuccessListener(new OnGestureSuccessListener() {

            @Override
            public void OnGestureSuccess() {
                // TODO Auto-generated method stub
                onUnlockPretendSuccessfully();
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, 
                        "appcover", "done_Beauty");
            }
        });
        mZipperView.setOnGestureTooFastListener(new OnGestureTooFastListener() {

            @Override
            public void OnGestureTooFast() {
                // TODO Auto-generated method stub
                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.zipper_too_fast), 0).show();
            }
        });
        mZipperView.setOnGestureTooSlowListener(new OnGestureTooSlowListener() {

            @Override
            public void OnGestureTooSlow() {
                // TODO Auto-generated method stub
                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.zipper_too_slow), 0).show();
            }
        });
    }

    public void onUnlockPretendSuccessfully() {
        if (mActivity instanceof LockScreenActivity) {
            LockScreenActivity lsa = (LockScreenActivity) mActivity;
            lsa.removePretendFrame();
        }
    }

    public void onUnlockPretendFailed() {
        if (mActivity instanceof LockScreenActivity) {
            LockScreenActivity lsa = (LockScreenActivity) mActivity;
            try {
                lsa.onBackPressed();
            } catch (Exception e) {
            }
        }
    }

}
