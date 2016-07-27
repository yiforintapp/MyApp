
package com.zlf.appmaster.eventbus.event;


public class DayTrafficSetEvent extends BaseEvent {
    public String eventMsg;
    
    public DayTrafficSetEvent() {
        mEventId = EventId.EVENT_DAY_TRAFFIC_SET;
        mEventMsg = "DayTrafficSet";
    }

    public DayTrafficSetEvent(String mEventMsg) {
        mEventId = EventId.EVENT_DAY_TRAFFIC_SET;
        this.eventMsg = mEventMsg;
    }

    public DayTrafficSetEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg = mEventMsg;
    }

}
