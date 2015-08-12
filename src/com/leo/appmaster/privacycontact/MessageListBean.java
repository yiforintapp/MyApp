package com.leo.appmaster.privacycontact;

import java.io.Serializable;
import java.util.List;


public class MessageListBean extends MessageBean implements Serializable{
    private static final long serialVersionUID = 1L;
    private List<MessageBean> messages;
    public MessageListBean() {
        super();
    }

    public List<MessageBean> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageBean> messages) {
        this.messages = messages;
    }

    
}
