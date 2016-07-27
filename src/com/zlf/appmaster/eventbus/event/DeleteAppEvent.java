package com.zlf.appmaster.eventbus.event;

public class DeleteAppEvent extends BaseEvent {
    public String eventMsg;
    
    public DeleteAppEvent() {
        mEventId = EventId.EVENT_DELETE;
        mEventMsg = "DeleteAppBus";
    }

    public DeleteAppEvent(String mEventMsg) {
        mEventId = EventId.EVENT_DELETE;
        this.eventMsg = mEventMsg;
    }

    public DeleteAppEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg = mEventMsg;
    }
}
