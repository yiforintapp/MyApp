
package com.leo.appmaster.applocker.model;

import java.util.List;

import android.graphics.Bitmap;

public class LockMode {
    public int modeId;
    public String modeName;
    public Bitmap modeIcon;
    public List<String> lockList;
    /**
     * 0: unlock all; 1: visitor mode; 2: office mode; 3: family mode; -1: other
     */
    public int defaultFlag;
    public boolean isCurrentUsed;
    public boolean haveEverOpened;
    public boolean selected;

    public LockMode() {
    }
}
