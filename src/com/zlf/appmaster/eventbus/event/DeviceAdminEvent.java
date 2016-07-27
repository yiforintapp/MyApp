package com.zlf.appmaster.eventbus.event;

public class DeviceAdminEvent extends BaseEvent {
    public DeviceAdminEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
    }
    public int getEventId(){
        return mEventId;
    }
}
