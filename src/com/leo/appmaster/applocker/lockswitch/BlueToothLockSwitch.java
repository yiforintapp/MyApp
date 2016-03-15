
package com.leo.appmaster.applocker.lockswitch;


/**
 * @author nic
 */
public class BlueToothLockSwitch extends SwitchGroup {
    private static final String TAG = "BlueToothLockSwitch";

    public BlueToothLockSwitch() {
        super();
    }

    @Override
    protected void switchOn() {

    }

    @Override
    protected void switchOff() {

    }

    @Override
    protected int getLockNum() {
        return 0;
    }

    @Override
    protected int setLockNum() {
        return 0;
    }

    @Override
    protected boolean isLockNow() {
        return false;
    }

}
