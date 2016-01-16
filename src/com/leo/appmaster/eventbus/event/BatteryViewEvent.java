
package com.leo.appmaster.eventbus.event;

import com.leo.appmaster.mgr.BatteryManager;

public class BatteryViewEvent extends BaseEvent {

    public String eventMsg;
    public BatteryManager.BatteryState state;
    public int remainTime;

    public BatteryViewEvent(int mEventId, BatteryManager.BatteryState state) {
        super(mEventId, "battery state change");

        this.state = state;
    }


    public BatteryViewEvent(String mEventMsg) {
        this.eventMsg = mEventMsg;
    }

}
