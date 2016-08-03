package com.zlf.appmaster.db.stock;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.zlf.appmaster.model.industry.IndustryItem;
import com.zlf.appmaster.model.sync.SyncBaseBean;
import com.zlf.appmaster.cache.StockJsonCache;
import com.zlf.appmaster.utils.QLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/4/1.
 */
public class IndustryTable {
    private static final String TAG = IndustryTable.class.getSimpleName();

    public static final String COLNAME_ID = "_id";
    public static final String COLNAME_INDUSTRY_ID = "industry_id";		//行业ID
    public static final String COLNAME_NAME = "name";		            // 名称
    public static final String COLNAME_COLOR = "color";		            // 名称
    public static final String COLNAME_CREATE_TIME = "create_time";     // 创建时间
    public static final String COLNAME_UPDATE_TIME = "update_time";	    // 加入时间

    private static final int COLNAME_INDUSTRY_ID_INDEX = 1;
    private static final int COLNAME_NAME_INDEX = 2;
    private static final int COLNAME_COLOR_INDEX = 3;
    private static final int COLNAME_CREATE_TIME_INDEX = 4;
    private static final int COLNAME_UPDATE_TIME_INDEX = 5;

    public static final String TABLE_NAME_INDUSTRY_INFO = "industry_info";

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_INDUSTRY_INFO
                    + "("
                    + COLNAME_ID 				+ 	 "	INTEGER	PRIMARY KEY AUTOINCREMENT,"
                    + COLNAME_INDUSTRY_ID 		+	 "	TEXT    NOT NULL ,"
                    + COLNAME_NAME 				+	 "	TEXT,"
                    + COLNAME_COLOR             +    "  INT,"
                    + COLNAME_CREATE_TIME       +    "	INT,"
                    + COLNAME_UPDATE_TIME       +    "	INT"
                    + ")";


    public static final String WHERE_ROW = COLNAME_INDUSTRY_ID + " = ?";

    private SQLiteDatabase db;
    private Context mContext;
    private StockDBHelper m_dbHelper;

    public IndustryTable(Context context){
        mContext = context;
        m_dbHelper = StockDBHelper.getInstance(context);
        createTable();
    }

    public void createTable(){
        synchronized (m_dbHelper){
            db = m_dbHelper.getWritableDatabase();
            db.execSQL(CREATE_TABLE);
            db.close();
        }
    }

    /**
     * 查询本表是否存在
     * @return
     */
    public boolean isDataExist() {
        boolean ret = false;
        synchronized (m_dbHelper) {
            db = m_dbHelper.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME_INDUSTRY_INFO, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst())    // 有结果集则认为数据表正常
                ret = true;
            cursor.close();
            db.close();
        }
        return ret;
    }

    public void saveItems(List<IndustryItem> industryArray) {
        synchronized (m_dbHelper) {

            db = m_dbHelper.getWritableDatabase();
            db.beginTransaction();
            for (IndustryItem item : industryArray) {
                String industryID = item.getID();
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLNAME_NAME, item.getName());
                contentValues.put(COLNAME_COLOR, item.getColor());
                contentValues.put(COLNAME_UPDATE_TIME, item.getUpdateTime());

                if (isIndustryExist(industryID)) {
                    db.update(TABLE_NAME_INDUSTRY_INFO, contentValues, WHERE_ROW, new String[]{industryID});
                } else {
                    contentValues.put(COLNAME_INDUSTRY_ID, industryID);
                    contentValues.put(COLNAME_CREATE_TIME, item.getCreateTime());
                    db.insert(TABLE_NAME_INDUSTRY_INFO, null, contentValues);
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    public void deleteItems(List<String> ids){
        synchronized (m_dbHelper) {
            db = m_dbHelper.getWritableDatabase();
            db.beginTransaction();

            for (String s: ids){
                db.delete(TABLE_NAME_INDUSTRY_INFO, WHERE_ROW, new String[]{s});
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }


    public IndustryItem getItem(String industryID){
        synchronized (m_dbHelper) {
            IndustryItem item = null;

            if (!TextUtils.isEmpty(industryID)) {
                db = m_dbHelper.getReadableDatabase();
                Cursor c = db.query(TABLE_NAME_INDUSTRY_INFO, null, WHERE_ROW, new String[]{industryID}, null, null, null);
                if (c.moveToFirst()) {
                    item = new IndustryItem();
                    item.setID(c.getString(COLNAME_INDUSTRY_ID_INDEX));
                    item.setName(c.getString(COLNAME_NAME_INDEX));
                    item.setColor(c.getInt(COLNAME_COLOR_INDEX));
                    item.setCreateTime(c.getLong(COLNAME_CREATE_TIME_INDEX));
                    item.setUpdateTime(c.getLong(COLNAME_UPDATE_TIME_INDEX));
                }
                c.close();
                db.close();
            }

            return item;
        }
    }


    private boolean isIndustryExist(String industryID){

        Cursor c = db.query(TABLE_NAME_INDUSTRY_INFO, null, WHERE_ROW, new String[] { industryID }, null, null, null);
        boolean ret = c.moveToFirst();
        c.close();

        return ret;
    }



    /**
     * 加载预置数据
     */
    public static void loadPreInitData(Context context) {
        IndustryTable industryTable = new IndustryTable(context);

        if (!industryTable.isDataExist()) {    // 不存在数据时才需加载

            QLog.i(TAG, "行业数据不存在，从assert中加载");
            try {
                List<IndustryItem> stockItems = new ArrayList<IndustryItem>();
                int len = 1024;
                byte[] buffer = new byte[len];
                InputStream fis = context.getAssets().open(StockJsonCache.DATA_PATH_ALL_INDUSTRY_ITEMS);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int nrb = fis.read(buffer, 0, len); // read up to len bytes
                while (nrb != -1) {
                    baos.write(buffer, 0, nrb);
                    nrb = fis.read(buffer, 0, len);
                }
                buffer = baos.toByteArray();
                fis.close();
                String json = new String(buffer, "UTF-8");
                JSONObject jsonObject = new JSONObject(json);
                stockItems.addAll(IndustryItem.resolveAllItems(jsonObject));

                QLog.i(TAG, "从assert刷新到数据库中");
                industryTable.saveItems(stockItems);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // 保存该数据的版本号
            try {
                // 获取Manifest中内置的版本号
                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                String versionCode = appInfo.metaData.getString("QINIU_DATA_VERSION_2").substring(1);
                long longVersionCode = Long.valueOf(versionCode);

                SyncVersionTable syncVersionTable = new SyncVersionTable(context, StockDBHelper.getInstance(context));
                syncVersionTable.setVersion(SyncBaseBean.SYNC_KEY_INDUSTRY, longVersionCode);
                syncVersionTable.close();

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            //

        }
    }
}
