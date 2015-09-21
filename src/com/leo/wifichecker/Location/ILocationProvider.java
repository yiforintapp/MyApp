package com.leo.wifichecker.Location;

import android.location.Location;

/**
 * Created by luqingyuan on 15/9/8.
 */
public interface ILocationProvider {
    /**
     * 定位服务是否可用
     * @return
     */
    public boolean isAvalible();

    /**
     * 启动定位服务
     * @return
     */
    public boolean start();

    /**
     * 停止定位服务
     */
    public void stop();

    /**
     * 获取最近一次定位的位置信息
     * @return
     */
    public Location getLastLocation();

    /**
     * 重新连接
     */
    public void restart();
}
