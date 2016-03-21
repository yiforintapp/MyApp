
package com.leo.appmaster.applocker.lockswitch;


import com.leo.appmaster.applocker.model.LockMode;

/**
 * @author nic
 */
public class BlueToothLockSwitch extends SwitchGroup {
    private static final String TAG = "BlueToothLockSwitch";

    public BlueToothLockSwitch() {
        super();
    }

    @Override
    public void switchOn(LockMode mode) {
        if (mode == null) {
            return;
        }
        String modeName = mode.modeName;
        mPreTable.putBoolean(modeName + "_" + BLUE_TOOTH_SWITCH, true);
    }

    @Override
    public void switchOff(LockMode mode) {
        if (mode == null) {
            return;
        }
        String modeName = mode.modeName;
        mPreTable.putBoolean(modeName + "_" + BLUE_TOOTH_SWITCH, false);
    }

    @Override
    public int getLockNum() {
        return mPreTable.getInt(BLUE_TOOTH_SWITCH, 4000000);
    }

    @Override
    public void setLockNum(int num) {
        mPreTable.putInt(BLUE_TOOTH_SWITCH, num);
    }

    @Override
    public boolean isLockNow(LockMode mode) {
        if (mode == null) {
            return false;
        }
        String modeName = mode.modeName;
        return mPreTable.getBoolean(modeName + "_" + BLUE_TOOTH_SWITCH, false);
    }

}
