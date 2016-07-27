
package com.zlf.appmaster.eventbus.event;

public class PrivacyEditFloatEvent extends BaseEvent {
    public String editModel;
    public int count;

    public PrivacyEditFloatEvent() {
        mEventId = EventId.EVENT_QUICK_GESTURE_FLOAT_WINDOW;
        mEventMsg = "PrivacyDeletEditEventBus";
    }

    public PrivacyEditFloatEvent(String eventMsg) {
        mEventId = EventId.EVENT_QUICK_GESTURE_FLOAT_WINDOW;
        this.editModel = eventMsg;
        mEventMsg = eventMsg;
    }

    public PrivacyEditFloatEvent(String eventMsg, int count) {
        mEventId = EventId.EVENT_QUICK_GESTURE_FLOAT_WINDOW;
        this.editModel = eventMsg;
        mEventMsg = eventMsg;
        this.count = count;
    }

    public PrivacyEditFloatEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
    }

}
