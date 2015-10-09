
package com.leo.appmaster.eventbus.event;

import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;

public class ClickQuickItemEvent extends BaseEvent {
    public String eventMsg;
    public QuickSwitcherInfo info;
    
    public ClickQuickItemEvent() {
        mEventId = EventId.EVENT_CLICKQUICKITEM;
        mEventMsg = "ClickItem";
    }

    public ClickQuickItemEvent(String mEventMsg) {
        mEventId = EventId.EVENT_CLICKQUICKITEM;
        this.eventMsg = mEventMsg;
    }

    public ClickQuickItemEvent(int mEventId, String mEventMsg) {
        super(mEventId, mEventMsg);
        this.eventMsg = mEventMsg;
    }
    
    public ClickQuickItemEvent(String mEventMsg,QuickSwitcherInfo mInfo) {
        this.eventMsg = mEventMsg;
        this.info = mInfo;
    }

}
