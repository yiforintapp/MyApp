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
    public void set(String key, String value) {
        preferenceTable.putString(key, value);
    }

    @Override
    public String get(String key, String def) {
        String value = preferenceTable.getString(key);
        return value == null ? def : value;
    }

    @Override
    public void setBundleMap(Map<String, Object> map) {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value == null) {
                continue;
            }

            preferenceTable.putString(key, String.valueOf(value));
        }
    }
}
