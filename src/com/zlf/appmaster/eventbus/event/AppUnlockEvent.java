
package com.zlf.appmaster.eventbus.event;

public class AppUnlockEvent extends BaseEvent {

    public static final int RESULT_UNLOCK_SUCCESSFULLY = 0;
    public static final int RESULT_UNLOCK_CANCELED = 1;
    public static final int RESULT_UNLOCK_OUTCOUNT = 2;

    public int mUnlockResult;
    public String mUnlockedPkg;

    public AppUnlockEvent(String pkg, int result) {
        mEventId = EventId.EVENT_APP_UNLOCKED;
        mEventMsg = "AppUnlockedEvent";
        mUnlockedPkg = pkg;
        mUnlockResult = result;
    }

    public AppUnlockEvent(int mEventId, String mEventMsg, String pkg) {
        super(mEventId, mEventMsg);
        mUnlockedPkg = pkg;
    }

}
