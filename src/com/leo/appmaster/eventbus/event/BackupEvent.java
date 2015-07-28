
package com.leo.appmaster.eventbus.event;

public class BackupEvent extends BaseEvent {
    public String eventMsg;
    
    public BackupEvent() {
        mEventId = EventId.EVENT_BACKUP;
        mEventMsg = "BackupBus";
    }

    public BackupEvent(String mEventMsg) {
        mEventId = EventId.EVENT_BACKUP;
        this.eventMsg = mEventMsg;
    }

    public BackupEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg = mEventMsg;
    }

}
