
package com.leo.appmaster.eventbus.event;

public class WifiSecurityEvent extends BaseEvent {

    public String eventMsg;

    public WifiSecurityEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);

        this.eventMsg = mEventMsg;
    }

}
