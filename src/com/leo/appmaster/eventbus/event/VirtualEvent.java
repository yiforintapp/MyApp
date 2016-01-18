package com.leo.appmaster.eventbus.event;

/**
 * Created by forint on 16-1-18.
 */
public class VirtualEvent extends BaseEvent {
    public boolean mIsVirtual;

    private VirtualEvent() {

    }

    public VirtualEvent(boolean isVirtual) {
        this.mIsVirtual = isVirtual;
    }

}
