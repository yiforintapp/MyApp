package com.leo.appmaster.db;

import java.util.Map;

/**
 * Created by Jasper on 2016/3/26.
 */
public class DatabaseSettings extends ISettings {
    private PreferenceTable preferenceTable;

    DatabaseSettings() {
        preferenceTable = PreferenceTable.getInstance();
    }

    @Override
    public void setBoolean(String key, boolean value) {
        preferenceTable.putBoolean(key, value);
    }

    @Override
    public void setInteger(String key, int value) {
        preferenceTable.putInt(key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        preferenceTable.putDouble(key, value);
    }

    @Override
    public void setFloat(String key, float value) {
        preferenceTable.putFloat(key, value);
    }

    @Override
    public void setLong(String key, long value) {
        preferenceTable.putLong(key, value);
    }

    @Override
    public void setString(String key, String value) {
        preferenceTable.putString(key, value);
    }


    @Override
    public boolean getBoolean(String key, boolean def) {
        return preferenceTable.getBoolean(key, def);
    }

    @Override
    public int getInteger(String key, int def) {
        return preferenceTable.getInt(key, def);
    }

    @Override
    public long getLong(String key, long def) {
        return preferenceTable.getLong(key, def);
    }

    @Override
    public float getFloat(String key, float def) {
        return preferenceTable.getFloat(key, def);
    }

    @Override
    public double getDouble(String key, double def) {
        return preferenceTable.getDouble(key, def);
    }

    @Override
    public String getString(String key, String def) {
        return preferenceTable.getString(key, def);
    }

    @Override
    public void setBundleMap(Map<String, Object> map, OnBundleSavedListener listener) {
        preferenceTable.putBundleMap(map, listener);
    }
}
