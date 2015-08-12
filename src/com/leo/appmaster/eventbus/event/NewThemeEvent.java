
package com.leo.appmaster.eventbus.event;

public class NewThemeEvent extends BaseEvent {

    public boolean newTheme;

    public NewThemeEvent(int mEventId, String mEventMsg, boolean newTheme) {
        super(mEventId, mEventMsg);

        this.newTheme = newTheme;
    }

}
