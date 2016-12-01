package com.zlf.appmaster.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/1.
 */
public class WordNewAdviceInfo implements Serializable {
    private String mName;
    private String mDesc;
    private String mIcon;

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String mDesc) {
        this.mDesc = mDesc;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String mIcon) {
        this.mIcon = mIcon;
    }
}
