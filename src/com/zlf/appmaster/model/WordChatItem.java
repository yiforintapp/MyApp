package com.zlf.appmaster.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/8.
 */
public class WordChatItem implements Serializable {

    private String mCName;
    private String mTName;
    private String mMsg;
    private String mAnswer;
    private String mAskTime;
    private String mAnswerTime;
    private String mAnswerImg;
    private String mAnswerHeadImg;

    public String getCName() {
        return mCName;
    }

    public void setCName(String mCName) {
        this.mCName = mCName;
    }

    public String getTName() {
        return mTName;
    }

    public void setTName(String mTName) {
        this.mTName = mTName;
    }

    public String getMsg() {
        return mMsg;
    }

    public void setMsg(String mMsg) {
        this.mMsg = mMsg;
    }

    public String getAskTime() {
        return mAskTime;
    }

    public void setAskTime(String mAskTime) {
        this.mAskTime = mAskTime;
    }

    public String getAnswer() {
        return mAnswer;
    }

    public void setAnswer(String mAnswer) {
        this.mAnswer = mAnswer;
    }

    public String getAnswerTime() {
        return mAnswerTime;
    }

    public void setAnswerTime(String mAnswerTime) {
        this.mAnswerTime = mAnswerTime;
    }

    public String getAnswerImg() {
        return mAnswerImg;
    }

    public void setAnswerImg(String mAnswerImg) {
        this.mAnswerImg = mAnswerImg;
    }

    public String getAnswerHeadImg() {
        return mAnswerHeadImg;
    }

    public void setAnswerHeadImg(String mAnswerHeadImg) {
        this.mAnswerHeadImg = mAnswerHeadImg;
    }
}
