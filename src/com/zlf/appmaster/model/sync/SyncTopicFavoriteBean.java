package com.zlf.appmaster.model.sync;

import android.content.Context;
import android.database.Cursor;

import com.zlf.appmaster.db.stock.TopicFavoriteTable;
import com.zlf.appmaster.utils.QLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SyncTopicFavoriteBean extends SyncBaseBean {

    private final static String TAG = SyncTopicFavoriteBean.class.getSimpleName();

	public SyncTopicFavoriteBean(Context context) {
		super(context, SYNC_KEY_TOPIC_FAVORITE);
		// TODO Auto-generated constructor stub
	}

    @Override
    protected boolean resolveJSONData(JSONObject syncData) {
        QLog.i(TAG, "syncData:" + syncData);
        TopicFavoriteTable table = new TopicFavoriteTable(mContext);
        table.syncData(syncData.optJSONArray(String.valueOf(SyncBaseBean.SYNC_KEY_TOPIC_FAVORITE)));
        return super.resolveJSONData(syncData);
    }


    @Override
    protected void accumulateOperator(JSONObject operatorJSONObject, HashMap<Integer, Object> operatorIDs) {

        final TopicFavoriteTable stockFavoriteTable = new TopicFavoriteTable(mContext);

        // 添加自选操作
        if (operatorIDs.containsKey(SyncOperator.ID_TOPIC_FAVORITE_ADD)){
            Cursor c = stockFavoriteTable.getCommitAddCursor();
            if (null != c) {
                try {

                    JSONArray jsonNewArray = new JSONArray();
                    while (c.moveToNext()) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("IId", c.getString(0));
                        jsonObject.put("Sort", c.getInt(1));
                        jsonObject.put("Type", c.getInt(2));
                        jsonNewArray.put(jsonObject);
                    }
                    operatorJSONObject.accumulate(String.valueOf(SyncOperator.ID_TOPIC_FAVORITE_ADD), jsonNewArray);
                } catch (JSONException e) {

                }
                finally {
                    c.close();
                }
            }

        }


        // 删除自选操作
        if (operatorIDs.containsKey(SyncOperator.ID_TOPIC_FAVORITE_DEL)){
            try {
                Cursor c = stockFavoriteTable.getCommitDeleteCursor();

                if (null != c){
                    JSONArray jsonDeleteArray = new JSONArray();
                    while(c.moveToNext()){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("IId", c.getString(0));
                        jsonObject.put("Type", c.getString(1));
                        jsonDeleteArray.put(jsonObject);
                    }
                    c.close();
                    operatorJSONObject.accumulate(String.valueOf(SyncOperator.ID_TOPIC_FAVORITE_DEL), jsonDeleteArray);
                }
            }
            catch (JSONException e){

            }
        }

    }


/*    @Override
    protected long getLocalVersion() {
        return 0;
    }*/
}
