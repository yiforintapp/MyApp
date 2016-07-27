
package com.zlf.appmaster.model;

import android.graphics.drawable.Drawable;

public class BaseInfo {

    /**
     * item type
     */
    public static final int ITEM_TYPE_NORMAL_APP = 0;
    public static final int ITEM_TYPE_FOLDER = 1;
    public static final int ITEM_TYPE_BUSINESS_APP = 2;
    // add at v2.3
    public static final int ITEM_TYPE_SWITCHER = 3;

    /**
     * label
     */
    public String label = "";

    /**
     * icon
     */
    public Drawable icon;

    /**
     * top index
     */
    public int topPos = -1;

    /**
     * item type
     */
    public int type = ITEM_TYPE_NORMAL_APP;

    /**
     * just for unread sms and call
     */
    public int eventNumber = 0;
    public int gesturePosition = -1000;
    public String swtichIdentiName;
}
