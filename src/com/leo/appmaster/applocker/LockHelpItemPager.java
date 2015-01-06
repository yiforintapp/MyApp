package com.leo.appmaster.applocker;

import android.text.SpannableString;

public class LockHelpItemPager {
    private String title;
    private SpannableString content;
    private String button;
    public LockHelpItemPager(String title, SpannableString content,String button) {
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
    public SpannableString getContent() {
        return content;
    }
    public void setContent(SpannableString content) {
        this.content = content;
    }
    public String getButton() {
        return button;
    }
    public void setButton(String button) {
        this.button = button;
    }
}
