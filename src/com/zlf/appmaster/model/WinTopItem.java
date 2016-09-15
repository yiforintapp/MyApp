package com.zlf.appmaster.model;


import java.io.Serializable;

public class WinTopItem implements Serializable {

    private String winName;
    private long winPrice;

    public long getWinPrice() {
        return winPrice;
    }

    public String getWinName() {
        return winName;
    }

    public void setWinName(String winName) {
        this.winName = winName;
    }

    public void setWinPrice(long winPrice) {
        this.winPrice = winPrice;
    }

}
