package com.leo.appmaster.eventbus.event;

/**
 * Created by Jasper on 2015/10/27.
 */
public class SecurityNotifyChangeEvent extends BaseEvent {

    public String mgr;

    public SecurityNotifyChangeEvent(String mgr) {
        super();
        mEventMsg = "SecurityNotifyChangeEvent";

        this.mgr = mgr;
    }
}

