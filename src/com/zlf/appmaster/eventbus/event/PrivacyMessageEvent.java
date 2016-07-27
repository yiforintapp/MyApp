
package com.zlf.appmaster.eventbus.event;


public class PrivacyMessageEvent extends BaseEvent {
    public  String  eventMsg;
    public PrivacyMessageEvent() {
        mEventId = EventId.EVENT_PRIVACY_EDIT_MODEL;
        mEventMsg = "PrivacyMessageEventBus";
    }
    public PrivacyMessageEvent(String mEventMsg) {
        mEventId = EventId.EVENT_PRIVACY_EDIT_MODEL;
        this.eventMsg = mEventMsg;
    }

    public PrivacyMessageEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg=mEventMsg;
    }

}
