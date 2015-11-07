package com.leo.appmaster.privacycontact;

import java.util.List;

/**
 * Created by runlee on 15-10-27.
 */
public class MessageCallLogBean {
    public List<ContactCallLog> calls;
    public List<MessageBean> msms;

    public MessageCallLogBean(List<ContactCallLog> calls, List<MessageBean> msms) {
        this.calls = calls;
        this.msms = msms;
    }
}
