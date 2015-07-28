
package com.leo.appmaster.fragment;


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
            try {
                lsa.onBackPressed();
            } catch (Exception e) {                
            }
        }
    }

    public void setSelector() {
        // TODO Auto-generated method stub
    }

}
