
package com.leo.appmaster.fragment;

import com.leo.appmaster.applocker.manager.LockManager;

public abstract class LockFragment extends BaseFragment {

    protected final int mMaxInput = 5;

    // public static final int FROM_SELF = 0;
    // public static final int FROM_SELF_HOME = 3;
    // public static final int FROM_OTHER = 1;
    // public static final int FROM_SCREEN_ON = 4;
    // public static final int FROM_RESTART = 2;
    protected int mLockMode = LockManager.LOCK_MODE_FULL;

    public static final int LOCK_TYPE_PASSWD = 0;
    public static final int LOCK_TYPE_GESTURE = 1;
    protected int mLockType = LOCK_TYPE_PASSWD;

    protected int mInputCount = 0;

    protected String mPackageName;

    public void setPackage(String pkg) {
        mPackageName = pkg;
    }

    public void setLockType(int type) {
        mLockType = type;
    }

    public void setLockMode(int mode) {
        mLockMode = mode;
    }

    public abstract void onLockPackageChanged(String mLockedPackage);

    public abstract void onNewIntent();

}
