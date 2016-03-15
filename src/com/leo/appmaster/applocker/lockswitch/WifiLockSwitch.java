
package com.leo.appmaster.applocker.lockswitch;


/**
 * @author nic
 */
public class WifiLockSwitch extends SwitchGroup {
    private static final String TAG = "WifiLockSwitch";

    public WifiLockSwitch() {
        super();
    }

    @Override
    public void switchOn() {

    }

    @Override
    public void switchOff() {

    }

    @Override
    public int getLockNum() {
        return 0;
    }

    @Override
    public int setLockNum() {
        return 0;
    }

    @Override
    public boolean isLockNow() {
        return true;
    }

}
