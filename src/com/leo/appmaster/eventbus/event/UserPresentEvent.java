package com.leo.appmaster.eventbus.event;

/**
 * Created by chenfs on 16-3-24.
 */
public class UserPresentEvent extends BaseEvent {
    public UserPresentEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
    }
    public int getEventId(){
        return mEventId;
    }
}
