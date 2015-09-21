package com.leo.appmaster.eventbus.event;

/**
 * Created by Jasper on 2015/9/16.
 */
public class AppLockChangeEvent extends BaseEvent {
    public static final int APP_ADD = 100;
    public static final int APP_REMOVE = 101;

    private String pkg;

    public AppLockChangeEvent(int id) {
        super(id, "AppLockChangeEvent");
    }
}
