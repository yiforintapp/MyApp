package com.zlf.appmaster.model.topic;

/**
 * Created by Huang on 2015/6/29.
 */
public class TopicFavoriteItem implements Comparable<TopicFavoriteItem> {
    private String mTopicID;
    private TopicInfo mTopicInfo;
    private int mSortCode;

    public TopicInfo getTopicInfo() {
        return mTopicInfo;
    }

    public void setTopicInfo(TopicInfo topicInfo) {
        this.mTopicInfo = topicInfo;
    }

    public int getSortCode() {
        return mSortCode;
    }

    public void setSortCode(int sortCode) {
        mSortCode = sortCode;
    }

    public String getTopicID() {
        return mTopicID;
    }

    public void setTopicID(String topicID) {
        this.mTopicID = topicID;
    }

    @Override
    public int compareTo(TopicFavoriteItem item) {
        return mSortCode - item.mSortCode;
    }


}
