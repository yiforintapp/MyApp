package com.zlf.appmaster.db.stock;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zlf.appmaster.model.stock.RecentTableProvider;
import com.zlf.appmaster.utils.QLog;
import com.zlf.appmaster.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * DBHelper 每个用户都有一个db库
 * @author Deping Huang
 *
 */
public class QiNiuDBHelper extends SQLiteOpenHelper {
    private static final String TAG = QiNiuDBHelper.class.getSimpleName();
	
    public static final int DATABASE_VERSION = 8;		// greenDao生成里面也有一个版本定义，如有用到请注意
    public static final String DATABASE_NAME = "QiNiu_%s.db";

    private Context mContext;
	private static QiNiuDBHelper mInstance = null;



	private QiNiuDBHelper(Context context) {
		super(context, getDBName(context), null, DATABASE_VERSION);
		mContext = context;
	}

	public static QiNiuDBHelper getInstance(Context context) {
		if (mInstance == null /*|| Utils.getAccountUin(context) == null*/) {
			mInstance = new QiNiuDBHelper(context);
		}
		return mInstance;
	}
	
	public String getDBName(){
		return String.format(DATABASE_NAME, Utils.getAccountUin(mContext));
	}
	private static String getDBName(Context context){
		return String.format(DATABASE_NAME, Utils.getAccountUin(context));
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		 db.execSQL(StockFriendTable.getCreateFriendTableString(StockFriendTable.TABLE_STOCK_FRIEND));//创建股友表
		 db.execSQL(RecentTableProvider.CREATE_TABLE);		// 最近消息表
		 db.execSQL(StockFavoriteTable.CREATE_TABLE);

		// 这里为通过greenDAO生成的表
//		DaoMaster.createAllTables(db, true);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        QLog.i(TAG, "oldVersion:"+oldVersion + "    newVersion:"+newVersion );
        if (newVersion > 7){    // 第7个版本清除以前的数据
            dropAllTable(db);
        }

        onCreate(db);
	}
	
	public void closeDB(){

		if(null != mInstance){
			mInstance.close();
			mInstance = null;
		}
	}


    /**
     * 清空所有表
     * @param db
     */
    public static void dropAllTable(SQLiteDatabase db){
        // 获取所有表名
        // select name from sqlite_master where type='table';
        Cursor c = db.query("sqlite_master", new String[]{"name"}, "type='table'", null, null, null, null);
        List<String> tableNames = new ArrayList<String>();
        if (c != null){
            while (c.moveToNext()){
                String tableName = c.getString(0);
                if (tableName.startsWith("sqlite_")|| tableName.startsWith("android_"))
                    continue;   // 特殊表不删除

                tableNames.add(c.getString(0));

            }
        }

        // 删除所有表
        for (String table:tableNames){
            //QLog.i("TAG", "table:"+table);
            db.execSQL("DROP TABLE " + table);
        }
    }


}
