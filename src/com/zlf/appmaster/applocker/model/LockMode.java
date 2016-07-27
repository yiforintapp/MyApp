
package com.zlf.appmaster.applocker.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class LockMode implements Parcelable {
    /**
     * 访客模式
     */
    public static final int MODE_VISITOR = 1;
    /**
     * 
     */
    public static final int MODE_OFFICE = 2;
    /**
     * 家庭模式
     */
    public static final int MODE_FAMILY = 3;
    /**
     * 其它模式
     */
    public static final int MODE_OTHER = -1;
    
    public int modeId;
    public String modeName;
    // public Bitmap modeIcon;
//    public int modeIconId;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(modeId);
        dest.writeString(modeName);
        dest.writeInt(defaultFlag);
        dest.writeInt(isCurrentUsed ? 1 : 0);
        dest.writeInt(haveEverOpened ? 1 : 0);
        dest.writeInt(selected ? 1 : 0);
        
        // 写入列表的长度
        dest.writeInt(lockList != null ? lockList.size() : 0);
        for (String lock : lockList) {
            dest.writeString(lock);
        }
    }

    public static final Creator<LockMode> CREATOR = new Creator<LockMode>() {
        @Override
        public LockMode createFromParcel(Parcel source) {
            LockMode lockMode = new LockMode();
            lockMode.modeId = source.readInt();
            lockMode.modeName = source.readString();
            lockMode.defaultFlag = source.readInt();
            lockMode.isCurrentUsed = source.readInt() == 1 ? true : false;
            lockMode.haveEverOpened = source.readInt() == 1 ? true : false;
            lockMode.selected = source.readInt() == 1 ? true : false;
            
            int listSize = source.readInt();
            for (int i = 0; i < listSize; i++) {
                if (lockMode.lockList == null) {
                    lockMode.lockList = new ArrayList<String>();
                }
                lockMode.lockList.add(source.readString());
            }
            return lockMode;
        }

        @Override
        public LockMode[] newArray(int size) {
            return new LockMode[size];
        }
    };
}
