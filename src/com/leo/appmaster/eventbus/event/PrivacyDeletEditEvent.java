
package com.leo.appmaster.eventbus.event;

public class PrivacyDeletEditEvent extends BaseEvent {
    public String editModel;
    public int count;

    public PrivacyDeletEditEvent() {
        mEventId = EventId.EVENT_QUICK_GESTURE_FLOAT_WINDOW;
        mEventMsg = "PrivacyDeletEditEventBus";
    }

    public PrivacyDeletEditEvent(String eventMsg) {
        mEventId = EventId.EVENT_QUICK_GESTURE_FLOAT_WINDOW;
        this.editModel = eventMsg;
        mEventMsg = eventMsg;
    }

    public PrivacyDeletEditEvent(String eventMsg, int count) {
        mEventId = EventId.EVENT_QUICK_GESTURE_FLOAT_WINDOW;
        this.editModel = eventMsg;
        mEventMsg = eventMsg;
        this.count = count;
    }

    public PrivacyDeletEditEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
    }

}
