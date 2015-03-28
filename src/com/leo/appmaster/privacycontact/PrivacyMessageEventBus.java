
package com.leo.appmaster.privacycontact;

import com.leo.appmaster.eventbus.event.BaseEvent;
import com.leo.appmaster.eventbus.event.EventId;

public class PrivacyMessageEventBus extends BaseEvent {
    public  String  eventMsg;
    public PrivacyMessageEventBus() {
        mEventId = EventId.EVENT_PRIVACY_EDIT_MODEL;
        mEventMsg = "PrivacyMessageEventBus";
    }
    public PrivacyMessageEventBus(String mEventMsg) {
        mEventId = EventId.EVENT_PRIVACY_EDIT_MODEL;
        this.eventMsg = mEventMsg;
    }

    public PrivacyMessageEventBus(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg=mEventMsg;
    }

}
