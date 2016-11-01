package com.zlf.appmaster.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/1.
 */
public class AdviceItem implements Serializable {

    private String mId;
    private String mTime;
    private String mType;
    private String mPosition;
    private String mKind;
    private String mOpen;
    private String mStop;
    private String mOnly;
    private String mName;

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getPosition() {
        return mPosition;
    }

    public void setPosition(String mPosition) {
        this.mPosition = mPosition;
    }

    public String getKind() {
        return mKind;
    }

    public void setKind(String mKind) {
        this.mKind = mKind;
    }

    public String getOpen() {
        return mOpen;
    }

    public void setOpen(String mOpen) {
        this.mOpen = mOpen;
    }

    public String getStop() {
        return mStop;
    }

    public void setStop(String mStop) {
        this.mStop = mStop;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getOnly() {
        return mOnly;
    }

    public void setOnly(String mOnly) {
        this.mOnly = mOnly;
    }
}
