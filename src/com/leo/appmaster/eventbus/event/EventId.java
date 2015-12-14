
package com.leo.appmaster.eventbus.event;

/**
 * this class hold all event id
 *
 * @author zhangwenyang
 */
public interface EventId {

    public static final int EVENT_NO_SUBSCRIBER = 1000;

    public static final int EVENT_LOCK_THEME_CHANGED = 1001;

    public static final int EVENT_APP_UNLOCKED = 1002;

    public static final int EVENT_PRIVACY_LEVEL_COMPUTED = 1003;

    /* Privacy contact eventId */
    public static final int EVENT_PRIVACY_EDIT_MODEL = 1004;
    public static final int EVENT_PRIVACY_DELET_EDIT_MODEL = 1005;
    /* Privacy contact end */

    public static final int EVENT_MODE_CHANGE = 1006;
    public static final int EVENT_TIME_LOCK_CHANGE = 1007;
    public static final int EVENT_LOCATION_LOCK_CHANGE = 1008;

    public static final int EVENT_BACKUP = 1009;
    public static final int EVENT_DELETE = 1010;
    public static final int EVENT_DAY_TRAFFIC_SET = 1011;

    public static final int EVENT_NEW_THEME = 1012;
    /* Quick gesture floatWindow eventId */
    public static final int EVENT_QUICK_GESTURE_FLOAT_WINDOW = 1013;
    /* Quick gesture floatWindow end */

    public static final int EVENT_CLICKQUICKITEM = 1014;
    public static final int EVENT_SUBMARINE_ANIM = 1015;

    public static final int EVENT_WIFISECURITY = 1091;

    public static final int EVENT_DEVICE_ADMIN_ENABLE = 1092;
    public static final int EVENT_DEVICE_ADMIN_DISABLE = 1093;

    public static final int EVENT_HOME_GUIDE_UP_ARROW = 1094;

    public static final int EVENT_HOME_GUIDE_GONE_ID = 1095;

}
