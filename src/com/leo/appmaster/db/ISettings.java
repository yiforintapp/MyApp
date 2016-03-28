package com.leo.appmaster.db;

/**
 * Created by Jasper on 2016/3/26.
 */
public interface ISettings {
    static final int PRIORITY_HIGH = 0;
    static final int PRIORITY_LOW = 1;

    static final String[] GROUP_HIGH = new String[] {

    };

    public void setInteger(String key, int value);
    public void setLong(String key, long value);
    public void setFloat(String key, float value);
    public void setDouble(String key, double value);
    public void setString(String key, String value);

    public int getInteger(String key, int def);
    public int getLong(String key, long def);
    public int getFloat(String key, float def);
    public int getDouble(String key, double def);
    public int getString(String key, String def);
}
