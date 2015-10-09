
package com.leo.appmaster.privacycontact;

import java.io.Serializable;

import android.graphics.Bitmap;

public class ContactBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private int contactId;
    private String contactName;
    private String contactNumber;
    private ContactCallLog callLog;
    private Bitmap contactIcon;
    private String sortLetter;
    private int answerType;
    private String answerStatus;
    private boolean isCheck = false;

    public ContactBean(int contactId, String contactName, String contactNumber,
            ContactCallLog callLog, Bitmap contactIcon, String sortLetter, boolean isCheck,
            int answerType, String answerStatus) {
        super();
        this.contactId = contactId;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.callLog = callLog;
        this.contactIcon = contactIcon;
        this.sortLetter = sortLetter;
        this.isCheck = isCheck;
        this.answerType = answerType;
        this.answerStatus = answerStatus;
    }

    public String getSortLetter() {
        return sortLetter;
    }

    public void setSortLetter(String sortLetter) {
        this.sortLetter = sortLetter;
    }

    public String getAnswerStatus() {
        return answerStatus;
    }

    public void setAnswerStatus(String answerStatus) {
        this.answerStatus = answerStatus;
    }

    public int getAnswerType() {
        return answerType;
    }

    public void setAnswerType(int answerType) {
        this.answerType = answerType;
    }

    public ContactBean() {
        super();
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public ContactCallLog getCallLog() {
        return callLog;
    }

    public void setCallLog(ContactCallLog callLog) {
        this.callLog = callLog;
    }

    public Bitmap getContactIcon() {
        return contactIcon;
    }

    public void setContactIcon(Bitmap contactIcon) {
        this.contactIcon = contactIcon;
    }
}
