package com.zlf.appmaster.model;


import java.io.Serializable;

public class WinTopItem implements Serializable {

    private String winName;
    private double winPrice;

    public double getWinPrice() {
        return winPrice;
    }

    public String getWinName() {
        return winName;
    }

    public void setWinName(String winName) {
        this.winName = winName;
    }

    public void setWinPrice(double winPrice) {
        this.winPrice = winPrice;
    }

}
