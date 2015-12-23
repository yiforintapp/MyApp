package com.leo.appmaster.callfilter;

import android.graphics.Bitmap;

/**
 * Created by runlee on 15-12-19.
 */
public class BlackListInfo {
    private int id;
    private String number;
    private String numberName;
    /**
     * 无：0
     * 骚扰电话:1
     * 广告推销:2
     * 诈骗电话:3
     */
    private int markerType;
    private Bitmap icon;
    private String numberArea;
    private int addBlackNumber;
    private int markerNumber;
    /**
     * 0:未上传
     * 1：已上传
     */
    private int uploadState = -1;
    private int locHandler = -1;
    private int isLocHandlerType = -1;
    private int readState = -1;
    private int removeState = -1;
    /**
     * 服务器下发黑名单是否存在语本地黑名单
     */
    private boolean existState;

    public int getAddBlackNumber() {
        return addBlackNumber;
    }

    public void setAddBlackNumber(int addBlackNumber) {
        this.addBlackNumber = addBlackNumber;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMarkerNumber() {
        return markerNumber;
    }

    public void setMarkerNumber(int markerNumber) {
        this.markerNumber = markerNumber;
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

    public String getNumberName() {
        return numberName;
    }

    public void setNumberName(String numberName) {
        this.numberName = numberName;
    }

    public int getIsLocHandlerType() {
        return isLocHandlerType;
    }

    public void setIsLocHandlerType(int isLocHandlerType) {
        this.isLocHandlerType = isLocHandlerType;
    }


    public int getMarkerType() {
        return markerType;
    }

    public void setMarkerType(int markerType) {
        this.markerType = markerType;
    }

    public boolean isExistState() {
        return existState;
    }

    public void setExistState(boolean existState) {
        this.existState = existState;
    }

    public int getLocHandler() {
        return locHandler;
    }

    public void setLocHandler(int locHandler) {
        this.locHandler = locHandler;
    }

    public int getReadState() {
        return readState;
    }

    public void setReadState(int readState) {
        this.readState = readState;
    }

    public int getRemoveState() {
        return removeState;
    }

    public void setRemoveState(int removeState) {
        this.removeState = removeState;
    }

    public int getUploadState() {
        return uploadState;
    }

    public void setUploadState(int uploadState) {
        this.uploadState = uploadState;
    }
}
