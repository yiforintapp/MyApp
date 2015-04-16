
package com.leo.appmaster.fragment;

import android.view.MotionEvent;

import com.leo.appmaster.applocker.LockScreenActivity;

public abstract class PretendFragment extends BaseFragment {

    public void onUnlockPretendSuccessfully() {
        if (mActivity instanceof LockScreenActivity) {
            LockScreenActivity lsa = (LockScreenActivity) mActivity;
            lsa.removePretendFrame();
        }
    }

    public void onUnlockPretendFailed() {
        if (mActivity instanceof LockScreenActivity) {
            LockScreenActivity lsa = (LockScreenActivity) mActivity;
            lsa.onBackPressed();
        }
    }

}
