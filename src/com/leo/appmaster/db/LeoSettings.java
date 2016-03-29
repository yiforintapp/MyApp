package com.leo.appmaster.db;

/**
 * Created by Jasper on 2016/3/26.
 */
public class LeoSettings {
    private ISettings mDatabase = new DatabaseSettings();
    private ISettings mPreference = new SharedSettings();

    public void set(String key, String value) {

    }

    public int get(String key, String def) {
        return 0;
    }
}
