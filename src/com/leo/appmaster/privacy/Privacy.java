package com.leo.appmaster.privacy;

import java.util.List;

/**
 * Created by Jasper on 2016/3/31.
 */
public abstract class Privacy<T> {

    /**
     * 获取新增个数
     * @return
     */
    public abstract int getAddedCount();

    /**
     * 获取总数
     * @return
     */
    public abstract int getTotalCount();

    /**
     * 获取已处理的个数
     * @return
     */
    public abstract int getProceedCount();

    public abstract List<T> getAddedList();

    public abstract int getFoundStringId();
    public abstract int getAddedStringId();
    public abstract int getProceedStringId();
}
