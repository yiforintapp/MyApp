
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
