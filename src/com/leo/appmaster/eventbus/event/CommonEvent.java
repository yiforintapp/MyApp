
package com.leo.appmaster.eventbus.event;

public class CommonEvent extends BaseEvent {
    public String eventMsg;

    public CommonEvent() {
        mEventId = EventId.EVENT_BACKUP;
        mEventMsg = "BackupBus";
    }

    public CommonEvent(String mEventMsg) {
        mEventId = EventId.EVENT_BACKUP;
        this.eventMsg = mEventMsg;
    }

    public CommonEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg = mEventMsg;
    }

}
