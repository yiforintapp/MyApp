
package com.leo.appmaster.fragment;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
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
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, 
                        "appcover", "fail_Beauty");
            }
        });
        mZipperView.setOnGestureTooSlowListener(new OnGestureTooSlowListener() {

            @Override
            public void OnGestureTooSlow() {
                // TODO Auto-generated method stub
                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.zipper_too_slow), 0).show();
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, 
                        "appcover", "fail_Beauty");
            }
        });
        
        // 背景使用565, 减少内存占用
        Options ops = new Options();
        ops.inPreferredConfig = Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_beauty, ops);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        mRootView.setBackgroundDrawable(drawable);
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
