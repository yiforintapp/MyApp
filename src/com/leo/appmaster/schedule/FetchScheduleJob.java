package com.leo.appmaster.schedule;

/**
 * 拉取业务基类
 *  每12小时拉取一次，失败每3小时拉取一次，持续3次
 * Created by Jasper on 2015/9/8.
 */
public abstract class FetchScheduleJob extends ScheduleJob {

    /**
     * 默认拉取间隔，12小时
     */
    private static final int FETCH_PERIOD = 12 * 60 * 60 * 1000;
    /**
     * 失败拉取间隔，2小时
     */
    private static final int FETCH_FAIL_ERIOD = 2 * 60 * 60 * 1000;
    /**
     * 默认失败重试次数
     */
    private static final int FETCH_FAIL_COUNT = 3;

    @Override
    public int getId() {
        return getClass().getSimpleName().hashCode();
    }

    /**
     * 获取拉取间隔
     * @return
     */
    protected int getPeriod() {
        return FETCH_PERIOD;
    }

    /**
     * 获取失败后的拉取间隔
     * @return
     */
    protected int getFailPeriod() {
        return FETCH_FAIL_ERIOD;
    }

    /**
     * 获取失败后的重试次数
     * @return
     */
    protected int getRetryCount() {
        return FETCH_FAIL_COUNT;
    }
}
