
package com.leo.appmaster.eventbus.event;

public class LockThemeChangeEvent extends BaseEvent {

    public LockThemeChangeEvent() {
        mEventId = EventId.EVENT_LOCK_THEME_CHANGED;
        mEventMsg = "LockThemeChangeEvent";
    }

    public LockThemeChangeEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
    }

}
