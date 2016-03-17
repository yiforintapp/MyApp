
package com.leo.appmaster.applocker.lockswitch;


import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.utils.LeoLog;

/**
 * @author nic
 */
public class WifiLockSwitch extends SwitchGroup {
    private static final String TAG = "WifiLockSwitch";

    public WifiLockSwitch() {
        super();
    }

    @Override
    public void switchOn(LockMode mode) {
        String modeName = mode.modeName;
        mPreTable.putBoolean(modeName + "_" + WIFI_SWITCH, true);
    }

    @Override
    public void switchOff(LockMode mode) {
        String modeName = mode.modeName;
        mPreTable.putBoolean(modeName + "_" + WIFI_SWITCH, false);
    }

    @Override
    public int getLockNum() {
        return mPreTable.getInt(WIFI_SWITCH, 500000);
    }

    @Override
    public void setLockNum(int num) {
        mPreTable.putInt(WIFI_SWITCH, num);
    }

    @Override
    public boolean isLockNow(LockMode mode) {
        String modeName = mode.modeName;
        return mPreTable.getBoolean(modeName + "_" + WIFI_SWITCH, false);
    }

}
