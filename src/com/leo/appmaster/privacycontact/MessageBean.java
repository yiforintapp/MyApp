
package com.leo.appmaster.privacycontact;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.leo.appmaster.Constants;
import com.leo.appmaster.model.BaseInfo;

public class MessageBean extends BaseInfo implements Serializable {
    private long msmId;
    private String messageName;
    private String phoneNumber;
    private String messageTime;
    private String messageProtocol;
    private int messageIsRead;
    private int messageStatus;
    private int messageType;
    private String messageBody;
    private String messageThreadId;
    private String messageServiceCenter;
    private boolean isCheck = false;
    private int count;
    private String showDate;
    private Bitmap contactIcon;
    private int contactId;
    private String flag;
    private boolean isShowReadTip;
    private int messageCount;

    public long getMsmId() {
        return msmId;
    }

    public void setMsmId(long msmId) {
        this.msmId = msmId;
    }

    public MessageBean(String messageName, String phoneNumber, String messageTime,
            String messageProtocol, int messageIsRead, int messageStatus, int messageType,
            String messageBody, String messageThreadId, String messageServiceCenter,
            boolean isCheck, int count, String showDate, Bitmap contactIcon, int contactId,int messageCount) {
        super();
        this.messageName = messageName;
        this.phoneNumber = phoneNumber;
        this.messageTime = messageTime;
        this.messageProtocol = messageProtocol;
        this.messageIsRead = messageIsRead;
        this.messageStatus = messageStatus;
        this.messageType = messageType;
        this.messageBody = messageBody;
        this.messageThreadId = messageThreadId;
        this.messageServiceCenter = messageServiceCenter;
        this.count = count;
        this.showDate = showDate;

        this.contactIcon = contactIcon;
        this.contactId = contactId;
        this.messageCount=messageCount;
    }


    public Bitmap getContactIcon() {
        return contactIcon;
    }

    public void setContactIcon(Bitmap contactIcon) {
        this.contactIcon = contactIcon;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public MessageBean() {
        super();
    }

    public String getMessageThreadId() {
        return messageThreadId;
    }

    public void setMessageThreadId(String messageThreadId) {
        this.messageThreadId = messageThreadId;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageProtocol() {
        return messageProtocol;
    }

    public void setMessageProtocol(String messageProtocol) {
        this.messageProtocol = messageProtocol;
    }

    public int isMessageIsRead() {
        return messageIsRead;
    }

    public void setMessageIsRead(int messageIsRead) {
        this.messageIsRead = messageIsRead;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getMessageServiceCenter() {
        return messageServiceCenter;
    }

    public void setMessageServiceCenter(String messageServiceCenter) {
        this.messageServiceCenter = messageServiceCenter;
    }

}
