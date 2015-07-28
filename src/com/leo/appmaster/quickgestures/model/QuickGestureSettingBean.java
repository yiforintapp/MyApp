
package com.leo.appmaster.quickgestures.model;

public class QuickGestureSettingBean {
    private int icon;
    private String name;
    private String content;
    private int type;
    private boolean isCheck;
    private int checkIcon;
    private int backageDraw;

    public QuickGestureSettingBean() {
    }

    public QuickGestureSettingBean(int icon, String name, int type, String content,
            boolean isCheck, int checkIcon,int backageDraw) {
        super();
        this.icon = icon;
        this.name = name;
        this.type = type;
        this.content = content;
        this.isCheck = isCheck;
        this.checkIcon = checkIcon;
        this.backageDraw=backageDraw;
    }

    public int getBackageDraw() {
        return backageDraw;
    }

    public void setBackageDraw(int backageDraw) {
        this.backageDraw = backageDraw;
    }

    public int getCheckIcon() {
        return checkIcon;
    }

    public void setCheckIcon(int checkIcon) {
        this.checkIcon = checkIcon;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

}
