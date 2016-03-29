package com.leo.appmaster.db;

/**
 * Created by Jasper on 2016/3/26.
 */
public abstract class ISettings {
    static final int PRIORITY_HIGH = 0;
    static final int PRIORITY_LOW = 1;

    static final String[] GROUP_HIGH = new String[] {
            "last_version","last_sync_business_time","last_business_red_tip"
            ,"lock_type","first_install_app","lock_remind","have_ever_load_apps"
            ,"last_alarm_set_time","need_cut_backup_uninstall_and_privacycontract"
            ,"remove_unlock_all_shortcut","update_quick_gesture_user","update_recovery_defatult_data"
            ,"last_guide_version","first_use_new_version","unlock_count","google_play_guide_tip_show"
            ,"splash_skip_url","splash_skip_to_client","splash_delay_time","unlock_success_tip_random"
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
