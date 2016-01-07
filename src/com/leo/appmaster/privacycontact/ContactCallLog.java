
package com.leo.appmaster.privacycontact;

import java.io.Serializable;

import com.leo.appmaster.model.BaseInfo;

import android.graphics.Bitmap;

public class ContactCallLog extends BaseInfo implements Serializable {
    private String callLogName;
    private String callLogNumber;
    private int clallLogType;
    private String clallLogDate;
    private int classLogId;
    private int callLogId;
    /*每个号码通话记录的条数*/
    private int callLogCount;
    private long callLogDuraction;
    private boolean isCheck = false;
    private int isRead;
    private String showDate;
    private Bitmap contactIcon;
    public String phoneNumber;
    public boolean isShowReadTip;
    private int addBlackNumber = -1;

    public ContactCallLog(String callLogName, String callLogNumber, int clallLogType,
                          String clallLogDate, int classLogId, int callLogCount, long callLogDuraction,
                          boolean isCheck, int isRead, String showDate, Bitmap contactIcon) {
        super();
        this.callLogName = callLogName;
        this.callLogNumber = callLogNumber;
        this.clallLogType = clallLogType;
        this.clallLogDate = clallLogDate;
        this.classLogId = classLogId;
        this.callLogCount = callLogCount;
        this.callLogDuraction = callLogDuraction;
        this.isCheck = isCheck;
        this.isRead = isRead;
        this.contactIcon = contactIcon;
    }

    public int getAddBlackNumber() {
        return addBlackNumber;
    }

    public void setAddBlackNumber(int addBlackNumber) {
        this.addBlackNumber = addBlackNumber;
    }

    public int getCallLogId() {
        return callLogId;
    }

    public void setCallLogId(int callLogId) {
        this.callLogId = callLogId;
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

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public ContactCallLog() {
        super();
    }

    public int getCallLogCount() {
        return callLogCount;
    }

    public long getCallLogDuraction() {
        return callLogDuraction;
    }

    public void setCallLogDuraction(long callLogDuraction) {
        this.callLogDuraction = callLogDuraction;
    }

    public void setCallLogCount(int callLogCount) {
        this.callLogCount = callLogCount;
    }

    public String getCallLogName() {
        return callLogName;
    }

    public void setCallLogName(String callLogName) {
        this.callLogName = callLogName;
    }

    public String getCallLogNumber() {
        return callLogNumber;
    }

    public void setCallLogNumber(String callLogNumber) {
        this.callLogNumber = callLogNumber;
    }

    public int getClallLogType() {
        return clallLogType;
    }

    public void setClallLogType(int clallLogType) {
        this.clallLogType = clallLogType;
    }

    public String getClallLogDate() {
        return clallLogDate;
    }

    public void setClallLogDate(String clallLogDate) {
        this.clallLogDate = clallLogDate;
    }

    public int getClassLogId() {
        return classLogId;
    }

    public void setClassLogId(int classLogId) {
        this.classLogId = classLogId;
    }

}
