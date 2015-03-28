
package com.leo.appmaster.privacycontact;

import com.leo.appmaster.eventbus.event.BaseEvent;
import com.leo.appmaster.eventbus.event.EventId;

public class PrivacyDeletEditEventBus extends BaseEvent {
    public String editModel;
    public int count;

    public PrivacyDeletEditEventBus() {
        mEventId = EventId.EVENT_PRIVACY_DELET_EDIT_MODEL;
        mEventMsg = "PrivacyDeletEditEventBus";
    }

    public PrivacyDeletEditEventBus(String eventMsg) {
        mEventId = EventId.EVENT_PRIVACY_DELET_EDIT_MODEL;
        this.editModel = eventMsg;
        mEventMsg = eventMsg;
    }

    public PrivacyDeletEditEventBus(String eventMsg, int count) {
        mEventId = EventId.EVENT_PRIVACY_DELET_EDIT_MODEL;
        this.editModel = eventMsg;
        mEventMsg = eventMsg;
        this.count = count;
    }

    public PrivacyDeletEditEventBus(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
    }

}
