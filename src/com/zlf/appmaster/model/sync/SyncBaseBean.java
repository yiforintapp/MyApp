package com.zlf.appmaster.model.sync;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.zlf.appmaster.db.stock.SyncVersionTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 同步信息请求  -- 基本类
 * @author Deping Huang
 *
 */
public abstract class SyncBaseBean {
	private final static String TAG = "SyncBase";

	/**
	 * 同步KEY对应的业务
	 */
    public static final int SYNC_KEY_DEFAULT =     -1;

    public static final int SYNC_KEY_USER_PROFILE           = 0x01;         // 用户属性
    //public static final int SYNC_KEY_STOCK_FAVORITE         = 0x02;		// 自选股////////////////////////////
    public static final int SYNC_KEY_BOOKMARKS              = 0x04;         // 收藏//////////////////////////////////

    public static final int SYNC_KEY_MSG                    = 0x10; 		// 消息
    public static final int SYNC_KEY_STOCK_BASE_DATA        = 0x20; 		// 股票基本数据
    public static final int SYNC_KEY_INDUSTRY               = 0x80;         // 行业
    public static final int SYNC_KEY_INDEX                  = 0x100;        // 指数
    public static final int SYNC_KEY_TOPIC                  = 0x200;        // 题材的基本信息
    public static final int SYNC_KEY_TOPIC_FAVORITE         = 0x400;        // 自选题材///////////////////////
    public static final int SYNC_KEY_CONTACTS               = 0x800;        // 联系人
    public static final int SYNC_KEY_FAVORITES              = 0x1000;       // 收藏(自选、主题、新闻收藏)
    public static final int SYNC_KEY_FANS                   = 0x2000;       // 粉丝
    public static final int SYNC_KEY_MY_PROGRAMME           = 0x4000;       // 用户自己的节目（主播 only）
    public static final int SYNC_KEY_SUBSCRIBE_PROGRAMME    = 0x8000;       // 用户订阅的节目



    public static final int ALL_BASE_COMMANDS = SYNC_KEY_STOCK_BASE_DATA|SYNC_KEY_INDUSTRY|SYNC_KEY_INDEX|SYNC_KEY_TOPIC;

    public static final int ALL_PERSONAL_COMMANDS = SYNC_KEY_USER_PROFILE|SYNC_KEY_BOOKMARKS|SYNC_KEY_MSG
            |SYNC_KEY_TOPIC_FAVORITE|SYNC_KEY_CONTACTS|SYNC_KEY_FAVORITES|SYNC_KEY_FANS| SYNC_KEY_MY_PROGRAMME |SYNC_KEY_SUBSCRIBE_PROGRAMME;


    protected int mCommand;		        // 命令字
    protected long mLVersion;		    // 本地版本号
    protected long mNVersion;		    // 服务器返回的版本号
    protected JSONObject mJSONValue = null;	// 本类对应的json

    protected Context mContext;



    public SyncBaseBean(Context context, int command){
        mContext = context;
        mCommand = command;
    }

    /**
     * 读取和保存同步版本号
     */
    protected long getLocalVersion(){
        SQLiteOpenHelper sqLiteOpenHelper = SyncFactory.getVersionDBHelper(mContext, mCommand);
        if (null != sqLiteOpenHelper){
            SyncVersionTable syncVersionTable = new SyncVersionTable(mContext, sqLiteOpenHelper);
            long version = syncVersionTable.getVersionByID(mCommand);
            syncVersionTable.close();
            return version;
        }
        return 0;
    }
    protected void setLocalVersion(long ncp){
        SQLiteOpenHelper sqLiteOpenHelper = SyncFactory.getVersionDBHelper(mContext, mCommand);
        if (null != sqLiteOpenHelper){
            SyncVersionTable syncVersionTable = new SyncVersionTable(mContext, sqLiteOpenHelper);
            syncVersionTable.setVersion(mCommand, mNVersion);
            syncVersionTable.close();
        }
    }


    /**
     * 以operatorJSONObject.accumulate(id, object)方式组装相应操作ID的JSON请求信息
     *
     * @param operatorJSONObject
     * @param operatorIDs
     */
    protected void accumulateOperator(JSONObject operatorJSONObject, HashMap<Integer, Object> operatorIDs){


    }


    // 应答服务器的同步信息
    protected boolean resolveJSONData(JSONObject syncData){

        return true;
    }

    /**
     * 无数据更新的回调通知
     */
    protected void callBackNotDataUpdate(){

    }


    /**
     * 获取本类JSON的名称
     */
    public String getJSONKey(){
        return String.valueOf(mCommand);
    }

	/**
	 * 获取/设置本类的对应的JSON
	 */
    JSONObject getJSONValue(){
		if(null == mJSONValue){
            mJSONValue = new JSONObject();

            // 头部
            try {
                mJSONValue.accumulate("target", mCommand);
                mJSONValue.accumulate("lcp", (mLVersion = getLocalVersion()));

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

			// assembleJSONObject(mJSONValue);
		}
		return mJSONValue;
	}

    void setJSONValue(JSONObject jsonData, Map<Integer, Long> targetArray) throws JSONException {

        // 服务器返回的版本号
        Long ncp = targetArray.get(mCommand);

        if (null != ncp){
            mNVersion = ncp;
            mLVersion = getLocalVersion();
            // 同步数据
            if (mNVersion > mLVersion) {

                JSONObject syncData = jsonData.optJSONObject("change");
                if (syncData != null) {
                    if (resolveJSONData(syncData)) {
                        // 解析成功后再刷新本地版本号
                        setLocalVersion(mNVersion);
                    }
                }

            } else {
                callBackNotDataUpdate();
            }
        }


    }



	

}
