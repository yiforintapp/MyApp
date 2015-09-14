package com.leo.appmaster.msgcenter;


/**
 * 消息中心数据结构
 * Created by Jasper on 2015/9/10.
 */
public class Message {
    // 更新日志
    public static final String CATEGORY_UPDATE = "001";

    // 活动时间
    public String time;
    // 类型名称
    public String categoryName;
    // 类型表示
    public String categoryCode;
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
    // 资源包地址
    public String resUrl;
    // 活动id
    public int msgId;

    // 未读标志
    public boolean unread = true;
    // 本地资源路径
    public String resPath;

    public boolean isCategoryUpdate() {
        return CATEGORY_UPDATE.equals(categoryCode);
    }

    @Override
    public String toString() {
        return "msgId: " + msgId + " | categoryCode: " + categoryCode + " | title: " + title +
                " | categoryName: " + categoryName;
    }
}
