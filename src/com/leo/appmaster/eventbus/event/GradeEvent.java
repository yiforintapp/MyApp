package com.leo.appmaster.eventbus.event;

/**
 * Created by forint on 16-3-21.
 */
public class GradeEvent extends BaseEvent {
    // 从应用页
    public static final int FROM_APP = 1;
    // 从图片隐藏页
    public static final int FROM_PIC = 2;
    // 从视频隐藏页
    public static final int FROM_VID = 3;

    public int mFromWhere;
    public boolean mShow;

    private GradeEvent() {

    }

    public GradeEvent(int fromWhere, boolean show) {
        this.mFromWhere = fromWhere;
        this.mShow = show;
    }

}
