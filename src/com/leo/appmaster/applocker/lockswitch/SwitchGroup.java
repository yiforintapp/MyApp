package com.leo.appmaster.applocker.lockswitch;


import com.leo.appmaster.AppMasterApplication;


/**
 * 应用锁开关抽象类
 *
 * @author nic
 */
public abstract class SwitchGroup {
    private static final String TAG = "SwitchGroup";

    public final static String WIFI_SWITCH = "com.wifi.lock";
    public final static String BLUE_TOOTH_SWITCH = "con.bluetooth.lock";
    public final static String MOBILE_DATA_SWITCH = "com.mobiledata.lock";

    protected AppMasterApplication mContext;

    SwitchGroup() {
        mContext = AppMasterApplication.getInstance();
    }

    protected abstract void switchOn();

    protected abstract void switchOff();

    protected abstract int getLockNum();

    protected abstract int setLockNum();

    protected abstract boolean isLockNow();

}
