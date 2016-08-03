package com.zlf.appmaster.model.stock;


import org.json.JSONObject;

public class MessageItem extends Message implements Comparable<MessageItem> {

    /**
     * 聊天类型定义
     *
     */
    public static class MsgType {
        public static final int UNKNOWN = 0;        // 未知
        public static final int P2P = 1;            // 点对点聊天
        public static final int GROUP = 2;          // 群组
        public static final int DISCUSS = 3;        // 讨论组
        public static final int LIVE = 4;           // 直播室

        /**
         * 最大类型数据 (预留20种，应该足够)
         * @return
         */
        public static int maxTypeSize(){
            return 20;
        }
    }

    public static class Direct {
        public static final int SEND = -1;
        public static final int RECEIVE = 1;
    }

    public static class Status {
        public static final int SUCCESS = 0;            // 发送成功
        public static final int FAIL = 1;               // 发送失败
        public static final int IN_PROGRESS = 2;        // 发送中
        public static final int CREATE = 3;             // 创建（例如发送文件等）
    }

    /**
     * 聊天的内容数据类型定义
     */
    public static class DataType {
        public static final int TXT = 1;                    // 正常的文本
        public static final int RE_POS = 2;                 // 组合调仓
        public static final int FOLLOW_PORTFOLIO = 3;       // 跟随组合
        public static final int IMAGE = 4;
        public static final int VIDEO = 5;
        public static final int LOCATION = 6;
        public static final int VOICE = 7;
        public static final int FILE = 8;
        public static final int CMD = 9;                    // 透传消息
        public static final int LIVE_PROMPT = 999;           // 直播间提示消息
        public static final int SYSTEM = 1000;              // 系统消息
        public static final int SYSTEM_PROMPT = 1001;       // 系统提示消息
    }


    public MessageItem() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 消息根据创建时间来排序
     * @return
     */
    @Override
	public int compareTo(MessageItem another) {
		// TODO Auto-generated method stub
		return (int) (this.getCreateTime() - another.getCreateTime());
	}


    public static MessageItem resolveJSONObject(JSONObject jsonData){
        MessageItem item = new MessageItem();
        item.setMsgID(jsonData.optLong("llid"));
        item.setSenderUin(jsonData.optLong("fuin"));
        item.setSenderName(jsonData.optString("fname"));
        item.setReceiverUin(jsonData.optLong("tuin"));

        item.setMsgType(jsonData.optInt("msg_type"));
        item.setMsgTypeID(jsonData.optLong("msg_type_id"));
        item.setMsgTypeName(jsonData.optString("msg_type_name"));
        item.setMsgTypeSeq(jsonData.optLong("msg_type_seq"));
        item.setDirect(jsonData.optInt("direct", MessageItem.Direct.RECEIVE)); // 目前同步都是收

        item.setData(jsonData.optString("content"));
        item.setDataType(jsonData.optInt("content_type"));
        item.setVoiceStatus(jsonData.optInt("voice_status"));
        item.setImgStatus(jsonData.optInt("img_status"));
        item.setPushContent(jsonData.optString("push_content"));
        item.setPushTitle(jsonData.optString("source"));
        item.setVersion(jsonData.optInt("version"));
        item.setDevice(jsonData.optString("device_type"));
        item.setCreateTime(jsonData.optLong("ctime"));
        item.setUpdateTime(jsonData.optLong("utime"));
        item.setClientID(jsonData.optLong("client_id"));

        return item;
    }



}
