
package com.zlf.appmaster.eventbus.event;

public class CommonEvent extends BaseEvent {
    public String eventMsg;
    private Object date;

    public Object getDate() {
        return date;
    }

    public void setDate(Object date) {
        this.date = date;
    }

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
