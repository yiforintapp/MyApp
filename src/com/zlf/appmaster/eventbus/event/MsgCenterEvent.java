package com.zlf.appmaster.eventbus.event;

/**
 * Created by Jasper on 2015/9/15.
 */
public class MsgCenterEvent extends BaseEvent {
    // html文件下载成功
    public static final int ID_HTML = 100;
    // 资源文件下载成功
    public static final int ID_RES = 101;
    // 消息列表拉取成功
    public static final int ID_MSG = 102;

    public int count;

    public MsgCenterEvent(int id) {
        super(id, "MsgCenterEvent");
    }

    public int getEventId() {
        return mEventId;
    }


}
