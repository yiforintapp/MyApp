
package com.zlf.appmaster.eventbus.event;


public class SubmaineAnimEvent extends BaseEvent {
    public  String  eventMsg;
    public SubmaineAnimEvent() {
        mEventId = EventId.EVENT_SUBMARINE_ANIM;
        mEventMsg = "LoadAdFailEvent";
    }
    public SubmaineAnimEvent(String mEventMsg) {
        mEventId = EventId.EVENT_SUBMARINE_ANIM;
        this.eventMsg = mEventMsg;
    }

    public SubmaineAnimEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg=mEventMsg;
    }

}
