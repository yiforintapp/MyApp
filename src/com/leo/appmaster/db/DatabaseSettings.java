package com.leo.appmaster.db;

import com.leo.appmaster.utils.PrefConst;

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

    }

    @Override
    public String get(String key, String def) {
        return null;
    }
}
