
package com.leo.appmaster.eventbus.event;


public class LoadAdFailEvent extends BaseEvent {
    public  String  eventMsg;
    public LoadAdFailEvent() {
        mEventId = EventId.EVENT_LOAD_FAIL_ROLL_AGAIN;
        mEventMsg = "LoadAdFailEvent";
    }
    public LoadAdFailEvent(String mEventMsg) {
        mEventId = EventId.EVENT_LOAD_FAIL_ROLL_AGAIN;
        this.eventMsg = mEventMsg;
    }

    public LoadAdFailEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg=mEventMsg;
    }

}
