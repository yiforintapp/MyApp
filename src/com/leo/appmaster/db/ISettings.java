package com.leo.appmaster.db;

/**
 * Created by Jasper on 2016/3/26.
 */
public abstract class ISettings {
    static final int PRIORITY_HIGH = 0;
    static final int PRIORITY_LOW = 1;

    static final String[] GROUP_HIGH = new String[] {

    };

    public abstract void set(String key, String value);
    public abstract int get(String key, String def);

    public boolean isHighPriority(String key) {
        return false;
    }

    public boolean isEncrypto() {
        return false;
    }
}
