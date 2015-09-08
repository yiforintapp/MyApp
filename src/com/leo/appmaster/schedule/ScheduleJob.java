package com.leo.appmaster.schedule;

/**
 * Created by Jasper on 2015/9/8.
 */
public abstract class ScheduleJob {

    /**
     * job的唯一id
     * @return
     */
    public abstract int getId();

    /**
     * 启动定时任务
     */
    public abstract void start();

    /**
     * 开始执行任务
     */
    protected abstract void work();

}
