package com.leo.appmaster.privacycontact;

import java.util.List;

/**
 * Created by runlee on 15-9-29.
 *
 * 隐私联系人数据服务
 */
public class PrivacyContactInterfaceManager {

    /**
     * 获取隐私联系人列表
     * @return
     */
    public static List<ContactBean> getPrivacyContactList(){
        return null;
    };

    /**
     * 获取隐私联系人数量
     * @return
     */
    public static int getPrivacyContactCount(){
        return 0;
    };

    /**
     * 获取隐私通话列表
     * @return
     */
    public static List<ContactCallLog> getPrivacyCallList(){
        return null;
    };

    /**
     * 获取隐私通话数量
     * @return
     */
    public static int getPrivacyCallCount(){
        return 0;
    };

    /**
     * 获取隐私短信列表
     * @return
     */
    public static List<MessageBean> getPrivacyMessage(){
        return null;
    };

    /**
     * 获取隐私短信数量
     * @return
     */
    public static int getPrivacyMessageCount(){
        return 0;
    };

    /**
     * 删除隐私联系人
     * @param phoneNumber
     * @return
     */
    public static boolean deletePrivacyContact(String phoneNumber){
        return false;
    };

    /**
     * 删除隐私通话记录
     * @param phoneNumber
     * @return
     */
    public static boolean deletePrivacyCall(String phoneNumber){
        return false;
    };

    /**
     * 删除隐私短信
     * @param phoneNumber
     * @return
     */
    public static boolean deletePrivacyMessage(String phoneNumber){
        return false;
    };

    /**
     * 清空隐私联系人
     * @return
     */
    public static boolean clearPrivacyContacts(){
        return false;
    };

    /**
     * 清空隐私通话
     * @return
     */
    public static boolean clearPrivacyCalls(){
        return false;
    };

    /**
     * 清空隐私短信
     * @return
     */
    public static boolean clearPrivacyMessages(){
        return false;
    };

    /**
     * 添加隐私联系人
     * @param phoneNumber
     * @return
     */
    public static boolean addPrivacyContact(String phoneNumber){
        return false;
    };

    /**
     * 隐私通话的未读数量
     * @return
     */
    public static int getPrivacyCallNoReadCount(){
        return 0;
    };

    /**
     * 隐私短信的未读数量
     * @return
     */
    public static int getPrivacyMessageNoReadCount(){
        return 0;
    };

    /**
     * 获取系统联系人列表
     * @return
     */
    public static List<ContactBean> getSystemContacts(){
        return null;
    };

    /**
     * 获取系统通话列表
     * @return
     */
    public static List<ContactCallLog> getSystemCalls(){
        return null;
    };

    /**
     * 获取系统短信列表
     * @return
     */
    public static List<MessageBean> getSystemMessages(){
        return null;
    };
}
