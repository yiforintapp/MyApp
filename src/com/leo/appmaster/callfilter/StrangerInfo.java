package com.leo.appmaster.callfilter;

/**
 * Created by runlee on 15-12-19.
 */
public class StrangerInfo {

    private int id;
    private String number;
    private long callDuration;
    private long date;
    /**
     * 每个陌生人的通话数量
     */
    private int starCount;
    private boolean tipState;
    /**
     * 号码归属地
     */
    private String numberArea;
    private boolean readState;
    private int callState;

    public long getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(long callDuration) {
        this.callDuration = callDuration;
    }

    public int getCallState() {
        return callState;
    }

    public void setCallState(int callState) {
        this.callState = callState;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumberArea() {
        return numberArea;
    }

    public void setNumberArea(String numberArea) {
        this.numberArea = numberArea;
    }

    public boolean isReadState() {
        return readState;
    }

    public void setReadState(boolean readState) {
        this.readState = readState;
    }

    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    public boolean isTipState() {
        return tipState;
    }

    public void setTipState(boolean tipState) {
        this.tipState = tipState;
    }
}
