
package com.zlf.appmaster.eventbus.event;

import android.content.Intent;

public class BaseBroadcastEvent extends BaseEvent {

    private String mAction;
    private Intent mIntent;

    public BaseBroadcastEvent(int mEventId, String mEventMsg, String action, Intent intent) {
        super(mEventId, mEventMsg);
        mAction = action;
        mIntent = intent;
    }

    public String getAction() {
        return mAction;
    }

    public Intent getIntent() {
        return mIntent;
    }
}
