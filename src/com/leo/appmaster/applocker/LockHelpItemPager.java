package com.leo.appmaster.applocker;


public class LockHelpItemPager {
    private String title;
    private String content;
    private String button;
    public LockHelpItemPager(String title, String content,String button) {
        super();
        this.title = title;
        this.content = content;
        this.button=button;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getButton() {
        return button;
    }
    public void setButton(String button) {
        this.button = button;
    }
}
