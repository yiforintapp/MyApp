package com.leo.appmaster.msgcenter;

import java.io.Serializable;

/**
 * 消息中心数据结构
 * Created by Jasper on 2015/9/10.
 */
public class Message implements Serializable {

    // 活动时间
    public String time;
    // 类型名称
    public String name;
    // 描述信息
    public String description;
    // 图片地址
    public String imageUrl;
    // 页面跳转地址
    public String jumpUrl;
    // 活动下线时间
    public String offlineTime;
    // 活动标题
    public String title;

    // 活动类型
    public String typeId;

    // 活动id
    public int id;

    // 未读标志
    public boolean unread;
}
