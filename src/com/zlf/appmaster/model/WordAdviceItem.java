package com.zlf.appmaster.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/4.
 */
public class WordAdviceItem implements Serializable {

    private String mContent;
    private String mDate;

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
    }
}
