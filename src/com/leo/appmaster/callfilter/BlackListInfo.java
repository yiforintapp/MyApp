package com.leo.appmaster.callfilter;

import java.io.Serializable;

import android.graphics.Bitmap;

/**
 * Created by runlee on 15-12-19.
 */
public class BlackListInfo implements Serializable {
    public int id = -1;
    public String number;
    public String name;
    /**
     * 无：0
     * 骚扰电话:1
     * 广告推销:2
     * 诈骗电话:3
     */
    public Bitmap icon;
    // 黑名单数量
    public int blackNum = -1;
    // 标记数量
    public int markNum;
    /**
     * 标记类型
     */
    public int markType = -1;
    /**
     * 0:未上传
     * 1：已上传
     */
    public int uploadState = -1;
    public int removeState = -1;

    public int filtUpState = -1;
    /**
     * 添加黑名单的时间
     */
    public long addBlkTime = -1;

}
