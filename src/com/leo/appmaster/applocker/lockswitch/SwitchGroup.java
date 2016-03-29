package com.leo.appmaster.applocker.lockswitch;


import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.db.LeoPreference;


/**
 * 应用锁开关抽象类
 *
 * @author nic
 */
public abstract class SwitchGroup {
    private static final String TAG = "SwitchGroup";

    public final static String WIFI_SWITCH = "com.wifi.lock";
    public final static String BLUE_TOOTH_SWITCH = "con.bluetooth.lock";
    public final static String SCREEN_SHOWED = "screen_showed";

    public AppMasterApplication mContext;
    public LeoPreference mPreTable;

    SwitchGroup() {
        mContext = AppMasterApplication.getInstance();
        mPreTable = LeoPreference.getInstance();
    }

    protected abstract void switchOn(LockMode mode);

    protected abstract void switchOff(LockMode mode);

    protected abstract int getLockNum();

    protected abstract void setLockNum(int num);

    protected abstract boolean isLockNow(LockMode mode);

    public boolean getScreenShowed() {
        return mPreTable.getBoolean(SCREEN_SHOWED, false);
    }

    public void setScreenShowed() {
        mPreTable.putBoolean(SCREEN_SHOWED, true);
    }
}
