package com.leo.appmaster.eventbus.event;

/**
 * Created by Jasper on 2015/9/15.
 */
public class MsgCenterEvent extends BaseEvent {
    public static final int ID_HTML = 100;
    public static final int ID_RES = 101;

    public MsgCenterEvent(int id) {
        super(id, "MsgCenterEvent");
    }


}
