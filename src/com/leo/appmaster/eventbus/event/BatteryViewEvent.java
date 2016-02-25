
package com.leo.appmaster.eventbus.event;

import com.leo.appmaster.mgr.BatteryManager;

public class BatteryViewEvent extends BaseEvent {

    public String eventMsg;
    public BatteryManager.BatteryState state;
    public int remainTimes[];

    public BatteryViewEvent(int mEventId, BatteryManager.BatteryState state, int[] times) {
        super(mEventId, "battery state change");

        this.state = state;
        this.remainTimes = times;
    }

    public BatteryViewEvent(String mEventMsg) {
        this.eventMsg = mEventMsg;
    }
}
