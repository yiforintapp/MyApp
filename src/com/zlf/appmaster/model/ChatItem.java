package com.zlf.appmaster.model;


import java.io.Serializable;

public class ChatItem implements Serializable {

    private String date;
    private String text;
    private String name;


    public void setDate(String title) {
        this.date = title;
    }

    public String getDate() {
        return date;
    }

    public void setText(String desc) {
        this.text = desc;
    }

    public String getText() {
        return text;
    }

    public void setName(String time) {
        this.name = time;
    }

    public String getName() {
        return name;
    }

}
