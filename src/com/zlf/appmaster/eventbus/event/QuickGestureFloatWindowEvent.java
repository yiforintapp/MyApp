
package com.zlf.appmaster.eventbus.event;

public class QuickGestureFloatWindowEvent extends BaseEvent {
    public String editModel;
    public int count;

    public QuickGestureFloatWindowEvent() {
        mEventId = EventId.EVENT_PRIVACY_DELET_EDIT_MODEL;
        mEventMsg = "PrivacyDeletEditEventBus";
    }

    public QuickGestureFloatWindowEvent(String eventMsg) {
        mEventId = EventId.EVENT_PRIVACY_DELET_EDIT_MODEL;
        this.editModel = eventMsg;
        mEventMsg = eventMsg;
    }

    public QuickGestureFloatWindowEvent(String eventMsg, int count) {
        mEventId = EventId.EVENT_PRIVACY_DELET_EDIT_MODEL;
        this.editModel = eventMsg;
        mEventMsg = eventMsg;
        this.count = count;
    }

    public QuickGestureFloatWindowEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
    }
}
