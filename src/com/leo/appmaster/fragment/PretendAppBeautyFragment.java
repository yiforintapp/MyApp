package com.leo.appmaster.fragment;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.ZipperView;
import com.leo.appmaster.applocker.ZipperView.OnGestureSuccessListener;

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
        mZipperView=(ZipperView) findViewById(R.id.zipperview_unlock);
        
        mZipperView.setOnGestureSuccessListener(new OnGestureSuccessListener() {
            
            @Override
            public void OnGestureSuccess() {
                // TODO Auto-generated method stub
                onUnlockPretendSuccessfully();
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
