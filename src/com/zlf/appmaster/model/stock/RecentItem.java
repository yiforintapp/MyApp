package com.zlf.appmaster.model.stock;

import android.content.Context;

/**
 * 最近联系列表里所需要的元素
 * @author Deping Huang
 *
 */
public class RecentItem implements Comparable<RecentItem> {
	private static final String TAG = RecentItem.class.getSimpleName();

	//-- 最近消息的类型 --//
	public static final int TYPE_FRIENDS_ZONE = 2;				// 股友圈
	public static final int TYPE_MSG = 7;                  		// 信息
	public static final int TYPE_NEWS = 11;       				// 资讯

	
	private int mRectType;			// 消息类型（参见本类中的type定义）
    private String mSubType;       	// 消息子类型，参考各子类型的定义，可能对应着字符串（如资讯的子类型定义），（可无）
	private String mSubTypeID;		// 消息子类型的唯一标识，如群组信息对应的群组ID，个人信息对应的个人ID(可无，兼容预留使用String)
	
	private String mTitle;			// 消息来自
	private String mSummary;		// 消息摘要
	private int mNewNum;			// 新消息数目
	private long mUpdateTime;		// 消息日期

	public RecentItem() {
		mSubTypeID = "0";
        mSubType = "0";
	}

	
	public int getRectType() {
		return mRectType;
	}

	public void setRectType(int mType) {
		this.mRectType = mType;
	}


	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getSummary() {
		return mSummary;
	}

	public void setSummary(String message) {
		this.mSummary = message;
	}

	public int getNewNum() {
		return mNewNum;
	}

	public void setNewNum(int newNum) {
		mNewNum = newNum;
	}

	public long getUpdateTime() {
		return mUpdateTime;
	}

	public void setUpdateTime(long updateTime) {
		mUpdateTime = updateTime;
	}

    public String getSubType() {
        return mSubType;
    }

    public void setSubType(String subType) {
        mSubType = subType;
    }

	public String getSubTypeID() {
		return mSubTypeID;
	}

	public void setSubTypeID(String subTypeID) {
		mSubTypeID = subTypeID;
	}


	@Override
	public int compareTo(RecentItem another) {
		// TODO Auto-generated method stub
		return (int) (another.mUpdateTime - this.mUpdateTime);
	}

	
	/**
	 * 转换某些数据类型在最近显示列表中显示文字提示
	 * @param context
	 * @param orgContent	原始内容
	 * @param msgType		消息类型
	 * @return
	 */
	public static String convertDisplayInfoByType(Context context, String orgContent, int msgType){
		String ret = "";
		/*switch(msgType){
		case MessageItem.MSG_DATA_TYPE_SHARE_DEFEAT:
		case MessageItem.MSG_DATA_TYPE_SHARE_NEWS:
			try {
				ShareItem shareItem = ShareItem.resolveJSONObject(orgContent,msgType);
				ret = shareItem.getComment() + context.getResources().getString(R.string.recent_share_display_info);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		case MessageItem.MSG_DATA_TYPE_BIG_EMOJI:
			QEmoji emoji = new QEmoji(orgContent);
			ret = emoji.getChatRecentDisplay();
			break;
		case MessageItem.MSG_DATA_TYPE_STOCK:
			StockTradeInfoShare stockTradeInfoShare = StockTradeInfoShare.resolveSource(orgContent);
			ret = stockTradeInfoShare.getChatDisplayFormatStr();
			break;
		default:
			ret = orgContent;
			break;
		}
*/
		return ret;
	}


}
