package com.zlf.appmaster.db;

/**
 * Created by Jasper on 2015/9/29.
 */
@Deprecated
public class LeoPreference {
    private static final byte[] LOCK = new byte[1];
    private static LeoPreference sInstance;

    public static LeoPreference getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new LeoPreference();
                }
            }
        }

        return sInstance;
    }

    public LeoPreference() {
    }

    public int getInt(String key, int def) {
        return LeoSettings.getInteger(key, def);
    }

    public long getLong(String key, long def) {
        return LeoSettings.getLong(key, def);
    }

    public double getDouble(String key, double def) {
        return LeoSettings.getDouble(key, def);
    }

    public float getFloat(String key, float def) {
        return LeoSettings.getFloat(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return LeoSettings.getBoolean(key, def);
    }

    public synchronized String getString(String key) {
        return LeoSettings.getString(key, null);
    }

    public void putInt(String key, int value) {
        putString(key, value + "");
    }

    public void putDouble(String key, double value) {
        putString(key, Double.toString(value));
    }

    public void putFloat(String key, float value) {
        putString(key, Float.toString(value));
    }

    public void putBoolean(String key, boolean value) {
        LeoSettings.setBoolean(key, value);
    }

    public void putLong(String key, long value) {
        putString(key, value + "");
    }

    public void putString(final String key, final String value) {
        LeoSettings.setString(key, value);
    }

}
