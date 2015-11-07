package com.leo.appmaster.eventbus.event;

/**
 * Created by Jasper on 2015/10/13.
 */
public class SecurityScoreEvent extends BaseEvent {
    public int securityScore;
    public SecurityScoreEvent(int securityScore) {
        super();
        mEventMsg = "SecurityScoreEvent";
        this.securityScore = securityScore;
    }

    public int getEventId() {
        return mEventId;
    }
}
