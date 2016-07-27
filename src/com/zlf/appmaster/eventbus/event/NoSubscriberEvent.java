
package com.zlf.appmaster.eventbus.event;

import com.zlf.appmaster.eventbus.LeoEventBus;

public final class NoSubscriberEvent extends BaseEvent {
    /**
     * The {@link LeoEventBus} instance to with the original event was posted
     * to.
     */
    public LeoEventBus mEventBus;

    /** The original event that could not be delivered to any subscriber. */
    public Object mOriginalEvent;

    public NoSubscriberEvent(LeoEventBus eventBus,
            Object originalEvent) {
        mEventId = EventId.EVENT_NO_SUBSCRIBER;
        mEventMsg = "NoSubscriberEvent";
        mEventBus = eventBus;
        mOriginalEvent = originalEvent;
    }

    public NoSubscriberEvent(int mEventId, String mEventMsg, LeoEventBus eventBus,
            Object originalEvent) {
        super(mEventId, mEventMsg);
        this.mEventBus = eventBus;
        this.mOriginalEvent = originalEvent;
    }

}
