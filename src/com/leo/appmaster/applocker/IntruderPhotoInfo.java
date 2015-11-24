package com.leo.appmaster.applocker;


public class IntruderPhotoInfo {
    private String filePath;
    private String fromAppPackage;
    private String timeStamp;

    public IntruderPhotoInfo(String filePath, String fromAppPackage,String timeStamp) {
        this.filePath = filePath;
        this.timeStamp = timeStamp;
        this.fromAppPackage = fromAppPackage;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFromAppPackage() {
        return fromAppPackage;
    }

    public void setFromAppPackage(String fromAppPackage) {
        this.fromAppPackage = fromAppPackage;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
