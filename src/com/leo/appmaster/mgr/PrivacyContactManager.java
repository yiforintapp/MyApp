package com.leo.appmaster.mgr;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.privacycontact.MessageCallLogBean;

import java.net.URI;
import java.util.List;

/**
 * 隐私联系人
 * Created by Jasper on 2015/9/28.
 */
public abstract class PrivacyContactManager extends Manager {
    @Override
    public String description() {
        return MgrContext.MGR_PRIVACY_CONTACT;
    }

    /**
     * 获取隐私联系人列表
     * 注：需加异步使用
     *
     * @return
     */
    public abstract Cursor getPrivacyContactList();

    /**
     * 获取隐私联系人数量
     *
     * @return
     */
    public abstract int getPrivacyContactCount();

    /**
     * 获取隐私通话列表
     *
     * @return
     */
    public abstract List<ContactCallLog> getPrivacyCallList();

    /**
     * 获取隐私通话数量
     *
     * @return
     */
    public abstract int getPrivacyCallCount();

    /**
     * 获取隐私短信列表
     *
     * @return
     */
    public abstract List<MessageBean> getPrivacyMessage();

    /**
     * 获取隐私短信数量
     *
     * @return
     */
    public abstract int getPrivacyMessageCount();

    /**
     * 删除隐私联系人
     *
     * @param phoneNumber
     * @return
     */
    public abstract int deletePrivacyContact(String phoneNumber);

    /**
     * 删除隐私通话记录
     *
     * @param phoneNumber
     * @return
     */
    public abstract boolean deletePrivacyCall(String phoneNumber);

    /**
     * 删除隐私短信
     *
     * @param phoneNumber
     * @return
     */
    public abstract boolean deletePrivacyMessage(String phoneNumber);

    /**
     * 清空隐私联系人
     *
     * @return
     */
    public abstract boolean clearPrivacyContacts();

    /**
     * 清空隐私通话
     *
     * @return
     */
    public abstract boolean clearPrivacyCalls();

    /**
     * 清空隐私短信
     *
     * @return
     */
    public abstract boolean clearPrivacyMessages();

    /**
     * 添加隐私联系人
     * 注：需加异步使用,必须有电话号码
     *
     * @param contact
     * @return
     */
    public abstract MessageCallLogBean addPrivacyContact(List<ContactBean> contact);

    /**
     * 隐私通话的未读数量
     *
     *
     * @return
     */
    public abstract int getPrivacyCallNoReadCount();

    /**
     * 隐私短信的未读数量
     *
     * @return
     */
    public abstract int getPrivacyMessageNoReadCount();

    /**
     * 获取系统联系人列表
     * 注：需加异步使用
     *
     * @param selection
     * @param selectionArgs
     * @return
     */
    public abstract Cursor getSystemContacts(String selection, String[] selectionArgs);

    /**
     * 获取系统通话列表
     * 注：耗时操作需加异步使用
     * @param selection
     * @param selectionArgs
     * @return
     */
    public abstract Cursor getSystemCalls(String selection,String[] selectionArgs,String sortOrder);

    /**
     * 获取系统短信列表
     * @param selection
     * @param selectionArgs
     * @return
     */
    public abstract Cursor getSystemMessages(String selection,String[] selectionArgs);

    /**
     * 获取频繁联系人列表
     *数据库耗时操作，需要异步
     * @return
     */
    public abstract List<ContactBean> getFrequentContacts();

    /**
     * 获取新增联系人
     * @return
     */
    public abstract Cursor getNewIncreaseContacts();

    /**
     * 发送短信
     * @param number
     * @param content
     * @param fromId,该短信来自哪里可以不用设置默认值：-1
     * @return
     */
    public abstract boolean sendMessage(String number,String content,int fromId);
    /*导入通话记录和短信记录*/
    public abstract boolean importCallAndMsms(MessageCallLogBean messageCall);
}
