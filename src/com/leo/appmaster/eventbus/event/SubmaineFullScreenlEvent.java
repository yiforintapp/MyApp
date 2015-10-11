
package com.leo.appmaster.eventbus.event;


public class SubmaineFullScreenlEvent extends BaseEvent {
    public  String  eventMsg;
    public SubmaineFullScreenlEvent() {
        mEventId = EventId.EVENT_SUBMARINE_FULL_SCREEN;
        mEventMsg = "LoadAdFailEvent";
    }
    public SubmaineFullScreenlEvent(String mEventMsg) {
        mEventId = EventId.EVENT_SUBMARINE_FULL_SCREEN;
        this.eventMsg = mEventMsg;
    }

    public SubmaineFullScreenlEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg=mEventMsg;
    }

}
