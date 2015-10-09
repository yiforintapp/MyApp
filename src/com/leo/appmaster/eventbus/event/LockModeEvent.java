
package com.leo.appmaster.eventbus.event;

public class LockModeEvent extends BaseEvent {

    public String eventMsg = "";
    
    public LockModeEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg = mEventMsg;
    }

}
