
package com.zlf.appmaster.eventbus.event;

/**
 * Base event class, all event must extend it
 * 
 * @author zhangwenyang
 */
public class BaseEvent {

    /**
     * event identify, if we need not this identify,just set 0;
     */
    protected int mEventId = 0;

    /**
     * the event description
     */
    protected String mEventMsg = "BaseEvent";

    public BaseEvent() {

    }

    public BaseEvent(int mEventId, String mEventMsg) {
        super();
        this.mEventId = mEventId;
        this.mEventMsg = mEventMsg;
    }

    @Override
    public String toString() {

        return "is: " + mEventId + "   msg: " + mEventMsg;
    }
}
