
package com.leo.appmaster.eventbus.event;

import com.leo.appmaster.mgr.impl.BatteryManagerImpl;

public class BatteryViewEvent extends BaseEvent {

    public String eventMsg;
    public BatteryManagerImpl.BatteryState state;
    public int remainTime;

    public BatteryViewEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);

        this.eventMsg = mEventMsg;
    }


    public BatteryViewEvent(String mEventMsg) {
        this.eventMsg = mEventMsg;
    }

}
