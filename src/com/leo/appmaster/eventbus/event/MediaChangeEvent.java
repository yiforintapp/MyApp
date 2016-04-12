package com.leo.appmaster.eventbus.event;

/**
 * Created by Jasper on 2016/4/11.
 */
public class MediaChangeEvent extends BaseEvent {

    public boolean isImage;

    public MediaChangeEvent(boolean isImage) {
        this.isImage = isImage;
    }
}
